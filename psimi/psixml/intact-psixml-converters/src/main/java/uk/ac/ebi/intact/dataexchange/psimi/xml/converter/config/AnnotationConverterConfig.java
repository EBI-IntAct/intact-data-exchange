package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.intact.model.CvTopic;

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

    public AnnotationConverterConfig() {
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
        return excludeAnnotationTopic.contains( topic );
    }

    public boolean hasExcludedCvTopic() {
        return !excludeAnnotationTopic.isEmpty();
    }
}
