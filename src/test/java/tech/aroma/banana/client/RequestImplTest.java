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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.aroma.banana.thrift.application.service.ApplicationService;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateEnum;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.epochNowWithinDelta;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class RequestImplTest 
{
    @Mock
    private ApplicationService.Iface applicationService;
    
    @Captor
    private ArgumentCaptor<SendMessageRequest> requestCaptor;
    
    private BananaClient bananaClient;
    
    private RequestImpl instance;
    
    @GenerateString
    private String message;
    
    @GenerateEnum
    private Urgency urgency;
    
    @GenerateString(ALPHABETIC)
    private String exceptionMessage;
    
    private Exception ex;
    
    @Before
    public void setUp()
    {
        ex = new RuntimeException(exceptionMessage);
        
        ExecutorService executor = MoreExecutors.newDirectExecutorService();
        bananaClient = new BananaClient(() -> applicationService, executor);
        
        instance = new RequestImpl(bananaClient, message, urgency);
    }

    @Test
    public void testMessage()
    {
        String newMessage = one(alphabeticString(100));
        
        Banana.Request result = instance.message(newMessage);
        assertThat(result, is(instanceOf(RequestImpl.class)));
        assertThat(result, not(sameInstance(instance)));
        
        RequestImpl newRequest = (RequestImpl) result;
        assertThat(newRequest.getUrgency(), is(instance.getUrgency()));
        assertThat(newRequest.getMessage(), is(newMessage));
    }
    
    @Test
    public void testMessageFormatting()
    {
        String first = one(alphabeticString(5));
        String second = one(alphabeticString(5));
        String third = one(alphabeticString(5));
        
        String formattedMessage = "First {} Second {} Third {}";
        String expected = String.format("First %s Second %s Third %s", first, second, third);
        
        Banana.Request result = instance.message(formattedMessage, first, second, third);
        assertThat(result, is(instanceOf(RequestImpl.class)));
        
        RequestImpl request = (RequestImpl) result;
        assertThat(request.getMessage(), is(expected));
        
        request = (RequestImpl) instance.message(formattedMessage, first, second, third, ex);
        assertThat(request.getMessage(), containsString(first));
        assertThat(request.getMessage(), containsString(second));
        assertThat(request.getMessage(), containsString(third));
        assertThat(request.getMessage(), containsString(exceptionMessage));
        assertThat(request.getMessage(), containsString(ex.getClass().getName()));
        
    }

    @Test
    public void testWithUrgency()
    {
        Urgency newUrgency = enumValueOf(Urgency.class).get();
        Banana.Request result = instance.withUrgency(newUrgency);
        assertThat(result, is(instanceOf(RequestImpl.class)));
        assertThat(result, not(sameInstance(instance)));
        
        RequestImpl newRequest = (RequestImpl) result;
        assertThat(newRequest.getUrgency(), is(newUrgency));
        assertThat(newRequest.getMessage(), is(instance.getMessage()));
    }

    @Test
    public void testSend() throws Exception
    {
        instance.send();
        
        verify(applicationService).sendMessage(requestCaptor.capture());
        
        SendMessageRequest request = requestCaptor.getValue();
        assertThat(request, notNullValue());
        assertThat(request.message, is(message));
        assertThat(request.urgency, is(urgency.toThrift()));
        
        checkThat(request.timeOfMessage)
            .is(epochNowWithinDelta(1000L));
    }

    @Test
    public void testGetMessage()
    {
        String result = instance.getMessage();
        assertThat(result, is(message));
    }

    @Test
    public void testGetUrgency()
    {
        Urgency result = instance.getUrgency();
        assertThat(result, is(urgency));
    }

}