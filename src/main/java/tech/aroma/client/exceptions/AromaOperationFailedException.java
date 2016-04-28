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

package tech.aroma.client.exceptions;

/**
 * Thrown when an Operation could not be completed.
 * 
 * @author SirWellington
 */
public class AromaOperationFailedException extends AromaException
{

    public AromaOperationFailedException()
    {
    }

    public AromaOperationFailedException(String message)
    {
        super(message);
    }

    public AromaOperationFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AromaOperationFailedException(Throwable cause)
    {
        super(cause);
    }

}
