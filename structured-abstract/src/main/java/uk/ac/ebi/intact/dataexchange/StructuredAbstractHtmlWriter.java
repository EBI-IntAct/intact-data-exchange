package uk.ac.ebi.intact.dataexchange;

import uk.ac.ebi.intact.model.Xref;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * HTML writer of Structured abstract
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16/07/13</pre>
 */

public class StructuredAbstractHtmlWriter extends AbstractStructuredAbstractWriter{


    public StructuredAbstractHtmlWriter(Writer writer) {
        super(writer);
    }

    public StructuredAbstractHtmlWriter(OutputStream stream) {
        super(stream);
    }

    public StructuredAbstractHtmlWriter(File file) throws IOException {
        super(file);
    }

    @Override
    protected void buildXrefOutput(Xref xref, String proteinName) {
        getStringBuilder().append("<a href=\"");
        getStringBuilder().append(XrefLinkUtils.getPrimaryIdLink(xref, getCvTermUrls()));
        getStringBuilder().append(" \" style=\"text-decoration:none; \"><b>");
        getStringBuilder().append(proteinName);
        getStringBuilder().append("</b></a>");
    }

    @Override
    protected void writeMIOutput(String MIcode, String verb) throws IOException {
        getWriter().write("<a href=\"http://www.ebi.ac.uk/ontology-lookup/?termId=");
        getWriter().write(MIcode);
        getWriter().write(" \" style=\"text-decoration:none; \">");
        getWriter().write(verb);
        getWriter().write("</a>");
    }

    @Override
    protected void writeInteractionAc(String mintAC, int num_int) throws IOException{
        getWriter().write("<a href=\"");
        getWriter().write(INTACT_LINK);
        getWriter().write(mintAC);
        getWriter().write(" \" >");
        if (num_int == 0) {
            getWriter().write("View interaction");

        } else {
            getWriter().write(Integer.toString(num_int));
        }
        getWriter().write("</a>");
    }
}
