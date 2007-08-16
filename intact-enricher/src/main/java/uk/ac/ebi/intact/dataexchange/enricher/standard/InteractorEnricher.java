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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.InteractorFetcher;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.*;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorEnricher extends AnnotatedObjectEnricher<Interactor> {

    private static final Log log = LogFactory.getLog(InteractorEnricher.class);

    private static ThreadLocal<InteractorEnricher> instance = new ThreadLocal<InteractorEnricher>() {
        @Override
        protected InteractorEnricher initialValue() {
            return new InteractorEnricher();
        }
    };

    public static InteractorEnricher getInstance() {
        return instance.get();
    }

    protected InteractorEnricher() {
    }

    public void enrich(Interactor objectToEnrich) {
        if (log.isDebugEnabled()) {
            log.debug("Enriching Interactor: " + objectToEnrich.getShortLabel());
        }

        if (objectToEnrich.getBioSource() != null) {
            BioSourceEnricher.getInstance().enrich(objectToEnrich.getBioSource());
        }

        CvObjectEnricher cvObjectEnricher = CvObjectEnricher.getInstance();

        if (objectToEnrich.getCvInteractorType() != null) {
            cvObjectEnricher.enrich(objectToEnrich.getCvInteractorType());
        }

        if (objectToEnrich instanceof Protein) {
            Protein proteinToEnrich = (Protein) objectToEnrich;

            UniprotProtein uniprotProt = null;

            int taxId = Integer.valueOf(proteinToEnrich.getBioSource().getTaxId());
            InteractorXref uniprotXref = ProteinUtils.getUniprotXref(proteinToEnrich);

            if (uniprotXref != null) {
                String uniprotId = uniprotXref.getPrimaryId();

                if (log.isDebugEnabled()) log.debug("\tEnriching Uniprot protein: " + uniprotId + " (taxid:"+taxId+")"+" (taxid:"+taxId+")");

                uniprotProt = InteractorFetcher.getInstance().fetchInteractorFromUniprot(uniprotId, taxId);

            } else  {
                if (log.isDebugEnabled()) log.debug("\tEnriching Uniprot protein by shortLabel: "+proteinToEnrich.getShortLabel()+" (taxid:"+taxId+")");

                if (proteinToEnrich.getShortLabel() != null) {
                    uniprotProt = InteractorFetcher.getInstance().fetchInteractorFromUniprot(proteinToEnrich.getShortLabel(), taxId);
                }
            }

            if (uniprotProt != null) {
                updateXrefs(proteinToEnrich, uniprotProt);
                updateAliases(proteinToEnrich, uniprotProt);
                proteinToEnrich.setSequence(uniprotProt.getSequence());

                proteinToEnrich.setShortLabel(uniprotProt.getId().toLowerCase());
                proteinToEnrich.setFullName(uniprotProt.getDescription());
            }

        }

        super.enrich(objectToEnrich);

    }

    private void updateAliases(Interactor interactor, UniprotProtein uniprotProt) {
        InteractorAlias aliasGeneName = null;

        for (InteractorAlias currentAlias : interactor.getAliases()) {
            String aliasTypePrimaryId = CvObjectUtils.getPsiMiIdentityXref(currentAlias.getCvAliasType()).getPrimaryId();

            if (aliasTypePrimaryId.equals(CvAliasType.GENE_NAME_MI_REF)) {
                aliasGeneName = currentAlias;
                break;
            }
        }

        Collection<String> uniprotGenes = uniprotProt.getGenes();

        // this boolean defines if the gene name is found in the uniprotGenes list
        boolean currentGeneNameFound = (aliasGeneName != null) && uniprotGenes.contains(aliasGeneName.getName());

        if (!currentGeneNameFound) {
            // if not found, remove the existing alias with gene name
            if (aliasGeneName != null) {
                interactor.removeAlias(aliasGeneName);
            }

            for (String geneName : uniprotProt.getGenes()) {
                if (log.isDebugEnabled()) log.debug("\t\tNew gene name (Alias): " + geneName);

                InteractorAlias alias = AliasUtils.createAliasGeneName(interactor, geneName);
                interactor.addAlias(alias);
            }
        }
    }

    private void updateXrefs(Protein protein, UniprotProtein uniprotProt) {
        InteractorXref uniprotXref = ProteinUtils.getUniprotXref(protein);

        if (uniprotXref == null) {
            CvObjectBuilder cvObjectBuilder = new CvObjectBuilder();
            CvXrefQualifier identityQual = cvObjectBuilder.createIdentityCvXrefQualifier(protein.getOwner());
            CvDatabase uniprotDb = CvObjectUtils.createCvObject(protein.getOwner(), CvDatabase.class, CvDatabase.UNIPROT_MI_REF, CvDatabase.UNIPROT);

            InteractorXref xref = XrefUtils.createIdentityXref(protein, uniprotProt.getPrimaryAc(), identityQual, uniprotDb);
            protein.addXref(xref);
        }
    }

    public void close() {
    }
}