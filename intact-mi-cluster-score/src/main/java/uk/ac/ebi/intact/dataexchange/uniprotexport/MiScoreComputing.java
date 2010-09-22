package uk.ac.ebi.intact.dataexchange.uniprotexport;

import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.util.uniprotExport.InteractionExtractorForMIScore;
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
        if( args.length != 3 ) {
            System.err.println( "Usage: MiScoreComputing <file1> <file2> <file3>" );
            System.err.println( "Usage: <file1> file containing all the scores" );
            System.err.println( "Usage: <file2> file containing all the scores of the interactions exported in uniprot" );
            System.err.println( "Usage: <file3> file containing all the scores of the interactions not exported in uniprot" );
            System.exit( 1 );
        }
        final String fileTotal = args[0];
        final String fileDataExported = args[1];
        final String fileDataNotExported = args[2];

        System.out.println( "filename with total scores = " + fileTotal );
        System.out.println( "filename with scores or interactions exported in uniprot = " + fileDataExported );
        System.out.println( "filename with scores or interactions not exported in uniprot = " + fileDataNotExported );

        String database = "zpro";
        IntactContext.initContext(new String[] {"/META-INF/"+database+".spring.xml"});

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        InteractionExtractorForMIScore interactionExtractor = new InteractionExtractorForMIScore();

        try {
            System.out.println("export interactions from intact");
            List<String> elligibleBinaryInteractions = interactionExtractor.extractInteractionsPossibleToExport(false);

            System.out.println("computes MI score");
            MiScoreClient scoreClient = new MiScoreClient();

            scoreClient.computeMiScoresFor(elligibleBinaryInteractions, fileTotal);

            System.out.println("export interactions from intact with current rules");
            List<String> exportedBinaryInteractions = interactionExtractor.extractInteractionsPossibleToExport(true);

            System.out.println("export interactions from intact");
            scoreClient.extractMiScoresFor(exportedBinaryInteractions, fileDataExported, fileDataNotExported);

        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        dataContext.rollbackTransaction(transactionStatus);
    }
}