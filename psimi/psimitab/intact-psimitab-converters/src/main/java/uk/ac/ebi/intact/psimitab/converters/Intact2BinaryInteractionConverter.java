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
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.processor.ClusterInteractorPairProcessor;
import psidev.psi.mi.tab.processor.PostProcessorStrategy;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.psimitab.converters.expansion.SpokeWithoutBaitExpansion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


/**
 * Converter for intact-model-interaction to psimi-tab-binaryinteraction.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class Intact2BinaryInteractionConverter {

    public static final Log logger = LogFactory.getLog( Intact2BinaryInteractionConverter.class );

    private ExpansionStrategy expansionStrategy;

    private InteractionConverter interactionConverter = new InteractionConverter();

    private Class binaryInteractionClass;

    private BinaryInteractionHandler biHandler;

    private PostProcessorStrategy postProcessor;
                                    
    public Intact2BinaryInteractionConverter() {
        binaryInteractionClass = IntactBinaryInteraction.class;
        biHandler = new IntactBinaryInteractionHandler();
        expansionStrategy = new SpokeWithoutBaitExpansion();
        postProcessor = new ClusterInteractorPairProcessor();
    }

    public Intact2BinaryInteractionConverter(ExpansionStrategy expansionStrategy, PostProcessorStrategy postProcessor) {
        binaryInteractionClass = IntactBinaryInteraction.class;
        biHandler = new IntactBinaryInteractionHandler();
        this.expansionStrategy = expansionStrategy;
        this.postProcessor = postProcessor;
    }

    /////////////////////
    // Getters & Setters

    public ExpansionStrategy getExpansionStrategy() {
        return expansionStrategy;
    }

    public void setExpansionStrategy( ExpansionStrategy expansionStrategy ) {
        this.expansionStrategy = expansionStrategy;
    }

    public Class getBinaryInteractionClass() {
        return binaryInteractionClass;
    }

    public void setBinaryInteractionClass( Class binaryInteractionClass ) {
        this.binaryInteractionClass = binaryInteractionClass;
    }

    public BinaryInteractionHandler getBinaryInteractionHandler() {
        return biHandler;
    }

    public void setBinaryInteractionHandler( BinaryInteractionHandler biHandler ) {
        this.biHandler = biHandler;
    }

    public PostProcessorStrategy getPostProssesorStrategy() {
        return postProcessor;
    }

    public void setPostProssesorStrategy( PostProcessorStrategy postProssesorStrategy ) {
        this.postProcessor = postProssesorStrategy;
    }

    public Collection<BinaryInteraction> convert( Interaction ... interactions ) {
        return convert(Arrays.asList(interactions));
    }

    public Collection<BinaryInteraction> convert( Collection<Interaction> interactions ) {
        if ( interactions == null ) {
            throw new IllegalArgumentException( "Interaction(s) must not be null" );
        }

        Collection<BinaryInteraction> result = new ArrayList<BinaryInteraction>();

        for ( Interaction interaction : interactions ) {
            BinaryInteraction bi;
            if ( binaryInteractionClass != null ) {
                interactionConverter.setBinaryInteractionClass( binaryInteractionClass );
            }

            interactionConverter.setBinaryInteractionHandler( biHandler );

            if ( expansionStrategy != null ) {
                Collection<Interaction> expandedInteractions = expansionStrategy.expand( interaction );
                final boolean isExpanded = expandedInteractions.size() > 1;

                for ( Interaction expandedInteraction : expandedInteractions ) {

                    bi = interactionConverter.toBinaryInteraction( expandedInteraction, expansionStrategy, isExpanded );

                    if ( bi != null ) {
                        result.add( bi );
                    }
                }

            } else {
                bi = interactionConverter.toBinaryInteraction( interaction );

                if ( bi != null ) {
                    result.add( bi );
                }
            }
        }

        result = doPostProcessing( result );
        
        return result;
    }


    /**
     * Apply post processing to the given collecition of interactions. if no processing was requested, the given
     * collection is returned.
     *
     * @param interactions the collection of interaction on which to apply post processing.
     * @return a non null collection of interactions.
     */
    private Collection<BinaryInteraction> doPostProcessing( final Collection<BinaryInteraction> interactions ) {

        if ( interactions == null ) {
            throw new IllegalArgumentException( "Interaction cannot be null." );
        }

        Collection<BinaryInteraction> processedInteractions = null;

        // Run post processing (if requested)
        if ( postProcessor != null ) {
            if ( logger.isDebugEnabled() ) {
                logger.debug( "Running " + postProcessor.getClass().getSimpleName() + "..." );
            }
            processedInteractions = postProcessor.process( interactions );
            logger.debug( "Post processing completed." );
        } else {
            logger.debug( "No post processing requested." );
            processedInteractions = interactions;
        }

        return processedInteractions;
    }
}
