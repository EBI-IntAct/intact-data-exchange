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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.chebi.webapps.chebiWS.client.ChebiWebServiceClient;
import uk.ac.ebi.chebi.webapps.chebiWS.model.ChebiWebServiceFault_Exception;
import uk.ac.ebi.chebi.webapps.chebiWS.model.Entity;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherException;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;
import uk.ac.ebi.intact.uniprot.service.UniprotRemoteService;
import uk.ac.ebi.intact.uniprot.service.UniprotService;

import java.util.Collection;

/**
 * Interactor Fetcher.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorFetcher {

    private static final Log log = LogFactory.getLog(InteractorFetcher.class);

    @Autowired
    private EnricherContext enricherContext;

    private UniprotService uniprotRemoteService;
    private ChebiWebServiceClient chebiServiceClient;

    public InteractorFetcher() {
    }

    public UniprotProtein fetchInteractorFromUniprot(String uniprotId, int taxId) {
        if (uniprotId == null) {
            throw new NullPointerException("Trying to fetch a protein with null uniprotId");
        }
        if (uniprotRemoteService == null) {
            uniprotRemoteService = new UniprotRemoteService();
        }

        EnricherCache interactorCache = enricherContext.getCacheManager().getCache("Interactor");

        if (interactorCache == null) {
            throw new IllegalStateException("Interactor cache was not found, when fetching: "+uniprotId);
        }

        UniprotProtein uniprotProtein = null;

        String cacheKey = cacheKey(uniprotId, taxId);

        if (interactorCache.isKeyInCache(cacheKey)) {
            uniprotProtein = (UniprotProtein) interactorCache.get(cacheKey);
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
                interactorCache.put(cacheKey(uniprotId, taxId), uniprotProtein);
            }
        }

        uniprotRemoteService.clearErrors();

        return uniprotProtein;
    }

    public Entity fetchInteractorFromChebi( String chebiId ) {
        if ( chebiId == null ) {
            throw new NullPointerException( "You must give a non null chebiId" );
        }

        if (chebiServiceClient == null) {
            chebiServiceClient = new ChebiWebServiceClient();
        }

        EnricherCache interactorCache = enricherContext.getCacheManager().getCache( "Interactor" );
        Entity smallMoleculeEntity = null;

        String cacheKey = chebiId;

        if ( interactorCache.isKeyInCache( cacheKey ) ) {
            smallMoleculeEntity = (Entity) interactorCache.get( cacheKey );
        }

        if ( smallMoleculeEntity == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug( "Retrieving SmallMoleculeEntity using Chebi Web Service: " + chebiId );
            }
            try {
                smallMoleculeEntity = chebiServiceClient.getCompleteEntity( chebiId );
            } catch ( ChebiWebServiceFault_Exception e ) {
                throw new EnricherException( "Retrieving SmallMoleculeEntity failed for " + chebiId, e );
            }

            if ( smallMoleculeEntity != null ) {
                interactorCache.put( chebiId, smallMoleculeEntity );
            }
        }
        return smallMoleculeEntity;
    }

    private String cacheKey(String uniprotId, int taxId) {
        return uniprotId+"_"+taxId;
    }
}