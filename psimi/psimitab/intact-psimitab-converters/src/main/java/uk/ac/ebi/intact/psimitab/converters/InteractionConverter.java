/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.converters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Interaction Converter.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class InteractionConverter {

    public static final Log logger = LogFactory.getLog( InteractionConverter.class );

    private static final String TAXID = "taxid";

    private InteractorConverter interactorConverter = new InteractorConverter();

    private CvObjectConverter cvObjectConverter = new CvObjectConverter();

    private CrossReferenceConverter xConverter = new CrossReferenceConverter();

    private CrossReference defaultSourceDatabase = CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" );

    ///////////////////////////
    // Getters &  Setters


    public IntactBinaryInteraction toBinaryInteraction( Interaction interaction ) {
        return toBinaryInteraction( interaction, null, false );
    }

    public IntactBinaryInteraction toBinaryInteraction( Interaction interaction,
                                      ExpansionStrategy expansionStrategy,
                                      boolean isExpanded ) {

        if ( interaction == null ) {
            throw new IllegalArgumentException( "Interaction must not be null" );
        }

        final Collection<Component> components = interaction.getComponents();
        if ( components.size() != 2 ) {
            throw new IllegalArgumentException( "We only convert binary interaction (2 components or a single with stoichiometry 2)" );
        }
        Iterator<Component> iterator = components.iterator();
        uk.ac.ebi.intact.model.Interactor intactInteractorA = iterator.next().getInteractor();
        uk.ac.ebi.intact.model.Interactor intactInteractorB = iterator.next().getInteractor();

        Interactor interactorA = interactorConverter.toMitab( intactInteractorA );
        Interactor interactorB = interactorConverter.toMitab( intactInteractorB );

        IntactBinaryInteraction bi = new IntactBinaryInteraction( interactorA, interactorB );

        // set authors
        List<Author> authors = new ArrayList<Author>();
        if ( interaction.getExperiments() != null ) {
            for ( Experiment experiment : interaction.getExperiments() ) {
                for ( Annotation a : experiment.getAnnotations() ) {
                    if (a.getCvTopic().getMiIdentifier() != null) {
                        if ( CvTopic.AUTHOR_LIST_MI_REF.equals( a.getCvTopic().getMiIdentifier() ) ) {
                            authors.add( new AuthorImpl( a.getAnnotationText().split(" ")[0] + " et al." ) );
                        }
                    }
                }
            }
        }
        bi.setAuthors( authors );

        // set interaction detection method
        List<InteractionDetectionMethod> detectionMethods = new ArrayList<InteractionDetectionMethod>();
        if ( interaction.getExperiments() != null ) {
            for ( Experiment experiment : interaction.getExperiments() ) {
                if ( experiment.getCvInteraction() != null ) {
                    detectionMethods.add( ( InteractionDetectionMethod ) cvObjectConverter.
                            toCrossReference( InteractionDetectionMethodImpl.class, experiment.getCvInteraction() ) );
                }
            }
        }
        bi.setDetectionMethods( detectionMethods );

        // set interaction acs list
        List<CrossReference> interactionAcs = new ArrayList<CrossReference>();
        if ( interaction.getAc() != null ) {
            interactionAcs.add( CrossReferenceFactory.getInstance().build( CvDatabase.INTACT, interaction.getAc() ) );
            bi.setInteractionAcs( interactionAcs );
        }

        // set interaction type list
        if ( interaction.getCvInteractionType() != null ) {
            List<InteractionType> interactionTypes = new ArrayList<InteractionType>();
            interactionTypes.add( ( InteractionType ) cvObjectConverter.toCrossReference( InteractionTypeImpl.class,
                                                                                 interaction.getCvInteractionType() ) );
            bi.setInteractionTypes( interactionTypes );
        }

        // set publication list
        List<CrossReference> publications = new ArrayList<CrossReference>();
        if ( interaction.getExperiments() != null ) {
            for ( Experiment experiment : interaction.getExperiments() ) {
                if ( experiment.getXrefs() != null ) {
                    for ( Xref xref : experiment.getXrefs() ) {
                        if ( xref.getCvXrefQualifier() != null && xref.getCvDatabase().getShortLabel() != null ) {
                            CvObjectXref idref = CvObjectUtils.getPsiMiIdentityXref( xref.getCvXrefQualifier() );
                            if ( idref != null && idref.getPrimaryId() != null && idref.getPrimaryId().equals( CvXrefQualifier.PRIMARY_REFERENCE_MI_REF ) ) {
                                CrossReference publication = CrossReferenceFactory.getInstance()
                                        .build( xref.getCvDatabase().getShortLabel(), xref.getPrimaryId() );
                                publications.add( publication );
                            }
                        }
                    }
                }
            }
        }
        bi.setPublications( publications );

        // set source database list
        if ( interaction.getOwner() != null && interaction.getOwner().getXrefs() != null ) {
            List<CrossReference> sourceDatabases = xConverter.toCrossReferences( interaction.getOwner().getXrefs(), true, false );
            if ( !sourceDatabases.isEmpty() ){
                bi.setSourceDatabases( sourceDatabases );
            } else {
                bi.getSourceDatabases().add( defaultSourceDatabase );
            }            
        }

        // process extended

        // set properties of interactor A
        List<CrossReference> propertiesA = xConverter.toCrossReferences( intactInteractorA.getXrefs(), false, true );
        bi.setPropertiesA( propertiesA );

        // set properties of interactor B
        List<CrossReference> propertiesB = xConverter.toCrossReferences( intactInteractorB.getXrefs(), false, true );
        bi.setPropertiesB( propertiesB );

        // set type of interactor A
        List<CrossReference> interactorTypesA = new ArrayList<CrossReference>();
        interactorTypesA.add( cvObjectConverter.toCrossReference( intactInteractorA.getCvInteractorType() ) );
        bi.setInteractorTypeA( interactorTypesA );

        // set type of interactor B
        List<CrossReference> interactorTypesB = new ArrayList<CrossReference>();
        interactorTypesB.add( cvObjectConverter.toCrossReference( intactInteractorB.getCvInteractorType() ) );
        bi.setInteractorTypeB( interactorTypesB );

        // set host organism
        List<CrossReference> hostOrganisms = new ArrayList<CrossReference>();
        for ( Experiment experiment : interaction.getExperiments() ) {
            String id = experiment.getBioSource().getTaxId();
            if ( id != null ) {
                String text = experiment.getBioSource().getShortLabel();
                hostOrganisms.add( CrossReferenceFactory.getInstance().build( TAXID, id, text ) );
            }
        }
        bi.setHostOrganism( hostOrganisms );

        // set expermimental role of interactor A and B
        List<CrossReference> experimentRolesA = new ArrayList<CrossReference>();
        List<CrossReference> experimentRolesB = new ArrayList<CrossReference>();
        List<CrossReference> biologicalRolesA = new ArrayList<CrossReference>();
        List<CrossReference> biologicalRolesB = new ArrayList<CrossReference>();
        for ( Component component : interaction.getComponents() ) {
            if ( component.getInteractor().equals( intactInteractorA ) ) {
                biologicalRolesA.add( cvObjectConverter.toCrossReference( component.getCvBiologicalRole() ) );
                experimentRolesA.add( cvObjectConverter.toCrossReference( component.getCvExperimentalRole() ) );
            }
            if ( component.getInteractor().equals( intactInteractorB ) ) {
                biologicalRolesB.add( cvObjectConverter.toCrossReference( component.getCvBiologicalRole() ) );
                experimentRolesB.add( cvObjectConverter.toCrossReference( component.getCvExperimentalRole() ) );
            }
        }
        bi.setExperimentalRolesInteractorA( experimentRolesA );
        bi.setExperimentalRolesInteractorB( experimentRolesB );
        bi.setBiologicalRolesInteractorA( biologicalRolesA );
        bi.setBiologicalRolesInteractorB( biologicalRolesB );

        // set dataset
        if ( interaction.getExperiments() != null ) {
            List<String> datasets = new ArrayList<String>();
            for ( Experiment experiment : interaction.getExperiments() ) {
                for ( Annotation annotation : experiment.getAnnotations() ) {
                    CvObjectXref idXref = CvObjectUtils.getPsiMiIdentityXref( annotation.getCvTopic() );
                    if ( idXref != null && idXref.getPrimaryId() != null && idXref.getPrimaryId().equals( CvTopic.DATASET_MI_REF ) ) {
                        datasets.add( annotation.getAnnotationText() );
                    }
                }
            }
            bi.setDataset( datasets );
        }

        if (expansionStrategy != null && isExpanded) {
            bi.getExpansionMethods().add( expansionStrategy.getName() );
        }

        // annotations
        for (Annotation intactAnnotation : intactInteractorA.getAnnotations()) {
            uk.ac.ebi.intact.psimitab.model.Annotation annot =
                    new uk.ac.ebi.intact.psimitab.model.Annotation(intactAnnotation.getCvTopic().getShortLabel(), intactAnnotation.getAnnotationText());
            bi.getAnnotationsA().add(annot);
        }

        for (Annotation intactAnnotation : intactInteractorB.getAnnotations()) {
            uk.ac.ebi.intact.psimitab.model.Annotation annot =
                    new uk.ac.ebi.intact.psimitab.model.Annotation(intactAnnotation.getCvTopic().getShortLabel(), intactAnnotation.getAnnotationText());
            bi.getAnnotationsB().add(annot);
        }

        return bi;
    }
}