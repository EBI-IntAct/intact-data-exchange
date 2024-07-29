package uk.ac.ebi.intact.ortholog;

import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.zip.GZIPInputStream;

@RequiredArgsConstructor
public class OrthologsFileReader{

    String urlToDB;
    String dataExtractedPath;

    public OrthologsFileReader(String urlPanther, String filePath) {
        urlToDB = urlPanther;
        dataExtractedPath = filePath;
    }


    public static void decompressGzip(String url, String filePath) throws IOException {
        URL gzipUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) gzipUrl.openConnection();
        int responseCode = connection.getResponseCode();
        Instant startTime = Instant.now();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Connected to URL");
            try (InputStream in = connection.getInputStream();
                 GZIPInputStream gis = new GZIPInputStream(in);
                 TarArchiveInputStream tis = new TarArchiveInputStream(gis)) {
                System.out.println("Decompressing...");
                while (tis.getNextTarEntry() != null) {
                    File outputFile = new File(filePath);
                    try (FileOutputStream fos = new FileOutputStream(outputFile, false);
                         // the false make it write over existing data
                         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = tis.read(buffer)) != -1) {
                            bos.write(buffer, 0, len);
                        }
                    }
                }
                System.out.println("File decompressed, data in " + filePath);
                Instant endTime = Instant.now();
                System.out.println("Processing time:" + Duration.between(startTime, endTime));
            }
            finally {
                connection.disconnect();
                System.out.println("Disconnected from URL");
            }
        }
        else {
            System.out.println("GZIP returned unexpected response: " + responseCode);
        }
    }
}
