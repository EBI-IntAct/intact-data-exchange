package uk.ac.ebi.intact.dataexchange.dbimporter.writer;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.util.Assert;
import uk.ac.ebi.intact.jami.synchronizer.listener.impl.DbSynchronizerStatisticsReporter;

import java.util.Map;
import java.util.logging.Logger;

/**
 * abstract IntAct importer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public abstract class AbstractIntactDbImporter<T> implements ItemWriter<T>,ItemStream{

    private DbSynchronizerStatisticsReporter synchronizerListener;

    private Logger log = Logger.getLogger(AbstractIntactDbImporter.class.getName());

    private final static String PERSIST_MAP_COUNT = "persisted.map";
    private final static String MERGE_MAP_COUNT = "merged.map";
    private final static String DELETED_MAP_COUNT = "deleted.map";
    private final static String MERGED_TRANSIENT_MAP_COUNT = "merged.transient.map";
    private final static String REPLACED_TRANSIENT_MAP_COUNT = "transient.replaced.map";

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

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
        // flush statistics
        log.info("Created objects in database: ");
        for (Map.Entry<Class, Integer> entry : synchronizerListener.getPersistedCounts().entrySet()){
            log.info(entry.getKey().getSimpleName()+": "+entry.getValue());
        }
        log.info("Objects already in the database: ");
        for (Map.Entry<Class, Integer> entry : synchronizerListener.getMergedTransientCounts().entrySet()){
            log.info(entry.getKey().getSimpleName()+": "+entry.getValue());
        }
        log.info("Objects replaced with existing instance in the database: ");
        for (Map.Entry<Class, Integer> entry : synchronizerListener.getTransientReplacedCounts().entrySet()){
            log.info(entry.getKey().getSimpleName()+": "+entry.getValue());
        }
        log.info("Existing Objects merged: ");
        for (Map.Entry<Class, Integer> entry : synchronizerListener.getMergedCounts().entrySet()){
            log.info(entry.getKey().getSimpleName()+": "+entry.getValue());
        }
        // remove listener
        this.synchronizerListener = null;
    }

    protected DbSynchronizerStatisticsReporter getSynchronizerListener() {
        return synchronizerListener;
    }
}
