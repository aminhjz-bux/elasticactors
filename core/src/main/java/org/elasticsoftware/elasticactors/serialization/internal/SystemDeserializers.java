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

package org.elasticsoftware.elasticactors.serialization.internal;

import org.elasticsoftware.elasticactors.cluster.ActorRefFactory;
import org.elasticsoftware.elasticactors.cluster.InternalActorSystems;
import org.elasticsoftware.elasticactors.messaging.internal.ActivateActorMessage;
import org.elasticsoftware.elasticactors.messaging.internal.CancelScheduledMessageMessage;
import org.elasticsoftware.elasticactors.messaging.internal.CreateActorMessage;
import org.elasticsoftware.elasticactors.messaging.internal.DestroyActorMessage;
import org.elasticsoftware.elasticactors.serialization.MessageDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joost van de Wijgerd
 */
public final class SystemDeserializers {
    private final Map<Class,MessageDeserializer> systemDeserializers = new HashMap<Class,MessageDeserializer>();

    public SystemDeserializers(InternalActorSystems cluster,ActorRefFactory actorRefFactory) {
        ActorRefDeserializer actorRefDeserializer = new ActorRefDeserializer(actorRefFactory);
        systemDeserializers.put(CreateActorMessage.class,new CreateActorMessageDeserializer(cluster));
        systemDeserializers.put(DestroyActorMessage.class,new DestroyActorMessageDeserializer(actorRefDeserializer));
        systemDeserializers.put(ActivateActorMessage.class,new ActivateActorMessageDeserializer());
        systemDeserializers.put(CancelScheduledMessageMessage.class,new CancelScheduledMessageMessageDeserializer());
        //@todo: add more deserializers here
    }

    public <T> MessageDeserializer<T> get(Class<T> messageClass) {
        return systemDeserializers.get(messageClass);
    }
}
