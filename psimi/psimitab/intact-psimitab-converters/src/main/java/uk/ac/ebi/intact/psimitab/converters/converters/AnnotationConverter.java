package uk.ac.ebi.intact.psimitab.converters.converters;

import psidev.psi.mi.tab.model.Annotation;
import psidev.psi.mi.tab.model.AnnotationImpl;
import uk.ac.ebi.intact.model.CvTopic;

import java.util.HashSet;
import java.util.Set;

/**
 * This class allows to convert a Intact annotation to a MITAB annotation
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/07/12</pre>
 */

public class AnnotationConverter {

    private Set<String> topicsToExclude;

    public AnnotationConverter(){
        topicsToExclude = new HashSet<String>();
    }

    private void initializeTopicsToExclude(){
        topicsToExclude.add(CvTopic.NEGATIVE);
    }

    public Annotation intactToMitab(uk.ac.ebi.intact.model.Annotation intactAnnotation){

        if (intactAnnotation != null && intactAnnotation.getCvTopic() != null){

            String topic = CvTopic.COMMENT;

            if (intactAnnotation.getCvTopic().getShortLabel() != null){
                topic = intactAnnotation.getCvTopic().getShortLabel();
            }

            if (!topicsToExclude.contains(topic)){
                Annotation annot = new AnnotationImpl(topic);
                annot.setText(intactAnnotation.getAnnotationText());

                return annot;
            }
        }

        return null;
    }
}
