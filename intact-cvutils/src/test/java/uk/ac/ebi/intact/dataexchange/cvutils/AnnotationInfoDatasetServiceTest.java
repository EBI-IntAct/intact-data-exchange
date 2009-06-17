package uk.ac.ebi.intact.dataexchange.cvutils;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.model.CvFeatureType;
import uk.ac.ebi.intact.model.CvInteraction;
import uk.ac.ebi.intact.model.CvTopic;

import java.util.Arrays;

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

        final CvTopic comment = getMockBuilder().createCvObject( CvTopic.class, "MI:0001", "comment" );
        final CvTopic remark = getMockBuilder().createCvObject( CvTopic.class, "MI:0002", "remark" );

        final CvFeatureType ft1 = getMockBuilder().createCvObject( CvFeatureType.class, "MI:0003", "ft1" );
        ft1.addAnnotation( getMockBuilder().createAnnotation( "bla", comment ) );
        final CvFeatureType ft2 = getMockBuilder().createCvObject( CvFeatureType.class, "MI:0004", "ft2" );
        final CvFeatureType ft3 = getMockBuilder().createCvObject( CvFeatureType.class, "MI:0005", "ft3" );

        final CvInteraction i1 = getMockBuilder().createCvObject( CvInteraction.class, "MI:0006", "int1" );
        i1.addAnnotation( getMockBuilder().createAnnotation( "bla", comment ) );

        PersisterHelper.saveOrUpdate( comment, remark, ft1, ft2, ft3, i1 );

        // Now retreive them
        AnnotationInfoDatasetService service = new AnnotationInfoDatasetService();
        final AnnotationInfoDataset dataset = service.retrieveAnnotationInfoDataset( Arrays.asList( comment ) );

        Assert.assertNotNull( dataset );
        Assert.assertEquals( 2, dataset.getAllAnnotationInfoSortedByTypeAndLabel().size() );

    }
}
