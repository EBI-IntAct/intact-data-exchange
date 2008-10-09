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
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;

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

    private CvObjectConverter cvObjectConverter = new CvObjectConverter();

    private CrossReferenceConverter xConverter = new CrossReferenceConverter();

    private CrossReference defaultSourceDatabase = CrossReferenceFactory.getInstance().build( "psi-mi", "MI:0469", "intact" );

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

        InteractorConverter interactorConverter = new InteractorConverter();
        
        ExtendedInteractor interactorA = interactorConverter.toMitab( intactInteractorA, interaction );
        ExtendedInteractor interactorB = interactorConverter.toMitab( intactInteractorB, interaction );

        IntactBinaryInteraction bi = new IntactBinaryInteraction( interactorA, interactorB );

        final Collection<Experiment> experiments = interaction.getExperiments();
        
        // set authors
        List<Author> authors = new ArrayList<Author>();
        if ( experiments != null ) {
            if (!experiments.isEmpty()) {
                Experiment experiment = experiments.iterator().next();

                Annotation authorAnnot = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(experiment, CvTopic.AUTHOR_LIST_MI_REF);
                Annotation yearAnnot = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(experiment, CvTopic.PUBLICATION_YEAR_MI_REF);

                String authorText = "-";

                if (authorAnnot != null) {
                    authorText = authorAnnot.getAnnotationText().split(" ")[0] + " et al.";
                }

                if (yearAnnot != null) {
                    authorText = authorText+" ("+yearAnnot.getAnnotationText()+")";
                }

                authors.add(new AuthorImpl(authorText));
            }
        }
        bi.setAuthors( authors );

        // set interaction detection method
        List<InteractionDetectionMethod> detectionMethods = new ArrayList<InteractionDetectionMethod>();
        if ( experiments != null ) {
            for ( Experiment experiment : experiments) {
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
        if ( experiments != null ) {
            for ( Experiment experiment : experiments) {
                    for ( Xref xref : experiment.getXrefs() ) {
                        if ( xref.getCvXrefQualifier() != null && xref.getCvDatabase().getShortLabel() != null ) {
                            if ( CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(xref.getCvXrefQualifier().getIdentifier()) ) {
                                CrossReference publication = CrossReferenceFactory.getInstance()
                                        .build( xref.getCvDatabase().getShortLabel(), xref.getPrimaryId() );
                                publications.add( publication );
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
                for (CrossReference sourceXref : sourceDatabases) {
                    sourceXref.setText(interaction.getOwner().getShortLabel());
                }
                bi.setSourceDatabases( sourceDatabases );
            } else {
                defaultSourceDatabase.setText(interaction.getOwner().getShortLabel());
                bi.getSourceDatabases().add( defaultSourceDatabase );
            }
        }

        // process extended

        // set host organism
        List<CrossReference> hostOrganisms = new ArrayList<CrossReference>();
        for ( Experiment experiment : experiments) {
            String id = experiment.getBioSource().getTaxId();
            if ( id != null ) {
                String text = experiment.getBioSource().getShortLabel();
                hostOrganisms.add( CrossReferenceFactory.getInstance().build( TAXID, id, text ) );
            }
        }
        bi.setHostOrganism( hostOrganisms );

        // set dataset
        if ( experiments != null ) {
            List<String> datasets = new ArrayList<String>();
            for ( Experiment experiment : experiments) {
                for ( Annotation annotation : experiment.getAnnotations() ) {
                    if ( CvTopic.DATASET_MI_REF.equals(annotation.getCvTopic().getIdentifier()) ) {
                        datasets.add( annotation.getAnnotationText() );
                    }
                }
            }
            bi.setDataset( datasets );
        }

        // expansion
        if (expansionStrategy != null && isExpanded) {
            bi.getExpansionMethods().add( expansionStrategy.getName() );
        }

        //parameters for interaction
        for ( InteractionParameter interactionParameter : interaction.getParameters() ) {
            uk.ac.ebi.intact.psimitab.model.Parameter parameterInteraction =
                    new uk.ac.ebi.intact.psimitab.model.Parameter(interactionParameter.getCvParameterType().getShortLabel(),
                                                                  interactionParameter.getFactor(),
                                                                  interactionParameter.getBase(),
                                                                  interactionParameter.getExponent(),
                                                                  (interactionParameter.getCvParameterUnit() != null? interactionParameter.getCvParameterUnit().getShortLabel() : null));
            bi.getParameters().add( parameterInteraction );
        }

        return bi;
    }
}