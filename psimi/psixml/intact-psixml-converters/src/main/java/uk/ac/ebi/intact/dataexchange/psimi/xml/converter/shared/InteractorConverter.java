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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.InteractorType;
import psidev.psi.mi.xml.model.Organism;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.util.Crc64;

import java.util.Set;

/**
 * Interactor converter.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorConverter extends AbstractAnnotatedObjectConverter<Interactor, psidev.psi.mi.xml.model.Interactor> {

    private OrganismConverter organismConverter;
    private InteractorTypeConverter interactorTypeConverter;

    public InteractorConverter( Institution institution ) {
        super( institution, InteractorImpl.class, psidev.psi.mi.xml.model.Interactor.class );
        organismConverter = new OrganismConverter(institution);
        interactorTypeConverter = new InteractorTypeConverter(institution );
    }

    public Interactor psiToIntact( psidev.psi.mi.xml.model.Interactor psiObject ) {
        Interactor interactor = super.psiToIntact( psiObject );

        if ( !isNewIntactObjectCreated() ) {
            return interactor;
        }

        psiStartConversion(psiObject);

        Organism organism = psiObject.getOrganism();

        if (organism != null) {
            BioSource bioSource = organismConverter.psiToIntact(organism);
            interactor.setBioSource(bioSource);
        }

        IntactConverterUtils.populateNames( psiObject.getNames(), interactor );
        IntactConverterUtils.populateXref( psiObject.getXref(), interactor, new XrefConverter<InteractorXref>( getInstitution(), InteractorXref.class ) );
        IntactConverterUtils.populateAnnotations( psiObject, interactor, getInstitution() );

        String sequence = psiObject.getSequence();

        // sequence
        if ( sequence != null && interactor instanceof Polymer ) {
            Polymer polymer = ( Polymer ) interactor;
            polymer.setSequence( sequence );
            polymer.setCrc64( Crc64.getCrc64( sequence ) );

        }

        // no uniprot update?
        if (interactor instanceof Protein){
            Protein protein = (Protein) interactor;

            InteractorXref uniprotIdentity = ProteinUtils.getUniprotXref(protein);

            if (uniprotIdentity == null){
                boolean hasNoUniprotUpdate = false;

                for (Annotation a : protein.getAnnotations()){
                    if (CvTopic.NON_UNIPROT.equalsIgnoreCase(a.getCvTopic().getShortLabel())){
                        hasNoUniprotUpdate = true;
                    }
                }

                if (!hasNoUniprotUpdate){
                    CvTopic noUniprotUpdate = CvObjectUtils.createCvObject(protein.getOwner(), CvTopic.class, null, CvTopic.NON_UNIPROT);
                    Annotation noUniprot = new Annotation(noUniprotUpdate);
                    protein.addAnnotation(noUniprot);
                }
            }
        }

        psiEndConversion(psiObject);

        return interactor;
    }

    @Override
    protected Interactor newIntactObjectInstance( psidev.psi.mi.xml.model.Interactor psiObject ) {
        String shortLabel = psiObject.getNames().getShortLabel();

        return newInteractorAccordingToType( psiObject.getOrganism(), shortLabel, psiObject.getInteractorType() );
    }

    public psidev.psi.mi.xml.model.Interactor intactToPsi( Interactor intactObject ) {
        psidev.psi.mi.xml.model.Interactor interactor = super.intactToPsi( intactObject );

        if ( !isNewPsiObjectCreated() ) {
            return interactor;
        }

        intactStartConversation(intactObject);

        if ( !ConverterContext.getInstance().getInteractorConfig().isExcludePolymerSequence() ) {
            if ( intactObject instanceof Polymer ) {
                String sequence = IntactCore.ensureInitializedSequence((Polymer) intactObject);
                interactor.setSequence( sequence );
            }
        }

        InteractorType interactorType = ( InteractorType )
                PsiConverterUtils.toCvType( intactObject.getCvInteractorType(),
                        this.interactorTypeConverter,
                        this );
        interactor.setInteractorType( interactorType );

        if (intactObject.getBioSource() != null) {
            Organism organism = this.organismConverter.intactToPsi(intactObject.getBioSource());
            interactor.setOrganism(organism);
        }

        intactEndConversion(intactObject);

        return interactor;
    }

    protected Interactor newInteractorAccordingToType( Organism psiOrganism, String shortLabel, InteractorType psiInteractorType ) {

        BioSource organism = organismConverter.psiToIntact( psiOrganism );
        CvInteractorType interactorType = interactorTypeConverter.psiToIntact( psiInteractorType );

        // MI identifier
        String typeId = null;

        if (psiInteractorType.getXref() != null && psiInteractorType.getXref().getPrimaryRef() != null) {
            typeId = psiInteractorType.getXref().getPrimaryRef().getId();
        }

        // label
        String interactorTypeLabel = psiInteractorType.getNames().getShortLabel();

        // in dip files, it seams that the type is stored in the full name and the short label is empty
        if ( interactorTypeLabel == null ) {
            interactorTypeLabel = psiInteractorType.getNames().getFullName();
        }

        // using the mi identifier and the shortlabel to identify the type of interactor
        Interactor interactor = null;

        Set<String> dnaMis = ConverterContext.getInstance().getDnaTypeMis();
        Set<String> rnaMis = ConverterContext.getInstance().getRnaTypeMis();

        Set<String> dnaLabels = ConverterContext.getInstance().getDnaTypeLabels();
        Set<String> rnaLabels = ConverterContext.getInstance().getRnaTypeLabels();

        if (typeId != null){
            if ( CvInteractorType.PROTEIN_MI_REF.equals(typeId)) {
                interactor = new ProteinImpl( getInstitution(), organism, shortLabel, interactorType );
            } else if ( CvInteractorType.PEPTIDE_MI_REF.equals(typeId)) { // found in dip
                interactor = new ProteinImpl( getInstitution(), organism, shortLabel, interactorType );
            } else if ( CvInteractorType.SMALL_MOLECULE_MI_REF.equals(typeId)) {
                interactor = new SmallMoleculeImpl( shortLabel, getInstitution(), interactorType );
                interactor.setBioSource( organism );
            } else if ( CvInteractorType.NUCLEIC_ACID_MI_REF.equals(typeId) || dnaMis.contains(typeId) ||
                    rnaMis.contains(typeId)) {
                interactor = new NucleicAcidImpl( getInstitution(), organism, shortLabel, interactorType );
            } else if ( CvInteractorType.BIOPOLYMER_MI_REF.equals(typeId)) {
                interactor = new BioPolymerImpl( shortLabel, getInstitution(), interactorType );
                interactor.setBioSource( organism );
            } else if ( CvInteractorType.POLYSACCHARIDE_MI_REF.equals(typeId)) {
                interactor = new PolySaccharideImpl( shortLabel, getInstitution(), interactorType );
                interactor.setBioSource( organism );
            } else if ( CvInteractorType.UNKNOWN_PARTICIPANT_MI_REF.equals(typeId)) {
                interactor = new UnknownParticipantImpl( shortLabel, getInstitution(), interactorType );
                interactor.setBioSource( organism );
            } else {
                throw new PsiConversionException( "Interactor of unexpected type: " + typeId + " ("+interactorTypeLabel+")" );
            }
        }
        else{
            if ( interactorTypeLabel.equals( CvInteractorType.PROTEIN ) ) {
                interactor = new ProteinImpl( getInstitution(), organism, shortLabel, interactorType );
            } else if (interactorTypeLabel.equals( "peptide" ) ) { // found in dip
                interactor = new ProteinImpl( getInstitution(), organism, shortLabel, interactorType );
            } else if ( interactorTypeLabel.equals( CvInteractorType.SMALL_MOLECULE ) ) {
                interactor = new SmallMoleculeImpl( shortLabel, getInstitution(), interactorType );
                interactor.setBioSource( organism );
            } else if (CvInteractorType.NUCLEIC_ACID.equals(interactorTypeLabel) || dnaLabels.contains(interactorTypeLabel) ||
                    rnaLabels.contains(interactorTypeLabel) ) {
                interactor = new NucleicAcidImpl( getInstitution(), organism, shortLabel, interactorType );
            } else if ( interactorTypeLabel.equals( CvInteractorType.BIOPOLYMER ) ) {
                interactor = new BioPolymerImpl( shortLabel, getInstitution(), interactorType );
                interactor.setBioSource( organism );
            } else if ( interactorTypeLabel.equals( CvInteractorType.POLYSACCHARIDE ) ) {
                interactor = new PolySaccharideImpl( shortLabel, getInstitution(), interactorType );
                interactor.setBioSource( organism );
            } else if ( interactorTypeLabel.equals( CvInteractorType.UNKNOWN_PARTICIPANT ) ) {
                interactor = new UnknownParticipantImpl( shortLabel, getInstitution(), interactorType );
                interactor.setBioSource( organism );
            } else {
                throw new PsiConversionException( "Interactor of unexpected type: " + typeId + " ("+interactorTypeLabel+")" );
            }
        }

        return interactor;
    }

    @Override
    public void setInstitution(Institution institution)
    {
        super.setInstitution(institution);
        organismConverter.setInstitution(institution);
        interactorTypeConverter.setInstitution( getInstitution() );
    }
}