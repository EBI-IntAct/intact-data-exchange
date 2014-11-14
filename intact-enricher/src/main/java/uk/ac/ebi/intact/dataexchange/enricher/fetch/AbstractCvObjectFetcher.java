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
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.fetcher.CvTermFetcher;
import psidev.psi.mi.jami.model.CvTerm;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;

import java.util.Collection;

/**
 * TODO comment this
 *
 */
public abstract class AbstractCvObjectFetcher<T extends CvTerm> implements CvTermFetcher<T>{

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(AbstractCvObjectFetcher.class);

    @Autowired
    private EnricherContext enricherContext;
    private CvTermFetcher<T> oboFetcher;

    public AbstractCvObjectFetcher() {
    }

    @Override
    public T fetchByIdentifier(String termIdentifier, String miOntologyName) throws BridgeFailedException {
        EnricherCache cvCache = enricherContext.getCacheManager().getCache("CvObject");
        String key = termIdentifier+"_"+miOntologyName;
        if (cvCache.isKeyInCache(key)) {
            return (T) cvCache.get(key);
        }
        else{
            T term = getOboFetcher().fetchByIdentifier(termIdentifier, miOntologyName);
            cvCache.put(key, term);
            return term;
        }
    }

    @Override
    public T fetchByIdentifier(String termIdentifier, CvTerm ontologyDatabase) throws BridgeFailedException {
        EnricherCache cvCache = enricherContext.getCacheManager().getCache("CvObject");
        String key = termIdentifier+"_"+ontologyDatabase.getShortName();
        if (cvCache.isKeyInCache(key)) {
            return (T) cvCache.get(key);
        }
        else{
            T term = getOboFetcher().fetchByIdentifier(termIdentifier, ontologyDatabase);
            cvCache.put(key, term);
            return term;
        }
    }

    @Override
    public T fetchByName(String searchName, String miOntologyName) throws BridgeFailedException {
        EnricherCache cvCache = enricherContext.getCacheManager().getCache("CvObject");
        String key = searchName+"_"+miOntologyName;
        if (cvCache.isKeyInCache(key)) {
            return (T) cvCache.get(key);
        }
        else{
            T term = getOboFetcher().fetchByName(searchName, miOntologyName);
            cvCache.put(key, term);
            return term;
        }
    }

    @Override
    public Collection<T> fetchByName(String searchName) throws BridgeFailedException {
        return getOboFetcher().fetchByName(searchName);
    }

    @Override
    public Collection<T> fetchByIdentifiers(Collection<String> termIdentifiers, String miOntologyName) throws BridgeFailedException {
        return getOboFetcher().fetchByIdentifiers(termIdentifiers, miOntologyName);
    }

    @Override
    public Collection<T> fetchByIdentifiers(Collection<String> termIdentifiers, CvTerm ontologyDatabase) throws BridgeFailedException {
        return getOboFetcher().fetchByIdentifiers(termIdentifiers, ontologyDatabase);
    }

    @Override
    public Collection<T> fetchByNames(Collection<String> searchNames, String miOntologyName) throws BridgeFailedException {
        return getOboFetcher().fetchByNames(searchNames, miOntologyName);
    }

    @Override
    public Collection<T> fetchByNames(Collection<String> searchNames) throws BridgeFailedException {
        return getOboFetcher().fetchByNames(searchNames);
    }

    protected CvTermFetcher<T> getOboFetcher() throws BridgeFailedException{
        if (this.oboFetcher == null){
            initialiseDefaultFetcher();
        }
        return oboFetcher;
    }

    protected abstract void initialiseDefaultFetcher() throws BridgeFailedException;

    protected void setOboFetcher(CvTermFetcher<T> oboFetcher) {
        this.oboFetcher = oboFetcher;
    }

    protected EnricherContext getEnricherContext() {
        return enricherContext;
    }
}
