package uk.ac.ebi.intact.psimitab.converters.converters;

import psidev.psi.mi.jami.model.Range;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.tab.model.FeatureImpl;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;
import uk.ac.ebi.intact.psimitab.converters.util.PsimitabTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts an Intact feature in MITAB
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>07/08/12</pre>
 */

public class FeatureConverter {

    public psidev.psi.mi.tab.model.Feature intactToMitab(IntactFeatureEvidence feature){
        if (feature != null){
            String name = null;
            String text = null;

            if (feature.getType() != null && feature.getType().getFullName() != null){
                name = feature.getType().getFullName();
            }
            else if (feature.getType() != null && feature.getType().getShortName() != null){
                name = feature.getType().getShortName();
            }
            else {
                name = CrossReferenceConverter.DATABASE_UNKNOWN;
            }

            List<String> rangesMitab = new ArrayList<String>(feature.getRanges().size());

            for (Range range : feature.getRanges()){
                String rangeAsString = PsimitabTools.convertRangeIntoString(range);
                if (rangeAsString != null){
                    rangesMitab.add(rangeAsString);
                }
            }

            // if no ranges are specified, use undetermined range
            if (rangesMitab.isEmpty()){
                rangesMitab.add("?-?");
            }

            psidev.psi.mi.tab.model.Feature mitabFeature = new FeatureImpl(name, rangesMitab);

            for (Xref refs : feature.getXrefs()){

                if (refs.getQualifier() != null && Xref.IDENTITY_MI.equalsIgnoreCase(refs.getQualifier().getMIIdentifier())){
                    if (refs.getId() != null){
                        mitabFeature.setText(refs.getId());
                    }
                }
            }
            return mitabFeature;
        }

        return null;
    }
}
