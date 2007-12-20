/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.psimitab;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.psimitab.exception.NameNotFoundException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.net.URL;

/**
 * The InterproNameHandler gets the Information from a entyFile using a Map.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0-Snapshot
 */
public class InterproNameHandler {

    private static final Log logger = LogFactory.getLog( InterproNameHandler.class );

    private static final Pattern INTERPRO_ENTRY_PATTERN = Pattern.compile( "IPR\\d{6}[ ].+" );

    private File entryFile;

    private Map<String, String> interproMap;

    public InterproNameHandler( File entryFile ) {
        this.entryFile = entryFile;
        if ( interproMap == null || interproMap.isEmpty() ) {
            init();
        }
    }

    private void init() {
        try {
            interproMap = new HashMap<String, String>();

            BufferedReader reader = new BufferedReader( new FileReader( entryFile ) );
            String line = reader.readLine();
            while ( line != null ) {

                if ( line != null ) {
                    if ( INTERPRO_ENTRY_PATTERN.matcher( line ).matches() ) {
                        int index = line.indexOf( " " );
                        String interproTerm = line.substring( 0, index );
                        String interproName = line.substring( index + 1, line.length() );
                        interproMap.put( interproTerm, interproName );
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        } catch ( FileNotFoundException e ) {
            logger.error( "Could not find file " + entryFile.getAbsolutePath() );
        } catch ( IOException e ) {
            logger.warn( "Could not read file " + entryFile.getAbsolutePath() );
        }
    }

    public String getNameById( String interproTerm ) throws NameNotFoundException {
        if ( interproMap != null && !interproMap.isEmpty() ) {
            return interproMap.get( interproTerm );
        } else {
            logger.error( "Map is not initialized or is Empty." );
        }
        throw new NameNotFoundException();
    }
}
