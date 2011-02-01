package uk.ac.ebi.intact.dataexchange.uniprotexport;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.uniprotexport.factory.InteractionExporterFactory;
import uk.ac.ebi.intact.dataexchange.uniprotexport.factory.InteractionFilterFactory;
import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.ExporterRule;
import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.InteractionSource;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportProcessor;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.InteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.MitabFilter;

import java.io.IOException;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public class UniprotExporter {

    public static void main( String[] args ) throws IOException {

        // Six possible arguments
        if( args.length != 6 ) {
            System.err.println( "Usage: UniprotExporter <rule> <source> <drFile> <ccFile> <goFile> <database>" );
            System.err.println( "Usage: <rule> is the type of rule we want to use to export the interaction to uniprot. " +
                    "Can be 'detection_method' if we want the rules based on detection method or 'mi_score' if we want the rules based on mi score" );
            System.err.println( "Usage: <source> is the source of the binary interactions we want to export." +
                    " Can be 'intact' if we want to export the interactions directly from intact (no file name is necessary) or 'mitab:fileName' if we want to export from mitab. In the last case" +
                    "the file name must be given in the source option preceded by ':'" );
            System.err.println( "Usage: <drFile> the name of the file which will contain the DR lines");
            System.err.println( "Usage: <ccFile> the name of the file which will contain the CC lines");
            System.err.println( "Usage: <goFile> the name of the file which will contain the GO lines");
            System.err.println( "Usage: <database> the database instance which will be used to extract supplementary information");

            System.exit( 1 );
        }
        final ExporterRule rule = InteractionExporterFactory.convertIntoExporterRule(args[0]);

        if (rule.equals(ExporterRule.none)){
            System.err.println( "Usage: <rule> is the type of rule we want to use to export the interaction to uniprot. " +
                    "Can be 'detection_method' if we want the rules based on detection method or 'mi_score' if we want the rules based on mi score" );
            System.exit( 1 );
        }

        String sourceType = args[1];
        String mitabFile = null;
        String drFile = args[2];
        String ccFile = args[3];
        String goFile = args[4];
        String database = args[5];

        if (sourceType.contains(":")){
            int index = args[1].indexOf(":");
            sourceType = args[1].substring(0, index);

            mitabFile =  args[1].substring(index);
        }

        final InteractionSource source = InteractionFilterFactory.convertIntoInteractionSourceName(sourceType);

        if (source.equals(InteractionSource.none) || (mitabFile == null && source.equals(InteractionSource.mitab)) || (mitabFile != null && !source.equals(InteractionSource.mitab))){
            System.err.println( "Usage: <source> is the source of the binary interactions we want to export." +
                    " Can be 'intact' if we want to export the interactions directly from intact or 'mitab:fileName' if we want to export from mitab. In the last case" +
                    "the file name must be given in the source option preceded by ':'" );
            System.exit( 1 );
        }

        System.out.println( "Exporter rule = " + rule.toString() );
        System.out.println( "Source = " + source.toString() );
        System.out.println( "DR file = " + drFile );
        System.out.println( "CC file = " + ccFile );
        System.out.println( "GO file = " + goFile );

        System.out.println( "database = " + database );

        if (mitabFile != null){
            System.out.println( "MITAB file = " + mitabFile );
        }

        InteractionExporter exporter = InteractionExporterFactory.createInteractionExporter(rule);
        InteractionFilter filter = InteractionFilterFactory.createInteractionFilter(source, exporter);

        if (mitabFile != null && filter instanceof MitabFilter){
             MitabFilter mitabFilter = (MitabFilter) filter;
            mitabFilter.setMitab(mitabFile);
        }

        UniprotExportProcessor processor = new UniprotExportProcessor(filter);

        IntactContext.initContext(new String[]{"/META-INF/" + database + ".spring.xml"});

        try {

            processor.runUniprotExport(drFile, ccFile, goFile);

        } catch (UniprotExportException e) {
            e.printStackTrace();
        }
    }
}
