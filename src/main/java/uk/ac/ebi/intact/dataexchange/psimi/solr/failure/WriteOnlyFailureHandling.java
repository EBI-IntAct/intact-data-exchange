/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.solr.failure;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.Writer;
import java.io.IOException;

/**
 * Failure handling that only outputs failed lines.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.3
 */
public class WriteOnlyFailureHandling extends AbstractFailureHandlingStrategy {

    public static final String NEW_LINE = System.getProperty( "line.separator" );

    private Writer writer;

    public WriteOnlyFailureHandling( Writer writer ) {
        if ( writer == null ) {
            throw new IllegalArgumentException( "You must give a non null writer" );
        }
        this.writer = writer;
    }

    public void handleFailure( Throwable t, String mitabLine, int lineCount ) {
        try {
            writer.write( "Error while processing MITAB line " + lineCount + ": " + NEW_LINE );
            writer.write( mitabLine + NEW_LINE );
            if( t != null ) {
                writer.write( "Reason:" + NEW_LINE );
                writer.write( ExceptionUtils.getStackTrace( t ) + NEW_LINE );
            }
            writer.write( "------------------------------------------------------------------------------" + NEW_LINE );
            writer.flush();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
