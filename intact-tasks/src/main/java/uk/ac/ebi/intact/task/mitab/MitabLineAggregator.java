package uk.ac.ebi.intact.task.mitab;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.file.transform.LineAggregator;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.MitabWriterUtils;
import psidev.psi.mi.tab.model.builder.PsimiTab;

/**
 * This is an aggregator of a Binary interaction
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/08/12</pre>
 */

public class MitabLineAggregator implements LineAggregator<BinaryInteraction> {

    private static final Log log = LogFactory.getLog(MitabLineAggregator.class);
    private boolean started = false;
    private int version = PsimiTab.VERSION_2_7;


    public MitabLineAggregator(int version, boolean started){
        super();
        if (version == PsimiTab.VERSION_2_5 || version == PsimiTab.VERSION_2_6 || version == PsimiTab.VERSION_2_7){
            this.version=version;
        }

        this.started = started;
    }

    @Override
    public String aggregate(BinaryInteraction item) {

        StringBuffer stringBuffer = new StringBuffer();
        String line = null;
        try {

            if (!started){
                started = true;
                stringBuffer.append(MitabWriterUtils.buildHeader(this.version));
            }
            stringBuffer.append(MitabWriterUtils.buildLine(item, this.version).replaceAll("\n",""));
            line = stringBuffer.toString();

        } catch (IllegalArgumentException e) {
            log.error("Impossible to write binary interaction.", e);
        }

        return line;
    }
}
