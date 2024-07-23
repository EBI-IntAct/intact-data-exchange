package uk.ac.ebi.intact.ortholog;

import lombok.RequiredArgsConstructor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;


import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.uniprot.UniprotProteinFetcher;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;

@RequiredArgsConstructor
public class OrthologsGrouper {

    private final UniprotProteinFetcher uniprotProteinFetcher;

    public static void main(String[] args) throws IOException, BridgeFailedException {
        String filePath = "orthologsData.txt";
        String urlPanther = "http://data.pantherdb.org/ftp/ortholog/current_release/AllOrthologs.tar.gz";
//        decompressGzip(urlPanther, filePath);
//        parseFile(filePath);
//        Map<String, String> uniprotAndPanther = parseFile(filePath);
//        uniprotToProtein(uniprotAndPanther);

        OrthologsGrouper orthologsGrouper = new OrthologsGrouper(new UniprotProteinFetcher());
        orthologsGrouper.getIntactProtein();
    }

    private static void uniprotToProtein(Map<String, String> uniprotAndPanther) throws BridgeFailedException {
        UniprotProteinFetcher proteinFetcher = new UniprotProteinFetcher();
        for (Map.Entry<String, String> entry : uniprotAndPanther.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }

    private static Map<String, String> parseFile(String filePath) {
        Pattern uniprotKBRegex = Pattern.compile("UniProtKB=([A-Z0-9]+)");
        Pattern pantherRegex = Pattern.compile("PTHR\\d+");
        Map<String, String> uniprotAndPTHR = new HashMap<>();
        Instant startTime = Instant.now();
        System.out.println("Parsing file...");

        try (
                BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                ArrayList<String> uniprotMatches = new ArrayList<>();

                Matcher uniprotMatcher = uniprotKBRegex.matcher(line);
                Matcher pantherMatcher = pantherRegex.matcher(line);

                while (uniprotMatcher.find()) {
                    uniprotMatches.add(uniprotMatcher.group(1));
                }
                while (pantherMatcher.find()) {
                    uniprotMatches.forEach(up -> uniprotAndPTHR.put(up, pantherMatcher.group()));
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Instant endTime = Instant.now();
        System.out.println("File parsed");
        System.out.println("Processing time:" + Duration.between(startTime, endTime));
        return uniprotAndPTHR;
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


        //code from UniplexComplexManager l.165

//    private void addPantherXref(UniplexCluster uniplexCluster, IntactComplex complex) throws CvTermNotFoundException {
//        for (String clusterId: uniplexCluster.getClusterIds()) {
//            InteractorXref xref = newHumapXref(clusterId);
//            complex.getIdentifiers().add(xref);
//        }
//
//        for (String uniprotId: protein.AC()) {
//            PantherXref xref = newPantherXref(pantherID);
//            protein.getIdentifiers.add(xref);
//        }
//
//    }
//
//    private InteractorXref newPantherXref(String id) throws CvTermNotFoundException {
//        IntactCvTerm database = findCvTerm(IntactUtils.DATABASE_OBJCLASS, HUMAP_DATABASE_ID);
//        // Currently we use identity as qualifier, as we are only importing exact matches.
//        // If we merge curated complexes with partial matches, we need to add a different qualifier (subset, see-also, etc.).
//        IntactCvTerm qualifier = findCvTerm(IntactUtils.QUALIFIER_OBJCLASS, Xref.IDENTITY_MI);
//        return new InteractorXref(database, id, qualifier);
//    }


    private IntactProtein getIntactProtein() throws BridgeFailedException {
        String proteinID = "Q8K305";
        return (IntactProtein) uniprotProteinFetcher.fetchByIdentifier(proteinID);
    }

}
