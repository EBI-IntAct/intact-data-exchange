package uk.ac.ebi.intact.dataexchange.cvutils;

import org.apache.geronimo.mail.util.StringBufferOutputStream;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfo;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;

import java.io.OutputStream;

/**
 * AnnotationInfoDatasetExporter Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public class AnnotationInfoDatasetExporterTest {

    private AnnotationInfoDataset buildDataset() {
        AnnotationInfoDataset dataset = new AnnotationInfoDataset();
        dataset.addCvAnnotation( new AnnotationInfo( "label1", "", "type1", "1", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label4", "", "type2", "2", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label3", "", "type1", "3", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label2", "", "type1", "4", "", "", false ) );
        dataset.addCvAnnotation( new AnnotationInfo( "label2", "", "type2", "5", "", "", false ) );
        return dataset;
    }

    @Test
    public void exportCSV_with_header() throws Exception {

        AnnotationInfoDataset dataset = buildDataset();

        AnnotationInfoDatasetExporter exporter = new AnnotationInfoDatasetExporter();
        StringBuffer sb = new StringBuffer();
        OutputStream os = new StringBufferOutputStream( sb );
        exporter.exportCSV( dataset, os, true );
        Assert.assertEquals( dataset.getAll().size() + 1, sb.toString().split( "\n" ).length );
    }

    @Test
    public void exportCSV_without_header() throws Exception {

        AnnotationInfoDataset dataset = buildDataset();

        AnnotationInfoDatasetExporter exporter = new AnnotationInfoDatasetExporter();
        StringBuffer sb = new StringBuffer();
        OutputStream os = new StringBufferOutputStream( sb );
        exporter.exportCSV( dataset, os, false );
        Assert.assertEquals( dataset.getAll().size(), sb.toString().split( "\n" ).length );
    }
}
