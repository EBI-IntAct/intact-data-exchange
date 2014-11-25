/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.exporter.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import uk.ac.ebi.intact.commons.util.CompressionUtils;

import java.io.*;
import java.util.List;

/**
 * A class that can compress files.
 *
 */
public class Compressor {

    private String compression;

    public Compressor() {
        compression = "zip";
    }

    public void compress(File outputFile, List<File> filesToCompress, boolean deleteFilesToCompress) throws IOException {
         if ("tar".equals(compression)) {
             tar(outputFile, filesToCompress);
         } else if ("tar.gz".equals(compression)) {
             File tarFile = new File(outputFile.getParentFile(), outputFile.getName()+"-temp.tar");
             tar(tarFile, filesToCompress);
             gz(outputFile, tarFile, true);
         } else if ("gz".equals(compression)) {
             for (File file : filesToCompress) {
                File outFile = new File(file.getParentFile(), file.getName()+".gz");
                gz(outFile, file, deleteFilesToCompress);
             }
         } else if ("zip".equals(compression)) {
             zip(outputFile, filesToCompress);
         } else {
             throw new IllegalArgumentException("Compression cannot be handled: "+compression+", available compressions: tar, tar.gz, gz, zip");
         }

        if (deleteFilesToCompress) {
            for (File fileToDelete : filesToCompress) {
                FileUtils.forceDelete(fileToDelete);
            }
        }
    }

    public void uncompress(File fileToUncompress, File destinationDir, boolean deleteCompressedFile) throws IOException {

         if ("tar".equals(compression)) {
             untar(fileToUncompress, destinationDir);
         } else if ("tar.gz".equals(compression)) {
             String gunzippedFilename = fileToUncompress.getName().replaceAll(".gz", "");
             File gunzippedFile = new File(destinationDir, gunzippedFilename);
             gunzip(fileToUncompress, gunzippedFile);
             untar(gunzippedFile, destinationDir);
         } else if ("gz".equals(compression)) {
             String outFile = fileToUncompress.getName().replaceAll(".gz", "");
             gunzip(fileToUncompress, new File(destinationDir, outFile));
         } else if ("zip".equals(compression)) {
             unzip(fileToUncompress, destinationDir);
         } else {
             throw new IllegalArgumentException("Compression cannot be handled: "+compression+", available compressions: tar, tar.gz, gz, zip");
         }

        if (deleteCompressedFile) {
            FileUtils.forceDelete(fileToUncompress);
        }
    }

    private void tar(File outputFile, List<File> filesToCompress) throws IOException {
        OutputStream os = new FileOutputStream(outputFile);
        TarArchiveOutputStream tarOutput = new TarArchiveOutputStream(os);

        for (File fileToCompress : filesToCompress) {
            TarArchiveEntry entry = new TarArchiveEntry(fileToCompress.getName());
            entry.setSize(fileToCompress.length());
            tarOutput.putArchiveEntry(entry);
            IOUtils.copy(new FileInputStream(fileToCompress), tarOutput);
            tarOutput.closeArchiveEntry();
        }


        tarOutput.finish();
        tarOutput.close();
        os.close();
    }

    private void untar(File compressedFile, File destinationDir) throws IOException {
        InputStream is = new FileInputStream(compressedFile);
        TarArchiveInputStream tais = new TarArchiveInputStream(is);

        ArchiveEntry te;

        while ((te = tais.getNextEntry()) != null) {
            File dest = new File(destinationDir, te.getName());

            FileOutputStream os = new FileOutputStream(dest);

            try {
                IOUtils.copy(tais, os);
            } finally {
                os.close();
            }
        }

        tais.close();
    }

    private void gz(File outputFile, File sourceFile, boolean deleteSourceFile) throws IOException {
        CompressionUtils.gzip(sourceFile, outputFile, deleteSourceFile);
    }

    private void gunzip(File compressedFile, File destinationFile) throws IOException {
        CompressionUtils.gunzip(compressedFile, destinationFile);
    }

    private void zip(File outputFile, List<File> filesToCompress) throws IOException {
        CompressionUtils.zip(filesToCompress.toArray(new File[filesToCompress.size()]), outputFile, false);
    }

    private void unzip(File compressedFile, File destinationDir) throws IOException {
        CompressionUtils.unzip(compressedFile, destinationDir);
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }
}
