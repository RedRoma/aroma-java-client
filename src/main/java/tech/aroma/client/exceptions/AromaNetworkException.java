/*
 * Copyright 2018 RedRoma, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.aroma.client.exceptions;


/**
 * Thrown when the {@linkplain tech.aroma.client.Aroma Aroma Client} cannot access the network.
 *
 * @author SirWellington
 */
public class AromaNetworkException extends AromaOperationFailedException
{


    public AromaNetworkException()
    {
    }

    public AromaNetworkException(String message)
    {
        super(message);
    }

    public AromaNetworkException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AromaNetworkException(Throwable cause)
    {
        super(cause);
    }

}
