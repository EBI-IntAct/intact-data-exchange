package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.imexcentral.mock.MockImexCentralClient;
import uk.ac.ebi.intact.core.config.CvPrimer;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Publication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * ImexAssigner Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class ImexAssignerTest extends IntactBasicTestCase {

    @Before
    public void setup() throws Exception {
        CvPrimer cvPrimer = new ImexCvPrimer( getDaoFactory() );
        cvPrimer.createCVs();
    }

    @Test
    public void update() throws Exception {

        // make a publication that can be exported to IMEx
        final Publication publication = getMockBuilder().createPublication( "123456789" );
        final Experiment experiment = getMockBuilder().createExperimentRandom( 2 );
        experiment.setPublication( null );
        addImexJournalAnnotations( experiment );
        publication.addExperiment( experiment );

        getCorePersister().saveOrUpdate( publication );

        // setup assigner
        final MockImexCentralClient imexCentralClient = new MockImexCentralClient();
        imexCentralClient.addPublication( "123456789", null, "NEW", "SAM" );
        ImexAssigner assigner = new ImexAssigner( imexCentralClient );
        assigner.setDryRun( false );
        final ImexAssignerConfig config = new ImexAssignerConfig();
        config.setUpdateLogsDirectory( new File( "target/123456789" ) );
        assigner.setImexUpdateConfig( config );

        assigner.update();


        Assert.assertEquals( 2, countLines( "target/123456789/processed.csv" ) );
        Assert.assertEquals( 2, countLines( "target/123456789/processed-imex.csv" ) );
        Assert.assertEquals( 2, countLines( "target/123456789/publication-assigned.csv" ) );
        Assert.assertEquals( 3, countLines( "target/123456789/interaction-assigned.csv" ) );
    }

    private int countLines( String resource ) throws IOException {
        return IOUtils.readLines( new FileInputStream( resource ) ).size();
    }

    public void assertTsvLineContains( String line, int column, String expectedValue ) {
        final String[] columns = line.split( "\t" );
        Assert.assertEquals( expectedValue, columns[ column + 1 ] );
    }

    private void addImexJournalAnnotations( AnnotatedObject ao ) {
        ao.addAnnotation( getMockBuilder().createAnnotation( "Cell (0092-8674)",
                                                             CvTopic.JOURNAL_MI_REF, CvTopic.JOURNAL ) );
        ao.addAnnotation( getMockBuilder().createAnnotation( "2007",
                                                             CvTopic.PUBLICATION_YEAR_MI_REF, CvTopic.PUBLICATION_YEAR ) );
    }
}
