package uk.ac.ebi.intact.export.complex.tab.writer;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ComplexFlatWriter implements ExportWriter {
    private static final String COL_SEPARATOR = "\t";
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final char HEADER_CHAR = '#';
    private final Writer writer;
    private boolean isContentWritten;
    private int headerCols;

    public ComplexFlatWriter(Writer writer) {
        this.writer = writer;
    }

    public void writeHeaderIfNecessary(String... colHeaderTexts) throws IOException {
        if (!isContentWritten) {
            getWriter().write(HEADER_CHAR);
            writeColumnValues(colHeaderTexts);
            headerCols = colHeaderTexts.length;
        }

    }

    public void writeColumnValues(String... colValues) throws IOException {
        if (headerCols > 0 && colValues.length != headerCols) {
            throw new IllegalArgumentException("Unexpected number of values, as the header contains " + headerCols + " columns and the values provided were: " + colValues.length);
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < colValues.length; i++) {
            if (i > 0) {
                sb.append(COL_SEPARATOR);
            }
            sb.append(colValues[i]);
        }

        sb.append(NEW_LINE);

        getWriter().write(sb.toString());

        isContentWritten = true;
    }

    public Writer getWriter() {
        return writer;
    }

    public void flush() throws IOException {
        getWriter().flush();
    }

    public void close() throws IOException {
        getWriter().close();
    }
}