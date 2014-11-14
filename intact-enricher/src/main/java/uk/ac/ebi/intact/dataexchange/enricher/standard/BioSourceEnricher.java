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
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.full.FullOrganismEnricher;
import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Organism;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.BioSourceFetcher;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component(value = "intactBioSourceEnricher")
@Lazy
public class BioSourceEnricher extends FullOrganismEnricher {
    @Autowired
    private EnricherContext enricherContext;

    @Autowired
    public BioSourceEnricher(@Qualifier("intactBioSourceFetcher")BioSourceFetcher bioSourceFetcher) {
        super(bioSourceFetcher);
    }

    @Override
    /**
     * Overrides scientific name if not the same
     */
    protected void processScientificName(Organism organismToEnrich, Organism organismFetched) {
        // Scientific name
        if((organismFetched.getScientificName() != null
                && ! organismFetched.getScientificName().equalsIgnoreCase(organismToEnrich.getScientificName())
                ||(organismFetched.getScientificName() == null && organismToEnrich.getScientificName() != null))){

            String oldValue = organismToEnrich.getScientificName();
            organismToEnrich.setScientificName(organismFetched.getScientificName());
            if (getOrganismEnricherListener() != null)
                getOrganismEnricherListener().onScientificNameUpdate(organismToEnrich, oldValue);
        }
    }

    @Override
    /**
     * Overrides common name
     */
    protected void processCommonName(Organism organismToEnrich, Organism organismFetched) {
        // Common name
        if(organismToEnrich.getCellType() != null || organismToEnrich.getTissue() != null
                || (organismFetched.getCommonName() != null
                && ! organismFetched.getCommonName().equalsIgnoreCase(organismToEnrich.getCommonName()))
                ||(organismFetched.getCommonName() == null && organismToEnrich.getCommonName() != null)){

            String oldValue = organismToEnrich.getCommonName();
            String newName = organismFetched.getCommonName() != null ? organismFetched.getCommonName() : Integer.toString(organismFetched.getTaxId());

            if (organismToEnrich.getCellType() != null){
                newName = newName+"-"+organismToEnrich.getCellType().getShortName();
            }
            if (organismToEnrich.getTissue() != null){
                newName = newName+"-"+organismToEnrich.getTissue().getShortName();
            }
            organismToEnrich.setCommonName(newName.toLowerCase());
            if (getOrganismEnricherListener() != null)
                getOrganismEnricherListener().onCommonNameUpdate(organismToEnrich, oldValue);
        }
    }

    @Override
    protected void processTaxid(Organism organismToEnrich, Organism organismFetched) throws EnricherException{
        if (organismToEnrich.getTaxId() == 0) {
            throw new EnricherException("Biosource has an invalid taxid: 0 ("+organismToEnrich+")");
        }
    }

    @Override
    protected void processCellType(Organism entityToEnrich, Organism fetched) throws psidev.psi.mi.jami.enricher.exception.EnricherException {
        // update cell types if required
        if (enricherContext.getConfig().isUpdateCellTypesAndTissues()
                && entityToEnrich.getCellType() != null
                && getCvTermEnricher() != null){
            getCvTermEnricher().enrich(entityToEnrich.getCellType());
        }
    }

    @Override
    protected void processTissue(Organism entityToEnrich, Organism fetched) throws psidev.psi.mi.jami.enricher.exception.EnricherException {
        // update cell types if required
        if (enricherContext.getConfig().isUpdateCellTypesAndTissues()
                && entityToEnrich.getTissue() != null
                && getCvTermEnricher() != null){
            getCvTermEnricher().enrich(entityToEnrich.getTissue());
        }
    }

    @Override
    protected void processCompartment(Organism entityToEnrich, Organism fetched) throws psidev.psi.mi.jami.enricher.exception.EnricherException {
        // nothing to do
    }

    @Override
    protected void processAliases(Organism organismToEnrich, Organism organismFetched) throws psidev.psi.mi.jami.enricher.exception.EnricherException {
        if (organismFetched != null){
            super.processAliases(organismToEnrich, organismFetched);
        }
        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null){
            for (Alias alias : organismToEnrich.getAliases()) {
                if (alias.getType()!= null) {
                    getCvTermEnricher().enrich(alias.getType());
                }
            }
        }
    }

    @Override
    protected void onEnrichedVersionNotFound(Organism objectToEnrich) throws psidev.psi.mi.jami.enricher.exception.EnricherException {

        if (objectToEnrich.getCommonName() != null){
            objectToEnrich.setCommonName(objectToEnrich.getCommonName().toLowerCase());
        }

        // process celltype
        processCellType(objectToEnrich, null);

        // process tissue
        processTissue(objectToEnrich, null);

        // process aliases
        processAliases(objectToEnrich, null);

        super.onEnrichedVersionNotFound(objectToEnrich);
    }

    @Override
    public CvTermEnricher<CvTerm> getCvTermEnricher() {
        if (super.getCvTermEnricher() == null){
            super.setCvTermEnricher((CvTermEnricher<CvTerm>)ApplicationContextProvider.getBean("intactCvObjectEnricher"));
        }
        return super.getCvTermEnricher();
    }
}
