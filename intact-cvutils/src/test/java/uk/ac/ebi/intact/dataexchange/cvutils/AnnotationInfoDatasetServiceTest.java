package uk.ac.ebi.intact.dataexchange.cvutils;

import static org.junit.Assert.*;
import org.junit.*;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.util.SchemaUtils;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.CvFeatureType;
import uk.ac.ebi.intact.model.CvInteraction;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.context.DataContext;
import uk.ac.ebi.intact.context.IntactContext;

import java.util.Arrays;

/**
 * AnnotationInfoDatasetService Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @since 2.0.1
 * @version $Id$
 */
public class AnnotationInfoDatasetServiceTest extends IntactBasicTestCase {

    @Before
    public void initialize() throws IntactTransactionException {
        SchemaUtils.createSchema();
    }

    @Test
    public void retrieveAnnotationInfoDataset_no_transaction() throws Exception {

        // here we do not manage transaction, the service should do it itself.

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
        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        Assert.assertFalse( dataContext.isTransactionActive() );

        AnnotationInfoDatasetService service = new AnnotationInfoDatasetService();
        final AnnotationInfoDataset dataset = service.retrieveAnnotationInfoDataset( Arrays.asList( comment ) );

        Assert.assertNotNull( dataset );
        Assert.assertEquals( 2, dataset.getAllAnnotationInfoSortedByTypeAndLabel().size() );

        Assert.assertFalse( dataContext.isTransactionActive() );
    }

    @Test
    public void retrieveAnnotationInfoDataset_local_transaction() throws Exception {

        // here we are localy managing transactions

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
        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        dataContext.beginTransaction();
        Assert.assertTrue( dataContext.isTransactionActive() );

        AnnotationInfoDatasetService service = new AnnotationInfoDatasetService();
        final AnnotationInfoDataset dataset = service.retrieveAnnotationInfoDataset( Arrays.asList( comment ) );

        Assert.assertNotNull( dataset );
        Assert.assertEquals( 2, dataset.getAllAnnotationInfoSortedByTypeAndLabel().size() );

        Assert.assertTrue( dataContext.isTransactionActive() );
        commitTransaction();
        Assert.assertFalse( dataContext.isTransactionActive() );
    }
}
