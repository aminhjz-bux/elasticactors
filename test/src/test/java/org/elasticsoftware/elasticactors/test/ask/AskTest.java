/*
 * Copyright 2013 - 2017 The Original Authors
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

package org.elasticsoftware.elasticactors.test.ask;

import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.ActorSystem;
import org.elasticsoftware.elasticactors.test.TestActorSystem;
import org.elasticsoftware.elasticactors.test.common.EchoGreetingActor;
import org.elasticsoftware.elasticactors.test.common.Greeting;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;

/**
 * @author Joost van de Wijgerd
 */
public class AskTest {
    @Test
    public void testAskGreeting() throws Exception {
        TestActorSystem testActorSystem = new TestActorSystem();
        testActorSystem.initialize();

        ActorSystem actorSystem = testActorSystem.getActorSystem();
        ActorRef echo = actorSystem.actorOf("e", EchoGreetingActor.class);


        Greeting response = echo.ask(new Greeting("echo"), Greeting.class).toCompletableFuture().get();

        assertEquals(response.getWho(), "echo");

        testActorSystem.destroy();
    }

    @Test
    public void testAskGreetingViaActor() throws Exception {
        TestActorSystem testActorSystem = new TestActorSystem();
        testActorSystem.initialize();

        ActorSystem actorSystem = testActorSystem.getActorSystem();
        ActorRef echo = actorSystem.actorOf("ask", AskForGreetingActor.class);


        Greeting response = echo.ask(new AskForGreeting(), Greeting.class).toCompletableFuture().get();

        assertEquals(response.getWho(), "echo");

        testActorSystem.destroy();
    }

    @Test
    public void testAskGreetingViaActorWithPersistOnReponse() throws Exception {
        TestActorSystem testActorSystem = new TestActorSystem();
        testActorSystem.initialize();

        ActorSystem actorSystem = testActorSystem.getActorSystem();
        ActorRef echo = actorSystem.actorOf("ask", AskForGreetingActor.class);


        Greeting response = echo.ask(new AskForGreeting(true), Greeting.class).toCompletableFuture().get();

        assertEquals(response.getWho(), "echo");

        testActorSystem.destroy();
    }


    @Test
    public void testAskGreetingAsync() throws Exception {
        TestActorSystem testActorSystem = new TestActorSystem();
        testActorSystem.initialize();

        ActorSystem actorSystem = testActorSystem.getActorSystem();
        ActorRef echo = actorSystem.actorOf("e", EchoGreetingActor.class);

        final CountDownLatch waitLatch = new CountDownLatch(1);
        final AtomicReference<String> response = new AtomicReference<>();

        echo.ask(new Greeting("echo"), Greeting.class).whenComplete((greeting, throwable) -> {
            System.out.println(Thread.currentThread().getName());
            response.set(greeting.getWho());
            waitLatch.countDown();
        });

        waitLatch.await();

        assertEquals(response.get(), "echo");

        testActorSystem.destroy();
    }

    @Test(expectedExceptions = ExecutionException.class)
    public void testUnexpectedResponse() throws Exception {
        TestActorSystem testActorSystem = new TestActorSystem();
        testActorSystem.initialize();

        ActorSystem actorSystem = testActorSystem.getActorSystem();
        ActorRef echo = actorSystem.actorOf("e", EchoGreetingActor.class);

        Greeting response = echo.ask(new Greeting("Santa Claus"), Greeting.class).toCompletableFuture().get();

        testActorSystem.destroy();
    }
}
