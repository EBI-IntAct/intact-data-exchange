package uk.ac.ebi.intact.task.mitab;

import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.visitor.BaseIntactVisitor;

import java.util.Collection;
import java.util.Iterator;

/**
 * Removes hidden annotations from all linked annotated objects.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.2
 */
public class HiddenAnnotationCleanerVisitor extends BaseIntactVisitor {

    @Override
    public void visitAnnotatedObject( AnnotatedObject annotatedObject ) {
        // CvTopic is also an AnnotatedObject
        removeHiddenAnnotations( annotatedObject.getAnnotations() );
    }

    private void removeHiddenAnnotations( Collection<Annotation> annotations ) {
        final Iterator<Annotation> i = annotations.iterator();
        while ( i.hasNext() ) {
            Annotation annotation = i.next();
            if( isHidden( annotation.getCvTopic() ) ) {
                i.remove();
            }
        }
    }

    private boolean isHidden( CvTopic cvTopic ) {
        for ( Annotation annotation : cvTopic.getAnnotations() ) {
            if( annotation.getCvTopic().getShortLabel().equals( "hidden" ) ) {
                return true;
            }
        }
        return false;
    }
}