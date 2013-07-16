package uk.ac.ebi.intact.dataexchange;

import uk.ac.ebi.intact.model.Xref;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Text writer of the structured abstract
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16/07/13</pre>
 */

public class StructuredAbstractTextWriter extends AbstractStructuredAbstractWriter{
    public StructuredAbstractTextWriter(Writer writer) {
        super(writer);
    }

    public StructuredAbstractTextWriter(OutputStream stream) {
        super(stream);
    }

    public StructuredAbstractTextWriter(File file) throws IOException {
        super(file);
    }

    @Override
    protected void buildXrefOutput(Xref xref, String proteinName) {
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
}
