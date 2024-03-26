package uk.ac.ebi.intact.psimitab.converters.converters;

import psidev.psi.mi.tab.model.Annotation;
import psidev.psi.mi.tab.model.AnnotationImpl;

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
        topicsToExclude.add("negative");
    }

    public Annotation intactToMitab(psidev.psi.mi.jami.model.Annotation intactAnnotation){

        if (intactAnnotation != null && intactAnnotation.getTopic() != null){

            String topic = psidev.psi.mi.jami.model.Annotation.COMMENT;

            if (intactAnnotation.getTopic().getShortName() != null){
                topic = intactAnnotation.getTopic().getShortName();
            }

            if (!topicsToExclude.contains(topic)){
                Annotation annot = new AnnotationImpl(topic);
                annot.setText(intactAnnotation.getValue());

                return annot;
            }
        }

        return null;
    }
}
