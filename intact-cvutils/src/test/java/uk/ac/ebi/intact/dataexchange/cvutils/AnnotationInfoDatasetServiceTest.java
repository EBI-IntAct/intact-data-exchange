package uk.ac.ebi.intact.dataexchange.cvutils;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfo;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.model.CvFeatureType;
import uk.ac.ebi.intact.model.CvInteraction;
import uk.ac.ebi.intact.model.CvTopic;

import java.util.Arrays;
import java.util.Collection;

/**
 * AnnotationInfoDatasetService Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @since 2.0.1
 * @version $Id$
 */
public class AnnotationInfoDatasetServiceTest extends IntactBasicTestCase {

    @Test
    public void retrieveAnnotationInfoDataset() throws Exception {

        final CvTopic comment = getMockBuilder().createCvObject( CvTopic.class, "MI:9999", "comment" );
        final CvTopic remark = getMockBuilder().createCvObject( CvTopic.class, "MI:8888", "remark" );

        final CvFeatureType ft1 = getMockBuilder().createCvObject( CvFeatureType.class, "MI:7777", "ft1" );
        ft1.addAnnotation( getMockBuilder().createAnnotation( "bla", comment ) );
        final CvFeatureType ft2 = getMockBuilder().createCvObject( CvFeatureType.class, "MI:6666", "ft2" );
        final CvFeatureType ft3 = getMockBuilder().createCvObject( CvFeatureType.class, "MI:5555", "ft3" );

        final CvInteraction i1 = getMockBuilder().createCvObject( CvInteraction.class, "MI:4444", "int1" );
        i1.addAnnotation( getMockBuilder().createAnnotation( "bla", comment ) );

        getIntactContext().getCorePersister().saveOrUpdate( comment, remark, ft1, ft2, ft3, i1 );

        // Now retrieve them
        AnnotationInfoDatasetService service = new AnnotationInfoDatasetService();
        final AnnotationInfoDataset dataset = service.retrieveAnnotationInfoDataset( Arrays.asList( comment ) );

        Assert.assertNotNull( dataset );

        assertHasDatasetGotCv( dataset.getAllAnnotationInfoSortedByTypeAndLabel(), "MI:7777" );
        assertHasDatasetGotCv( dataset.getAllAnnotationInfoSortedByTypeAndLabel(), "MI:4444" );

        Assert.assertEquals( 2, dataset.getAllAnnotationInfoSortedByTypeAndLabel().size() );
    }

    private void assertHasDatasetGotCv( Collection<AnnotationInfo> annotations, String mi ) {
        for ( AnnotationInfo annotation : annotations ) {
            if( annotation.getMi().equals( mi ) ) {
                return;
            }
        }
        Assert.fail( "Could not find CV term with identifier " + mi + " in the given dataset");
    }
}
