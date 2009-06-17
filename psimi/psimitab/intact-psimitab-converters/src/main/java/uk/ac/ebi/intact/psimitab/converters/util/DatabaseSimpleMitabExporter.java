/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
 * limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.converters.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.builder.Row;
import uk.ac.ebi.intact.commons.util.ETACalculator;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.irefindex.seguid.RigDataModel;
import uk.ac.ebi.intact.irefindex.seguid.RigidGenerator;
import uk.ac.ebi.intact.irefindex.seguid.RogidGenerator;
import uk.ac.ebi.intact.irefindex.seguid.SeguidException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.psimitab.PsimitabTools;
import uk.ac.ebi.intact.psimitab.converters.InteractionConverter;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.psimitab.converters.expansion.NotExpandableInteractionException;
import uk.ac.ebi.intact.psimitab.converters.expansion.SpokeWithoutBaitExpansion;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;

import javax.persistence.Query;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Creates a MITAB file where each line represents a single interaction. Data is retrieved from the database and
 * the resulting data file may feature multiple lines involving the same interactors (ie. non clustered interactions).
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
@Deprecated
public class DatabaseSimpleMitabExporter {

    private static final Log log = LogFactory.getLog( DatabaseSimpleMitabExporter.class );
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String SMALLMOLECULE_MI_REF = "MI:0328";
    private static final String UNKNOWN_TAXID = "-3";


    public DatabaseSimpleMitabExporter() {
    }

    public void exportAllInteractions(Writer mitabWriter) throws IOException {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        final int interactionCount = daoFactory.getInteractorDao( InteractionImpl.class ).countAll();

        final String allInteractorsHql = "from InteractorImpl where objclass = '" + InteractionImpl.class.getName()+"'";
        exportInteractions(allInteractorsHql, interactionCount, mitabWriter);
    }

    public void exportInteractions(String interactionHqlQuery,
                                   int interactionTotalCount,
                                   Writer mitabWriter) throws IOException {

        if (interactionHqlQuery == null) {
            throw new NullPointerException("Query for interactions is null: interactionQuery");
        }
        if (mitabWriter == null) {
            throw new NullPointerException("mitabWriter is null");
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "Preparing to index " + interactionTotalCount + " interaction(s)." );
        }

        ETACalculator eta = null;
        if( interactionTotalCount > 0 ) {
            eta = new ETACalculator( interactionTotalCount );
        }

        // build the interaction clusters

        List<? extends Interaction> interactions = null;

        InteractionConverter converter = new InteractionConverter();

        int firstResult = 0;
        int maxResults = 1;
        int interactionCount = 0;
        int lineCount = 0;

        final IntactDocumentDefinition docDef = new IntactDocumentDefinition();
        final ExpansionStrategy expansion = new SpokeWithoutBaitExpansion();

        do {
            interactions = findInteractions(interactionHqlQuery, firstResult, maxResults);

            firstResult = firstResult + maxResults;

            for (Interaction interaction : interactions) {

                if (log.isTraceEnabled()) log.trace("Processing interaction: "+interaction.getShortLabel());

                if (!expansion.isExpandable(interaction)) {
                    if (log.isTraceEnabled()) log.trace("\tNot expandable. Skipped");
                    continue;
                }

                // expand our interaction into binary
                final Collection<Interaction> expandedInteractions;

                try {
                    expandedInteractions = expansion.expand( interaction );
                } catch (NotExpandableInteractionException e) {
                    throw new IllegalStateException("Should be expandable as we checked just before");
                }

                final boolean isExpanded = expandedInteractions.size() > 1;
                if (log.isTraceEnabled()) log.trace( expansion.getName() + " generated "+ expandedInteractions.size() + " binary interactions");

                for ( Interaction expandedInteraction : expandedInteractions ) {
                    
                    final IntactBinaryInteraction mitabInteraction = converter.toBinaryInteraction( expandedInteraction );

                    //adding the expansion strategy here
                    if ( isExpanded ) {
                        mitabInteraction.getExpansionMethods().add( expansion.getName() );
                    }

                    flipInteractorsIfNecessary(mitabInteraction);

                    Interactor[] pair = findInteractors( expandedInteraction, mitabInteraction );

                    // Update Interactors' ROGID - first, identify in which order they are stored in MITAB
                    RogidGenerator rogidGenerator = new RogidGenerator();
                    RigDataModel rigA = buildRigDataModel( pair[0] );
                    RigDataModel rigB = buildRigDataModel( pair[1] );
                    try {
                        final String rogA = rogidGenerator.calculateRogid( rigA.getSequence(), rigA.getTaxid() );
                        mitabInteraction.getInteractorA().getAlternativeIdentifiers().add(
                                new CrossReferenceImpl( "irefindex", rogA, "rogid" ) );

                        final String rogB = rogidGenerator.calculateRogid( rigB.getSequence(), rigB.getTaxid() );
                        mitabInteraction.getInteractorB().getAlternativeIdentifiers().add(
                                new CrossReferenceImpl( "irefindex", rogB, "rogid" ) );

                        // Update Interaction RIGID
                        RigidGenerator rigidGenerator = new RigidGenerator();
                        rigidGenerator.addSequence( rigA.getSequence(), rigA.getTaxid() );
                        rigidGenerator.addSequence( rigB.getSequence(), rigB.getTaxid() );
                        String rig = rigidGenerator.calculateRigid();
                        mitabInteraction.getInteractionAcs().add( new CrossReferenceImpl( "irefindex", rig, "rigid" ) );

                    } catch ( SeguidException e ) {
                        throw new RuntimeException( "An error occured while generating RIG/ROG identifier for " +
                                                    "interaction " + interaction.getAc(), e );
                    }

                    // write MITAB line
                    final Row row = docDef.createInteractionRowConverter().createRow( mitabInteraction );

                    // here we could save some processing by converting first the line to a Row,
                    // and then converting tow to string and giving this row to the indexers

                    final String line = row.toString();
                    mitabWriter.write(line+ NEW_LINE);
                    lineCount++;
                }

                interactionCount++;

                if( (interactionCount % 100) == 0 ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug( "Processed " + interactionCount + " of " + interactionTotalCount + " interactions ("
                                   + lineCount + " MITAB lines) - ETA: " + eta.printETA( interactionCount ) );
                    }
                    mitabWriter.flush();
                }
            }

        } while (!interactions.isEmpty());

        if ( log.isInfoEnabled() ) {
            log.info( "Completed export of Interactions as MITAB." );
        }
    }

    private RigDataModel buildRigDataModel( Interactor interactor ) {

        String taxid = null;
        if( interactor.getBioSource() != null ) {
            taxid =interactor.getBioSource().getTaxId();
        } else {
            taxid = UNKNOWN_TAXID;
        }

        String seq = null;
        if( interactor.getClass().isAssignableFrom( Polymer.class ) ) {
            Polymer polymer = (Polymer) interactor;
            seq = polymer.getSequence();
        }

        if( seq == null ) {
            if( interactor instanceof SmallMolecule ) {
                // find INCHI key
                final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel( interactor, "MI:2010" );// INCHI_MI_REF
                if( annotation != null ) {
                    seq = annotation.getAnnotationText();
                }
            }

            if( seq == null ) {
                seq = interactor.getAc();
            }
        }

        return new RigDataModel( seq, taxid );
    }

    private Interactor[] findInteractors( Interaction interaction, IntactBinaryInteraction binaryInteraction ) {

        Interactor[] pair = new Interactor[2];

        String interactorA = getIntactAc( binaryInteraction.getInteractorA() );
        String interactorB = getIntactAc( binaryInteraction.getInteractorB() );

        for ( Component component : interaction.getComponents() ) {

            final String interactorAc = component.getInteractor().getAc();

            if( interactorAc.equals( interactorA ) && pair[0] == null ) {
                pair[0] = component.getInteractor();
            } else if( interactorAc.equals( interactorB ) ){
                pair[1] = component.getInteractor();
            } else {
                throw new IllegalStateException( "Interaction AC: "+ interaction.getAc() + " with "+
                                                 interaction.getComponents().size()+" participants" + 
                                                 ", found Interactor '"+ interactorAc +
                                                 "' when expecting '"+interactorA+"' or '"+interactorB+"'" );
            }
        }

        if( pair[0] == null ) {
            System.out.println( interaction );
            throw new IllegalStateException( "Interaction '"+ interaction.getAc() +"': Could not identify interactor A: AC='" + interactorA +"' ");
        }

        if( pair[1] == null ) {
            System.out.println( interaction );
            throw new IllegalStateException( "Interaction '"+ interaction.getAc() +"':Could not identify interactor B: AC='" + interactorB +"' ");
        }

        return pair;
    }

    private String getIntactAc( ExtendedInteractor interactor ) {
        for ( CrossReference reference : interactor.getIdentifiers() ) {
            if( reference.getDatabase().equalsIgnoreCase("intact" ) ) {
                return reference.getIdentifier();
            }
        }
        return null;
    }

    /**
     * Flips the interactors if necessary, so the small molecule is always interactor A
     * @param bi
     */
    private void flipInteractorsIfNecessary(IntactBinaryInteraction bi) {
        PsimitabTools.reorderInteractors(bi, new Comparator<ExtendedInteractor>() {

            public int compare(ExtendedInteractor o1, ExtendedInteractor o2) {
                final CrossReference type1 = o1.getInteractorType();
                final CrossReference type2 = o2.getInteractorType();

                if (type1 != null && SMALLMOLECULE_MI_REF.equals(type1.getIdentifier())) {
                    return 1;
                } else if (type2 != null && SMALLMOLECULE_MI_REF.equals(type2.getIdentifier())) {
                    return -1;
                }
                return 0;
            }
        });
    }

    private static List<? extends Interaction> findInteractions( String interactionHqlQuery, int firstResult, int maxResults ) {
        Query q = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getEntityManager().createQuery(interactionHqlQuery);
        q.setFirstResult(firstResult);
        q.setMaxResults(maxResults);

        return q.getResultList();
    }

    private static List<? extends Interactor> findInteractors(String hql, int firstResult, int maxResults) {
        Query q = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getEntityManager().createQuery(hql);
        q.setFirstResult(firstResult);
        q.setMaxResults(maxResults);

        return q.getResultList();
    }
}