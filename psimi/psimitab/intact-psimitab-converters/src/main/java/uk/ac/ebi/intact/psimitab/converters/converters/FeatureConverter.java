package uk.ac.ebi.intact.psimitab.converters.converters;

import psidev.psi.mi.tab.model.FeatureImpl;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.FeatureXref;
import uk.ac.ebi.intact.model.Range;
import uk.ac.ebi.intact.model.util.FeatureUtils;

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

    public psidev.psi.mi.tab.model.Feature intactToMitab(Feature feature){
        if (feature != null){
            String name = null;
            String text = null;

            if (feature.getCvFeatureType() != null && feature.getCvFeatureType().getFullName() != null){
                name = feature.getCvFeatureType().getFullName();
            }
            else if (feature.getCvFeatureType() != null && feature.getCvFeatureType().getShortLabel() != null){
                name = feature.getCvFeatureType().getShortLabel();
            }
            else {
                name = CrossReferenceConverter.DATABASE_UNKNOWN;
            }

            List<String> rangesMitab = new ArrayList<String>(feature.getRanges().size());

            for (Range range : feature.getRanges()){
                String rangeAsString = FeatureUtils.convertRangeIntoString(range);
                if (rangeAsString != null){
                    rangesMitab.add(rangeAsString);
                }
            }

            psidev.psi.mi.tab.model.Feature mitabFeature = new FeatureImpl(name, rangesMitab);

            for (FeatureXref refs : feature.getXrefs()){

                if (refs.getCvXrefQualifier() != null && CvXrefQualifier.IDENTITY_MI_REF.equalsIgnoreCase(refs.getCvXrefQualifier().getIdentifier())){
                    if (refs.getPrimaryId() != null){
                        mitabFeature.setText(refs.getPrimaryId());
                    }
                }
            }
            return mitabFeature;
        }

        return null;
    }
}
