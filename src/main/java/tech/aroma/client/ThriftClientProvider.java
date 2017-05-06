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

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.exceptions.AromaNetworkException;
import tech.aroma.client.exceptions.AromaOperationFailedException;
import tech.aroma.thrift.application.service.ApplicationService;
import tech.aroma.thrift.endpoint.Endpoint;
import tech.aroma.thrift.endpoint.HttpThriftEndpoint;
import tech.aroma.thrift.endpoint.TcpEndpoint;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern.Role.FACTORY;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validPort;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validURL;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@Internal
@FactoryPattern(role = FACTORY)
final class ThriftClientProvider implements Supplier<ApplicationService.Client>
{

    private final static Logger LOG = LoggerFactory.getLogger(ThriftClientProvider.class);

    private final Supplier<Endpoint> endpointSupplier;

    ThriftClientProvider(@Required Supplier<Endpoint> endpointSupplier)
    {
        checkThat(endpointSupplier).is(notNull());
        checkThat(endpointSupplier.get())
            .usingMessage("endpointSupplier returned null")
            .is(notNull());

        this.endpointSupplier = endpointSupplier;
    }

    @Override
    public ApplicationService.Client get()
    {
        Endpoint endpoint = endpointSupplier.get();
        checkThat(endpoint)
            .usingMessage("missing endpoint")
            .is(notNull());

        if (endpoint.isSetTcp())
        {
            return fromTcp(endpoint.getTcp());
        }

        if (endpoint.isSetHttpThrift())
        {
            return fromHttp(endpoint.getHttpThrift());
        }

        throw new AromaOperationFailedException("Endpoint not supported: " + endpoint);
    }

    private ApplicationService.Client fromTcp(TcpEndpoint tcp)
    {
        checkThat(tcp)
            .usingMessage("missing TCP Endpoint")
            .is(notNull());

        String hostname = tcp.hostname;
        int port = tcp.port;

        checkThat(hostname)
            .usingMessage("missing hostname")
            .is(nonEmptyString());

        checkThat(port)
            .is(validPort());

        long timeout = TimeUnit.SECONDS.toMillis(45);
        TSocket socket = new TSocket(hostname, port, (int) timeout);

        try
        {
            socket.open();
        }
        catch (TTransportException ex)
        {
            LOG.error("Failed to open TCP Port at {}", tcp, ex);
            throw new AromaNetworkException("Failed to connect to: " + tcp, ex);
        }

        TProtocol protocol = new TBinaryProtocol(socket);
        return new ApplicationService.Client(protocol);
    }

    private ApplicationService.Client fromHttp(HttpThriftEndpoint http)
    {
        checkThat(http)
            .usingMessage("missing HTTP Endpoint")
            .is(notNull());

        String url = http.url;
        checkThat(url)
            .is(validURL());

        THttpClient client;
        try
        {
            client = new THttpClient(url);
        }
        catch (TTransportException ex)
        {
            LOG.error("Failed to create connection to Endpoint: {}", url);
            throw new AromaNetworkException("Failed to connect to: " + url, ex);
        }

        TProtocol protocol = new TJSONProtocol(client);
        return new ApplicationService.Client(protocol);

    }

}
