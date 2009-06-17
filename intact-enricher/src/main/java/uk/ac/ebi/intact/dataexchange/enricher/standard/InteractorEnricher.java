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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.chebi.webapps.chebiWS.model.Entity;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.CvObjectFetcher;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.InteractorFetcher;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.*;
import uk.ac.ebi.intact.uniprot.model.Organism;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;

import java.util.Collection;

/**
 * This class enriches ie adds additional information to the Interactor by utilizing the webservices from UniProt and Chebi.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class InteractorEnricher extends AnnotatedObjectEnricher<Interactor> {

    private static final Log log = LogFactory.getLog(InteractorEnricher.class);

    @Autowired
    private CvObjectEnricher cvObjectEnricher;

    @Autowired
    private BioSourceEnricher bioSourceEnricher;

    @Autowired
    private InteractorFetcher interactorFetcher;

    @Autowired
    private CvObjectFetcher cvObjectFetcher;

    public InteractorEnricher() {
    }

    /**
     * If the interactor is an instance of Protein then,
     1.	Update Biosource, if taxid is known, if not update it will -3 //unknown
     2.	Get the UniprotXref from the interactor and fetch the UniprotProtein with  the uniprotId  using the UniprotRemoteService from intact-uniprot module in the bridges.
     3.	Update the Interactor to be enriched with data from the returned UniprotProtein with xrefs, aliases, sequences, shortlabel and fullname.

     If the interactor is an instance of Small molecule then,

     1. Fetch the chemical entity from Chebi using the Chebi Web Service.
     2. Update the shortlabel with ChebiAscii name.
     3. Add Chebi Inchi id as annotation.
     * @param objectToEnrich Interactor
     */
    public void enrich(Interactor objectToEnrich) {
        if (log.isDebugEnabled()) {
            log.debug("Enriching Interactor: " + objectToEnrich.getShortLabel());
        }

        // replace short label invalid chars
        objectToEnrich.setShortLabel(replaceLabelInvalidChars(objectToEnrich.getShortLabel()));

        if (objectToEnrich.getCvInteractorType() != null) {
            cvObjectEnricher.enrich(objectToEnrich.getCvInteractorType());
        }

        if (objectToEnrich instanceof Protein) {
            Protein proteinToEnrich = (Protein) objectToEnrich;

            UniprotProtein uniprotProt = null;

            int taxId;

            if (proteinToEnrich.getBioSource() != null) {
               taxId = Integer.valueOf(proteinToEnrich.getBioSource().getTaxId());
            } else {
                taxId = -3; // unknown
            }
            
            InteractorXref uniprotXref = ProteinUtils.getUniprotXref(proteinToEnrich);

            if (uniprotXref != null) {
                String uniprotId = uniprotXref.getPrimaryId();

                if (log.isDebugEnabled()) log.debug("\tEnriching Uniprot protein: " + uniprotId + " (taxid:"+taxId+")"+" (taxid:"+taxId+")");

                uniprotProt = interactorFetcher.fetchInteractorFromUniprot(uniprotId, taxId);

            } else  {
                if (log.isDebugEnabled()) log.debug("\tEnriching Uniprot protein by shortLabel: "+proteinToEnrich.getShortLabel()+" (taxid:"+taxId+")");

                if (proteinToEnrich.getShortLabel() != null) {
                    uniprotProt = interactorFetcher.fetchInteractorFromUniprot(proteinToEnrich.getShortLabel(), taxId);
                }
            }

            if (uniprotProt != null) {
                updateXrefs(proteinToEnrich, uniprotProt);
                updateAliases(proteinToEnrich, uniprotProt);
                proteinToEnrich.setSequence(uniprotProt.getSequence());

                proteinToEnrich.setShortLabel(AnnotatedObjectUtils.prepareShortLabel(uniprotProt.getId().toLowerCase()));
                proteinToEnrich.setFullName(uniprotProt.getDescription());

                Organism organism = uniprotProt.getOrganism();
                BioSource bioSource = new BioSource(objectToEnrich.getOwner(), organism.getName(), String.valueOf(organism.getTaxid()));
                proteinToEnrich.setBioSource(bioSource);
            }

            if (objectToEnrich.getBioSource() != null) {
                bioSourceEnricher.enrich(objectToEnrich.getBioSource());
            }

        }

        if ( objectToEnrich instanceof SmallMolecule ) {
            enrichWithChebi( objectToEnrich );
        }
        super.enrich(objectToEnrich);

    }

    /**
     *  Enriches the small molecule with chebiAsciiName and inchi Annotation
     * @param objectToEnrich SmallMolecule to be enriched
     */
    private void enrichWithChebi( Interactor objectToEnrich ) {
        if ( objectToEnrich == null ) {
            throw new NullPointerException( "You must give a non null objectToEnrich" );
        }
        if(!(objectToEnrich instanceof SmallMoleculeImpl)){
          throw new IllegalStateException( "Interactor is not SmallMolecule"+objectToEnrich.getShortLabel());
        }
        SmallMolecule smallMoleculeToEnrich = ( SmallMolecule ) objectToEnrich;
        final InteractorXref chebiXref = SmallMoleculeUtils.getChebiXref( smallMoleculeToEnrich );

        if ( chebiXref != null ) {
            String chebiId = chebiXref.getPrimaryId();
            if ( log.isDebugEnabled() ) {
                log.debug( "Enriching Chebi SmallMolecule: " + chebiId );
            }
            final Entity smallMoleculeChebiEntity = interactorFetcher.fetchInteractorFromChebi( chebiId );

            if ( smallMoleculeChebiEntity != null ) {
                String chebiAsciiName = smallMoleculeChebiEntity.getChebiAsciiName();
                //update shortlabel
                if ( chebiAsciiName != null ) {
                    smallMoleculeToEnrich.setShortLabel( prepareSmallMoleculeShortLabel( chebiAsciiName ) );
                }
                String inchiId = smallMoleculeChebiEntity.getInchi();
                //update annotation
                CvTopic inchiCvTopic = cvObjectFetcher.fetchByTermId(CvTopic.class, CvTopic.INCHI_ID_MI_REF);
                if(inchiCvTopic!=null){
                updateAnnotations( smallMoleculeToEnrich,inchiCvTopic,inchiId);
                }
            }
        }
    }

    private void updateAnnotations( Interactor interactor, CvTopic cvTopic, String annotationText ) {
        Annotation annotation = new Annotation( interactor.getOwner(), cvTopic, annotationText );
        interactor.addAnnotation( annotation );
    }

    /**
     * max size is 255 only
     * @param name chebiasciiname to be edited
     * @return altered chebiasciiname
     */
    private String prepareSmallMoleculeShortLabel(String name){
        if ( name == null ) {
            throw new NullPointerException( "You must give a non null name" );
        }
        if(name.length()>255){
            return name.substring( 0,255 );
        }
       return name.trim().toLowerCase();
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

        CvObjectBuilder cvObjectBuilder = new CvObjectBuilder();
        CvXrefQualifier identityQual = cvObjectBuilder.createIdentityCvXrefQualifier(protein.getOwner());
        CvDatabase uniprotDb = CvObjectUtils.createCvObject(protein.getOwner(), CvDatabase.class, CvDatabase.UNIPROT_MI_REF, CvDatabase.UNIPROT);

        if (uniprotXref == null) {
            InteractorXref xref = XrefUtils.createIdentityXref(protein, uniprotProt.getPrimaryAc(), identityQual, uniprotDb);
            protein.addXref(xref);
        }

        CvXrefQualifier secondaryAcQual = CvObjectUtils.createCvObject(protein.getOwner(), CvXrefQualifier.class, CvXrefQualifier.SECONDARY_AC_MI_REF, CvXrefQualifier.SECONDARY_AC);

        for (String secondaryAc : uniprotProt.getSecondaryAcs()) {
            InteractorXref xref = XrefUtils.createIdentityXref(protein, secondaryAc, secondaryAcQual, uniprotDb);
            protein.addXref(xref);
        }
    }

    protected String replaceLabelInvalidChars(String label) {
        label = label.replaceAll("-", "");
        return label;
    }
}
