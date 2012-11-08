package uk.ac.ebi.intact.task.mitab.index;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.InteractionOntologyLuceneIndexer;

import java.io.File;

/**
 * This tasklet will create a lucen index and index all the ontologies with interaction results
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/08/12</pre>
 */

public class InteractionOntologyPopulatorTasklet implements Tasklet {

    private String ontologiesSolrUrl;
    private String interactionSolrUrl;

    private String luceneDirectory;

    private InteractionOntologyLuceneIndexer interactionOntologyIndexer;

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (ontologiesSolrUrl == null) {
            throw new NullPointerException("ontologiesSolrUrl is null");
        }
        if (interactionSolrUrl == null) {
            throw new NullPointerException("interactionsSolrUrl is null");
        }
        if (luceneDirectory == null) {
            throw new NullPointerException("luceneDirectory is null");
        }

        this.interactionOntologyIndexer = new InteractionOntologyLuceneIndexer(this.ontologiesSolrUrl, this.interactionSolrUrl);

        this.interactionOntologyIndexer.loadAndIndexAllFacetFieldCounts(new File(luceneDirectory));

        this.interactionOntologyIndexer.shutDown();

        return RepeatStatus.FINISHED;
    }

    public void setOntologiesSolrUrl(String ontologiesSolrUrl) {
        this.ontologiesSolrUrl = ontologiesSolrUrl;
    }

    public void setInteractionSolrUrl(String interactionSolrUrl) {
        this.interactionSolrUrl = interactionSolrUrl;
    }

    public void setLuceneDirectory(String luceneDirectory) {
        this.luceneDirectory = luceneDirectory;
    }
}
