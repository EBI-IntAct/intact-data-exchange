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
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.psimitab.converters.expansion.IsExpansionStrategyAware;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

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

    private Class binaryInteractionClass;

    private InteractorConverter interactorConverter = new InteractorConverter();

    private CvObjectConverter cvObjectConverter = new CvObjectConverter();

    private CrossReferenceConverter xConverter = new CrossReferenceConverter();

    private BinaryInteractionHandler biHandler;

    private CrossReference defaultSourceDatabase = CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" );

    ///////////////////////////
    // Getters &  Setters

    public Class getBinaryInteractionClass() {
        return binaryInteractionClass;
    }

    public void setBinaryInteractionClass( Class binaryInteractionClass ) {
        this.binaryInteractionClass = binaryInteractionClass;
    }

    public void setBinaryInteractionHandler( BinaryInteractionHandler handler ) {
        this.biHandler = handler;
    }

    public BinaryInteraction toBinaryInteraction( Interaction interaction ) {
        return toBinaryInteraction( interaction, null, false );
    }

    public BinaryInteraction toBinaryInteraction( Interaction interaction,
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

        BinaryInteraction bi = new IntactBinaryInteraction( interactorA, interactorB );

        // set authors
        List<Author> authors = new ArrayList<Author>();
        if ( interaction.getExperiments() != null ) {
            for ( Experiment experiment : interaction.getExperiments() ) {
                for ( Annotation a : experiment.getAnnotations() ) {
                    final String mi = a.getCvTopic().getIdentifier();
                    if ( mi != null) {
                        if ( CvTopic.AUTHOR_LIST_MI_REF.equals( mi ) ) {
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

        // process extra columns
        if ( biHandler != null ) {
            if ( logger.isDebugEnabled() ) {
                logger.debug( "Starting to extra process: " + biHandler.getClass().getSimpleName() );
            }

            if ( biHandler instanceof IsExpansionStrategyAware ) {
                if ( isExpanded ) {
                    if ( logger.isDebugEnabled() ) {
                        logger.debug( "Using an expansion stategy aware column handler: " + biHandler.getClass().getName() );
                    }

                    ( ( IsExpansionStrategyAware ) biHandler ).process( ( BinaryInteractionImpl ) bi,
                                                                        interaction,
                                                                        expansionStrategy );
                } else {
                    biHandler.process( ( BinaryInteractionImpl ) bi, interaction );
                }
            } else {
                biHandler.process( ( BinaryInteractionImpl ) bi, interaction );
            }

        }

        return bi;
    }
}