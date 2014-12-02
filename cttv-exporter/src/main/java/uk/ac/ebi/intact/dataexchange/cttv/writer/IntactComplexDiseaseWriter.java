package uk.ac.ebi.intact.dataexchange.cttv.writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import uk.ac.ebi.intact.dataexchange.cttv.converter.DefaultComplexCttvConverter;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maitesin on 17/11/2014.
 */
public class IntactComplexDiseaseWriter implements ItemWriter<IntactComplex>, ItemStream {

    @Autowired
    @Qualifier("intactDao")
    private IntactDao intactDao;

    private FileChannel fileChannel;
    private String filename;
    private ComplexDiseaseWriter writer;
    private long position;
    private final static String CURRENT_POSITION = "current_position";

    private static final Log log = LogFactory.getLog(IntactComplexDiseaseWriter.class);

    public IntactComplexDiseaseWriter() throws FileNotFoundException {
        this("default_output_file.json");
    }

    public IntactComplexDiseaseWriter(String filename) throws FileNotFoundException {
        this.filename = filename;
        Map<String, Object> options = new HashMap<String, Object>();
        options.put(ComplexDiseaseWriterOptions.OUTPUT_OPTION_KEY, filename);
        options.put(ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER, new DefaultComplexCttvConverter());
        this.writer = new ComplexDiseaseWriter(options);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");
        try {
            if (executionContext.containsKey(this.CURRENT_POSITION)) {
                //ReOpen
                    updatePosition();
            }
            else {
                //Open
                this.position = 0;
                this.fileChannel = new FileOutputStream(this.filename).getChannel();
                this.writer.start();
            }
        } catch (IOException e) {
            throw new ItemStreamException("Impossible to get the last position of the writer", e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        try {
            updatePosition();
            executionContext.putLong(CURRENT_POSITION, this.position);

        } catch (IOException e) {
            throw new ItemStreamException("Impossible to get the last position of the writer", e);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (this.fileChannel != null)
            try {
                this.writer.end();
                this.fileChannel.close();
                this.writer.flush();
                this.writer.close();
            } catch (IOException e) {
                throw new ItemStreamException("Impossible to close the file chanel", e);
            }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, value = "jamiTransactionManager")
    public void write(List<? extends IntactComplex> intactComplexes) throws Exception {
        for (IntactComplex complex : intactComplexes) {
            IntactComplex merged = intactDao.getEntityManager().merge(complex);
            this.writer.write(merged);
        }
    }


    private void updatePosition() throws IOException {
        if (fileChannel == null) {
            this.position = 0L;
        }
        else {
            this.writer.flush();
            this.position = fileChannel.position();
        }
    }

}
