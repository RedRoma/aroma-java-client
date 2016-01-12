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

import java.util.function.Supplier;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.client.Banana.Request;
import tech.aroma.banana.thrift.application.service.ApplicationService;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.thrift.clients.Clients;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@ThreadSafe
final class BananaClient implements Banana
{

    private final static Logger LOG = LoggerFactory.getLogger(BananaClient.class);

    private final Supplier<ApplicationService.Iface> applicationServiceProvider;

    BananaClient(@Required Supplier<ApplicationService.Iface> applicationServiceProvider)
    {
        checkThat(applicationServiceProvider).is(notNull());
        
        this.applicationServiceProvider = applicationServiceProvider;
    }
    

    @Override
    public Request begin()
    {
        return new RequestImpl(this);
    }

    void sendMessage(@Required RequestImpl request)
    {
        ApplicationService.Iface client = applicationServiceProvider.get();
        checkThat(client)
            .usingMessage("service provider returned null")
            .is(notNull());
        
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
            .setMessage(request.getMessage())
            .setUrgency(request.getUrgency().toThrift());
        
        try
        {
            client.sendMessage(sendMessageRequest);
            LOG.debug("Successfully sent message to Banana Application Service");
        }
        catch(TException ex)
        {
            //TODO: Decide if swallowing the exception is appropriate here
            LOG.error("Failed to send message to Banana Application Service", ex);
        }
        finally
        {
            Clients.attemptCloseSilently(client);
        }
            
    }

}
