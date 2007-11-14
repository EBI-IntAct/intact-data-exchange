/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.commons.util.ZipBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Build all ZIP file from PSI XML file generated with the pubmed classification.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02-Feb-2006</pre>
 */
public class ZipFileGenerator {

    private static final Log log = LogFactory.getLog(ZipFileGenerator.class);

    ///////////////////////
    // Constants

    /**
     * If set to true, the program displays verbose output.
     */
    private static boolean VERBOSE;

    ////////////////////////
    // Zip Building

    /**
     * Process a directory and it's subdirectory if required.
     *
     * @param directory the directory to process.
     */
    public static void clusterAllXmlFilesFromDirectory( File directory, boolean processSubdirectories ) {

        // TODO remove recursivity, should be easy.

        if ( directory == null ) {
            throw new NullPointerException( "Please give a non null directory." );
        }

        if ( ! directory.isDirectory() ) {
            throw new IllegalArgumentException( directory.getAbsolutePath() + " is not a directory." );
        }

        boolean isCurrentDirectoryWriteable = directory.canWrite();

        log.debug( "Processing directory: " + directory.getAbsolutePath() );

        if ( ! directory.canRead() ) {
            throw new IllegalArgumentException( "Cannot read: " + directory.getAbsolutePath() );
        }

        if ( ! directory.canWrite() ) {
            throw new IllegalArgumentException( "Cannot write: " + directory.getAbsolutePath() );
        }

        // start processing.
        Stack subdirectories = new Stack();

        File[] files = directory.listFiles(new PmidXmlFileFilter());

        Map pmid2files = new HashMap( files.length );

        // Screen the directory and cluster xml files per pmid.
        for ( int i = 0; i < files.length; i++ ) {
            File file = files[ i ];

            if ( file.isDirectory() ) {

                if ( processSubdirectories ) {
                    // put recursive call on hold, we process one directory at a time.
                    subdirectories.push( file );
                }

            } else {

                String pmid = extractPubmedId( file.getName() );
                if ( pmid != null ) {

                    Collection filenames = null;
                    if ( pmid2files.containsKey( pmid ) ) {
                        // get existing collection
                        filenames = (Collection) pmid2files.get( pmid );
                    } else {
                        // not there yet, create it.
                        filenames = new ArrayList( 2 );
                        pmid2files.put( pmid, filenames );
                    }

                    filenames.add( file );
                } else {
                    log.error( "Could not extract a pubmed id from filename: " + file.getName() );
                }
            }

            files[ i ] = null; // free resource as we go
        }

        files = null; // free resource

        // Process the map and produce ZIP files
        if ( isCurrentDirectoryWriteable ) {
            for ( Iterator iterator = pmid2files.keySet().iterator(); iterator.hasNext(); ) {
                String pmid = (String) iterator.next();
                Collection xmlFiles = (Collection) pmid2files.get( pmid );

                // build plateform specific filename
                String zipFullpath = directory.getAbsolutePath() + File.separator + pmid + FileHelper.ZIP_FILE_EXTENSION;

                File zipFile = new File( zipFullpath );
                if ( zipFile.exists() ) {
                    if ( VERBOSE ) {
                        log.debug( zipFile.getName() + " already exists, skip." );
                    }
                } else {
                    try {
                        ZipBuilder.createZipFile( zipFile, xmlFiles, VERBOSE );
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }

                iterator.remove(); // free resource as we go
            }
        } else {
            log.error( "The current directory is not writable: " + directory.getAbsolutePath() );
        }

        // process all subdirectories
        while ( ! subdirectories.isEmpty() ) {
            File subdirectory = (File) subdirectories.pop();
            clusterAllXmlFilesFromDirectory( subdirectory, processSubdirectories );
        }
    }

    ////////////////////////////
    // Specific Common Cli

    /**
     * Displays usage for the program.
     *
     * @param options the options (common-cli).
     */
    private static void displayUsage( Options options ) {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "ZipFileGenerator " +
                             "-dir <directory_to_process> " +
                             "-verbose " +
                             "-recursive",
                             options );
    }

    /**
     * Setup the command line options.
     *
     * @return the options (common-cli).
     */
    private static Options setupCommandLineOptions() {
        // create Option objects
        Option helpOpt = new Option( "help", "print this message." );

        Option directoryOpt = OptionBuilder.withArgName( "startDirectory" )
                .hasArg()
                .withDescription( "Start directory" )
                .create( "dir" );
        directoryOpt.setRequired( true );

        Option recursiveOpt = OptionBuilder.withArgName( "recursive" )
                .hasArg( false )
                .withDescription( "if true: process directory recursively" )
                .create( "recursive" );
        recursiveOpt.setRequired( false );

        Option verboseOpt = OptionBuilder.withArgName( "verbose" )
                .hasArg( false )
                .withDescription( "if true: verbose message" )
                .create( "verbose" );
        verboseOpt.setRequired( false );

        Options options = new Options();

        options.addOption( helpOpt );
        options.addOption( directoryOpt );
        options.addOption( recursiveOpt );
        options.addOption( verboseOpt );

        return options;
    }

    public static String extractPubmedId( String filename ) {

        String pmid = null;

        Matcher matcher = PmidXmlFileFilter.PMID_FILENAME_PATTERN.matcher( filename );
        boolean matchFound = matcher.find();

        if ( matchFound ) {
            // Get group for this match
            pmid = matcher.group( 1 );
        }

        return pmid;
    }

    ///////////////////////////
    // M A I N

    /**
     * COmmand line tool allowing to generate zip files based on the PSI-MI XML files classified by pubmed id.
     *
     * @param args [0] directory, [1] recursive, [2] verbose.
     */
    public static void main( String[] args ) {

        Options options = setupCommandLineOptions();

        // create the parser
        CommandLineParser parser = new BasicParser();
        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse( options, args, true );
        } catch ( ParseException exp ) {
            // Oops, something went wrong

            displayUsage( options );

            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            System.exit( 1 );
        }


        if ( line.hasOption( "help" ) ) {
            displayUsage( options );
            System.exit( 0 );
        }

        // Process arguments
        String directoryName = line.getOptionValue( "dir" );
        log.debug( "Directory: " + directoryName );

        final boolean recursive = line.hasOption( "recursive" );
        log.debug( "Recursive: " + recursive );

        final boolean verbose = line.hasOption( "verbose" );
        log.debug( "Verbose: " + verbose );
        VERBOSE = verbose;

        // build plateform specific filename
        directoryName = FileHelper.fixFileSeparator( directoryName );

        File directory = new File( directoryName );

        // start processing...
        clusterAllXmlFilesFromDirectory( directory, recursive );
    }
}