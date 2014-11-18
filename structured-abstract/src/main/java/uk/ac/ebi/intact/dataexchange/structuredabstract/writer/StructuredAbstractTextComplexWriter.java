package uk.ac.ebi.intact.dataexchange.structuredabstract.writer;

import psidev.psi.mi.jami.model.Xref;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Text writer of the structured abstract  for biological complex
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16/07/13</pre>
 */

public class StructuredAbstractTextComplexWriter extends AbstractStructuredAbstractComplexWriter{
    public StructuredAbstractTextComplexWriter(Writer writer) {
        super(writer);
    }

    public StructuredAbstractTextComplexWriter(OutputStream stream) {
        super(stream);
    }

    public StructuredAbstractTextComplexWriter(File file) throws IOException {
        super(file);
    }

    @Override
    protected void buildXrefOutput(Xref xref, String proteinName, String ac) {
        getStringBuilder().append(" ");
        getStringBuilder().append(proteinName);
    }

    @Override
    protected void writeMIOutput(String MIcode, String verb) throws IOException {
        getWriter().write(" ");
        getWriter().write(verb);
    }

    @Override
    protected void writeInteractionAc(String mintAC, int num_int) throws IOException{
        getWriter().write(" ");
        getWriter().write(mintAC);
    }

    @Override
    protected void writeLineSeparator() throws IOException {
        getWriter().write("\n");
    }
}
