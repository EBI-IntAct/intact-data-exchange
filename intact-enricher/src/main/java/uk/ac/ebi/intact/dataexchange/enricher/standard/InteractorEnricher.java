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
import uk.ac.ebi.chebi.webapps.chebiWS.model.DataItem;
import uk.ac.ebi.chebi.webapps.chebiWS.model.Entity;
import uk.ac.ebi.chebi.webapps.chebiWS.model.OntologyDataItem;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.CvObjectFetcher;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.InteractorFetcher;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.*;
import uk.ac.ebi.intact.uniprot.model.Organism;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;
import uk.ac.ebi.intact.uniprot.model.UniprotProteinTranscript;

import java.util.Collection;
import java.util.List;

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

    @Autowired
    private EnricherConfig enricherConfig;

    @Autowired
    private EnricherContext enricherContext;

    public InteractorEnricher() {
    }

    /**
     * <pre>
     * If the interactor is an instance of Protein then,
     * 1.	Update Biosource, if taxid is known, if not update it will -3 //unknown
     * 2.	Get the UniprotXref from the interactor and fetch the UniprotProtein with  the uniprotId  using the UniprotRemoteService from intact-uniprot module in the bridges.
     * 3.	Update the Interactor to be enriched with data from the returned UniprotProtein with xrefs, aliases, sequences, shortlabel and fullname.
     *
     * If the interactor is an instance of Small molecule then,
     *
     * 1. Fetch the chemical entity from Chebi using the Chebi Web Service.
     * 2. Update the shortlabel with ChebiAscii name.
     * 3. Add Chebi Inchi id as annotation.
     * </pre>
     *
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

            // TODO use the EnricherConfig to find out if we want to enrich the protein or not.
            if( enricherConfig.isUpdateProteins() ) {

                Protein proteinToEnrich = (Protein) objectToEnrich;

                UniprotProtein uniprotProt = null;
                UniprotProteinTranscript uniprotTrans = null;

                int taxId;

                if (proteinToEnrich.getBioSource() != null) {
                    taxId = Integer.valueOf(proteinToEnrich.getBioSource().getTaxId());
                } else {
                    taxId = -3; // unknown
                }

                InteractorXref uniprotXref = ProteinUtils.getUniprotXref(proteinToEnrich);

                if (uniprotXref != null) {
                    String uniprotId = uniprotXref.getPrimaryId();

                    if (uniprotId.contains("-") || uniprotId.contains("PRO_")){
                        if (log.isDebugEnabled()) log.debug("\tEnriching Uniprot protein isoform/chain: " + uniprotId + " (taxid:"+taxId+")");

                        uniprotTrans = interactorFetcher.fetchProteinTranscriptFromUniprot(uniprotId, taxId);
                    }
                    else{
                        if (log.isDebugEnabled()) log.debug("\tEnriching Uniprot protein: " + uniprotId + " (taxid:"+taxId+")");

                        uniprotProt = interactorFetcher.fetchInteractorFromUniprot(uniprotId, taxId);
                    }

                } //else  {
                //if (log.isDebugEnabled()) log.debug("\tEnriching Uniprot protein by shortLabel: "+proteinToEnrich.getShortLabel()+" (taxid:"+taxId+")");

                //if (proteinToEnrich.getShortLabel() != null) {
                //uniprotProt = interactorFetcher.fetchInteractorFromUniprot(proteinToEnrich.getShortLabel(), taxId);
                //}
                //}

                if (uniprotProt != null) {
                    updateXrefs(proteinToEnrich, uniprotProt);
                    updateAliases(proteinToEnrich, uniprotProt);
                    updateSequence(proteinToEnrich, uniprotProt);

                    proteinToEnrich.setShortLabel(AnnotatedObjectUtils.prepareShortLabel(uniprotProt.getId().toLowerCase()));
                    proteinToEnrich.setFullName(uniprotProt.getDescription());

                    Organism organism = uniprotProt.getOrganism();
                    BioSource bioSource = new BioSource(objectToEnrich.getOwner(), organism.getName(), String.valueOf(organism.getTaxid()));
                    proteinToEnrich.setBioSource(bioSource);
                }
                else if (uniprotTrans != null){
                    updateIsoformXrefs(proteinToEnrich, uniprotTrans);
                    updateAliases(proteinToEnrich, uniprotTrans.getMasterProtein());
                    updateSequence(proteinToEnrich, uniprotTrans);

                    proteinToEnrich.setShortLabel(AnnotatedObjectUtils.prepareShortLabel(uniprotTrans.getPrimaryAc().toLowerCase()));
                    proteinToEnrich.setFullName(uniprotTrans.getDescription());

                    Organism organism = uniprotTrans.getOrganism();
                    BioSource bioSource = new BioSource(objectToEnrich.getOwner(), organism.getName(), String.valueOf(organism.getTaxid()));
                    proteinToEnrich.setBioSource(bioSource);
                }
                else {
                    String shortlabel = proteinToEnrich.getShortLabel();

                    if (shortlabel != null){
                        proteinToEnrich.setShortLabel(proteinToEnrich.getShortLabel().toLowerCase());
                    }
                }

                if (proteinToEnrich.getBioSource() != null) {
                    bioSourceEnricher.enrich(objectToEnrich.getBioSource());
                }

            }

        } else if ( objectToEnrich instanceof SmallMolecule ) {

            enrichWithChebi( objectToEnrich );
        }

        super.enrich(objectToEnrich);
    }


    /**
     * Enriches the small molecule with chebiAsciiName and inchi Annotation.
     * @param objectToEnrich SmallMolecule to be enriched
     */
    private void enrichWithChebi( Interactor objectToEnrich ) {

        // check in the config whether we want to enrich small molecules or not
        log.debug( "enricherConfig.isUpdateSmallMolecules()="+enricherConfig.isUpdateSmallMolecules() );
        log.debug( "enricherContext.getConfig().isUpdateSmallMolecules()="+enricherContext.getConfig().isUpdateSmallMolecules() );

        if( enricherConfig.isUpdateSmallMolecules() ) {

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
                final Entity chebiEntity = interactorFetcher.fetchInteractorFromChebi( chebiId );

                if ( chebiEntity != null ) {
                    String chebiAsciiName = chebiEntity.getChebiAsciiName();
                    // update shortlabel
                    if ( chebiAsciiName != null ) {
                        smallMoleculeToEnrich.setShortLabel( prepareSmallMoleculeShortLabel( chebiAsciiName ) );
                    }

                    // Check on chebi identifier, and if need be fix their qualifiers
                    if( enricherConfig.isUpdateSmallMoleculeChebiXrefs() ) {
                        updateSmallMoleculeXrefs( objectToEnrich, chebiEntity );
                    }

                    // IUPAC name
                    CvAliasType iupacAlias = cvObjectFetcher.fetchByTermId( CvAliasType.class, "MI:2007" );
                    for ( DataItem item : chebiEntity.getIupacNames() ) {
                        updateAliases( smallMoleculeToEnrich, iupacAlias, item.getData() );
                    }

                    for ( DataItem item : chebiEntity.getSynonyms() ) {
                        boolean hasSameLabel = item.getData().toLowerCase().equals( smallMoleculeToEnrich.getShortLabel() );
                        if( item.getType().equals( "INN" ) && ! hasSameLabel ) {
                            updateAliases( smallMoleculeToEnrich, null, item.getData() );
                        }
                    }

                    //update annotation (inchi)
                    String inchiId = chebiEntity.getInchi();
                    CvTopic inchiCvTopic = cvObjectFetcher.fetchByTermId( CvTopic.class, CvTopic.INCHI_ID_MI_REF );
                    if( inchiCvTopic != null ){
                        updateAnnotations( smallMoleculeToEnrich, inchiCvTopic, inchiId );
                    }

                    // update aliases (ontology role)
                    CvTopic functionTopic = cvObjectFetcher.fetchByTermId( CvTopic.class, CvTopic.FUNCTION_MI_REF );
                    if( functionTopic != null ) {
                        for ( OntologyDataItem parent : chebiEntity.getOntologyParents() ) {
                            if( "has role".equals( parent.getType() ) ) {
                                updateAnnotations( smallMoleculeToEnrich, functionTopic, parent.getChebiName() );
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateSmallMoleculeXrefs( Interactor objectToEnrich, Entity chebiEntity ) {
        final CvXrefQualifier identity = cvObjectFetcher.fetchByTermId( CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF );
        final CvXrefQualifier secondary = cvObjectFetcher.fetchByTermId( CvXrefQualifier.class, CvXrefQualifier.SECONDARY_AC_MI_REF );

        // store chebi identifiers, so that we can identify the missing one in the given interactor.
        List<String> chebiSecondaries = chebiEntity.getSecondaryChEBIIds();
        String chebiPrimary = chebiEntity.getChebiId();

        final Collection<InteractorXref> chebiRefs = AnnotatedObjectUtils.searchXrefs( objectToEnrich, CvDatabase.CHEBI_MI_REF, null );
        for ( InteractorXref chebiRef : chebiRefs ) {
            if( chebiPrimary != null && chebiPrimary.equals( chebiRef.getPrimaryId() ) ) {
                chebiRef.setCvXrefQualifier( identity );
                chebiPrimary = null;
            } else if( chebiSecondaries.contains( chebiRef.getPrimaryId() ) ) {
                chebiRef.setCvXrefQualifier( secondary );
                chebiSecondaries.remove( chebiRef.getPrimaryId() );
            } else {
                log.warn( "Small molecule (ac:"+ objectToEnrich.getAc() +
                        ", shortlabel:"+ objectToEnrich.getShortLabel() +") has a chebi ID ("+
                        chebiRef.getPrimaryId() +") that isn't found in the corresponding ChEBI entity" );
            }
        }

        // add missing identity
        if( chebiPrimary != null ) {
            InteractorXref xref =
                    new InteractorXref( objectToEnrich.getOwner(),
                            cvObjectFetcher.fetchByTermId( CvDatabase.class, CvDatabase.CHEBI_MI_REF ),
                            chebiPrimary,
                            identity );
            objectToEnrich.addXref( xref );
        }

        // add missing secondary identifier
        for ( String chebiSecondary : chebiSecondaries ) {
            InteractorXref xref =
                    new InteractorXref( objectToEnrich.getOwner(),
                            cvObjectFetcher.fetchByTermId( CvDatabase.class, CvDatabase.CHEBI_MI_REF ),
                            chebiSecondary,
                            secondary );
            objectToEnrich.addXref( xref );
        }
    }

    private void updateAnnotations( Interactor interactor, CvTopic cvTopic, String annotationText ) {
        Annotation annotation = new Annotation( interactor.getOwner(), cvTopic, annotationText );
        if( ! interactor.getAnnotations().contains( annotation )) {
            interactor.addAnnotation( annotation );
        }
    }

    private void updateAliases( Interactor interactor, CvAliasType cvAlias, String aliasText ) {
        InteractorAlias alias = new InteractorAlias( interactor.getOwner(), interactor, cvAlias, aliasText );
        if( ! interactor.getAliases().contains( alias )) {
            interactor.addAlias( alias );
        }
    }

    private void updateSequence(Protein proteinToEnrich, UniprotProtein uniprotProt) {
        if (uniprotProt.getSequence() != null && !uniprotProt.getSequence().equals(proteinToEnrich.getSequence())) {
            proteinToEnrich.setSequence(uniprotProt.getSequence());
        }
    }

    private void updateSequence(Protein proteinToEnrich, UniprotProteinTranscript uniprotProt) {
        if (uniprotProt.getSequence() != null && !uniprotProt.getSequence().equals(proteinToEnrich.getSequence())) {
            proteinToEnrich.setSequence(uniprotProt.getSequence());
        }
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
            if( currentAlias.getCvAliasType() != null ) {
                final CvObjectXref xref = CvObjectUtils.getPsiMiIdentityXref( currentAlias.getCvAliasType() );
                if( xref != null ) {
                    String aliasTypePrimaryId = xref.getPrimaryId();

                    if (aliasTypePrimaryId.equals(CvAliasType.GENE_NAME_MI_REF)) {
                        aliasGeneName = currentAlias;
                        break;
                    }
                }
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

    private void updateIsoformXrefs(Protein protein, UniprotProteinTranscript uniprotProt) {
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
