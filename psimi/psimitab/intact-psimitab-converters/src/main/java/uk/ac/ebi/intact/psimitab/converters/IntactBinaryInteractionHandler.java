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

import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceFactory;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles intact specific columns in the MITAB data import/export.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class IntactBinaryInteractionHandler implements BinaryInteractionHandler<IntactBinaryInteraction> {

    private static final Log log = LogFactory.getLog( IntactBinaryInteractionHandler.class );

    private CrossReferenceConverter<InteractorXref> xConverter = new CrossReferenceConverter<InteractorXref>();
    private CvObjectConverter<CrossReferenceImpl, CvObject> cvObjectConverter = new CvObjectConverter<CrossReferenceImpl, CvObject>();
    private static final String TAXID = "taxid";


    public void process( IntactBinaryInteraction ibi, Interaction interaction ) {
        Iterator<Component> iterator = interaction.getComponents().iterator();
        final uk.ac.ebi.intact.model.Interactor intactInteractorA = iterator.next().getInteractor();
        final uk.ac.ebi.intact.model.Interactor intactInteractorB = iterator.next().getInteractor();

        // set properties of interactor A
        List<CrossReference> propertiesA = xConverter.toCrossReferences( intactInteractorA.getXrefs(), false, true );
        ibi.getPropertiesA().addAll( propertiesA );

        // set properties of interactor B
        List<CrossReference> propertiesB = xConverter.toCrossReferences( intactInteractorB.getXrefs(), false, true );
        ibi.getPropertiesB().addAll( propertiesB );

        ibi.getInteractorTypeA().add( cvObjectConverter.toCrossReference( intactInteractorA.getCvInteractorType() ) );

        ibi.getInteractorTypeB().add( cvObjectConverter.toCrossReference( intactInteractorB.getCvInteractorType() ) );

        // set host organism
        for ( Experiment experiment : interaction.getExperiments() ) {
            String id = experiment.getBioSource().getTaxId();
            if ( id != null ) {
                String text = experiment.getBioSource().getShortLabel();
                ibi.getHostOrganism().add( CrossReferenceFactory.getInstance().build( TAXID, id, text ) );
            }
        }

        // set expermimental role of interactor A and B
        for ( Component component : interaction.getComponents() ) {
            final List<CrossReference> br;
            final List<CrossReference> er;

            // here we can afford to use == as we are checking if we have the same object instance
            if ( component.getInteractor() == intactInteractorA  ) {
                br = ibi.getBiologicalRolesInteractorA();
                er = ibi.getExperimentalRolesInteractorA();
            } else if ( component.getInteractor() == intactInteractorB ) {
                br = ibi.getBiologicalRolesInteractorB();
                er = ibi.getExperimentalRolesInteractorB();
            } else {
                throw new IllegalStateException( "Unexpected Interactor reference found: " + component.getInteractor() );
            }

            if( component.getCvBiologicalRole() != null ){
                br.add( cvObjectConverter.toCrossReference( component.getCvBiologicalRole() ) );
            }

            final Collection<CvExperimentalRole> experimentalRoles = component.getExperimentalRoles();
            if( ! experimentalRoles.isEmpty() ) {
                er.add( cvObjectConverter.toCrossReference( experimentalRoles.iterator().next() ) );
                if( er.size() > 1) {
                    if ( log.isWarnEnabled() ) {
                        log.warn( "Could not transfert all "+ er.size() +" ExperimentalRoles of Component: " + component.getAc() );
                    }
                }
            }
        }

        // set dataset
        if ( interaction.getExperiments() != null ) {
            for ( Experiment experiment : interaction.getExperiments() ) {
                for ( Annotation annotation : experiment.getAnnotations() ) {
                    if ( annotation.getCvTopic().getIdentifier().equals( CvTopic.DATASET_MI_REF ) ) {
                        ibi.getDataset().add( annotation.getAnnotationText() );
                    }
                }
            }
        }
    }

    public void mergeCollection( IntactBinaryInteraction interaction, IntactBinaryInteraction target ) {
        //To change body of implemented methods use File | Settings | File Templates.
        throw new UnsupportedOperationException( "Not implemented yet." );
    }

    ///////////////////////////////
    // IsExpansionStrategyAware

    public void process( IntactBinaryInteraction bi, Interaction interaction, ExpansionStrategy expansionStrategy )
             {
        // first, do the usual processing
        process( bi, interaction );

        // deal with expansion strategy now
        bi.getExpansionMethods().add( expansionStrategy.getName() );
    }
}
