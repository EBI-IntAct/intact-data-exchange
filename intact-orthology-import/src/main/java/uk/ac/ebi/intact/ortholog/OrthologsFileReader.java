package uk.ac.ebi.intact.ortholog;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.zip.GZIPInputStream;

public class OrthologsFileReader{

    public static void decompressGzip(String url, String filePath) throws IOException {
        URL gzipUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) gzipUrl.openConnection();
        int responseCode = connection.getResponseCode();
        Instant startTime = Instant.now();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Connected to URL.");
            try (InputStream in = connection.getInputStream();
                 GZIPInputStream gis = new GZIPInputStream(in);
                 TarArchiveInputStream tis = new TarArchiveInputStream(gis)) {
                System.out.println("Decompressing...");
                while (tis.getNextTarEntry() != null) {
                    File outputFile = new File(filePath);
                    try (FileOutputStream fos = new FileOutputStream(outputFile, false)) {
                        // the false make it write over existing data
                        IOUtils.copy(tis, fos);
                    }
                }
                System.out.println("File decompressed, data in " + filePath);
                Instant endTime = Instant.now();
                System.out.println("Processing time:" + Duration.between(startTime, endTime));
            }
            finally {
                connection.disconnect();
                System.out.println("Disconnected from URL.");
            }
        }
        else {
            System.out.println("GZIP returned unexpected response: " + responseCode);
        }
    }
}