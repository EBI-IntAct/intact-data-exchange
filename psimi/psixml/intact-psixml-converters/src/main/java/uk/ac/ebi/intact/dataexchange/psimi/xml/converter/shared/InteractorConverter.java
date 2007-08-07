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
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.Crc64;

/**
 * Interactor converter.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorConverter extends AbstractAnnotatedObjectConverter<Interactor, psidev.psi.mi.xml.model.Interactor> {

    public InteractorConverter( Institution institution ) {
        super( institution, InteractorImpl.class, psidev.psi.mi.xml.model.Interactor.class );
    }

    public Interactor psiToIntact( psidev.psi.mi.xml.model.Interactor psiObject ) {
        Interactor interactor = super.psiToIntact( psiObject );

        if ( !isNewIntactObjectCreated() ) {
            return interactor;
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

        if ( !ConverterContext.getInstance().getInteractorConfig().isExcludePolymerSequence() ) {
            if ( intactObject instanceof Polymer ) {
                String sequence = ( ( Polymer ) intactObject ).getSequence();
                interactor.setSequence( sequence );
            }
        }

        InteractorType interactorType = ( InteractorType )
                PsiConverterUtils.toCvType( intactObject.getCvInteractorType(),
                                            new InteractorTypeConverter( getInstitution() ) );
        interactor.setInteractorType( interactorType );

        return interactor;
    }

    protected String psiElementKey( psidev.psi.mi.xml.model.Interactor psiObject ) {
        return "ior:" + psiObject.getId();
    }

    protected Interactor newInteractorAccordingToType( Organism psiOrganism, String shortLabel, InteractorType psiInteractorType ) {
        BioSource organism = new OrganismConverter( getInstitution() ).psiToIntact( psiOrganism );
        CvInteractorType interactorType = new InteractorTypeConverter( getInstitution() ).psiToIntact( psiInteractorType );

        String interactorTypeLabel = psiInteractorType.getNames().getShortLabel();

        // in dip files, it seams that the type is stored in the full name and the short label is empty
        if ( interactorTypeLabel == null ) {
            interactorTypeLabel = psiInteractorType.getNames().getFullName();
        }

        Interactor interactor = null;

        if ( interactorTypeLabel.equals( CvInteractorType.PROTEIN ) ) {
            interactor = new ProteinImpl( getInstitution(), organism, shortLabel, interactorType );
        } else if ( interactorTypeLabel.equals( "peptide" ) ) { // found in dip
            interactor = new ProteinImpl( getInstitution(), organism, shortLabel, interactorType );
        } else if ( interactorTypeLabel.equals( CvInteractorType.SMALL_MOLECULE ) ) {
            interactor = new SmallMoleculeImpl( shortLabel, getInstitution(), interactorType );
            interactor.setBioSource( organism );
        } else
        if ( interactorTypeLabel.equals( CvInteractorType.NUCLEIC_ACID ) || interactorTypeLabel.equals( CvInteractorType.DNA ) ) {
            interactor = new NucleicAcidImpl( getInstitution(), organism, shortLabel, interactorType );
        } else {
            throw new RuntimeException( "Interaction of unexpected type: " + interactorTypeLabel );
        }

        return interactor;
    }
}