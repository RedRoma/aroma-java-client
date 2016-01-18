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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.client.exceptions.BananaException;
import tech.aroma.banana.thrift.application.service.ApplicationService;
import tech.aroma.banana.thrift.endpoint.Endpoint;
import tech.aroma.banana.thrift.endpoint.HttpRestEndpoint;
import tech.aroma.banana.thrift.endpoint.HttpThriftEndpoint;
import tech.aroma.banana.thrift.endpoint.TcpEndpoint;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateInteger;
import tech.sirwellington.alchemy.test.junit.runners.GenerateURL;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateInteger.Type.RANGE;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class ThriftClientProviderTest
{

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @GenerateURL
    private URL url;

    private HttpThriftEndpoint http;

    private final String hostname = "localhost";

    @GenerateInteger(value = RANGE, min = 7000, max = 10_000)
    private int port;

    private TcpEndpoint tcp;

    private Endpoint endpoint;

    private ThriftClientProvider instance;

    private ExecutorService executor;
    private ServerSocket serverSocket;

    @Before
    public void setUp() throws IOException
    {
        setupEndpoint();
        openServerAtPort(port);

        instance = new ThriftClientProvider(() -> endpoint);
    }

    private void setupEndpoint()
    {
        endpoint = new Endpoint();
        boolean useHttp = one(booleans());
        if (useHttp)
        {
            http = new HttpThriftEndpoint(url.toString());
            endpoint.setHttpThrift(http);
        }
        else
        {
            tcp = new TcpEndpoint("localhost", port);
            endpoint.setTcp(tcp);
        }
    }

    private void openServerAtPort(int port) throws IOException
    {
        executor = Executors.newSingleThreadExecutor();
        try
        {
            serverSocket = new ServerSocket(port);
        }
        catch (IOException ex)
        {
            LOG.error("Could not open port at {}", port);
            throw ex;
        }

        executor.submit(() -> serverSocket.accept());
    }

    @After
    public void tearDown() throws IOException
    {
        if (executor != null)
        {
            executor.shutdownNow();
        }

        if (serverSocket != null)
        {
            serverSocket.close();
        }
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new ThriftClientProvider(null))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new ThriftClientProvider(() -> null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGet()
    {
        ApplicationService.Client result = instance.get();
        assertThat(result, notNullValue());
    }

    @Test
    public void testWithInvalidEndpointType()
    {
        HttpRestEndpoint rest = new HttpRestEndpoint(url.toString());
        endpoint.setHttpRest(rest);

        assertThrows(() -> instance.get())
            .isInstanceOf(BananaException.class);
    }

    @Test
    public void testWithBadUrl()
    {
        String badUrl = one(hexadecimalString(45));
        http = new HttpThriftEndpoint(badUrl);
        endpoint.setHttpThrift(http);

        assertThrows(() -> instance.get())
            .isInstanceOf(IllegalArgumentException.class);
    }

}
