/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.psimitab.converters.util;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.net.URL;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import psidev.psi.mi.tab.model.CrossReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Stores all binary interaction for a specific pair of interactor.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.2
 */
class BinaryInteractionClusterBuilder {

    private static final Log log = LogFactory.getLog( BinaryInteractionClusterBuilder.class );

    /**
     * Where the ehcache configuration file is.
     */
    public static final String EHCACHE_CONFIG_FILE = "/interactions-ehcache-config.xml";

    public static final String CACHE_NAME = "interaction-cluster-cache";

    /**
     * Cache for storing all binary interaction for a specific pair of interactor.
     */
    private Cache cache;

    public BinaryInteractionClusterBuilder() {
        // building cache
        URL url = getClass().getResource( EHCACHE_CONFIG_FILE );

        if ( log.isDebugEnabled() ) {
            log.debug( "Loading ehcache configuration from " + url );
        }

        this.cache = new CacheManager( url ).getCache( CACHE_NAME );

        if ( cache == null ) {
            throw new RuntimeException( "Could not load ontology cache: " + CACHE_NAME );
        }
    }

    private String buildKey( IntactBinaryInteraction ibi ) {
        String a = getIntactAc( ibi.getInteractorA() );
        String b = getIntactAc( ibi.getInteractorB() );
        if ( a.compareTo( b ) > 0 ) {
            // a is greater than b lexicographicaly
            return b + "-" + a;
        } else {
            return a + "-" + b;
        }
    }

    private String getIntactAc( ExtendedInteractor interactor ) {
        for ( CrossReference xref : interactor.getIdentifiers() ) {
            if ( "intact".equals( xref.getDatabase() ) ) {
                return xref.getIdentifier();
            }
        }
        return null;
    }

    public void releaseResources() {
        cache.dispose();
    }

    public void addBinaryInteraction( IntactBinaryInteraction ibi ) {
        final String key = buildKey( ibi );

        Collection<IntactBinaryInteraction> interactions = null;

        Element element = cache.get( key );
        if ( element == null ) {
            interactions = new ArrayList<IntactBinaryInteraction>( 4 );
            element = new Element( key, interactions );
            cache.put( element );
        }

        interactions = ( Collection<IntactBinaryInteraction> ) element.getValue();
        interactions.add( ibi );
    }

    public int countProteinPairs() {
        return cache.getKeys().size();
    }

    public Iterator<ProteinPair> iterate() {
        return new ProteinPairIterator( cache );
    }
}