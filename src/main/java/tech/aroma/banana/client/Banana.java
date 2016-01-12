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
import tech.aroma.banana.thrift.endpoint.Endpoint;
import tech.aroma.banana.thrift.endpoint.TcpEndpoint;
import tech.aroma.banana.thrift.service.BananaServiceConstants;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.NonNull;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
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
        Request message(@NonNull String message, @Optional Object...args);
        
        Request withUrgency(@NonNull Urgency level) throws IllegalArgumentException;
        
        void send() throws IllegalArgumentException;
    }
    
    static Banana create()
    {
        return null;
    }
    
    @BuilderPattern(role = BUILDER)
    static class Builder 
    {
        
        private String hostname = BananaServiceConstants.PRODUCTION_ENDPOINT.getHostname();
        private int port = BananaServiceConstants.PRODUCTION_ENDPOINT.getPort();
        private ExecutorService async;
        
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
        
        public Builder withAsyncExecutorService(@NonNull ExecutorService executor) throws IllegalArgumentException
        {
            checkThat(executor)
                .is(notNull());
            
            this.async = executor;
            
            return this;
        }
        
        public Banana build() throws IllegalStateException
        {
            checkThat(hostname)
                .usingMessage("missing hostname")
                .is(nonEmptyString());
            
            checkThat(port)
                .is(validPort());
            
            if (async == null)
            {
                async = Executors.newSingleThreadExecutor();
            }
            
            TcpEndpoint tcpEndpoint = new TcpEndpoint(hostname, port);
            Endpoint endpoint = new Endpoint();
            endpoint.setTcp(tcpEndpoint);
            
            return null; 
            
        }
        
    }
    
}
