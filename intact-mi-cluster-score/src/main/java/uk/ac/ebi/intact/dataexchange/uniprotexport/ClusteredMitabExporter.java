package uk.ac.ebi.intact.dataexchange.uniprotexport;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.uniprotexport.factory.InteractionExporterFactory;
import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.ExporterRule;
import uk.ac.ebi.intact.util.uniprotExport.miscore.ClusteredBinaryInteractionProcessor;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.ClusteredMitabFilter;

import java.io.IOException;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public class ClusteredMitabExporter {

    public static void main( String[] args ) throws IOException {

        // Six possible arguments
        if( args.length != 5 ) {
            System.err.println( "Usage: UniprotExporter <rule> <mitabFile> <fileExported> <fileExcluded> <database>" );
            System.err.println( "Usage: <rule> is the type of rule we want to use to export the interaction to uniprot. " +
                    "Can be 'detection_method' if we want the rules based on detection method or 'mi_score' if we want the rules based on mi score" );
            System.err.println( "Usage: <mitabFile> is the clustered mitab file with pre-computed mi scores" );
            System.err.println( "Usage: <fileExported> the name of the file which will contain the results of the export ");
            System.err.println( "Usage: <fileExcluded> the name of the file which will contain the excluded interactions ");
            System.err.println( "Usage: <database> the database instance which will be used to extract supplementary information");

            System.exit( 1 );
        }
        String clusteredMitab = args[1];
        String fileExported = args[2];
        String fileExcluded = args[3];
        String database = args[4];

        IntactContext.initContext(new String[]{"/META-INF/" + database + ".spring.xml"});

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

        System.out.println( "database = " + database );

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
