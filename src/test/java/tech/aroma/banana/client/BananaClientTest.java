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

package tech.aroma.banana.client;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.aroma.banana.thrift.application.service.ApplicationService;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.epochNowWithinDelta;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class BananaClientTest 
{
    
    @Mock
    private ApplicationService.Client applicationService;
    
    private final ExecutorService executor = MoreExecutors.newDirectExecutorService();
    
    @Captor
    private ArgumentCaptor<SendMessageRequest> requestCaptor;
    
    private RequestImpl request;
    
    private BananaClient instance;
    
    @GenerateString
    private String message;
    
    private Urgency urgency;
    
    @Before
    public void setUp()
    {
        urgency = enumValueOf(Urgency.class).get();
        
        Supplier<ApplicationService.Iface> serviceProvider = () -> applicationService;
        
        instance = new BananaClient(serviceProvider, executor);

        request = new RequestImpl(instance, message, urgency);
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new BananaClient(() -> applicationService, null))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new BananaClient(null, executor))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBegin()
    {
        Banana.Request result = instance.begin();
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(RequestImpl.class));
    }

    @Test
    public void testSendMessage() throws Exception
    {
        instance.sendMessage(request);
        
        verify(applicationService).sendMessage(requestCaptor.capture());
        
        SendMessageRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.message, is(message));
        assertThat(requestMade.urgency, is(urgency.toThrift()));
        checkThat(requestMade.timeOfMessage)
            .is(epochNowWithinDelta(50L));
    }

}