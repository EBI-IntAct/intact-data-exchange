package uk.ac.ebi.intact.dataexchange.psimi.exporter.simple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.util.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

/**
 * abstract IntAct exporter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public abstract class AbstractIntactDbExporter<T> implements ItemWriter<T>,ItemStream{

    private Resource output;

    private long currentPosition=0;
    private Writer outputBufferedWriter;
    private FileChannel fileChannel;
    private FileOutputStream os;
    private Map<String, Object> writerOptions;

    private final static String CURRENT_POSITION = "current_position";

    private static final Log logger = LogFactory.getLog(AbstractIntactDbExporter.class);

    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        if (output == null){
            throw new IllegalStateException("Output resource must be provided. ");
        }

        if (this.writerOptions == null || this.writerOptions.isEmpty()){
            throw new IllegalStateException("Options to instantiate the writer must be provided");
        }

        try {
            File outputFile = output.getFile();

            if (!outputFile.getCanonicalFile().getParentFile().canWrite()) {
                logger.warn("Cannot write to the output file " + output.getDescription());
                throw new IllegalStateException("Needs to write in output file: " + output);
            }

            // we initialize the bufferWriter
            if (executionContext.containsKey(CURRENT_POSITION)) {
                this.currentPosition = executionContext.getLong(CURRENT_POSITION);
                initializeBufferedWriter(outputFile, true);
            }
            else {
                initializeBufferedWriter(outputFile, false);
            }
        } catch (IOException e) {
            throw new ItemStreamException("Cannot open the output file", e);
        }
    }

    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        try {
            currentPosition = position();
            executionContext.putLong(CURRENT_POSITION, currentPosition);

        } catch (IOException e) {
            throw new ItemStreamException( "Impossible to get the last position of the writer" );
        }
    }

    public void close() throws ItemStreamException {
        if (fileChannel != null) {
            try {
                fileChannel.close();
            } catch (IOException e) {
                throw new ItemStreamException( "Impossible to close " + output.getDescription(), e );
            }
        }

        this.currentPosition = 0;
        this.fileChannel = null;
        this.os = null;
    }

    /**
     * Creates the buffered writer for the output file channel based on
     * configuration information.
     * @throws java.io.IOException
     */
    protected void initializeBufferedWriter(File file, boolean restarted) throws IOException {

        FileUtils.setUpOutputFile(file, restarted, restarted, !restarted);

        os = new FileOutputStream(file.getAbsolutePath(), true);
        fileChannel = os.getChannel();

        Writer writer = Channels.newWriter(fileChannel, "UTF-8");
        outputBufferedWriter = new BufferedWriter(writer);

        Assert.state(outputBufferedWriter != null);

        // in case of restarting reset position to last committed point
        if (restarted) {
            checkFileSize();
            truncate();
        }

        outputBufferedWriter.flush();

        initialiseObjectWriter(restarted);
    }

    protected abstract void initialiseObjectWriter(boolean restarted);

    protected abstract void registerWriters();

    /**
     * Checks (on setState) to make sure that the current output file's size
     * is not smaller than the last saved commit point. If it is, then the
     * file has been damaged in some way and whole task must be started over
     * again from the beginning.
     * @throws java.io.IOException if there is an IO problem
     */
    private void checkFileSize() throws IOException {
        long size = -1;

        outputBufferedWriter.flush();
        size = fileChannel.size();

        if (size < currentPosition) {
            throw new ItemStreamException("Current file size is smaller than size at last commit");
        }
    }

    /**
     * Truncate the output at the last known good point.
     *
     * @throws java.io.IOException
     */
    public void truncate() throws IOException {
        fileChannel.truncate(currentPosition);
        fileChannel.position(currentPosition);
    }

    /**
     * Return the byte offset position of the cursor in the output file as a
     * long integer.
     */
    public long position() throws IOException {
        long pos = 0;

        if (fileChannel == null) {
            return 0;
        }

        outputBufferedWriter.flush();
        pos = fileChannel.position();

        return pos;

    }

    public abstract void write(List<? extends T> items) throws Exception;

    public void setWriterOptions(Map<String, Object> writerOptions) {
        this.writerOptions = writerOptions;
    }

    public void setOutput(Resource output) {
        this.output = output;
    }

    protected Map<String, Object> getWriterOptions() {
        return writerOptions;
    }

    protected Writer getOutputBufferedWriter() {
        return outputBufferedWriter;
    }

    protected FileChannel getFileChannel() {
        return fileChannel;
    }

    protected FileOutputStream getOs() {
        return os;
    }
}
