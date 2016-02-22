/*
 * Copyright 2016 Aroma Tech.
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

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateInteger;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateInteger.Type.RANGE;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class BananaTest 
{
    
    @GenerateString
    private String hostname;
    
    @GenerateInteger(value = RANGE, min = 80, max = 10_000)
    private int port;
    
    @GenerateString(HEXADECIMAL)
    private String applicationToken;
    
    private ExecutorService executor;
    
    private Banana instance;
    private Banana.Builder builder;
    
    @Before
    public void setUp()
    {
        instance = Banana.create();
        executor = MoreExecutors.newDirectExecutorService();
        builder = Banana.newBuilder();
    }

    @Test
    public void testBegin()
    {
        Banana.Request request = instance.begin();
        assertThat(request, notNullValue());
    }

    @Test
    public void testCreate()
    {
        Banana result = Banana.create();
        assertThat(result, notNullValue());
    }

    @Test
    public void testNewBuilder()
    {
        Banana.Builder result = Banana.newBuilder();
        assertThat(result, notNullValue());
    }

    @Test
    public void testBuilderWithExecutorService()
    {
        Banana.Builder result = builder.withAsyncExecutorService(executor);
        assertThat(result, notNullValue());
    }    
    
    @DontRepeat
    @Test
    public void testBuilderWithExecutorServiceWithBadArgs()
    {
        Banana.Builder result = builder.withAsyncExecutorService(executor);
        assertThat(result, notNullValue());
    }    
    
    @Test
    public void testBuilderWithEndpoint()
    {
        Banana.Builder result = builder.withEndpoint(hostname, port);
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
        
        Banana result = builder.build();
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
