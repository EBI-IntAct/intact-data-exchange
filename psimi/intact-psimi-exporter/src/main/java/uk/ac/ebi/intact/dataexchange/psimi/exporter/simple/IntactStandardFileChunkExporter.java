package uk.ac.ebi.intact.dataexchange.psimi.exporter.simple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.datasource.InteractionWriter;
import psidev.psi.mi.jami.exception.MIIOException;
import psidev.psi.mi.jami.factory.InteractionWriterFactory;
import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.Interaction;
import uk.ac.ebi.intact.dataexchange.psimi.mitab.IntactPsiMitab;
import uk.ac.ebi.intact.dataexchange.psimi.xml.IntactPsiXml;
import uk.ac.ebi.intact.jami.dao.IntactDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * IntAct standard interaction chunk exporter which can write collection of interactions in a single standard file
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public class IntactStandardFileChunkExporter<T extends Interaction> extends AbstractIntactDbExporter<Collection<T>>{


    private InteractionWriter interactionWriter;

    private static final Log logger = LogFactory.getLog(AbstractIntactInteractionExporter.class);

    @Autowired
    @Qualifier("intactDao")
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

    @Override
    protected void registerWriters() {
        // register default MI writers
        IntactPsiMitab.initialiseAllIntactMitabWriters();

        // override writers for Intact xml
        IntactPsiXml.initialiseAllIntactXmlWriters();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void write(List<? extends Collection<T>> items) throws Exception {
        if (this.interactionWriter == null){
            throw new IllegalStateException("The writer needs to be initialised before writing");
        }
        for (Collection<T> i : items){
            Collection<T> reloadedInteractions = new ArrayList<T>(i.size());

            for (T interaction : i){
                if (!intactDao.getEntityManager().contains(interaction)){
                    reloadedInteractions.add(intactDao.getEntityManager().merge(interaction));
                }
                else{
                    reloadedInteractions.add(interaction);
                }
            }
            this.interactionWriter.write(reloadedInteractions);
        }
    }

    protected InteractionWriter getInteractionWriter() {
        return interactionWriter;
    }

    protected IntactDao getIntactDao() {
        return intactDao;
    }
}

