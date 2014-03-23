/*
 * Copyright 2013 - 2014 The Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsoftware.elasticactors.cluster.scheduler;

import org.elasticsoftware.elasticactors.*;
import org.elasticsoftware.elasticactors.cluster.InternalActorSystem;
import org.elasticsoftware.elasticactors.cluster.InternalActorSystems;
import org.elasticsoftware.elasticactors.scheduler.ScheduledMessageRef;
import org.elasticsoftware.elasticactors.serialization.MessageSerializer;
import org.elasticsoftware.elasticactors.util.concurrent.DaemonThreadFactory;
import org.elasticsoftware.elasticactors.util.concurrent.ShardedScheduledWorkManager;
import org.elasticsoftware.elasticactors.util.concurrent.WorkExecutor;
import org.elasticsoftware.elasticactors.util.concurrent.WorkExecutorFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * @author Joost van de Wijgerd
 */
public final class ShardedScheduler implements SchedulerService,WorkExecutorFactory,ScheduledMessageRefFactory {
    private ShardedScheduledWorkManager<ShardKey,ScheduledMessage> workManager;
    private ScheduledMessageRepository scheduledMessageRepository;
    private InternalActorSystem actorSystem;


    @PostConstruct
    public void init() {
        ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory("SCHEDULER"));
        workManager = new ShardedScheduledWorkManager<>(executorService,this,Runtime.getRuntime().availableProcessors());
        workManager.init();
    }

    @PreDestroy
    public void destroy() {
        workManager.destroy();
    }

    @Inject
    public void setScheduledMessageRepository(ScheduledMessageRepository scheduledMessageRepository) {
        this.scheduledMessageRepository = scheduledMessageRepository;
    }

    @Inject
    public void setActorSystem(InternalActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public void registerShard(ShardKey shardKey) {
        // obtain the scheduler shard
        workManager.registerShard(shardKey);
        // fetch block from repository
        // @todo: for now we'll fetch all, this obviously has memory issues
        List<ScheduledMessage> scheduledMessages = scheduledMessageRepository.getAll(shardKey);
        workManager.schedule(shardKey,scheduledMessages.toArray(new ScheduledMessage[scheduledMessages.size()]));
    }

    @Override
    public void unregisterShard(ShardKey shardKey) {
        workManager.unregisterShard(shardKey);
    }

    @Override
    public ScheduledMessageRef scheduleOnce(ActorRef sender, Object message, ActorRef receiver, long delay, TimeUnit timeUnit) {
        // this method only works when sender is a local persistent actor (so no temp or service actor)
        if(sender instanceof ActorContainerRef) {
            ActorContainer actorContainer = ((ActorContainerRef)sender).get();
            if(actorContainer instanceof ActorShard) {
                ActorShard actorShard = (ActorShard) actorContainer;
                if(actorShard.getOwningNode().isLocal()) {
                    // we're in business
                    try {
                        long fireTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delay,timeUnit);
                        MessageSerializer serializer = actorSystem.getSerializer(message.getClass());
                        ScheduledMessage scheduledMessage = new ScheduledMessageImpl(fireTime,sender,receiver,message.getClass(),serializer.serialize(message));
                        scheduledMessageRepository.create(actorShard.getKey(), scheduledMessage);
                        workManager.schedule(actorShard.getKey(),scheduledMessage);
                        return null;
                    } catch(Exception e) {
                        throw new RejectedExecutionException(e);
                    }
                }
            }
        }
        // sender param didn't fit the criteria
        throw new IllegalArgumentException(format("sender ref: %s needs to be a non-temp, non-service, locally sharded actor ref",sender.toString()));
    }

    @Override
    public WorkExecutor create() {
        return new ScheduledMessageExecutor();
    }

    @Override
    public ScheduledMessageRef create(String refSpec) {
        // @todo: this is a bit hacky since we need to cast here
        final InternalActorSystems cluster = (InternalActorSystems) actorSystem.getParent();
        return ScheduledMessageRefTools.parse(refSpec,cluster);
    }

    @Override
    public void cancel(ShardKey shardKey,ScheduledMessageKey messageKey) {
        // sanity check if this is actually a local shard that we manage
        // bit of a hack to send in a broken ScheduledMessage (only the key set)
        workManager.unschedule(shardKey,new ScheduledMessageImpl(messageKey.getId(),messageKey.getFireTime()));
        scheduledMessageRepository.delete(shardKey,messageKey);
    }

    private final class ScheduledMessageExecutor implements WorkExecutor<ShardKey,ScheduledMessage> {

        @Override
        public void execute(final ShardKey shardKey,final ScheduledMessage message) {
            // send the message
            final ActorRef receiverRef = message.getReceiver();
            receiverRef.tell(message.getMessageBytes(),message.getSender());
            // remove from the backing store
            scheduledMessageRepository.delete(shardKey, message.getKey());
        }
    }
}
