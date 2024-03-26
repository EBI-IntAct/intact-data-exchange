package uk.ac.ebi.intact.dataexchange.uniprotexport;

import uk.ac.ebi.intact.dataexchange.uniprotexport.factory.InteractionFilterFactory;
import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.InteractionSource;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.filters.InteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;
import uk.ac.ebi.intact.util.uniprotExport.filters.mitab.NonClusteredMitabFilter;

import java.io.IOException;

/**
 * This script will cluster interactions and then filter interactions eligible for uniprot export.
 *
 * It will compute the mi score of clustered interactions and save the results in a mitab file and text file
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/03/11</pre>
 */

public class UniprotExportFilter {

    public static void main( String[] args ) throws IOException {

        // Six possible arguments
        if( args.length != 7 ) {
            System.err.println( "Usage: UniprotExportFilter <source> <result> <binaryOnly> <highConfidence> <proteinOnly> <positiveOnly>" );
            System.err.println( "Usage: <source> is the source of the binary interactions we want to export." +
                    " Can be 'intact' if we want to export the interactions directly from intact (no file name is necessary) or 'mitab:fileName' if we want to export from mitab. In the last case" +
                    "the file name must be given in the source option preceded by ':'" );
            System.err.println( "Usage: <result> the name of the mitab file which will contain the results. Four files will be generated : two files for positive interactions, two for negative interactions. One mitab file and one text file");
            System.err.println( "Usage: <binaryOnly> true : exclude spoke expanded interactions from the cluster or false : accept spoke expanded interactions in the cluster. By default, is false.");
            System.err.println( "Usage: <highConfidence> true : exclude low confidence interactions (dr-export = no or condition is not respected) from the cluster or false : accept low confidence interactions in the cluster. By default, is true.");
            System.err.println( "Usage: <proteinOnly> true : exclude interactions with a non protein interactor from the cluster or false : accept interactions having a non protein interactor in the cluster. By default, is true.");
            System.err.println( "Usage: <positiveOnly> true : exclude negative interactions from the cluster or false : accept negative interactions in the cluster. By default, is true.");
            System.exit( 1 );
        }

        String sourceType = args[0];
        String mitabFile = null;
        String results = args[1];
        boolean excludeSpokeExpanded = Boolean.parseBoolean(args[2]);
        boolean excludeLowConfidence = Boolean.parseBoolean(args[3]);
        boolean excludeNonProtein = Boolean.parseBoolean(args[4]);
        boolean excludeNegative = Boolean.parseBoolean(args[5]);

        IntactContext.initContext(new String[]{"/META-INF/jpa.spring.xml", "/META-INF/uniprotExport.spring.xml"});

        FilterConfig config = FilterContext.getInstance().getConfig();

        config.setExcludeLowConfidenceInteractions(excludeLowConfidence);
        config.setExcludeNegativeInteractions(excludeNegative);
        config.setExcludeNonUniprotInteractors(excludeNonProtein);
        config.setExcludeSpokeExpandedInteractions(excludeSpokeExpanded);

        if (sourceType.contains(":")){
            int index = args[0].indexOf(":");
            sourceType = args[0].substring(0, index);

            mitabFile =  args[0].substring(index + 1);
        }

        final InteractionSource source = InteractionFilterFactory.convertIntoInteractionSourceName(sourceType);

        if (source.equals(InteractionSource.none) || (mitabFile == null && source.equals(InteractionSource.mitab)) || (mitabFile != null && !source.equals(InteractionSource.mitab))){
            System.err.println( "Usage: <source> is the source of the binary interactions we want to export." +
                    " Can be 'intact' if we want to export the interactions directly from intact or 'mitab:fileName' if we want to export from mitab. In the last case" +
                    "the file name must be given in the source option preceded by ':'" );
            System.exit( 1 );
        }

        System.out.println( "Source = " + source.toString() );
        if (mitabFile != null){
            System.out.println( "MITAB file = " + mitabFile );
        }
        System.out.println( "Results file = " + results );

        System.out.println("Filter spoke expanded interactions : " + excludeSpokeExpanded );
        System.out.println("Filter low confidence interactions : " + excludeLowConfidence);
        System.out.println("Filter non uniprot proteins : " + excludeNonProtein);
        System.out.println("Filter negative interactions : " + excludeNegative) ;

        InteractionFilter filter = InteractionFilterFactory.createInteractionFilter(source, null);

        if (mitabFile != null && filter instanceof NonClusteredMitabFilter){
            NonClusteredMitabFilter mitabFilter = (NonClusteredMitabFilter) filter;
            mitabFilter.setMitab(mitabFile);
        }

        try {
            filter.saveClusterAndFilterResultsFrom(mitabFile, results);
        } catch (UniprotExportException e) {
            e.printStackTrace();
        }
    }
}
