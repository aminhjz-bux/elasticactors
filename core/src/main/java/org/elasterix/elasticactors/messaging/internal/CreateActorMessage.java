/*
 * Copyright 2013 Joost van de Wijgerd
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

package org.elasterix.elasticactors.messaging.internal;

import org.elasterix.elasticactors.ActorState;

import java.io.Serializable;

/**
 * @author Joost van de Wijgerd
 */
public final class CreateActorMessage implements Serializable {
    private final String actorSystem;
    private final String actorId;
    private final String actorClass;
    private final ActorState initialState;

    public CreateActorMessage(String actorSystem, String actorClass, String actorId, ActorState initialState) {
        this.actorSystem = actorSystem;
        this.actorId = actorId;
        this.actorClass = actorClass;
        this.initialState = initialState;
    }

    public String getActorSystem() {
        return actorSystem;
    }

    public String getActorClass() {
        return actorClass;
    }

    public String getActorId() {
        return actorId;
    }

    public ActorState getInitialState() {
        return initialState;
    }
}