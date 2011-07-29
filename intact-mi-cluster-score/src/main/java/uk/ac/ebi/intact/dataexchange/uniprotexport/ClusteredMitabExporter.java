package uk.ac.ebi.intact.dataexchange.uniprotexport;

import uk.ac.ebi.intact.dataexchange.uniprotexport.factory.InteractionExporterFactory;
import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.ExporterRule;
import uk.ac.ebi.intact.util.uniprotExport.ClusteredBinaryInteractionProcessor;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.filters.mitab.ClusteredMitabFilter;

import java.io.IOException;

/**
 * This script will use the clustered binary interactions in a mitab file and run a uniprot export simulation
 * @deprecated not used
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public class ClusteredMitabExporter {

    public static void main( String[] args ) throws IOException {

        // Six possible arguments
        if( args.length != 4 ) {
            System.err.println( "Usage: UniprotExporter <rule> <mitabFile> <fileExported> <fileExcluded>" );
            System.err.println( "Usage: <rule> is the type of rule we want to use to export the interaction to uniprot. " +
                    "Can be 'detection_method' if we want the rules based on detection method or 'mi_score' if we want the rules based on mi score" );
            System.err.println( "Usage: <mitabFile> is the clustered mitab file with pre-computed mi scores" );
            System.err.println( "Usage: <fileExported> the name of the file which will contain the results of the export ");
            System.err.println( "Usage: <fileExcluded> the name of the file which will contain the excluded interactions ");

            System.exit( 1 );
        }
        String clusteredMitab = args[1];
        String fileExported = args[2];
        String fileExcluded = args[3];

        final ExporterRule rule = InteractionExporterFactory.convertIntoExporterRule(args[0]);

        if (rule.equals(ExporterRule.none)){
            System.err.println( "Usage: <rule> is the type of rule we want to use to export the interaction to uniprot. " +
                    "Can be 'detection_method' if we want the rules based on detection method or 'mi_score' if we want the rules based on mi score" );
            System.exit( 1 );
        }

        System.out.println( "Exporter rule = " + rule.toString() );
        System.out.println( "Clustered mitab = " + clusteredMitab.toString() );
        System.out.println( "file containing exported interactions = " + fileExported );
        System.out.println( "file containing excluded interactions = " + fileExcluded );

        InteractionExporter exporter = InteractionExporterFactory.createInteractionExporter(rule);
        ClusteredMitabFilter filter = new ClusteredMitabFilter(exporter, clusteredMitab);

        ClusteredBinaryInteractionProcessor processor = new ClusteredBinaryInteractionProcessor(filter);

        try {

            processor.processClusteredBinaryInteractions(fileExported, fileExcluded);

        } catch (UniprotExportException e) {
            e.printStackTrace();
        }
    }
}
