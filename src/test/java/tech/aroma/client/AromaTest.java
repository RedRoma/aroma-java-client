/*
 * Copyright 2017 RedRoma, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.aroma.client;

import java.util.concurrent.ExecutorService;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateInteger.Type.RANGE;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class AromaTest
{

    @GenerateString
    private String hostname;

    @GenerateInteger(value = RANGE, min = 80, max = 10_000)
    private int port;

    @GenerateString(UUID)
    private String applicationToken;

    @GenerateString
    private String title;

    @GenerateString
    private String body;


    private ExecutorService executor;

    private Aroma instance;
    private Aroma.Builder builder;


    @Before
    public void setUp()
    {
        instance = Aroma.create(applicationToken);
        executor = MoreExecutors.newDirectExecutorService();
        builder = Aroma.newBuilder();
    }

    @Test
    public void testBegin()
    {
        Aroma.Request request = instance.begin();
        assertThat(request, notNullValue());
    }

    @Test
    public void sendLowPriorityMessage() throws Exception
    {

        instance.sendLowPriorityMessage(title);

        assertThrows(() -> instance.sendLowPriorityMessage(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void sendLowPriorityMessage1() throws Exception
    {
        instance.sendLowPriorityMessage(title, body);

        assertThrows(() -> instance.sendLowPriorityMessage(null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void sendMediumPriorityMessage() throws Exception
    {

        instance.sendMediumPriorityMessage(title);

        assertThrows(() -> instance.sendMediumPriorityMessage(null))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void sendMediumPriorityMessage1() throws Exception
    {
        instance.sendMediumPriorityMessage(title, body);

        assertThrows(() -> instance.sendMediumPriorityMessage(null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void sendHighPriorityMessage() throws Exception
    {
        instance.sendHighPriorityMessage(title);

        assertThrows(() -> instance.sendHighPriorityMessage(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void sendHighPriorityMessage1() throws Exception
    {
        instance.sendHighPriorityMessage(title, body);

        assertThrows(() -> instance.sendHighPriorityMessage(null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void sendMessage() throws Exception
    {

        Priority priority = enumValueOf(Priority.class).get();

        instance.sendMessage(priority, title, body);

        assertThrows(() -> instance.sendMessage(null, title, body))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testCreate()
    {
        Aroma result = Aroma.create(applicationToken);
        assertThat(result, notNullValue());
    }

    @Test
    public void testCreateWithAppToken()
    {
        Aroma result = Aroma.create(applicationToken);
        assertThat(result, notNullValue());
        result.begin();
    }

    @Test
    public void testCreateWithBadToken()
    {
        assertThrows(() -> Aroma.create(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testCreateNoOpInstance()
    {
        Aroma instance = Aroma.createNoOpInstance();

        assertThat(instance, notNullValue());

        instance.sendMediumPriorityMessage(title, body);
    }

    @Test
    public void testNewBuilder()
    {
        Aroma.Builder result = Aroma.newBuilder();
        assertThat(result, notNullValue());
    }

    @Test
    public void testBuilderWithExecutorService()
    {
        Aroma.Builder result = builder.withAsyncExecutorService(executor);
        assertThat(result, notNullValue());
    }

    @DontRepeat
    @Test
    public void testBuilderWithExecutorServiceWithBadArgs()
    {
        Aroma.Builder result = builder.withAsyncExecutorService(executor);
        assertThat(result, notNullValue());
    }

    @Test
    public void testBuilderWithEndpoint()
    {
        Aroma.Builder result = builder.withEndpoint(hostname, port);
        assertThat(result, notNullValue());
    }

    @DontRepeat
    @Test
    public void testBuilderWithEndpointWithBadArgs()
    {
        int badPort = one(negativeIntegers());

        assertThrows(() -> builder.withEndpoint(hostname, badPort))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> builder.withEndpoint(hostname, 0))
                .isInstanceOf(IllegalArgumentException.class);


        assertThrows(() -> builder.withEndpoint("", port))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBuilderBuild()
    {
        builder.withEndpoint(hostname, port)
               .withAsyncExecutorService(executor)
               .withApplicationToken(applicationToken);

        Aroma result = builder.build();
        assertThat(result, notNullValue());
    }

    @DontRepeat
    @Test
    public void testBuilderBuildWithBadState()
    {
        assertThrows(() -> builder.build())
                .isInstanceOf(IllegalStateException.class);

        //Missing application token
        builder = builder.withAsyncExecutorService(executor);
        assertThrows(() -> builder.build())
                .isInstanceOf(IllegalStateException.class);

    }
}
