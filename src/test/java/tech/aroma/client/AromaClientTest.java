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
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.aroma.thrift.application.service.ApplicationService;
import tech.aroma.thrift.application.service.SendMessageRequest;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.sirwellington.alchemy.annotations.testing.TimeSensitive;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.nowWithinDelta;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class AromaClientTest 
{
    
    @Mock
    private TProtocol protocol;
    
    @Mock
    private TTransport transport;
    
    @Mock
    private ApplicationService.Client applicationService;
    
    @GeneratePojo
    private ApplicationToken token;
    
    private final ExecutorService executor = MoreExecutors.newDirectExecutorService();
    
    @Captor
    private ArgumentCaptor<SendMessageRequest> requestCaptor;
    
    private RequestImpl request;
    
    private AromaClient instance;
    
    @GenerateString
    private String body;
    
    @GenerateString
    private String title;
    
    private Urgency urgency;
    
    @Before
    public void setUp()
    {
        urgency = enumValueOf(Urgency.class).get();

        Supplier<ApplicationService.Iface> serviceProvider = () -> applicationService;

        instance = new AromaClient(serviceProvider, executor, token);

        request = new RequestImpl(instance, title, body, urgency);

        setupThriftTransports();
    }

    private void setupThriftTransports()
    {

        when(applicationService.getInputProtocol())
            .thenReturn(protocol);
        when(applicationService.getOutputProtocol())
            .thenReturn(protocol);

        when(protocol.getTransport())
            .thenReturn(transport);
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new AromaClient(() -> applicationService, null, null))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new AromaClient(null, executor, null))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new AromaClient(null, null, token))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBegin()
    {
        Aroma.Request result = instance.begin();
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(RequestImpl.class));
    }

    @TimeSensitive
    @Test
    public void testSendMessage() throws Exception
    {
        instance.sendMessage(request);
        
        verify(applicationService).sendMessage(requestCaptor.capture());
        
        SendMessageRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.body, is(body));
        assertThat(requestMade.title, is(title));
        assertThat(requestMade.urgency, is(urgency.toThrift()));
        assertThat(requestMade.applicationToken, is(token));
        
        Instant timeOfMessage = Instant.ofEpochMilli(requestMade.timeOfMessage);
        checkThat(timeOfMessage)
            .is(nowWithinDelta(1000L));
        
        verify(transport, atLeastOnce()).close();
    }
    
    @Test
    public void testSendMessageWhenOperationFails() throws Exception
    {
        when(applicationService.sendMessage(Mockito.any()))
            .thenThrow(new OperationFailedException());
            
        instance.sendMessage(request);
    }


}
