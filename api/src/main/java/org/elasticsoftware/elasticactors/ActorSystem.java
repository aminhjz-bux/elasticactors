/*
 * Copyright 2013 the original authors
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

package org.elasticsoftware.elasticactors;

import org.elasticsoftware.elasticactors.scheduler.Scheduler;
import org.elasticsoftware.elasticactors.serialization.Deserializer;

/**
 * An {@link ActorSystem} is a collection of {@link ElasticActor} instances. {@link ElasticActor}s are persistent and
 * have an {@link ActorState}. An application implementing and {@link ActorSystem} should create classes that implement
 * the {@link ElasticActor#onReceive(ActorRef, Object)} method. Within this method the associated {@link ActorState} can
 * be obtained by calling the {@link ActorContextHolder#getState(Class)} method.
 * The ElasticActors framework will take care of persisting the state. The application can control the serialization and
 * deserialization by providing appropriate {@link Deserializer} in the {@link org.elasticsoftware.elasticactors.ActorSystemConfiguration#getActorStateDeserializer()}
 *
 * @author Joost van de Wijgerd
 */
public interface ActorSystem {
    /**
     * The name of this {@link ActorSystem}. The name has to be unique within the same cluster
     *
     * @return
     */
    String getName();

    <T> ActorRef actorOf(String actorId, Class<T> actorClass) throws Exception;

    <T> ActorRef actorOf(String actorId, Class<T> actorClass, ActorState initialState) throws Exception;

    <T> ActorRef tempActorOf(Class<T> actorClass, ActorState initialState) throws Exception;

    ActorRef actorFor(String actorId);

    ActorRef serviceActorFor(String actorId);

    Scheduler getScheduler();

    ActorSystems getParent();

    void stop(ActorRef actorRef) throws Exception;

}
