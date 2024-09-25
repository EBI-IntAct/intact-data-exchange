package uk.ac.ebi.intact.ortholog;

import lombok.extern.log4j.Log4j;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

@Log4j
public class OrthologsFileReader{

    public static void decompressGzip(String url, String filePath) throws IOException {
        URL gzipUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) gzipUrl.openConnection();
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            log.info("Connected to URL.");
            try (InputStream in = connection.getInputStream();
                 GZIPInputStream gis = new GZIPInputStream(in);
                 TarArchiveInputStream tis = new TarArchiveInputStream(gis)) {
                log.info("Decompressing...");
                while (tis.getNextTarEntry() != null) {
                    File outputFile = new File(filePath);
                    try (FileOutputStream fos = new FileOutputStream(outputFile, false)) {
                        // the false make it write over existing data
                        IOUtils.copy(tis, fos);
                    }
                }
                log.info("File decompressed, data in " + filePath);
            }
            finally {
                connection.disconnect();
                log.info("Disconnected from URL.");
            }
        }
        else {
            log.info("GZIP returned unexpected response: " + responseCode);
        }
    }
}