package uk.ac.ebi.intact.dataexchange.dbimporter.writer;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.model.Interaction;
import uk.ac.ebi.intact.jami.service.IntactService;
import uk.ac.ebi.intact.jami.synchronizer.listener.impl.DbSynchronizerStatisticsReporter;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public class IntactDbImporter<I extends Interaction> implements ItemWriter<I>,ItemStream{

    private IntactService<I> interactionService;
    private DbSynchronizerStatisticsReporter synchronizerListener;

    private Map<Class, Integer> persistedCounts;
    private Map<Class, Integer> mergedCounts;
    private Map<Class, Integer> deletedCounts;
    private Map<Class, Integer> mergedTransientCounts;
    private Map<Class, Integer> transientReplacedCounts;

    private Logger log = Logger.getLogger(IntactDbImporter.class.getName());

    private final static String PERSIST_MAP_COUNT = "persisted.map";
    private final static String MERGE_MAP_COUNT = "merged.map";
    private final static String DELETED_MAP_COUNT = "deleted.map";
    private final static String MERGED_TRANSIENT_MAP_COUNT = "merged.transient.map";
    private final static String REPLACED_TRANSIENT_MAP_COUNT = "transient.replaced.map";

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        if (interactionService == null){
            throw new IllegalStateException("The interaction service must be provided. ");
        }

        this.synchronizerListener = new DbSynchronizerStatisticsReporter();

        // restore previous statistics
        if (!executionContext.isEmpty()){
            for (Map.Entry<String, Object> entry : executionContext.entrySet()){
                if (entry.getKey().startsWith(PERSIST_MAP_COUNT+"_")){
                    try {
                        this.synchronizerListener.getPersistedCounts()
                                .put(Class.forName(entry.getKey().substring(PERSIST_MAP_COUNT.length() + 1)), (Integer) entry.getValue());
                    } catch (ClassNotFoundException e) {
                        throw new ItemStreamException("Cannot reload persisted statistics from execution context",e);
                    }
                }
                else if (entry.getKey().startsWith(MERGE_MAP_COUNT+"_")){
                    try {
                        this.synchronizerListener.getMergedCounts()
                                .put(Class.forName(entry.getKey().substring(MERGE_MAP_COUNT.length() + 1)), (Integer) entry.getValue());
                    } catch (ClassNotFoundException e) {
                        throw new ItemStreamException("Cannot reload persisted statistics from execution context",e);
                    }
                }
                else if (entry.getKey().startsWith(DELETED_MAP_COUNT+"_")){
                    try {
                        this.synchronizerListener.getDeletedCounts()
                                .put(Class.forName(entry.getKey().substring(DELETED_MAP_COUNT.length() + 1)), (Integer) entry.getValue());
                    } catch (ClassNotFoundException e) {
                        throw new ItemStreamException("Cannot reload persisted statistics from execution context",e);
                    }
                }
                else if (entry.getKey().startsWith(MERGED_TRANSIENT_MAP_COUNT+"_")){
                    try {
                        this.synchronizerListener.getMergedTransientCounts()
                                .put(Class.forName(entry.getKey().substring(MERGED_TRANSIENT_MAP_COUNT.length() + 1)), (Integer) entry.getValue());
                    } catch (ClassNotFoundException e) {
                        throw new ItemStreamException("Cannot reload persisted statistics from execution context",e);
                    }
                }
                else if (entry.getKey().startsWith(REPLACED_TRANSIENT_MAP_COUNT+"_")){
                    try {
                        this.synchronizerListener.getTransientReplacedCounts()
                                .put(Class.forName(entry.getKey().substring(REPLACED_TRANSIENT_MAP_COUNT.length() + 1)), (Integer) entry.getValue());
                    } catch (ClassNotFoundException e) {
                        throw new ItemStreamException("Cannot reload persisted statistics from execution context",e);
                    }
                }
            }
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        // persist statistics
        for (Map.Entry<Class,Integer> entry : this.synchronizerListener.getPersistedCounts().entrySet()){
            executionContext.put(PERSIST_MAP_COUNT+"_"+entry.getKey().getCanonicalName(), entry.getValue());
        }
        for (Map.Entry<Class,Integer> entry : this.synchronizerListener.getMergedCounts().entrySet()){
            executionContext.put(MERGE_MAP_COUNT+"_"+entry.getKey().getCanonicalName(), entry.getValue());
        }
        for (Map.Entry<Class,Integer> entry : this.synchronizerListener.getTransientReplacedCounts().entrySet()){
            executionContext.put(REPLACED_TRANSIENT_MAP_COUNT+"_"+entry.getKey().getCanonicalName(), entry.getValue());
        }
        for (Map.Entry<Class,Integer> entry : this.synchronizerListener.getMergedTransientCounts().entrySet()){
            executionContext.put(MERGED_TRANSIENT_MAP_COUNT+"_"+entry.getKey().getCanonicalName(), entry.getValue());
        }
        for (Map.Entry<Class,Integer> entry : this.synchronizerListener.getDeletedCounts().entrySet()){
            executionContext.put(DELETED_MAP_COUNT+"_"+entry.getKey().getCanonicalName(), entry.getValue());
        }
    }

    @Override
    public void close() throws ItemStreamException {
        // remove listener
        this.synchronizerListener = null;
    }

    @Override
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void write(List<? extends I> is) throws Exception {
        if (this.interactionService == null){
            throw new IllegalStateException("The writer must have a non null interaction service");
        }
        if (this.synchronizerListener == null){
            throw new IllegalStateException("The writer cannot write before calling the method open as it is not initialised");
        }

        this.interactionService.saveOrUpdate(is);
    }

    public IntactService<I> getInteractionService() {
        return interactionService;
    }

    public void setInteractionService(IntactService<I> interactionService) {
        this.interactionService = interactionService;
    }
}
