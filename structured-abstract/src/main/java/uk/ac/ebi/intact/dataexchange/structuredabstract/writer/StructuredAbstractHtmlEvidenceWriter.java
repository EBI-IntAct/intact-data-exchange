package uk.ac.ebi.intact.dataexchange.structuredabstract.writer;

import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.dataexchange.structuredabstract.utils.XrefLinkUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * HTML writer of Structured abstract for interaction evidence
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16/07/13</pre>
 */

public class StructuredAbstractHtmlEvidenceWriter extends AbstractStructuredAbstractEvidenceWriter{

    public StructuredAbstractHtmlEvidenceWriter() {
        super();
    }

    public StructuredAbstractHtmlEvidenceWriter(Writer writer) {
        super(writer);
    }

    public StructuredAbstractHtmlEvidenceWriter(OutputStream stream) {
        super(stream);
    }

    public StructuredAbstractHtmlEvidenceWriter(File file) throws IOException {
        super(file);
    }

    @Override
    protected void buildXrefOutput(Xref xref, String proteinName, String ac) {
        String linkValue = XrefLinkUtils.getPrimaryIdLink(ac, xref, getCvTermUrls());
        if (linkValue == null){
            getStringBuilder().append(" ");
            getStringBuilder().append(proteinName);
        }
        else {
            getStringBuilder().append("<a href=\"");
            getStringBuilder().append(linkValue);
            getStringBuilder().append(" \" style=\"text-decoration:none; \"><b>");
            getStringBuilder().append(proteinName);
            getStringBuilder().append("</b></a>");
        }
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

    @Override
    protected void writeLineSeparator() throws IOException {
        getWriter().write("<br/>");
    }
}
