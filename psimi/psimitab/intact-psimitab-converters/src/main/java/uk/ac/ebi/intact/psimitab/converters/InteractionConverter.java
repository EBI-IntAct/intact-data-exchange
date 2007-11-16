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
import psidev.psi.mi.tab.converter.xml2tab.BinaryInteractionUtils;
import psidev.psi.mi.tab.converter.xml2tab.TabConvertionException;
import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
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

    public static final String TAXID = "taxid";

    private Class binaryInteractionClass;

    private InteractorConverter interactorConverter = new InteractorConverter();

    private CvObjectConverter cvObjectConverter = new CvObjectConverter();

    private CrossReferenceConverter xConverter = new CrossReferenceConverter();

    ///////////////////////////
    // Getters &  Setters

    public Class getBinaryInteractionClass() {
        return binaryInteractionClass;
    }

    public void setBinaryInteractionClass( Class binaryInteractionClass ) {
        this.binaryInteractionClass = binaryInteractionClass;
    }

    public BinaryInteraction toMitab( Interaction interaction ) throws Intact2TabException {
        return toMitab( interaction, null, false );
    }

    public BinaryInteraction toMitab( Interaction interaction,
                                      ExpansionStrategy expansionStrategy,
                                      boolean isExpanded ) throws Intact2TabException {

        if ( interaction == null ) {
            // TODO propagate this behavious to other classes
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

        BinaryInteraction bi;
        try {
            bi = BinaryInteractionUtils.buildInteraction( interactorA, interactorB, binaryInteractionClass );
        } catch ( TabConvertionException e ) {
            throw new Intact2TabException( "Error while building a BinaryInteraction", e );
        }

        // set authors
        List<Author> authors = new ArrayList<Author>();
        if ( interaction.getExperiments() != null ) {
            for ( Experiment experiment : interaction.getExperiments() ) {
                for ( Annotation a : experiment.getAnnotations() ) {
                    CvObjectXref idXref = CvObjectUtils.getPsiMiIdentityXref( a.getCvTopic() );
                    if ( CvTopic.AUTHOR_LIST_MI_REF.equals( idXref.getPrimaryId() ) )
                        authors.add( new AuthorImpl( a.getCvTopic().getShortLabel() ) );
                }
            }
        }
        bi.setAuthors( authors );

        // set interaction detection method
        List<InteractionDetectionMethod> detectionMethods = new ArrayList<InteractionDetectionMethod>();
        if ( interaction.getExperiments() != null ) {
            for ( Experiment experiment : interaction.getExperiments() ) {
                if (experiment.getCvInteraction() != null) {
                    //TODO check experiment.getCvInteraction() for NULL
                    detectionMethods.add( ( InteractionDetectionMethod ) cvObjectConverter.
                            toMitab( InteractionDetectionMethodImpl.class, experiment.getCvInteraction() ) );
                }
            }
        }
        bi.setDetectionMethods( detectionMethods );

        // set interaction acs list
        List<CrossReference> interactionAcs = new ArrayList<CrossReference>();
        if ( interaction.getAc() != null ) {
            // TODO check interaction.getAc() for  NULL
            if (interaction.getAc() != null){
                interactionAcs.add( CrossReferenceFactory.getInstance().build( CvDatabase.INTACT, interaction.getAc() ) );
            }
        }
        bi.setInteractionAcs( interactionAcs );

        // set interaction type list
        if( interaction.getCvInteractionType() != null ) {
            List<InteractionType> interactionTypes = new ArrayList<InteractionType>();
            interactionTypes.add( ( InteractionType ) cvObjectConverter.toMitab( InteractionTypeImpl.class,
                                                                                 interaction.getCvInteractionType() ) );
            bi.setInteractionTypes( interactionTypes );
        }

        // set publication list
        List<CrossReference> publications = new ArrayList<CrossReference>();
        if ( interaction.getExperiments() != null ) {
            for ( Experiment experiment : interaction.getExperiments() ) {
                for ( Xref xref : experiment.getXrefs() ) {
                    // TODO filter on qualifier(primary-reference)
                    CrossReference publication = CrossReferenceFactory.getInstance()
                            .build( xref.getCvDatabase().getShortLabel(), xref.getPrimaryId() );
                    publications.add( publication );
                }
            }
        }
        bi.setPublications( publications );

        // set source database list
        // TODO check owner is null ?
        List<CrossReference> sourceDatabases = xConverter.toMitab( interaction.getOwner().getXrefs(), true );
        bi.setSourceDatabases( sourceDatabases );

        // TODO Move this code into a BinaryInteractionHandler, similar to what we did in MITAB parser
        if ( binaryInteractionClass != null && binaryInteractionClass.equals( IntActBinaryInteraction.class ) ) {

            IntActBinaryInteraction ibi = ( IntActBinaryInteraction ) bi;

            // set properties of interactor A
            List<CrossReference> propertiesA = xConverter.toMitab( intactInteractorA.getXrefs(), false );
            ibi.setPropertiesA( propertiesA );

            // set properties of interactor B
            List<CrossReference> propertiesB = xConverter.toMitab( intactInteractorB.getXrefs(), false );
            ibi.setPropertiesB( propertiesB );

            // set type of interactor A
            List<CrossReference> interactorTypesA = new ArrayList<CrossReference>();
            interactorTypesA.add( cvObjectConverter.toMitab( intactInteractorA.getCvInteractorType() ) );
            ibi.setInteractorTypeA( interactorTypesA );

            // set type of interactor B
            List<CrossReference> interactorTypesB = new ArrayList<CrossReference>();
            interactorTypesB.add( cvObjectConverter.toMitab( intactInteractorB.getCvInteractorType() ) );
            ibi.setInteractorTypeB( interactorTypesB );

            // set host organism
            List<CrossReference> hostOrganisms = new ArrayList<CrossReference>();
            for ( Experiment experiment : interaction.getExperiments() ) {
                String id = experiment.getBioSource().getTaxId();
                if ( id != null ) {
                    String text = experiment.getBioSource().getShortLabel();
                    hostOrganisms.add( CrossReferenceFactory.getInstance().build( TAXID, id, text ) );
                }
            }
            ibi.setHostOrganism( hostOrganisms );

            // set expermimental role of interactor A
            List<CrossReference> experimentRolesA = new ArrayList<CrossReference>();
            for ( Component activeInstance : intactInteractorA.getActiveInstances() ) {
                experimentRolesA.add( cvObjectConverter.toMitab( activeInstance.getCvExperimentalRole() ) );
            }
            ibi.setExperimentalRolesInteractorA( experimentRolesA );

            // set expermimental role of interactor B
            List<CrossReference> experimentRolesB = new ArrayList<CrossReference>();
            for ( Component activeInstance : intactInteractorB.getActiveInstances() ) {
                experimentRolesB.add( cvObjectConverter.toMitab( activeInstance.getCvExperimentalRole() ) );
            }
            ibi.setExperimentalRolesInteractorB( experimentRolesB );

            // set expansion method
            if ( isExpanded ) {
                ibi.setExpansionMethod( expansionStrategy.getName() );
            } else {
                ibi.setExpansionMethod( null );
            }

            // set dataset
            if ( interaction.getExperiments() != null ) {
                List<String> datasets = new ArrayList<String>();
                for ( Experiment experiment : interaction.getExperiments() ) {
                    for ( Annotation annotation : experiment.getAnnotations() ) {
                        //CvObjectXref idXref = CvObjectUtils.getPsiMiIdentityXref(annotation.getCvTopic());
                        if ( CvTopic.DATASET.equals( annotation.getCvTopic().getShortLabel() ) )
                            datasets.add( annotation.getCvTopic().getShortLabel() );
                    }
                }
            }
            ibi.setDataset( null );
        }

        return bi;
    }
}