/*
 * Copyright 2015 Aroma Tech.
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tech.aroma.banana.client.exceptions.BananaException;
import tech.aroma.thrift.application.service.ApplicationServiceConstants;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.endpoint.Endpoint;
import tech.aroma.thrift.endpoint.TcpEndpoint;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.FluidAPIDesign;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validPort;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@ThreadSafe
@BuilderPattern(role = PRODUCT)
@FluidAPIDesign
public interface Banana
{
    
    Request begin();
    
    interface Request
    {
        Request text(@Required String message, @Optional Object...args);
        
        Request titled(@Required String title);
        
        Request withUrgency(@Required Urgency level) throws IllegalArgumentException;
        
        void send() throws IllegalArgumentException, BananaException;
    }
    
    static Banana create()
    {
        return newBuilder()
            .withAsyncExecutorService(Executors.newSingleThreadExecutor())
            .withApplicationToken("Banana")
            .build();
    }
    
    static Builder newBuilder()
    {
        return new Builder();
    }
    
    @BuilderPattern(role = BUILDER)
    static final class Builder 
    {
        
        private String hostname = ApplicationServiceConstants.PRODUCTION_ENDPOINT.getHostname();
        private int port = ApplicationServiceConstants.PRODUCTION_ENDPOINT.getPort();
        private String applicationToken = "";
        private ExecutorService async;
        
        Builder() 
        {
            
        }
        
        /**
         * Set the Token ID created from the Aroma App.
         * 
         * @param applicationToken
         * @return
         * 
         * @throws IllegalArgumentException 
         */
        public Builder withApplicationToken(@Required String applicationToken) throws IllegalArgumentException
        {
            checkThat(applicationToken)
                .are(nonEmptyString());
            
            this.applicationToken = applicationToken;
            
            return this;
        }
        
        public Builder withEndpoint(@NonEmpty String hostname, int port) throws IllegalArgumentException
        {
            checkThat(hostname)
                .usingMessage("hostname cannot be empty")
                .is(nonEmptyString());
            
            checkThat(port)
                .is(validPort());
                
            this.hostname = hostname;
            this.port = port;
            
            return this;
        }
        
        public Builder withAsyncExecutorService(@Required ExecutorService executor) throws IllegalArgumentException
        {
            checkThat(executor)
                .is(notNull());
            
            this.async = executor;
            
            return this;
        }
        
        public Banana build() throws IllegalStateException
        {
            checkThat(hostname)
                .throwing(IllegalStateException.class)
                .usingMessage("missing hostname")
                .is(nonEmptyString());
            
            checkThat(applicationToken)
                .throwing(IllegalStateException.class)
                .usingMessage("missing Application Token")
                .is(nonEmptyString());
            
            checkThat(port)
                .throwing(IllegalStateException.class)
                .is(validPort());
            
            if (async == null)
            {
                async = Executors.newSingleThreadExecutor();
            }
            
            Endpoint endpoint = createEndpoint();
            
            ApplicationToken token = new ApplicationToken().setTokenId(applicationToken);
            
            ThriftClientProvider clientProvider = new ThriftClientProvider(() -> endpoint);
            BananaClient banana = new BananaClient(() -> clientProvider.get(), async, token);
            return banana;
            
        }

        private Endpoint createEndpoint()
        {
            TcpEndpoint tcpEndpoint = new TcpEndpoint(hostname, port);

            Endpoint endpoint = new Endpoint();
            endpoint.setTcp(tcpEndpoint);
            return endpoint;
        }
        
    }
    
}
