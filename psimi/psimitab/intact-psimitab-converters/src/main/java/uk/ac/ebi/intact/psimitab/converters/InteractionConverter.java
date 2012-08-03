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
import psidev.psi.mi.tab.model.Confidence;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;

import java.util.*;

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

    private CrossReference defaultSourceDatabase = new CrossReferenceImpl( "psi-mi", "MI:0469", "intact" );

    ///////////////////////////
    // Getters & Setters

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
        final Component componentA = iterator.next();
        final Component componentB = iterator.next();

        InteractorConverter interactorConverter = new InteractorConverter();

        ExtendedInteractor interactorA = interactorConverter.toMitab( componentA, interaction );
        ExtendedInteractor interactorB = interactorConverter.toMitab( componentB, interaction );

        IntactBinaryInteraction bi = new IntactBinaryInteraction( interactorA, interactorB );

        final Collection<Experiment> experiments = interaction.getExperiments();

        List<Author> authors = new ArrayList<Author>();
        List<CrossReference> detectionMethods = new ArrayList<CrossReference>();
        Set<CrossReference> publications = new HashSet<CrossReference>();
        List<CrossReference> hostOrganisms = new ArrayList<CrossReference>();
        List<String> datasets = new ArrayList<String>();

        if ( experiments != null ) {
            for (Experiment experiment : experiments) {
                if (authors.isEmpty()) {
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

                // det methods
                if (experiment.getCvInteraction() != null) {
                    detectionMethods.add( cvObjectConverter.toCrossReference(CrossReferenceImpl.class, experiment.getCvInteraction()));
                }

                // publications and imex at the publication level
                Collection<Xref> expAndPublicationXrefs = new HashSet<Xref>();
                expAndPublicationXrefs.addAll(experiment.getXrefs());

                if (experiment.getPublication() != null) {
                    expAndPublicationXrefs.addAll(experiment.getPublication().getXrefs());
                }

                for (Xref xref : expAndPublicationXrefs) {
                    if (xref.getCvXrefQualifier() != null && xref.getCvDatabase().getShortLabel() != null) {
                        // publications
                        if (CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(xref.getCvXrefQualifier().getIdentifier())) {
                            CrossReference publication = new CrossReferenceImpl(xref.getCvDatabase().getShortLabel(), xref.getPrimaryId());
                            publications.add(publication);
                        }
                        // imexId
                        else if (CvXrefQualifier.IMEX_PRIMARY_MI_REF.equals(xref.getCvXrefQualifier().getIdentifier())) {
                            CrossReference publication =  new CrossReferenceImpl(xref.getCvDatabase().getShortLabel(), xref.getPrimaryId());
                            publications.add(publication);
                        }
                    }
                }

                // host organism
                String id = experiment.getBioSource().getTaxId();
                if (id != null) {
                    String text = experiment.getBioSource().getShortLabel();
                    hostOrganisms.add( new CrossReferenceImpl(TAXID, id, text));
                }

                // datasets
                for ( Annotation annotation : experiment.getAnnotations() ) {
                    if ( CvTopic.DATASET_MI_REF.equals(annotation.getCvTopic().getIdentifier()) ) {
                        datasets.add( annotation.getAnnotationText() );
                    }
                }

                // imex identifiers

            }


        }

        bi.setAuthors( authors );
        bi.setDetectionMethods( detectionMethods );
        bi.setPublications( new ArrayList<CrossReference>(publications) );
        bi.setHostOrganism( hostOrganisms );
        bi.setDataset( datasets );


        // set interaction acs list
        List<CrossReference> interactionAcs = new ArrayList<CrossReference>();
        if ( interaction.getAc() != null ) {
            interactionAcs.add( new CrossReferenceImpl( CvDatabase.INTACT, interaction.getAc() ) );
        }

        // imex
        for (InteractorXref xref : interaction.getXrefs()) {
            if (xref.getCvXrefQualifier() != null) {
                if (CvXrefQualifier.IMEX_PRIMARY_MI_REF.equals(xref.getCvXrefQualifier().getIdentifier()) ||
                        CvXrefQualifier.IMEX_EVIDENCE_MI_REF.equals(xref.getCvXrefQualifier().getIdentifier())) {
                    interactionAcs.add(new CrossReferenceImpl( CvDatabase.IMEX, xref.getPrimaryId() ));
                }
            }
        }

        bi.setInteractionAcs( interactionAcs );

        // set interaction type list
        if ( interaction.getCvInteractionType() != null ) {
            List<CrossReference> interactionTypes = new ArrayList<CrossReference>();
            interactionTypes.add( cvObjectConverter.toCrossReference( CrossReferenceImpl.class,interaction.getCvInteractionType() ) );
            bi.setInteractionTypes( interactionTypes );
        }

        // set source database list
        if ( interaction.getOwner() != null) {
            List<CrossReference> sourceDatabases = xConverter.toCrossReferences( interaction.getOwner().getXrefs(), true, false,CvDatabase.PSI_MI_MI_REF );

            if ( !sourceDatabases.isEmpty() ){
                for (CrossReference sourceXref : sourceDatabases) {
                    sourceXref.setText(interaction.getOwner().getShortLabel());
                }

            } else {
                String label = interaction.getOwner().getShortLabel();

                if (label == null || label.isEmpty()) {
                    label = "not specified";
                }

                label = label.toLowerCase();

                CrossReference sourceXref = new CrossReferenceImpl("unknown", label, label);
                sourceDatabases.add(sourceXref);

            }

            bi.setSourceDatabases( sourceDatabases );
        }

        // set interaction confidences
        if (!interaction.getConfidences().isEmpty()){
            List<Confidence> confidences = new ArrayList<Confidence>();

            for (uk.ac.ebi.intact.model.Confidence conf : interaction.getConfidences()){
                if (conf.getValue() != null){
                    String type = "unknown";

                    if (conf.getCvConfidenceType() != null){
                        type = conf.getCvConfidenceType().getShortLabel();
                    }

                    Confidence mitabConf = new ConfidenceImpl(type, conf.getValue());
                    confidences.add(mitabConf);
                }
            }

            bi.setConfidenceValues(confidences);
        }

        // process extended

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