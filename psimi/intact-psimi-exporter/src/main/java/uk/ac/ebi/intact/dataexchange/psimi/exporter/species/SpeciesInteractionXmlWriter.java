package uk.ac.ebi.intact.dataexchange.psimi.exporter.species;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.xml.cache.InMemoryIdentityObjectCache;
import psidev.psi.mi.jami.xml.cache.PsiXmlObjectCache;
import psidev.psi.mi.jami.xml.model.extension.factory.options.PsiXmlWriterOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * The SpeciesInteractionXmlWriter is an ItemStream and ItemWriter which can write the negative and positive interactions of each SpeciesInteractionUnit
 * to the proper directory.
 *
 * It will keep track of the generated identifiers for each interaction
 *
 * Some properties can be customized :
 * - the speciesParentFolderName which is the released directory where to copy the species files
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/09/11</pre>
 */

public class SpeciesInteractionXmlWriter extends SpeciesInteractionWriter {
    private static final Log log = LogFactory.getLog(SpeciesInteractionXmlWriter.class);

    /**
     * The name of the sequence id which is persisted
     */
    private final static String SEQUENCE_ID = "sequence_id";

    private int currentId = 0;


    private List<InteractionEvidence> interactionsToWrite;

    public SpeciesInteractionXmlWriter(){
        super();
        this.interactionsToWrite = new ArrayList<InteractionEvidence>();
    }
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        super.open(executionContext);

        // we get the last id generated by this processor
        if (executionContext.containsKey(SEQUENCE_ID)){
            currentId = executionContext.getInt(SEQUENCE_ID);
        }
        else {
            // we need to reset the pointers
            currentId = 0;
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

        super.update(executionContext);

        // update current pointers
        executionContext.put(SEQUENCE_ID, this.currentId);

        this.interactionsToWrite.clear();
    }

    @Override
    public void close() throws ItemStreamException {
        this.currentId = 0;
        this.interactionsToWrite.clear();
        super.close();
    }

    protected void addSupplementaryOptions() {
        if (getWriterOptions().containsKey(PsiXmlWriterOptions.ELEMENT_WITH_ID_CACHE_OPTION)){
            PsiXmlObjectCache previousCache = (PsiXmlObjectCache)getWriterOptions().get(PsiXmlWriterOptions.ELEMENT_WITH_ID_CACHE_OPTION);
            this.currentId = previousCache.getLastGeneratedId();
        }
        // add cache with id cache
        PsiXmlObjectCache cache = new InMemoryIdentityObjectCache();
        cache.resetLastGeneratedIdTo(this.currentId);
        getWriterOptions().put(PsiXmlWriterOptions.ELEMENT_WITH_ID_CACHE_OPTION, cache);
    }

    @Override
    protected void writeInteractions(InteractionEvidenceChunkIterator positiveIterator) {

        getPsiWriter().write(this.interactionsToWrite);
        this.interactionsToWrite.clear();
    }


    protected void preProcessInteractions(InteractionEvidenceChunkIterator iterator) {
        this.interactionsToWrite.clear();

        while(iterator.hasNext()){
            InteractionEvidence interaction = iterator.next();

            if (interaction != null){
                this.interactionsToWrite.add(interaction);
            }
        }

        // re-init current chunk  if we know we will have another batch of elements
        if (iterator.hasNextChunk() && getCurrentChunk() == 0){
            setCurrentChunk(1);
        }
    }
}
