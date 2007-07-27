/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import java.io.File;

/**
 * Help handling files for PSI XML generation.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02-Feb-2006</pre>
 */
public class FileHelper {

    /////////////////////////
    // Constants

    /**
     *
     */
    public static final String START_CHUNK_FLAG = "[";

    /**
     *
     */
    public static final String STOP_CHUNK_FLAG = "]";

    /**
     * Extension of an XML file.
     */
    public static final String XML_FILE_EXTENSION = ".xml";

    /**
     * Extension of an ZIP file.
     */
    public static final String ZIP_FILE_EXTENSION = ".zip";

    public static final String SLASH = "/";
//    public static final String SLASH = System.getProperty("file.separator");

    ////////////////////////
    // Helper methods

    /**
     * Replace all file sepator (namely / and \\) by the plateform specific one.
     *
     * @param filename the path to fix
     *
     * @return the fixed path
     *
     */
    public static String fixFileSeparator( String filename ) {

        String currentSystem = File.separator;
        // using == for String comparison doesn't work under Linux !!!
        String otherSystem = ( currentSystem.equals( "/" ) ? "\\" : "/" ); // if unix, give windows and vice versa.

        if ( filename.indexOf( otherSystem ) != -1 ) {

            // 2. replace all slash by plateform specific file separator (if necessary)
            StringBuffer sb = new StringBuffer( filename );

            int idx;
            while ( ( idx = sb.indexOf( otherSystem ) ) != -1 ) {
                sb.replace( idx, idx + 1, currentSystem );
            }

            filename = sb.toString();
        }

        return filename;
    }

    /**
     * Make sure that the parent directories of the given filename exist.
     *
     * @param filename      The filename for which we want to make sure that the parent directories are created.
     * @param fileSeparator The file separator in use in that filename.
     *
     * @return true if the parent directories exist, false otherwise.
     */
    public static boolean createParentDirectories( String filename, String fileSeparator ) {
        boolean success = true;

        int index = filename.lastIndexOf( fileSeparator );
        String path = filename.substring( 0, index );

        // create all parent directories
        File pathFile = new File( path );

        if ( ! pathFile.exists() ) {
            System.out.println( path + " doesn't exist yet, creating it..." );
            success = new File( path ).mkdirs();
            if ( ! success ) {
                System.out.println( "ERROR: Could not create " + path );
            } else {
                System.out.println( path + " was created successfully." );
            }
        }

        return success;
    }

    /**
     * Check and create all missing parent directory.
     *
     * @param filename the filename to check.
     */
    public static void checkParentDirectory( String filename ) {
        // check that all parent directory exist in the given filename
        // 1, change SLASH by the
        if ( filename.indexOf( FileGenerator.SLASH ) != -1 ) {

            // 2. replace all slash by plateform specific file separator (if necessary)
            final String separator = System.getProperty( "file.separator" );
            if ( ! FileGenerator.SLASH.equals( separator ) ) {
                StringBuffer sb = new StringBuffer( filename );

                int idx;
                while ( ( idx = sb.indexOf( FileGenerator.SLASH ) ) != -1 ) {
                    sb.replace( idx, idx + 1, separator );
                }

                filename = sb.toString();
            }

            // 3. make sure that the parent directories exists before to create the file.
            createParentDirectories( filename, separator );
        }
    }

    /**
     * Holds the result of removeChunkFlag.
     */
    public static class ChunkSize {
        private String cleanedString;
        private int chunkSize;

        /**
         * Forbids no-arg constructor.
         */
        private ChunkSize() {
        }

        /**
         * Constructs a ChunkSize Object.
         * @param cleanedString
         * @param chunkSize
         */
        public ChunkSize( String cleanedString, int chunkSize ) {
            this.cleanedString = cleanedString;
            this.chunkSize = chunkSize;
        }

        /**
         * Returns the cleaned filename.
         * @return
         */
        public String getCleanedString() {
            return cleanedString;
        }

        /**
         * Returns the Chunk size.
         * @return chunk size as an int.
         */
        public int getChunkSize() {
            return chunkSize;
        }
    }

    /**
     * Remove the chunk size flag from a String.
     *
     * @param filename the String to update
     *
     * @return the same string stripped out of the chunk size flag.
     */
    public static ChunkSize removeChunkFlag( String filename ) {

        // check if filename contains [integer], if so, remove it and use the integer value to split the unique
        // experiment into chunks of that size.
        int startIdx = filename.indexOf( FileHelper.START_CHUNK_FLAG );
        int stopIdx = filename.indexOf( FileHelper.STOP_CHUNK_FLAG );
        int chunkSize = -1;

        if ( startIdx != -1 && stopIdx != -1 ) {
            // found [ and ]
            String chunkSizeStr = filename.substring( startIdx + 1, stopIdx );

            try {
                chunkSize = Integer.parseInt( chunkSizeStr );
                if ( chunkSize < 1 ) {
                    System.err.println( "Chunk size (" + chunkSize + ") was incorrect, set it to default (" + 2500 + ")" );
                    chunkSize = 2500;
                }

                // replace the original filename from which we remove the [123]
                String tmp = filename.substring( 0, startIdx ) + filename.substring( stopIdx + 1, filename.length() );
                System.out.println( "Replacing filename: '" + filename + "' -> '" + tmp + "'" );
                filename = tmp;

                System.out.println( "chunkSize = " + chunkSize );

            } catch ( NumberFormatException e ) {
                throw new IllegalArgumentException( "filename " + filename + " has a specified chunk size that is not of type integer: " + chunkSizeStr + "" );
            }

        } else if ( startIdx != -1 || stopIdx != -1 ) {
            // found [ xor ]
            throw new IllegalArgumentException( "filename " + filename + " has an incorrect format (eg. abc[11])." );
        }

        return new ChunkSize( filename, chunkSize );
    }
}