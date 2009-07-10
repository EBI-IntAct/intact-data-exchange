/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
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
package uk.ac.ebi.intact.dataexchange.enricher.fetch;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;
import uk.ac.ebi.intact.uniprot.service.UniprotRemoteService;
import uk.ac.ebi.intact.uniprot.service.UniprotService;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherException;
import uk.ac.ebi.chebi.webapps.chebiWS.model.Entity;
import uk.ac.ebi.chebi.webapps.chebiWS.model.ChebiWebServiceFault_Exception;
import uk.ac.ebi.chebi.webapps.chebiWS.client.ChebiWebServiceClient;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorFetcher {

    private static final Log log = LogFactory.getLog(InteractorFetcher.class);

    private static ThreadLocal<InteractorFetcher> instance = new ThreadLocal<InteractorFetcher>() {
        @Override
        protected InteractorFetcher initialValue() {
            return new InteractorFetcher();
        }
    };
    private UniprotService uniprotRemoteService;
    private ChebiWebServiceClient chebiServiceClient;

    public static InteractorFetcher getInstance() {
        return instance.get();
    }

    public InteractorFetcher() {
        uniprotRemoteService = new UniprotRemoteService();
        chebiServiceClient = new ChebiWebServiceClient();
    }

    public UniprotProtein fetchInteractorFromUniprot(String uniprotId, int taxId) {
        if (uniprotId == null) {
            throw new NullPointerException("Trying to fetch a protein with null uniprotId");
        }

        Cache interactorCache = CacheManager.getInstance().getCache("Interactor");

        if (interactorCache == null) {
            throw new IllegalStateException("Interactor cache was not found, when fetching: "+uniprotId);
        }

        UniprotProtein uniprotProtein = null;

        String cacheKey = cacheKey(uniprotId, taxId);

        if (interactorCache.isKeyInCache(cacheKey)) {
            final Element element = interactorCache.get(cacheKey);

            if (element != null) {
                uniprotProtein = (UniprotProtein) element.getObjectValue();
            } else {
                if (log.isDebugEnabled())
                    log.debug("Interactor was found in the cache but the element returned was null: "+uniprotId);            }
        }

        if (uniprotProtein == null) {
            if (log.isDebugEnabled()) log.debug("\t\tRemotely retrieving protein: "+uniprotId+" (taxid:"+taxId+")");

            Collection<UniprotProtein> uniprotProteins = uniprotRemoteService.retrieve(uniprotId);

            // if only one result, return it. If more, return the one that matches the tax id
            if (uniprotProteins.size() == 1) {
                uniprotProtein = uniprotProteins.iterator().next();
            } else {
                 for (UniprotProtein candidate : uniprotProteins) {
                    if (candidate.getOrganism().getTaxid() == taxId) {
                        uniprotProtein = candidate;
                        break;
                    }
                }
            }

            if (uniprotProtein != null) {
                interactorCache.put(new Element(cacheKey(uniprotId, taxId), uniprotProtein));
            }
        }

        uniprotRemoteService.clearErrors();

        return uniprotProtein;
    }

    public Entity fetchInteractorFromChebi( String chebiId ) {
        if ( chebiId == null ) {
            throw new NullPointerException( "You must give a non null chebiId" );
        }

        Cache interactorCache = CacheManager.getInstance().getCache( "Interactor" );
        Entity smallMoleculeEntity = null;


        String cacheKey = chebiId;

        if ( interactorCache.isKeyInCache( cacheKey ) ) {
            final Element element = interactorCache.get( cacheKey );

            if ( element != null ) {
                smallMoleculeEntity = ( Entity ) element.getObjectValue();
            } else {
                if ( log.isDebugEnabled() )
                    log.debug( "SmallMoleculeEntity was found in the cache but the element returned was null: " + chebiId );
            }
        }

        if ( smallMoleculeEntity == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug( "Retrieving SmallMoleculeEntity using Chebi Web Service" );
            }
            try {
                smallMoleculeEntity = chebiServiceClient.getCompleteEntity( chebiId );
            } catch ( ChebiWebServiceFault_Exception e ) {
                throw new EnricherException( "Retriving SmallMoleculeEntity failed for " + chebiId, e );
            }

            if ( smallMoleculeEntity != null ) {
                interactorCache.put( new Element( chebiId, smallMoleculeEntity ) );
            }
        }
        return smallMoleculeEntity;
    }

    private String cacheKey(String uniprotId, int taxId) {
        return uniprotId+"_"+taxId;
    }

}