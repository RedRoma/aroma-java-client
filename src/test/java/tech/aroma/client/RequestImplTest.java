/*
 * Copyright 2016 RedRoma, Inc.
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.aroma.thrift.application.service.ApplicationService;
import tech.aroma.thrift.application.service.ApplicationServiceConstants;
import tech.aroma.thrift.application.service.SendMessageRequest;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateEnum;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
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
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
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
    
    @GeneratePojo
    private ApplicationToken token;
    
    @Captor
    private ArgumentCaptor<SendMessageRequest> requestCaptor;
    
    private AromaClient aromaClient;
    
    private RequestImpl instance;
    
    @GenerateString
    private String body;
    
    @GenerateString
    private String title;
    
    @GenerateEnum
    private Priority priority;
    
    @GenerateString(ALPHABETIC)
    private String exceptionMessage;
    
    private Exception ex;
    
    @Before
    public void setUp()
    {
        ex = new RuntimeException(exceptionMessage);
        
        ExecutorService executor = MoreExecutors.newDirectExecutorService();
        aromaClient = new AromaClient(() -> applicationService, executor, token);
        
        instance = new RequestImpl(aromaClient, title, body, priority);
    }

    @Test
    public void testMessage()
    {
        String newMessage = one(alphabeticString(100));
        
        Aroma.Request result = instance.withBody(newMessage);
        assertThat(result, is(instanceOf(RequestImpl.class)));
        assertThat(result, not(sameInstance(instance)));
        
        RequestImpl newRequest = (RequestImpl) result;
        assertThat(newRequest.getPriority(), is(instance.getPriority()));
        assertThat(newRequest.getText(), is(newMessage));
    }
    
    @Test
    public void testMessageFormatting()
    {
        String first = one(alphabeticString(5));
        String second = one(alphabeticString(5));
        String third = one(alphabeticString(5));
        
        String formattedMessage = "First {} Second {} Third {}";
        String expected = String.format("First %s Second %s Third %s", first, second, third);
        
        Aroma.Request result = instance.withBody(formattedMessage, first, second, third);
        assertThat(result, is(instanceOf(RequestImpl.class)));
        
        RequestImpl request = (RequestImpl) result;
        assertThat(request.getText(), is(expected));
        
        request = (RequestImpl) instance.withBody(formattedMessage, first, second, third, ex);
        assertThat(request.getText(), containsString(first));
        assertThat(request.getText(), containsString(second));
        assertThat(request.getText(), containsString(third));
        assertThat(request.getText(), containsString(exceptionMessage));
        assertThat(request.getText(), containsString(ex.getClass().getName()));
        
    }

    @Test
    public void testWithPriority()
    {
        Priority newPriority = enumValueOf(Priority.class).get();
        Aroma.Request result = instance.withPriority(newPriority);
        assertThat(result, is(instanceOf(RequestImpl.class)));
        assertThat(result, not(sameInstance(instance)));
        
        RequestImpl newRequest = (RequestImpl) result;
        assertThat(newRequest.getPriority(), is(newPriority));
        assertThat(newRequest.getText(), is(instance.getText()));
    }

    @Test
    public void testSend() throws Exception
    {
        instance.send();
        
        verify(applicationService).sendMessage(requestCaptor.capture());
        
        SendMessageRequest request = requestCaptor.getValue();
        assertThat(request, notNullValue());
        assertThat(request.body, is(body));
        assertThat(request.title, is(title));
        assertThat(request.urgency, is(priority.toThrift()));
        assertThat(request.applicationToken, is(token));
        
        checkThat(request.timeOfMessage)
            .is(epochNowWithinDelta(1000L));
    }

    @Test
    public void testGetMessage()
    {
        String result = instance.getText();
        assertThat(result, is(body));
    }

    @Test
    public void testGetPriority()
    {
        Priority result = instance.getPriority();
        assertThat(result, is(priority));
    }
    
    @Test
    public void testGetTitle()
    {
        String result = instance.getTitle();
        assertThat(result, is(title));
    }

    @Test
    public void testTitled()
    {
        String newTitle = one(alphabeticString(10));

        Aroma.Request result = instance.titled(newTitle);
        assertThat(result, is(instanceOf(RequestImpl.class)));
        assertThat(result, not(sameInstance(instance)));
        
        RequestImpl newRequest = (RequestImpl) result;
        assertThat(newRequest.getPriority(), is(instance.getPriority()));
        assertThat(newRequest.getText(), is(body));
        assertThat(newRequest.getTitle(), is(newTitle));
    }

    @Test
    public void testWithLongTitle()
    {
        int length = one(integers(ApplicationServiceConstants.MAX_TITLE_LENGTH + 1, 
                                  ApplicationServiceConstants.MAX_TITLE_LENGTH * 2));
        
        String longTitle = one(alphabeticString(length));
        assertThrows(() -> instance.titled(longTitle));
    }
    
    @Test
    public void testWithShortTitle()
    {
        int length = one(integers(1, 2));
        
        String shortTitle = one(alphabeticString(length));
        assertThrows(() -> instance.titled(shortTitle));
    }
    
}
