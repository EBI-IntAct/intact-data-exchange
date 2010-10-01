package uk.ac.ebi.intact.dataexchange.uniprotexport;

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
 * @since <pre>24-Sep-2010</pre>
 */

public class MiScoreSorting {

    public static void main( String[] args ) throws IOException {

        // three possible arguments
        if( args.length != 4 ) {
            System.err.println( "Usage: MiScoreComputing <file1> <file2> <file3>" );
            System.err.println( "Usage: <file1> file containing all the scores" );
            System.err.println( "Usage: <file2> file containing all the scores of the interactions exported in uniprot" );
            System.err.println( "Usage: <file3> file containing all the scores of the interactions not exported in uniprot" );
            System.err.println( "Usage: <file4> file containing all the interaction acs which are currently exported in uniprot" );
            System.exit( 1 );
        }
        final String fileTotal = args[0];
        final String fileDataExported = args[1];
        final String fileDataNotExported = args[2];
        final String fileInteractionExported = args[3];

        System.out.println( "filename with total scores = " + fileTotal );
        System.out.println( "filename with scores or interactions exported in uniprot = " + fileDataExported );
        System.out.println( "filename with scores or interactions not exported in uniprot = " + fileDataNotExported );
        System.out.println( "filename with interactions acs currently exported in uniprot = " + fileInteractionExported );

        String database = "zpro";
        IntactContext.initContext(new String[] {"/META-INF/"+database+".spring.xml"});

        InteractionExtractorForMIScore interactionExtractor = new InteractionExtractorForMIScore();

        try {

            System.out.println("create MI score client");
            MiScoreClient scoreClient = new MiScoreClient();

            System.out.println("export interactions from intact with current rules");
            //List<String> exportedBinaryInteractions = interactionExtractor.extractInteractionsPossibleToExport(true, fileInteractionExported);
            //List<String> exportedBinaryInteractions = interactionExtractor.extractInteractionsFromFile("/home/marine/Desktop/Intact_interactions_exported.txt");
            List<String> exportedBinaryInteractions = interactionExtractor.extractInteractionsWithoutRulesForInteractionDetectionMethod(fileInteractionExported);

            System.out.println("export interactions scores");
            scoreClient.extractMiScoresFromFile(exportedBinaryInteractions, fileTotal, fileDataExported);

        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }
}
