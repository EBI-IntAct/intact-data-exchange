package uk.ac.ebi.intact.export.complex.tab.writer;

import uk.ac.ebi.intact.export.complex.tab.exception.ComplexExportException;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public interface ExportWriter extends Flushable, Closeable {
    void writeComplex(IntactComplex complex) throws ComplexExportException, IOException;

    void writeHeaderIfNecessary(String ... colHeaderTexts) throws IOException;

    void writeColumnValues(String ... colValues) throws IOException;

}