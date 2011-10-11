package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config;

import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.InteractionConverter;
import uk.ac.ebi.intact.model.CvTopic;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the configuration of the converter.
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 2.0.0
 */
public class AnnotationConverterConfig {

    /**
     * Only export Interactor Xref that have a CvDatabase that is in the list.
     */
    private Set<CvTopic> excludeAnnotationTopic = new HashSet<CvTopic>( );

    private boolean excludeHiddenTopics = false;

    public AnnotationConverterConfig() {

        // always exclude negative, Intra-molecular and modelled
        addExcludeAnnotationTopic(new CvTopic(InteractionConverter.NEGATIVE));
        addExcludeAnnotationTopic(new CvTopic(InteractionConverter.INTRA_MOLECULAR));
        addExcludeAnnotationTopic(new CvTopic(InteractionConverter.MODELLED));
    }

    ///////////////////////////////
    // excludePolymerSequence

    public void addExcludeAnnotationTopic( CvTopic topic ) {
        if ( topic == null ) {
            throw new NullPointerException( "You must give a non null topic" );
        }

        excludeAnnotationTopic.add
                ( topic );
    }

    public void removeExcludeAnnotationTopic( CvTopic topic  ) {
        if ( topic == null ) {
            throw new NullPointerException( "You must give a non null topic" );
        }

        excludeAnnotationTopic.remove( topic );
    }

    public boolean isExcluded( CvTopic topic  ) {

        if (topic != null && !topic.getAnnotations().isEmpty() && excludeHiddenTopics){

            for (uk.ac.ebi.intact.model.Annotation ann : topic.getAnnotations()){
                if (ann.getCvTopic() != null && ann.getCvTopic().getShortLabel().equalsIgnoreCase(CvTopic.HIDDEN)){
                    return true;
                }
            }
        }
        return excludeAnnotationTopic.contains( topic );
    }

    public boolean hasExcludedCvTopic() {
        return !excludeAnnotationTopic.isEmpty();
    }

    public boolean isExcludeHiddenTopics() {
        return excludeHiddenTopics;
    }

    public void setExcludeHiddenTopics(boolean excludeHiddenTopics) {
        this.excludeHiddenTopics = excludeHiddenTopics;
    }
}
