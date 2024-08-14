package uk.ac.ebi.intact.export.complex.tab.writer;

import uk.ac.ebi.intact.export.complex.tab.exception.ComplexExportException;
import uk.ac.ebi.intact.export.complex.tab.helper.RowFactory;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ComplexFlatWriter implements ExportWriter {

    private static final String[] HEADER_COLUMNS = new String[]{
            "Complex ac", "Recommended name", "Aliases for complex",
            "Taxonomy identifier", "Identifiers (and stoichiometry) of molecules in complex", "Evidence Code",
            "Experimental evidence", "Go Annotations", "Cross references", "Description", "Complex properties",
            "Complex assembly", "Ligand", "Disease", "Agonist", "Antagonist", "Comment", "Source", "Expanded participant list"
    };
    private static final String COL_SEPARATOR = "\t";
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final char HEADER_CHAR = '#';

    private final Writer writer;
    private boolean isContentWritten;
    private int headerCols;

    public ComplexFlatWriter(Writer writer) {
        this.writer = writer;
    }

    public void writeComplex(IntactComplex complex) throws ComplexExportException, IOException {
        String[] field = RowFactory.convertComplexToExportLine(complex);
        writeHeaderIfNecessary(HEADER_COLUMNS);
        writeColumnValues(
                field[0], field[1], field[2], field[3], field[4], field[5], field[6], field[7], field[8], field[9],
                field[10], field[11], field[12], field[13], field[14], field[15], field[16], field[17], field[18]);
        flush();
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