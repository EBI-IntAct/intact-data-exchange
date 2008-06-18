package uk.ac.ebi.intact.dataexchange.cvutils.model;

import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * AnnotationInfoDataset Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class AnnotationInfoDatasetTest {

    @Test
    public void getCvAnnotation() throws Exception {
        fail( "Not yet implemented." );
    }

    @Test
    public void getAllAnnotationInfoSorted() throws Exception {

        AnnotationInfoDataset dataset = new AnnotationInfoDataset();
        dataset.addCvAnnotation( new AnnotationInfo( "label1", "", "type1", "1", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label4", "", "type2", "2", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label3", "", "type1", "3", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label2", "", "type1", "4", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label2", "", "type2", "5", "", "", false ) );

        final Collection<AnnotationInfo> infos = dataset.getAllAnnotationInfoSorted( new Comparator<AnnotationInfo>() {
            public int compare( AnnotationInfo o1, AnnotationInfo o2 ) {
                return o2.getMi().compareTo( o1.getMi() ); // reverse MI order.
            }
        } );
        Assert.assertEquals( 5, infos.size() );

        final Iterator<AnnotationInfo> i = infos.iterator();
        AnnotationInfo info;

        info = i.next();
        Assert.assertEquals( "5", info.getMi() );

        info = i.next();
        Assert.assertEquals( "4", info.getMi() );

        info = i.next();
        Assert.assertEquals( "3", info.getMi() );

        info = i.next();
        Assert.assertEquals( "2", info.getMi() );

        info = i.next();
        Assert.assertEquals( "1", info.getMi() );

    }

    @Test
    public void getAllAnnotationInfoSortedByTypeAndLabel() throws Exception {
        AnnotationInfoDataset dataset = new AnnotationInfoDataset();
        dataset.addCvAnnotation( new AnnotationInfo( "label1", "", "type1", "1", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label4", "", "type2", "2", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label3", "", "type1", "3", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label2", "", "type1", "4", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label2", "", "type2", "5", "", "", false ) );

        final Collection<AnnotationInfo> infos = dataset.getAllAnnotationInfoSortedByTypeAndLabel();
        Assert.assertEquals( 5, infos.size() );

        final Iterator<AnnotationInfo> i = infos.iterator();
        AnnotationInfo info;

        info = i.next();
        Assert.assertEquals( "label1", info.getShortLabel() );
        Assert.assertEquals( "type1", info.getType() );

        info = i.next();
        Assert.assertEquals( "label2", info.getShortLabel() );
        Assert.assertEquals( "type1", info.getType() );

        info = i.next();
        Assert.assertEquals( "label3", info.getShortLabel() );
        Assert.assertEquals( "type1", info.getType() );

        info = i.next();
        Assert.assertEquals( "label2", info.getShortLabel() );
        Assert.assertEquals( "type2", info.getType() );

        info = i.next();
        Assert.assertEquals( "label4", info.getShortLabel() );
        Assert.assertEquals( "type2", info.getType() );
    }
}
