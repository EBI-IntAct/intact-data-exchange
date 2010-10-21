package uk.ac.ebi.intact.dataexchange.uniprotexport;

import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.util.uniprotExport.miscore.InteractionExtractorForMIScore;
import uk.ac.ebi.intact.util.uniprotExport.miscore.MiScoreClient;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16-Sep-2010</pre>
 */

public class MiScoreComputing {

    public static void main( String[] args ) throws IOException {

        // three possible arguments
        if( args.length != 5 ) {
            System.err.println( "Usage: MiScoreComputing <file1> <file2> <file3>" );
            System.err.println( "Usage: <file1> file containing all the scores" );
            System.err.println( "Usage: <file2> file containing all the scores of the interactions exported in uniprot" );
            System.err.println( "Usage: <file3> file containing all the scores of the interactions not exported in uniprot" );
            System.err.println( "Usage: <file4> file containing all the interaction acs which are eligible for a uniprot export" );
            System.err.println( "Usage: <file5> file containing all the interaction acs which are currently exported in uniprot" );
            System.exit( 1 );
        }
        final String fileTotal = args[0];
        final String fileDataExported = args[1];
        final String fileDataNotExported = args[2];
        final String fileInteractionEligible = args[3];
        final String fileInteractionExported = args[4];

        System.out.println( "filename with total scores = " + fileTotal );
        System.out.println( "filename with scores or interactions exported in uniprot = " + fileDataExported );
        System.out.println( "filename with scores or interactions not exported in uniprot = " + fileDataNotExported );
        System.out.println( "filename with interactions acs eligible for uniprot export = " + fileInteractionEligible );
        System.out.println( "filename with interactions acs currently exported in uniprot = " + fileInteractionExported );

        String database = "enzpro";
        IntactContext.initContext(new String[] {"/META-INF/"+database+".spring.xml"});

        InteractionExtractorForMIScore interactionExtractor = new InteractionExtractorForMIScore();

        try {
            // all interactions
            System.out.println("export all interactions from intact which passed the dr export annotation");
            List<String> eligibleBinaryInteractions = interactionExtractor.extractInteractionsFromReleasedExperimentsPossibleToExport(fileInteractionEligible);

            System.out.println("computes MI score");
            MiScoreClient scoreClient1 = new MiScoreClient();
            scoreClient1.getInteractionClusterScore().setDirectInteractionWeight_3();
            //scoreClient1.getInteractionClusterScore().setPublicationWeight(0.5f);
            //scoreClient1.getInteractionClusterScore().setMethodWeight(0.8f);

            scoreClient1.computeMiScoresFor(eligibleBinaryInteractions, fileTotal);

            System.out.println("export interactions from intact with current rules on interaction detection method");
            List<Integer> exportedBinaryInteractions = interactionExtractor.extractInteractionsExportedWithRulesOnInteractionMethodForAllExperiment(scoreClient1.getInteractionClusterScore(), fileInteractionExported);
            //List<Integer> exportedBinaryInteractions = interactionExtractor.extractInteractionsCurrentlyExported(scoreClient1.getInteractionClusterScore(), fileInteractionExported);

            System.out.println("export interactions from intact");
            scoreClient1.extractComputedMiScoresFor(exportedBinaryInteractions, fileDataExported, fileDataNotExported);

            // only binary interactions
            System.out.println("extract only binary interactions from intact which passed the dr export annotation");
            List<String> eligibleBinary = interactionExtractor.extractBinaryInteractionsPossibleToExport(eligibleBinaryInteractions, fileInteractionEligible + "_only_binary.txt");

            System.out.println("computes MI score only binary interactions");
            MiScoreClient scoreClient2 = new MiScoreClient();
            scoreClient2.getInteractionClusterScore().setDirectInteractionWeight_3();
            //scoreClient2.getInteractionClusterScore().setPublicationWeight(0.5f);
            //scoreClient2.getInteractionClusterScore().setMethodWeight(0.8f);

            scoreClient2.computeMiScoresFor(eligibleBinary, fileTotal + "_only_binary.txt");

            System.out.println("export only binary interactions from intact with current rules on interaction detection method");
            List<Integer> exportedBinary = interactionExtractor.extractInteractionsExportedWithRulesOnInteractionMethodForAllExperiment(scoreClient2.getInteractionClusterScore(), fileInteractionExported + "_only_binary.txt");
            //List<Integer> exportedBinary = interactionExtractor.extractInteractionsCurrentlyExported(scoreClient2.getInteractionClusterScore(), fileInteractionExported + "_only_binary.txt");

            System.out.println("export vinary interactions from intact");
            scoreClient2.extractComputedMiScoresFor(exportedBinary, fileDataExported + "_only_binary.txt", fileDataNotExported + "_only_binary.txt");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}