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
import uk.ac.ebi.intact.psimitab.converters.Intact2BinaryInteractionConverter;
import uk.ac.ebi.intact.psimitab.search.IntactInteractorIndexWriter;
import uk.ac.ebi.intact.psimitab.search.IntactPsimiTabIndexWriter;

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

    private final Directory ontologiesDirectory;
    private final OntologyNameFinder nameFinder;

    public DatabaseMitabExporter(Directory ontologiesDirectory, String ... supportedOntologies) {
        this.ontologiesDirectory = ontologiesDirectory;
        this.nameFinder = new OntologyNameFinder(ontologiesDirectory);

        for (String supportedOntology : supportedOntologies) {
            nameFinder.addOntologyName(supportedOntology);
        }
    }

    public void exportAllInteractors(Writer mitabWriter, Directory interactionDirectory, Directory interactorDirectory) throws IOException, IntactTransactionException {
        Query q = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getEntityManager().createQuery("from InteractorImpl where objclass <> :objClass");
        q.setParameter("objClass", InteractionImpl.class.getName());

        exportInteractors(q, mitabWriter, interactionDirectory, interactorDirectory);
    }

    public void exportInteractors(Query interactorQuery, Writer mitabWriter, Directory interactionDirectory, Directory interactorDirectory) throws IOException, IntactTransactionException {
        if (interactorQuery == null) {
            throw new NullPointerException("Query for interactors is null: interactorQuery");
        }
        if (mitabWriter == null) {
            throw new NullPointerException("mitabWriter is null");
        }
        if (interactionDirectory == null && interactorDirectory == null) {
            throw new IllegalArgumentException("At least one of the directories has to be non-null in order to index something!");
        }

        // create the ontologies index

        IndexWriter interactionIndexWriter = null;
        IndexWriter interactorIndexWriter = null;

        IntactPsimiTabIndexWriter interactionIndexer = null;
        IntactInteractorIndexWriter interactorIndexer = null;

        if (interactionDirectory != null) {
            interactionIndexWriter = new IndexWriter(interactionDirectory, new StandardAnalyzer(), true);
            interactionIndexer = new IntactPsimiTabIndexWriter(ontologiesDirectory);
        }

        if (interactorDirectory != null) {
            interactorIndexWriter = new IndexWriter(interactorDirectory, new StandardAnalyzer(), true);
            interactorIndexer = new IntactInteractorIndexWriter(ontologiesDirectory);
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
            interactors = findInteractors(interactorQuery, firstResult, maxResults);

            firstResult = firstResult + maxResults;

            for (Interactor interactor : interactors) {

                if (log.isTraceEnabled()) log.trace("Processing interactor: "+interactor.getShortLabel());

                interactorCount++;

                List<Interaction> interactions = new ArrayList<Interaction>();
                for (Component comp : interactor.getActiveInstances()) {
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

                for (IntactBinaryInteraction bi : binaryInteractions) {
                    final String line = docDef.interactionToString(bi);
                    mitabWriter.write(line+ NEW_LINE);

                    try {
                        if (interactionIndexer != null) {
                            interactionIndexer.addLineToIndex(interactionIndexWriter, line );
                        }
                        if (interactorIndexer != null) {
                            interactorIndexer.addLineToIndex(interactorIndexWriter, line );
                        }
                    } catch (MitabLineException e) {
                        log.error("Problem indexing binary interaction: "+bi, e);
                    }

                    interactionCount++;

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


    private static List<? extends Interactor> findInteractors(Query q, int firstResult, int maxResults) {
        q.setFirstResult(firstResult);
        q.setMaxResults(maxResults);

        return q.getResultList();
    }

}
