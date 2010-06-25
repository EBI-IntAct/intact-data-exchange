package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.bridges.imexcentral.mock.MockImexCentralClient;
import uk.ac.ebi.intact.core.config.CvPrimer;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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

        // updates a publication that is registered in IMEx Central

        // make a publication that can be exported to IMEx
        final Publication publication = getMockBuilder().createPublication( "123456789" );
        final Experiment experiment = getMockBuilder().createExperimentRandom( 2 );
        experiment.setPublication( null );
        addImexJournalAnnotations( experiment );
        addAcceptedAnnotations( experiment );
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

        // check in the database
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        Assert.assertEquals( experiment.getShortLabel(), daoFactory.getExperimentDao().getByXref( "IM-1" ).getShortLabel() );
        Assert.assertEquals( "123456789", daoFactory.getPublicationDao().getByXref( "IM-1" ).getShortLabel() );
        Assert.assertEquals( 2, daoFactory.getInteractionDao().getByXrefLike( "IM-1-%" ).size() );

        // check log files
        Assert.assertEquals( 1, countLines( "target/123456789/processed.csv" ) );
        Assert.assertEquals( 1, countLines( "target/123456789/processed-imex.csv" ) );
        Assert.assertEquals( 1, countLines( "target/123456789/publication-assigned.csv" ) );
        Assert.assertEquals( 2, countLines( "target/123456789/interaction-assigned.csv" ) );
    }

    @Test
    public void update_2() throws Exception {

        // updates a publication that is NOT YET registered in IMEx Central

        // make a publication that can be exported to IMEx
        final Publication publication = getMockBuilder().createPublication( "123456789" );
        final Experiment experiment = getMockBuilder().createExperimentRandom( 4 );
        experiment.setPublication( null );
        addImexJournalAnnotations( experiment );
        addAcceptedAnnotations( experiment );
        publication.addExperiment( experiment );

        getCorePersister().saveOrUpdate( publication );

        // setup assigner
        final MockImexCentralClient imexCentralClient = new MockImexCentralClient();
//        imexCentralClient.addPublication( "123456789", null, "NEW", "SAM" );
        ImexAssigner assigner = new ImexAssigner( imexCentralClient );
        assigner.setDryRun( false );
        final ImexAssignerConfig config = new ImexAssignerConfig();
        config.setUpdateLogsDirectory( new File( "target/123456789_2" ) );
        assigner.setImexUpdateConfig( config );

        assigner.update();

        // check in the database
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        Assert.assertEquals( experiment.getShortLabel(), daoFactory.getExperimentDao().getByXref( "IM-1" ).getShortLabel() );
        Assert.assertEquals( "123456789", daoFactory.getPublicationDao().getByXref( "IM-1" ).getShortLabel() );
        Assert.assertEquals( 4, daoFactory.getInteractionDao().getByXrefLike( "IM-1-%" ).size() );

        // check log files
        Assert.assertEquals( 1, countLines( "target/123456789_2/processed.csv" ) );
        Assert.assertEquals( 1, countLines( "target/123456789_2/processed-imex.csv" ) );
        Assert.assertEquals( 1, countLines( "target/123456789_2/publication-assigned.csv" ) );
        Assert.assertEquals( 4, countLines( "target/123456789_2/interaction-assigned.csv" ) );
    }

    @Test
    public void update_3() throws Exception {

        // updates a publication that already had an IMEx ID

        // make a publication that can be exported to IMEx
        final Publication publication = getMockBuilder().createPublication( "123456789" );
        CvDatabase imex = getMockBuilder().createCvObject( CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX );
        CvXrefQualifier imexPrimary = getMockBuilder().createCvObject( CvXrefQualifier.class,
                CvXrefQualifier.IMEX_PRIMARY_MI_REF,
                CvXrefQualifier.IMEX_PRIMARY );
        publication.addXref( getMockBuilder().createXref( publication, "IM-1", imexPrimary, imex ) );
        final Experiment experiment = getMockBuilder().createExperimentRandom( 3 );
        experiment.setPublication( null );
        addImexJournalAnnotations( experiment );
        addAcceptedAnnotations( experiment );
        publication.addExperiment( experiment );

        getCorePersister().saveOrUpdate( publication );

        // setup assigner
        final MockImexCentralClient imexCentralClient = new MockImexCentralClient();
//        imexCentralClient.addPublication( "123456789", null, "NEW", "SAM" );
        ImexAssigner assigner = new ImexAssigner( imexCentralClient );
        assigner.setDryRun( false );
        final ImexAssignerConfig config = new ImexAssignerConfig();
        config.setUpdateLogsDirectory( new File( "target/123456789_3" ) );
        assigner.setImexUpdateConfig( config );

        assigner.update();

        // check in the database
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        Assert.assertEquals( experiment.getShortLabel(), daoFactory.getExperimentDao().getByXref( "IM-1" ).getShortLabel() );
        Assert.assertEquals( "123456789", daoFactory.getPublicationDao().getByXref( "IM-1" ).getShortLabel() );
        Assert.assertEquals( 3, daoFactory.getInteractionDao().getByXrefLike( "IM-1-%" ).size() );

        // check log files
        Assert.assertEquals( 1, countLines( "target/123456789_3/processed.csv" ) );
        Assert.assertEquals( 1, countLines( "target/123456789_3/processed-imex.csv" ) );
        Assert.assertEquals( 0, countLines( "target/123456789_3/publication-assigned.csv" ) );
        Assert.assertEquals( 3, countLines( "target/123456789_3/interaction-assigned.csv" ) );
    }

    @Test
    public void update_4() throws Exception {

        //  Publication not part of any IMEx covered journal, but IMEx id assigned manually by curator

        final Publication publication = getMockBuilder().createPublication( "123456789" );
        CvDatabase imex = getMockBuilder().createCvObject( CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX );
        CvXrefQualifier imexPrimary = getMockBuilder().createCvObject( CvXrefQualifier.class,
                CvXrefQualifier.IMEX_PRIMARY_MI_REF,
                CvXrefQualifier.IMEX_PRIMARY );
        final Experiment experiment = getMockBuilder().createExperimentRandom( 2 );
        experiment.addXref( getMockBuilder().createXref( experiment, "IM-1", imexPrimary, imex ) );
        experiment.setPublication( null );
        addImexJournalAnnotations( experiment );
        addAcceptedAnnotations( experiment );
        publication.addExperiment( experiment );

        getCorePersister().saveOrUpdate( publication );

        // setup assigner
        final MockImexCentralClient imexCentralClient = new MockImexCentralClient();
        ImexAssigner assigner = new ImexAssigner( imexCentralClient );
        assigner.setDryRun( false );
        final ImexAssignerConfig config = new ImexAssignerConfig();

        config.setUpdateLogsDirectory( new File( "target/123456789_4" ) );
        assigner.setImexUpdateConfig( config );

        assigner.update();

        // check in the database
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        Assert.assertEquals( experiment.getShortLabel(), daoFactory.getExperimentDao().getByXref( "IM-1" ).getShortLabel() );
        Assert.assertEquals( "123456789", daoFactory.getPublicationDao().getByXref( "IM-1" ).getShortLabel() );
        Assert.assertEquals( 2, daoFactory.getInteractionDao().getByXrefLike( "IM-1-%" ).size() );

        // check log files
        Assert.assertEquals( 1, countLines( "target/123456789_4/processed.csv" ) );
        Assert.assertEquals( 1, countLines( "target/123456789_4/processed-imex.csv" ) );
        Assert.assertEquals( 1, countLines( "target/123456789_4/publication-assigned.csv" ) );
        Assert.assertEquals( 2, countLines( "target/123456789_4/interaction-assigned.csv" ) );
    }

    @Test
    public void update_5() throws Exception {

        // Publication created without IMEx ID and experiment with IMEx id.
        // Experiment isn't part of an IMEx covered journal
        // The experiment's IMEx ID should get assigned to the publication,

        final Publication publication = getMockBuilder().createPublication( "123456789" );
        CvDatabase imex = getMockBuilder().createCvObject( CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX );
        CvXrefQualifier imexPrimary = getMockBuilder().createCvObject( CvXrefQualifier.class,
                CvXrefQualifier.IMEX_PRIMARY_MI_REF,
                CvXrefQualifier.IMEX_PRIMARY );
        final Experiment experiment = getMockBuilder().createExperimentRandom( 2 );
        experiment.addXref( getMockBuilder().createXref( experiment, "IM-1", imexPrimary, imex ) );
        experiment.setPublication( null );
        experiment.getAnnotations().clear();
        addAcceptedAnnotations( experiment );
        publication.addExperiment( experiment );

        getCorePersister().saveOrUpdate( publication );

        // setup assigner
        final MockImexCentralClient imexCentralClient = new MockImexCentralClient();
        imexCentralClient.initImexSequence( 3 );

        ImexAssigner assigner = new ImexAssigner( imexCentralClient );
        assigner.setDryRun( false );
        final ImexAssignerConfig config = new ImexAssignerConfig();
        config.setUpdateLogsDirectory( new File( "target/123456789_5" ) );
        assigner.setImexUpdateConfig( config );

        assigner.update();

        Assert.assertEquals( 3, imexCentralClient.getNextSequenceValue() );

        // check in the database
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        Assert.assertEquals( experiment.getShortLabel(), daoFactory.getExperimentDao().getByXref( "IM-1" ).getShortLabel() );
        Assert.assertEquals( "123456789", daoFactory.getPublicationDao().getByXref( "IM-1" ).getShortLabel() );
        Assert.assertEquals( 2, daoFactory.getInteractionDao().getByXrefLike( "IM-1-%" ).size() );

        // check log files
        Assert.assertEquals( 1, countLines( "target/123456789_5/processed.csv" ) );
        Assert.assertEquals( 1, countLines( "target/123456789_5/processed-imex.csv" ) );
        Assert.assertEquals( 1, countLines( "target/123456789_5/publication-assigned.csv" ) );
        Assert.assertEquals( 2, countLines( "target/123456789_5/interaction-assigned.csv" ) );
    }

    private int countLines( String resource ) throws IOException {
        final List lines = IOUtils.readLines( new FileInputStream( resource ) );
        for ( Iterator iterator = lines.iterator(); iterator.hasNext(); ) {
            String line = ( String ) iterator.next();
            if( line.startsWith( "#" ) ) {
                iterator.remove();
            }
        }
        return lines.size();
    }

    @Test
    public void update_annotation_Already_Here_once() throws Exception {

        // updates a publication that is registered in IMEx Central

        TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        // make a publication that can be exported to IMEx
        final Publication publication = getMockBuilder().createPublication( "123456789" );
        final Experiment experiment = getMockBuilder().createExperimentRandom( 2 );
        experiment.setPublication( null );
        addImexJournalAnnotations( experiment );
        addAcceptedAnnotations( experiment );
        publication.addExperiment( experiment );
        addImexCuration( experiment );

        getCorePersister().saveOrUpdate( publication );

        // setup assigner
        final MockImexCentralClient imexCentralClient = new MockImexCentralClient();
        imexCentralClient.addPublication( "123456789", null, "NEW", "SAM" );
        ImexAssigner assigner = new ImexAssigner( imexCentralClient );
        assigner.setDryRun( false );
        final ImexAssignerConfig config = new ImexAssignerConfig();
        config.setUpdateLogsDirectory( new File( "target/123456789" ) );
        assigner.setImexUpdateConfig( config );

        IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);

        assigner.update();

        // check in the database
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        Experiment ex = daoFactory.getExperimentDao().getByXref( "IM-1" );

        Assert.assertEquals( experiment.getShortLabel(), ex.getShortLabel() );

        int numberImexCuration = 0;

        for (Annotation a : ex.getAnnotations()){
            if (a.getCvTopic().getShortLabel().equals("imex curation")){
                numberImexCuration ++;
            }
        }
        Assert.assertEquals(1, numberImexCuration);
    }

    public void assertTsvLineContains( String line, int column, String expectedValue ) {
        final String[] columns = line.split( "\t" );
        Assert.assertEquals( expectedValue, columns[column + 1] );
    }

    private void addImexJournalAnnotations( AnnotatedObject ao ) {
        ao.addAnnotation( getMockBuilder().createAnnotation( "Cell (0092-8674)",
                CvTopic.JOURNAL_MI_REF, CvTopic.JOURNAL ) );
        ao.addAnnotation( getMockBuilder().createAnnotation( "2007",
                CvTopic.PUBLICATION_YEAR_MI_REF, CvTopic.PUBLICATION_YEAR ) );
    }

    private void addAcceptedAnnotations( AnnotatedObject ao ) {
        ao.addAnnotation( getMockBuilder().createAnnotation( "By sandra today",
                "IA:0278", CvTopic.ACCEPTED ) );
    }

    private void addImexCuration( Experiment exp ){
        Annotation a = getMockBuilder().createAnnotation( null,
                "MI:0959", "imex curation" );
        exp.addAnnotation( a );
    }
}
