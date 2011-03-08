package uk.ac.ebi.intact.dataexchange.uniprotexport;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.uniprotexport.factory.*;
import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.ExporterRule;
import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.InteractionSource;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportProcessor;
import uk.ac.ebi.intact.util.uniprotExport.converters.InteractorToDRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToCCLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToGoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.filters.InteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;
import uk.ac.ebi.intact.util.uniprotExport.filters.mitab.NonClusteredMitabFilter;

import java.io.IOException;

/**
 * The main class for uniprot export
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public class UniprotExporter {

    public static void main( String[] args ) throws IOException {

        // Six possible arguments
        if( args.length != 10 ) {
            System.err.println( "Usage: UniprotExporter <rule> <source> <drFile> <ccFile> <goFile> <database> <binaryOnly> <highConfidence> <proteinOnly> <positiveOnly>" );
            System.err.println( "Usage: <rule> is the type of rule we want to use to export the interaction to uniprot. " +
                    "Can be 'detection_method' if we want the rules based on detection method or 'mi_score' if we want the rules based on mi score" );
            System.err.println( "Usage: <source> is the source of the binary interactions we want to export." +
                    " Can be 'intact' if we want to export the interactions directly from intact (no file name is necessary) or 'mitab:fileName' if we want to export from mitab. In the last case" +
                    "the file name must be given in the source option preceded by ':'" );
            System.err.println( "Usage: <drFile:version> the name of the file which will contain the DR lines. The version is optional and by default is 1");
            System.err.println( "Usage: <ccFile:version> the name of the file which will contain the CC lines. The version is optional and by default is 1");
            System.err.println( "Usage: <goFile:version> the name of the file which will contain the GO lines. The version is optional and by default is 1");
            System.err.println( "Usage: <database> the database instance which will be used to extract supplementary information");
            System.err.println( "Usage: <binaryOnly> true : exclude spoke expanded interactions from the cluster or false : accept spoke expanded interactions in the cluster. By default, is false.");
            System.err.println( "Usage: <highConfidence> true : exclude low confidence interactions (dr-export = no or condition is not respected) from the cluster or false : accept low confidence interactions in the cluster. By default, is true.");
            System.err.println( "Usage: <proteinOnly> true : exclude interactions with a non protein interactor from the cluster or false : accept interactions having a non protein interactor in the cluster. By default, is true.");
            System.err.println( "Usage: <positiveOnly> true : exclude negative interactions from the cluster or false : accept negative interactions in the cluster. By default, is true.");
            System.exit( 1 );
        }

        String ruleArg = args[0];
        String sourceType = args[1];
        String mitabFile = null;
        String drFile = args[2];
        int version_drFile;
        String ccFile = args[3];
        int version_ccFile;
        String goFile = args[4];
        int version_goFile;
        String database = args[5];
        boolean excludeSpokeExpanded = Boolean.getBoolean(args[6]);
        boolean excludeLowConfidence = Boolean.getBoolean(args[7]);
        boolean excludeNonProtein = Boolean.getBoolean(args[8]);
        boolean excludeNegative = Boolean.getBoolean(args[9]);

        IntactContext.initContext(new String[]{"/META-INF/" + database + ".spring.xml"});
        FilterConfig config = FilterContext.getInstance().getConfig();

        config.setExcludeLowConfidenceInteractions(excludeLowConfidence);
        config.setExcludeNegativeInteractions(excludeNegative);
        config.setExcludeNonUniprotInteractors(excludeNonProtein);
        config.setExcludeSpokeExpandedInteractions(excludeSpokeExpanded);

        final ExporterRule rule = InteractionExporterFactory.convertIntoExporterRule(ruleArg);

        if (rule.equals(ExporterRule.none)){
            System.err.println( "Usage: <rule> is the type of rule we want to use to export the interaction to uniprot. " +
                    "Can be 'detection_method' if we want the rules based on detection method or 'mi_score' if we want the rules based on mi score" );
            System.exit( 1 );
        }

        if (sourceType.contains(":")){
            int index = args[1].indexOf(":");
            sourceType = args[1].substring(0, index);

            mitabFile =  args[1].substring(index + 1);
        }

        if (drFile.contains(":")){
            int index = args[2].indexOf(":");
            drFile = args[2].substring(0, index);

            version_drFile =  Integer.parseInt(args[2].substring(index + 1));
        }
        else{
            version_drFile = 1;
        }

        if (ccFile.contains(":")){
            int index = args[3].indexOf(":");
            ccFile = args[3].substring(0, index);

            version_ccFile =  Integer.parseInt(args[3].substring(index + 1));
        }
        else{
            version_ccFile = 1;
        }

        if (goFile.contains(":")){
            int index = args[4].indexOf(":");
            goFile = args[4].substring(0, index);

            version_goFile =  Integer.parseInt(args[4].substring(index + 1));
        }
        else{
            version_goFile = 1;
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
        if (mitabFile != null){
            System.out.println( "MITAB file = " + mitabFile );
        }
        System.out.println( "DR file = " + drFile );
        System.out.println( "Version of the DR writer = " + version_drFile );
        System.out.println( "CC file = " + ccFile );
        System.out.println( "Version of the CC writer = " + version_ccFile );
        System.out.println( "GO file = " + goFile );
        System.out.println( "Version of the GO writer = " + version_goFile );

        System.out.println( "database = " + database );

        InteractionExporter exporter = InteractionExporterFactory.createInteractionExporter(rule);
        InteractionFilter filter = InteractionFilterFactory.createInteractionFilter(source, exporter);

        if (mitabFile != null && filter instanceof NonClusteredMitabFilter){
            NonClusteredMitabFilter mitabFilter = (NonClusteredMitabFilter) filter;
            mitabFilter.setMitab(mitabFile);
        }

        InteractorToDRLineConverter drConverter = DRConverterFactory.createDRLineConverter(version_drFile);
        EncoreInteractionToGoLineConverter goConverter = GOConverterFactory.createGOConverter(version_goFile);
        EncoreInteractionToCCLineConverter ccConverter = CCConverterFactory.createCCConverter(version_ccFile);

        UniprotExportProcessor processor = new UniprotExportProcessor(filter, goConverter, ccConverter, drConverter);

        try {
            processor.runUniprotExport(drFile, ccFile, goFile);

        } catch (UniprotExportException e) {
            e.printStackTrace();
        }
    }
}
