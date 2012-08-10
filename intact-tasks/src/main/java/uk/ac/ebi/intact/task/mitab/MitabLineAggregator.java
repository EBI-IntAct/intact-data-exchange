package uk.ac.ebi.intact.task.mitab;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.file.transform.LineAggregator;
import psidev.psi.mi.tab.io.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.PsimiTab;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * This is an aggregator of a Binary interaction
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/08/12</pre>
 */

public class MitabLineAggregator implements LineAggregator<BinaryInteraction> {

    private static final Log log = LogFactory.getLog(MitabLineAggregator.class);
    private PsimiTabWriter mitabWriter;
    private boolean started = false;

    public MitabLineAggregator(int version, boolean started){
        super();
        if (version == PsimiTab.VERSION_2_5 || version == PsimiTab.VERSION_2_6 || version == PsimiTab.VERSION_2_7){
            this.mitabWriter = new PsimiTabWriter(version);
        }
        else {
            this.mitabWriter = new PsimiTabWriter(PsimiTab.VERSION_2_7);
        }

        this.started = started;
    }

    @Override
    public String aggregate(BinaryInteraction item) {

        if (mitabWriter == null){
            mitabWriter = new PsimiTabWriter();
        }
        StringWriter stringWriter = new StringWriter();
        String line = null;
        try {

            if (!started){
                started = true;
                writeHeader(stringWriter);
            }
            mitabWriter.write(item, stringWriter);
            line = stringWriter.toString();
        } catch (IOException e) {
            log.error("Impossible to write binary interaction.", e);
        }
        finally {
            try {
                stringWriter.close();
            } catch (IOException e) {
                log.error("Impossible to close writer for binary interaction.", e);
            }
        }

        return line;
    }

    public void writeHeader(Writer writer) {

        if (mitabWriter == null){
            mitabWriter = new PsimiTabWriter();
        }
        try {
            mitabWriter.writeMitabHeader(writer);
        } catch (IOException e) {
            log.error("Impossible to write header.", e);
        }
    }
}
