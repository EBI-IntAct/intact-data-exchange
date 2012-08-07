package uk.ac.ebi.intact.psimitab.converters;

import psidev.psi.mi.tab.model.ConfidenceImpl;
import uk.ac.ebi.intact.model.Confidence;

/**
 * This class allows to convert a Intact interaction confidence to a MITAB confidence
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/07/12</pre>
 */

public class ConfidenceConverter {

    public final static String CONFIDENCE = "confidence";
    public final static String CONFIDENCE_MI = "MI:1064";
    public final static String UNKNOWN = "unknown";

    public psidev.psi.mi.tab.model.Confidence intactToCalimocho(Confidence conf){
        if (conf != null && conf.getValue() != null){
            psidev.psi.mi.tab.model.Confidence confMitab = new ConfidenceImpl();

            String db = UNKNOWN;
            if (conf.getCvConfidenceType() != null && conf.getCvConfidenceType().getShortLabel() != null){
                db= conf.getCvConfidenceType().getShortLabel();
            }

            confMitab.setType(db);
            confMitab.setValue(conf.getValue());

            return confMitab;
        }

        return null;
    }
}
