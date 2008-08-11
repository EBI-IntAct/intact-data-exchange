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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The InterproNameHandler gets the Information from a entyFile using a Map.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class InterproNameHandler {

    private static final Log logger = LogFactory.getLog( InterproNameHandler.class );

    private static final Pattern INTERPRO_ENTRY_PATTERN = Pattern.compile( "IPR\\d{6}[ ].+" );

    private Map<String, String> interproMap;


    public InterproNameHandler( InputStream stream )  {
       BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        try {
            init(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem initializing using a stream", e);
        }
    }

    public InterproNameHandler( URL url ) {
        try {
            BufferedReader reader = new BufferedReader( new InputStreamReader((url.openStream())));
            init(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem opening stream from URL", e);
        }
    }

    private void init( BufferedReader reader ) throws IOException {
        interproMap = new HashMap<String, String>();

        String line = null;
        while ( ( line = reader.readLine() ) != null ) {
            if ( INTERPRO_ENTRY_PATTERN.matcher( line ).matches() ) {
                int index = line.indexOf( " " );
                String interproTerm = line.substring( 0, index );
                String interproName = line.substring( index + 1, line.length() );

                interproMap.put( interproTerm, interproName );
            }
        }
        reader.close();

        if ( logger.isDebugEnabled() ) {
            logger.debug( "Number of Interpro entries " + interproMap.keySet().size() );
        }
    }

    public String getNameById( String interproTerm ) {
        if (interproMap.isEmpty()) {
            throw new IllegalStateException("InterPro map is empty");
        }

        return interproMap.get(interproTerm);
    }
}
