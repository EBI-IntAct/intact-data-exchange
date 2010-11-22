package uk.ac.ebi.intact.dataexchange.uniprotexport;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.util.uniprotExport.miscore.MiScoreClient;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extractor.InteractionExtractorForMIScore;

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

        try {

            System.out.println("computes MI score for all interactions");
            MiScoreClient scoreClient = new MiScoreClient();

            scoreClient.processExportWithFilterOnBinaryInteraction(fileInteractionEligible, fileTotal, fileDataExported, fileDataNotExported);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UniprotExportException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}