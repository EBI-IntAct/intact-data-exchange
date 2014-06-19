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
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyTerm;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherException;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.BioSourceFetcher;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class BioSourceEnricher extends AnnotatedObjectEnricher<BioSource> {

    @Autowired
    private BioSourceFetcher bioSourceFetcher;

    @Autowired
    private CvObjectEnricher cvObjectEnricher;

    public BioSourceEnricher() {
    }

    public void enrich(BioSource objectToEnrich) {

        // get the taxonomy term from newt
        int taxId = Integer.valueOf(objectToEnrich.getTaxId());

        if (taxId == 0) {
            throw new EnricherException("Biosource has an invalid taxid: "+taxId+" ("+objectToEnrich.getFullName()+")");
        }

        TaxonomyTerm term = bioSourceFetcher.fetchByTaxId(taxId);

        String label = objectToEnrich.getShortLabel();
        String fullName = objectToEnrich.getFullName();

        // we don't have cell types so we override
        if (objectToEnrich.getCvTissue() == null &&
                objectToEnrich.getCvCellType() == null && term != null){
            if (term.getCommonName() != null){
                label = term.getCommonName();
            }
            else if (term.getScientificName() != null){
                label = term.getScientificName();
            }
            else if (label == null && fullName != null) {
                label = fullName;
            }
            else if (label == null){
                label = objectToEnrich.getTaxId();
            }
            fullName = term.getScientificName();
        }
        // the label is null and needs to be updated
        else if (label == null){
            if (fullName != null){
               label = fullName;
            }
            else if (term != null && term.getCommonName() != null){
                label = term.getCommonName();
            }
            else if (term != null && term.getScientificName() != null){
                label = term.getScientificName();
            }
            else {
                label = objectToEnrich.getTaxId();
            }
        }

        if (objectToEnrich.getCvCellType() != null) {
            // update cell types if required
            if (getEnricherContext().getConfig().isUpdateCellTypesAndTissues()){
                cvObjectEnricher.enrich(objectToEnrich.getCvCellType());
            }
            // only override name when not provided because shortlabel is mandatory
            if (objectToEnrich.getShortLabel() == null && objectToEnrich.getCvCellType().getShortLabel() != null){
                label = label+"-"+ objectToEnrich.getCvCellType().getShortLabel();
            }
        }
        if (objectToEnrich.getCvTissue() != null) {
            // update cell types if required
            if (getEnricherContext().getConfig().isUpdateCellTypesAndTissues()){
                cvObjectEnricher.enrich(objectToEnrich.getCvTissue());
            }
            // only override name when not provided because shortlabel is mandatory
            if (objectToEnrich.getShortLabel() == null && objectToEnrich.getCvTissue().getShortLabel() != null){
                label = label+"-"+ objectToEnrich.getCvTissue().getShortLabel();
            }
        }

        if (label != null) {
            label = AnnotatedObjectUtils.prepareShortLabel(label.toLowerCase());
            objectToEnrich.setShortLabel(label);
        }

        if (fullName != null) {
            objectToEnrich.setFullName(fullName);
        }

        super.enrich(objectToEnrich);
    }

}
