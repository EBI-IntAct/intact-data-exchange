/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.dbutil.cv;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05-Oct-2006</pre>
 */
public class InvalidOboFormatException extends RuntimeException
{

    public InvalidOboFormatException()
    {
        super();
    }

    public InvalidOboFormatException(String message)
    {
        super(message);
    }

    public InvalidOboFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidOboFormatException(Throwable cause)
    {
        super(cause);
    }
}
