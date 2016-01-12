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

package tech.aroma.banana.client.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thrown when an Operation could not be completed.
 * 
 * @author SirWellington
 */
public class BananaOperationFailedException extends BananaException
{

    private final static Logger LOG = LoggerFactory.getLogger(BananaOperationFailedException.class);

    public BananaOperationFailedException()
    {
    }

    public BananaOperationFailedException(String message)
    {
        super(message);
    }

    public BananaOperationFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BananaOperationFailedException(Throwable cause)
    {
        super(cause);
    }

}
