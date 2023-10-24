package uk.ac.ebi.intact.dataexchange.dbimporter.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.*;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.commons.MIDataSourceOptionFactory;
import psidev.psi.mi.jami.commons.PsiJami;
import psidev.psi.mi.jami.datasource.InteractionStream;
import psidev.psi.mi.jami.exception.MIIOException;
import psidev.psi.mi.jami.factory.MIDataSourceFactory;
import psidev.psi.mi.jami.factory.options.MIFileDataSourceOptions;
import psidev.psi.mi.jami.model.Interaction;
import uk.ac.ebi.intact.dataexchange.dbimporter.listener.IntactStrictParserListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Abstract class for MI file readers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public abstract class AbstractMIFileReader<I extends Interaction> implements MIFileReader<I> {

    private InteractionStream<I> interactionDataSource;
    private int interactionCount = 0;
    private static final String COUNT_OPTION = "interaction_count";
    private Resource resource;
    private static final Log logger = LogFactory.getLog(AbstractMIFileReader.class);
    private Iterator<I> interactionIterator;

    public I read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        if (this.interactionIterator == null){
            throw new IllegalStateException("The reader must be opened before reading interactions.");
        }

        I nextObject = interactionIterator.hasNext() ? interactionIterator.next() : null;
        this.interactionCount++;
        return nextObject;
    }

    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        PsiJami.initialiseAllMIDataSources();

        if (resource == null){
            throw new IllegalStateException("Input resource must be provided. ");
        }

        if (!resource.exists()) {
            logger.warn("Input resource does not exist " + resource.getDescription());
            throw new IllegalStateException("Input resource must exist: " + resource);
        }

        if (!resource.isReadable()) {
            logger.warn("Input resource is not readable " + resource.getDescription());
            throw new IllegalStateException("Input resource must be readable: "
                    + resource);
        }

        initialiseInputDataStream();

        if (this.interactionDataSource == null){
            throw new ItemStreamException("The resource " + resource.getDescription() + " is not recognized as a valid MI file datasource.");
        }

        try{
            this.interactionIterator = this.interactionDataSource.getInteractionsIterator();

            // the job has been restarted, we update iterator
            if (executionContext.containsKey(COUNT_OPTION)){
                this.interactionCount = executionContext.getInt(COUNT_OPTION);

                int count = 0;
                while (count < this.interactionCount && this.interactionIterator.hasNext()){
                    this.interactionIterator.next();
                    count++;
                }
            }
        }
        catch (MIIOException e) {
            logger.error("Problem reading the input source: " + resource.getDescription(), e);
            throw new ItemStreamException("Problem reading the input source: " + resource.getDescription(), e);
        }
    }

    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");
        executionContext.put(COUNT_OPTION, interactionCount);
    }

    public void close() throws ItemStreamException {
        if (this.interactionDataSource != null){
            this.interactionDataSource.close();
        }
        this.interactionCount = 0;
        this.interactionDataSource = null;
        this.interactionIterator = null;
    }

    public void setResource(Resource source) {
        this.resource = source;
    }

    protected void initialiseInputDataStream() {
        InputStream inputStreamToAnalyse = null;
        try {
            inputStreamToAnalyse = resource.getInputStream();

            MIDataSourceFactory dataSourceFactory = MIDataSourceFactory.getInstance();
            MIDataSourceOptionFactory optionFactory = MIDataSourceOptionFactory.getInstance();

            this.interactionDataSource = dataSourceFactory.getInteractionSourceWith(createDatasourceOptions(inputStreamToAnalyse, optionFactory));
        } catch (IOException e) {
            logger.warn("Input resource cannot be opened " + resource.getDescription());
            throw new ItemStreamException("Input resource must be readable: "
                    + resource, e);
        }
    }

    protected Map<String, Object> createDatasourceOptions(InputStream inputStreamToAnalyse, MIDataSourceOptionFactory optionFactory) throws IOException {
        Map<String, Object> options = optionFactory.getDefaultOptions(inputStreamToAnalyse);
        options.put(MIFileDataSourceOptions.PARSER_LISTENER_OPTION_KEY, new IntactStrictParserListener());
        return options;
    }

    protected void setInteractionDataSource(InteractionStream interactionDataSource) {
        this.interactionDataSource = interactionDataSource;
    }

    public Resource getResource() {
        return resource;
    }
}
