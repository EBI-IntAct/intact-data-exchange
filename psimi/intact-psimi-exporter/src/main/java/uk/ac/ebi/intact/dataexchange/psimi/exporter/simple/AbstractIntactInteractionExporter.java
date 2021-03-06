package uk.ac.ebi.intact.dataexchange.psimi.exporter.simple;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.datasource.InteractionWriter;
import psidev.psi.mi.jami.exception.MIIOException;
import psidev.psi.mi.jami.factory.InteractionWriterFactory;
import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.Interaction;
import uk.ac.ebi.intact.jami.dao.IntactDao;

import javax.annotation.Resource;
import java.util.List;

/**
 * abstract IntAct interaction exporter which can write in a single file and cane be restarted from where it stopped
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public abstract class AbstractIntactInteractionExporter<T extends Interaction> extends AbstractIntactDbExporter<T>{


    private InteractionWriter interactionWriter;

    @Resource(name ="intactDao")
    private IntactDao intactDao;

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        this.interactionWriter.flush();

        super.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        if (interactionWriter != null) {
            try {
                interactionWriter.end();
                interactionWriter.close();
            } catch (MIIOException e) {
                throw new ItemStreamException( "Impossible to close output file", e );
            }
            finally {
                super.close();
            }
        }

        this.interactionWriter = null;
    }

    @Override
    protected void initialiseObjectWriter(boolean restarted) {
        // initialise writers
        registerWriters();
        // add mandatory options
        getWriterOptions().put(InteractionWriterOptions.OUTPUT_OPTION_KEY, getOutputBufferedWriter());

        InteractionWriterFactory writerFactory = InteractionWriterFactory.getInstance();
        this.interactionWriter = writerFactory.getInteractionWriterWith(getWriterOptions());

        if (this.interactionWriter == null){
            throw new IllegalStateException("We cannot find a valid interaction writer with the given options.");
        }
        if(!restarted){
            this.interactionWriter.start();
        }
    }

    protected abstract void registerWriters();

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void write(List<? extends T> items) throws Exception {
        if (this.interactionWriter == null){
            throw new IllegalStateException("The writer needs to be initialised before writing");
        }

        for (T i : items){
            if (!intactDao.getEntityManager().contains(i)){
                i = intactDao.getEntityManager().merge(i);
            }
            this.interactionWriter.write(i);
        }
    }

    protected InteractionWriter getInteractionWriter() {
        return interactionWriter;
    }

    protected IntactDao getIntactDao() {
        return intactDao;
    }
}
