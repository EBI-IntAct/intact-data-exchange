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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.builder.Row;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.DataContext;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.InteractionImpl;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.psimitab.OntologyNameFinder;
import uk.ac.ebi.intact.psimitab.PsimitabTools;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.psimitab.converters.Intact2BinaryInteractionConverter;
import uk.ac.ebi.intact.psimitab.search.IntactInteractorIndexWriter;
import uk.ac.ebi.intact.psimitab.search.IntactPsimiTabIndexWriter;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.commons.util.ETACalculator;

import javax.persistence.Query;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Creates a MITAB file, and the interaction and interactors indexes directly using database data.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DatabaseMitabExporter {

    private static final Log log = LogFactory.getLog( DatabaseMitabExporter.class );
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String SMALLMOLECULE_MI_REF = "MI:0328";

    private final OntologyIndexSearcher ontologiesIndexSearcher;
    private final OntologyNameFinder nameFinder;
    private final String[] ontologiesToExpand;

    public DatabaseMitabExporter(OntologyIndexSearcher ontologiesIndexSearcher, String ... supportedOntologies) {
        this.ontologiesIndexSearcher = ontologiesIndexSearcher;
        this.nameFinder = new OntologyNameFinder(ontologiesIndexSearcher);
        this.ontologiesToExpand = supportedOntologies;

        for (String supportedOntology : supportedOntologies) {
            nameFinder.addOntologyName(supportedOntology);
        }
    }

    public void exportAllInteractors(Writer mitabWriter, Directory interactionDirectory, Directory interactorDirectory) throws IOException, IntactTransactionException {
        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        final int interactionCount = daoFactory.getInteractorDao( InteractionImpl.class ).countAll();
        final int allinteractorCount = daoFactory.getInteractorDao().countAll();
        final int interactingMoleculeTotalCount = allinteractorCount - interactionCount;
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();

        final String allInteractorsHql = "from InteractorImpl where objclass <> '" + InteractionImpl.class.getName()+"'";
        exportInteractors(allInteractorsHql, interactingMoleculeTotalCount, mitabWriter, interactionDirectory, interactorDirectory);
    }

    public void exportInteractors(String interactorQueryHql, int interactorTotalCount,
                                  Writer mitabWriter,
                                  Directory interactionDirectory,
                                  Directory interactorDirectory) throws IOException, IntactTransactionException {
        if (interactorQueryHql == null) {
            throw new NullPointerException("Query for interactors is null: interactorQuery");
        }
        if (mitabWriter == null) {
            throw new NullPointerException("mitabWriter is null");
        }
        if (interactionDirectory == null && interactorDirectory == null) {
            throw new IllegalArgumentException("At least one of the directories has to be non-null in order to index something!");
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "Preparing to index " + interactorTotalCount + " interactor(s)." );
        }

        ETACalculator eta = null;
        if( interactorTotalCount > 0 ) {
            eta = new ETACalculator( interactorTotalCount );
        }

        // create the ontologies index

        IndexWriter interactionIndexWriter = null;
        IndexWriter interactorIndexWriter = null;

        IntactPsimiTabIndexWriter interactionIndexer = null;
        IntactInteractorIndexWriter interactorIndexer = null;

        if (interactionDirectory != null) {
            interactionIndexWriter = new IndexWriter(interactionDirectory, new StandardAnalyzer(), true);
            interactionIndexer = new IntactPsimiTabIndexWriter(ontologiesIndexSearcher, ontologiesToExpand);
        }

        if (interactorDirectory != null) {
            interactorIndexWriter = new IndexWriter(interactorDirectory, new StandardAnalyzer(), true);
            interactorIndexer = new IntactInteractorIndexWriter(ontologiesIndexSearcher, ontologiesToExpand);
        }

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        final Set<String> interactionAcProcessed = new HashSet<String>();

        List<? extends Interactor> interactors = null;

        Intact2BinaryInteractionConverter converter = new Intact2BinaryInteractionConverter();

        int firstResult = 0;
        int maxResults = 1;
        int interactionCount = 0;
        int interactorCount = 0;

        do {
            dataContext.beginTransaction();
            interactors = findInteractors(interactorQueryHql, firstResult, maxResults);

            firstResult = firstResult + maxResults;

            for (Interactor interactor : interactors) {

                final Collection<Component> components = interactor.getActiveInstances();
                if( components.isEmpty() ) {
                    // not need to index
                    log.info( "Interactor " + interactor.getShortLabel() + " isn't involved in any interactions, skipping." );
                    continue;
                }

                if (log.isTraceEnabled()) log.trace("Processing interactor: "+interactor.getShortLabel());

                List<Interaction> interactions = new ArrayList<Interaction>();
                for (Component comp : components) {
                    final Interaction interaction = comp.getInteraction();

                    if (!interactionAcProcessed.contains(interaction.getAc())) {
                        interactions.add(interaction);
                        interactionAcProcessed.add(interaction.getAc());
                    }
                }

                if (log.isTraceEnabled()) log.trace("Starting conversion and property enrichment: "+interactor.getShortLabel());

                Collection<IntactBinaryInteraction> binaryInteractions = converter.convert(interactions);
                enrich(binaryInteractions);

                final IntactDocumentDefinition docDef = new IntactDocumentDefinition();

                if ( log.isTraceEnabled() ) log.trace( "Processing " + binaryInteractions.size() + " interactions..." );

                int count = 0;
                for (IntactBinaryInteraction bi : binaryInteractions) {
                    count++;
                    if ( log.isTraceEnabled() ) {
                        log.trace( "Processing interaction #" + count );
                    }

                    flipInteractorsIfNecessary(bi);

                    final Row row = docDef.createInteractionRowConverter().createRow( bi );

                    // here we could save some processing by converting first the line to a Row,
                    // and then converting tow to string and giving this row to the indexers

                    final String line = row.toString();
                    mitabWriter.write(line+ NEW_LINE);

                    if (interactionIndexer != null) {
                        if ( log.isTraceEnabled() ) log.trace( "Indexing interaction..." );
                        long start  = System.currentTimeMillis();
                        interactionIndexer.addBinaryInteractionToIndex(interactionIndexWriter, row );
                        long stop  = System.currentTimeMillis();
                        if ( log.isTraceEnabled() ) log.trace( "Took " + (stop - start) + "ms" );
                    }

                    if (interactorIndexer != null) {
                        if ( log.isTraceEnabled() ) log.trace( "Indexing interactor..." );
                        long start  = System.currentTimeMillis();
                        interactorIndexer.addBinaryInteractionToIndex(interactorIndexWriter, bi );
                        long stop  = System.currentTimeMillis();
                        if ( log.isTraceEnabled() ) log.trace( "Took " + (stop - start) + "ms" );
                    }

                    interactionCount++;

                    if (interactorCount % 50 == 0) {
                        if (eta != null) {
                            if ( log.isDebugEnabled() ) {
                                log.debug( "Processed "+interactorCount+"/"+interactorTotalCount+
                                           " [ETA: " + eta.printETA( interactorCount ) +"]");
                            }
                        }
                    }

                    if (interactorCount % 500 == 0) {
                        if (log.isDebugEnabled()) log.debug("Auto optimization of the interactor index");
                        if (interactorIndexWriter != null) {
                            interactorIndexWriter.optimize();
                        }
                    }

                    if (interactionCount % 100 == 0) {
                        if (log.isDebugEnabled()) log.debug("Processed "+interactionCount+" interactions ("+interactorCount+" interactors)");
                        mitabWriter.flush();

                        if (interactionIndexWriter != null) {
                            interactionIndexWriter.flush();
                        }

                        if (interactorIndexWriter != null) {
                            interactorIndexWriter.flush();
                        }
                    }
                }

                interactorCount++;
            }

            dataContext.commitTransaction();

        } while (!interactors.isEmpty());

        mitabWriter.flush();

        if (interactionIndexWriter != null) {
            if (log.isDebugEnabled()) log.debug("Optimizing interaction index");
            interactionIndexWriter.optimize();
            interactionIndexWriter.close();
            if (log.isInfoEnabled()) log.info("Indexed "+interactionCount+" interactions");
        }

        if (interactorIndexWriter != null) {
            if (log.isDebugEnabled()) log.debug("Optimizing interactor index");
            interactorIndexWriter.optimize();
            interactorIndexWriter.close();
            if (log.isInfoEnabled()) log.info("Indexed "+interactorCount+" interactors");
        }
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

    private void enrich(Collection<IntactBinaryInteraction> binaryInteractions) {
        for (IntactBinaryInteraction bi : binaryInteractions) {
            enrichCrossReferences(bi.getInteractorA().getProperties());
            enrichCrossReferences(bi.getInteractorB().getProperties());
        }
    }

    private void enrichCrossReferences(Collection<CrossReference> properties) {
        for (CrossReference xref : properties) {
            if (nameFinder.isOntologySupported(xref.getDatabase())) {
                try {
                    String name = nameFinder.getNameByIdentifier(xref.getIdentifier());
                    xref.setText(name);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static List<? extends Interactor> findInteractors(String hql, int firstResult, int maxResults) {
        Query q = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getEntityManager().createQuery(hql);
        q.setFirstResult(firstResult);
        q.setMaxResults(maxResults);

        return q.getResultList();
    }
}
