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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma.Request;
import tech.aroma.thrift.application.service.ApplicationService;
import tech.aroma.thrift.application.service.SendMessageRequest;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.thrift.clients.Clients;

import static java.time.Instant.now;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@ThreadSafe
final class AromaClient implements Aroma
{

    private final static Logger LOG = LoggerFactory.getLogger(AromaClient.class);

    private final Supplier<ApplicationService.Iface> applicationServiceProvider;
    private final ExecutorService executor;
    private final ApplicationToken token;
    
    private final String operatingSystem = getNameOfOS();
    private final String hostname = getHostname();
    private final String deviceName = getHostname();

    AromaClient(@Required Supplier<ApplicationService.Iface> applicationServiceProvider,
                @Required ExecutorService executor,
                @Required ApplicationToken token)
    {
        checkThat(applicationServiceProvider, executor, token)
            .are(notNull());

        checkThat(token.tokenId)
            .usingMessage("token is missing")
            .is(nonEmptyString());

        this.applicationServiceProvider = applicationServiceProvider;
        this.executor = executor;
        this.token = token;
    }

    @Override
    public Request begin()
    {
        return new RequestImpl(this, "", "", Priority.LOW);
    }

    void sendMessage(@Required RequestImpl request)
    {
        Instant now = now();

        SendMessageRequest sendMessageRequest = new SendMessageRequest()
            .setApplicationToken(token)
            .setBody(request.getText())
            .setTitle(request.getTitle())
            .setUrgency(request.getPriority().toThrift())
            .setHostname(hostname)
            .setDeviceName(deviceName)
            .setOperatingSystemName(operatingSystem)
            .setIpv4Address(getIpv4Address())
            .setTimeOfMessage(now.toEpochMilli());

        executor.submit(() -> sendMessageAsync(sendMessageRequest));
    }

    private void sendMessageAsync(SendMessageRequest request)
    {
        ApplicationService.Iface client = applicationServiceProvider.get();

        checkThat(client)
            .usingMessage("service provider returned null")
            .is(notNull());

        try
        {
            client.sendMessage(request);
            LOG.debug("Successfully sent message to the Aroma Application Service");
        }
        catch (TException ex)
        {
            //TODO: Decide if swallowing the exception is appropriate here
            LOG.error("Failed to send message to the Aroma Application Service", ex);
        }
        finally
        {
            Clients.attemptCloseSilently(client);
        }
    }

    private String getHostname()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException ex)
        {
            LOG.warn("Could not determine Hostname", ex);
            return "";
        }
    }

    private String getIpv4Address()
    {
        try
        {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex)
        {
            LOG.warn("Could not determine IPv4 Address", ex);
            return "";
        }
    }
    
    private String getNameOfOS()
    {
        String os = System.getProperty("os.name");
        return os != null ? os : "";
    }

}
