package uk.ac.ebi.intact.dataexchange.uniprotexport;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.uniprotexport.factory.*;
import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.ExporterRule;
import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.InteractionSource;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportProcessor;
import uk.ac.ebi.intact.util.uniprotExport.converters.DRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.CCLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.GoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.filters.InteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;
import uk.ac.ebi.intact.util.uniprotExport.filters.mitab.NonClusteredMitabFilter;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;

/**
 * The main class for running uniprot export.
 *
 * The possible arguments are :
 * - rule : only 'mi_score' available. It defines the rules we want to apply to the binary interactions
 * - source : can be 'mitab' or 'intact' depending if we export interactions from a mitab file or directly from intact.
 * we can specify the name of the mitab file like that : -Dsource=mitab:filename
 * - drFile : name of the file containing the DR lines which will be generated. If we want to specify the version of the DR format to use we can write :
 * -DdrFile:1. The current versions for DR formats are : 1.
 * - ccFile : name of the file containing the CC lines which will be generated. If we want to specify the version of the CC format to use we can write :
 * -DccFile:1. The current versions for CC formats are : 1 or 2.
 * - goFile : name of the file containing the GO lines which will be generated. If we want to specify the version of the GO format to use we can write :
 * -DgoFile:1. The current versions for GO formats are : 1.
 * - database : name of the database instance to use for reading private annotations (such as no-uniprot-update, uniprot-dr-export, etc.)
 * - binaryOnly : can be 'false' or 'true'. It tells if we want to work with only true binary interactions or if we accept spoke expanded binary interactions
 * - highConfidence : can be 'false' or 'true'. It tells if we want to work with only high confidence interactions (exclude uniprot-dr-export = no or author-confidence = low)
 * - proteinOnly : can be 'false' or 'true'. It tells if we want binary interactions only involving uniprot proteins
 * - positiveOnly : can be 'false' or 'true'. It tells if we want only positive binary interactions (exclude negative binary interactions)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */
public class UniprotExporter {

    public static void main( String[] args ) {

        // Six possible arguments
        if( args.length != 11 ) {
            System.err.println( "Usage: UniprotExporter <rule> <source> <drFile> <ccFile> <silverCCFile> <goFile> <binaryOnly> <highConfidence> <proteinOnly> <positiveOnly> <excludeInferred>" );
            System.err.println( "Usage: <rule> is the type of rule we want to use to export the interaction to uniprot. " +
                    "Currently only available rule based in a modified version of 'mi_score'" );
            System.err.println( "Usage: <source> is the source of the binary interactions we want to export." +
                    " Can be 'intact' if we want to export the interactions directly from intact (no file name is necessary) or 'mitab:fileName' if we want to export from mitab. In the last case" +
                    "the file name must be given in the source option preceded by ':'" );
            System.err.println( "Usage: <drFile:version> the name of the file which will contain the DR lines. The version is optional and by default is 1");
            System.err.println( "Usage: <ccFile:version> the name of the file which will contain the CC lines. The version is optional and by default is 1");
            System.err.println( "Usage: <silverCCFile:version> the name of the file which will contain the silver CC lines. The version is optional and by default is 1");
            System.err.println( "Usage: <goFile:version> the name of the file which will contain the GO lines. The version is optional and by default is 1");
            System.err.println( "Usage: <binaryOnly> true : exclude spoke expanded interactions from the cluster or false : accept spoke expanded interactions in the cluster. By default, is false.");
            System.err.println( "Usage: <highConfidence> true : exclude low confidence interactions (dr-export = no or condition is not respected) from the cluster or false : accept low confidence interactions in the cluster. By default, is true.");
            System.err.println( "Usage: <proteinOnly> true : exclude interactions with a non protein interactor from the cluster or false : accept interactions having a non protein interactor in the cluster. By default, is true.");
            System.err.println( "Usage: <positiveOnly> true : exclude negative interactions from the cluster or false : accept negative interactions in the cluster. By default, is true.");
            System.err.println( "Usage: <excludeInferred> true : exclude inferred interactions from the cluster or false : accept interactions inferred by the author and/or curator in the cluster. By default, is true.");
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
        String silverCcFile = args[5];
        int version_silverCcFile;
        boolean excludeSpokeExpanded = Boolean.parseBoolean(args[6]);
        boolean excludeLowConfidence = Boolean.parseBoolean(args[7]);
        boolean excludeNonProtein = Boolean.parseBoolean(args[8]);
        boolean excludeNegative = Boolean.parseBoolean(args[9]);
        boolean excludeInferred = Boolean.parseBoolean(args[10]);

        IntactContext.initContext(new String[]{"/META-INF/jpa.spring.xml", "/META-INF/uniprotExport.spring.xml"});

        FilterConfig config = FilterContext.getInstance().getConfig();

        config.setExcludeLowConfidenceInteractions(excludeLowConfidence);
        config.setExcludeNegativeInteractions(excludeNegative);
        config.setExcludeNonUniprotInteractors(excludeNonProtein);
        config.setExcludeSpokeExpandedInteractions(excludeSpokeExpanded);
        config.setExcludeInferredInteractions(excludeInferred);

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

        if (silverCcFile.contains(":")){
            int index = args[5].indexOf(":");
            silverCcFile = args[5].substring(0, index);

            version_silverCcFile =  Integer.parseInt(args[5].substring(index + 1));
        }
        else{
            version_silverCcFile = 1;
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
        System.out.println( "Silver CC file = " + silverCcFile );
        System.out.println( "Version of the silver CC writer = " + version_silverCcFile );

        System.out.println("Filter spoke expanded interactions : " + excludeSpokeExpanded );
        System.out.println("Filter low confidence interactions : " + excludeLowConfidence);
        System.out.println("Filter non uniprot proteins : " + excludeNonProtein);
        System.out.println("Filter negative interactions : " + excludeNegative) ;
        System.out.println("Filter inferred interactions : " + excludeInferred) ;

        InteractionExporter exporter = InteractionExporterFactory.createInteractionExporter(rule);
        InteractionFilter filter = InteractionFilterFactory.createInteractionFilter(source, exporter);

        if (mitabFile != null && filter instanceof NonClusteredMitabFilter){
            NonClusteredMitabFilter mitabFilter = (NonClusteredMitabFilter) filter;
            mitabFilter.setMitab(mitabFile);
        }

        DRLineConverter drConverter = DRConverterFactory.createDRLineConverter(version_drFile);
        GoLineConverter<? extends GOParameters> goConverter = GOConverterFactory.createGOConverter(version_goFile);
        CCLineConverter ccConverter = CCConverterFactory.createCCConverter(version_ccFile);
        CCLineConverter silverCcConverter = ccConverter;

        if (version_ccFile != version_silverCcFile){
            silverCcConverter = CCConverterFactory.createCCConverter(version_silverCcFile);
        }

        UniprotExportProcessor processor = new UniprotExportProcessor(filter, goConverter, ccConverter, silverCcConverter, drConverter);

        try {
            processor.runUniprotExport(drFile, ccFile, goFile, silverCcFile);

        } catch (UniprotExportException e) {
            e.printStackTrace();
        }
    }
}
