/*
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dataexchange.psimi.exporter.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.*;


/**
 * Various IntAct related utilities. If a pice of code is used more
 * than once, but does not really belong to a specific class, it
 * should become part of this class.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk) et at.
 * @version $Id$
 */
public class CompressionUtils {

    private CompressionUtils() {
        // no instantiable
    }

    /**
     * Compresses a file using GZIP
     * @param sourceFile the file to compress
     * @param destFile the zipped file
     * @param deleteOriginalFile if true, the original file is deleted and only the gzipped file remains
     * @throws java.io.IOException thrown if there is a problem finding or writing the files
     */
    public static void gzip(File sourceFile, File destFile, boolean deleteOriginalFile) throws IOException
    {
        // Create the GZIP output stream
        GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(destFile));

        // Open the input file
        FileInputStream in = new FileInputStream(sourceFile);

        try{
            // Transfer bytes from the input file to the GZIP output stream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
        finally {
            in.close();

            // Complete the GZIP file
            out.finish();
            out.close();
        }

        if (deleteOriginalFile)
        {
            sourceFile.delete();
        }
    }

    public static void zip( File[] sourceFiles, File destFile, boolean deleteOriginalFiles ) throws IOException {
        zip( sourceFiles, destFile, deleteOriginalFiles, false );

    }

    /**
     * Compresses a file (or directory) using GZIP
     *
     * @param sourceFiles         the files to include in the zip
     * @param destFile the zipped file
     * @param deleteOriginalFiles if true, the original file is deleted and only the gzipped file remains
     * @param includeFullPathName if true, then zip file is given full path name, if false, only the file name
     * @throws java.io.IOException thrown if there is a problem finding or writing the files
     */
    public static void zip( File[] sourceFiles, File destFile, boolean deleteOriginalFiles, boolean includeFullPathName ) throws IOException {

        // Create a buffer for reading the files
        byte[] buf = new byte[1024];

        // Create the ZIP file
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destFile));

        try{
            // Compress the files
            for (File sourceFile : sourceFiles)
            {
                if (sourceFile.isDirectory()){
                    addFolderToZip("", sourceFile.getAbsolutePath(), out, includeFullPathName);
                }
                else {
                    FileInputStream in = new FileInputStream(sourceFile);

                    try{
                        // Add ZIP entry to output stream.
                        if ( includeFullPathName ) {
                            out.putNextEntry( new ZipEntry( sourceFile.toString() ) );
                        } else {
                            out.putNextEntry( new ZipEntry( sourceFile.getName() ) );
                        }

                        // Transfer bytes from the file to the ZIP file
                        int len;
                        while ((len = in.read(buf)) > 0)
                        {
                            out.write(buf, 0, len);
                        }
                    }
                    finally {
                        // Complete the entry
                        out.closeEntry();
                        in.close();
                    }
                }
            }

            if (deleteOriginalFiles)
            {
                for (File sourceFile : sourceFiles)
                {
                    sourceFile.delete();
                }
            }
        }
        finally {
            // Complete the ZIP file
            out.close();
        }
    }

    static private void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws IOException {

        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip, false);
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            try{
                zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
            }
            finally {
                in.close();
            }
        }
    }

    /**
     * Zip the subdirectory and exclude already zipped files
     * @param path
     * @param srcFolder
     * @param zip
     * @param includeFullPath
     * @throws java.io.IOException
     */
    static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip, boolean includeFullPath) throws IOException {
        File folder = new File(srcFolder);

        for (String fileName : folder.list()) {
            if (path.equals("") && !fileName.endsWith(".zip")) {
                if (includeFullPath){
                    addFileToZip(folder.toString(), srcFolder + "/" + fileName, zip);
                }
                else {
                    addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
                }
            } else if (!fileName.endsWith(".zip")) {
                addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
            }
        }
    }

    /**
     * Uncompress gzipped files
     * @param gzippedFile The file to uncompress
     * @param destinationFile The resulting file
     * @throws java.io.IOException thrown if there is a problem finding or writing the files
     */
    public static void gunzip(File gzippedFile, File destinationFile) throws IOException
    {
        int buffer = 2048;

        FileInputStream in = new FileInputStream(gzippedFile);
        GZIPInputStream zipin = new GZIPInputStream(in);

        byte[] data = new byte[buffer];

        // decompress the file
        FileOutputStream out = new FileOutputStream(destinationFile);
        try{
            int length;
            while ((length = zipin.read(data, 0, buffer)) != -1)
                out.write(data, 0, length);
        }
        finally {
            out.close();

            zipin.close();
            in.close();
        }

    }

    /**
     * Uncompresses zipped files
     * @param zippedFile The file to uncompress
     * @return list of unzipped files
     * @throws java.io.IOException thrown if there is a problem finding or writing the files
     */
    public static List<File> unzip(File zippedFile) throws IOException
    {
        return unzip(zippedFile, null);
    }

    /**
     * Uncompresses zipped files
     * @param zippedFile The file to uncompress
     * @param destinationDir Where to put the files
     * @return  list of unzipped files
     * @throws java.io.IOException thrown if there is a problem finding or writing the files
     */
    public static List<File> unzip(File zippedFile, File destinationDir) throws IOException
    {
        int buffer = 2048;

        List<File> unzippedFiles = new ArrayList<File>();

        BufferedOutputStream dest;
        BufferedInputStream is;
        ZipEntry entry;
        ZipFile zipfile = new ZipFile(zippedFile);
        Enumeration e = zipfile.entries();
        while (e.hasMoreElements())
        {
            entry = (ZipEntry) e.nextElement();

            is = new BufferedInputStream
                    (zipfile.getInputStream(entry));
            int count;
            byte data[] = new byte[buffer];

            File destFile;

            if (destinationDir != null)
            {
                destFile = new File(destinationDir, entry.getName());
            }
            else
            {
                destFile = new File(entry.getName());
            }

            FileOutputStream fos = new FileOutputStream(destFile);
            dest = new
                    BufferedOutputStream(fos, buffer);
            try{
                while ((count = is.read(data, 0, buffer))
                        != -1)
                {
                    dest.write(data, 0, count);
                }


                unzippedFiles.add(destFile);
            }
            finally {
                dest.flush();
                dest.close();
                is.close();
            }
        }

        return unzippedFiles;
    }


}