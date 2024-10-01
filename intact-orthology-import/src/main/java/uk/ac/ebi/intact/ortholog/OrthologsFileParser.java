package uk.ac.ebi.intact.ortholog;

import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j
public class OrthologsFileParser {

    private static final Pattern UNIPROT_KB_REGEX = Pattern.compile("UniProtKB=([A-Z0-9]+)");
    private static final Pattern PANTHER_REGEX = Pattern.compile("PTHR\\d+");

    public static void parseFileAndSave(String inputFilePath, String outputDirPath) throws IOException {
        File outputDir = new File(outputDirPath);
        // First, we empty de directory to start clean
        if (outputDir.exists()) {
            log.info("Deleting previous data...");
            FileUtils.deleteDirectory(outputDir);
            log.info("Previous data deleted.");
        }
        outputDir.mkdirs();
        long linesRead = 0;

        log.info("Parsing file...");

        // First we store all matches in a map to ensure there's no duplication
        Map<String, Set<String>> uniprotAndPTHR = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                linesRead++;
                ArrayList<String> uniprotMatches = new ArrayList<>();

                Matcher uniprotMatcher = UNIPROT_KB_REGEX.matcher(line);
                Matcher pantherMatcher = PANTHER_REGEX.matcher(line);

                while (uniprotMatcher.find()) {
                    uniprotMatches.add(uniprotMatcher.group(1));
                }
                while (pantherMatcher.find()) {
                    for (String uniprotMatch : uniprotMatches) {
                        uniprotAndPTHR.putIfAbsent(uniprotMatch, new HashSet<>());
                        uniprotAndPTHR.get(uniprotMatch).add(pantherMatcher.group());
                    }
                }

                if (linesRead % 250_000 == 0) {
                    log.info(linesRead + " lines read, " + uniprotAndPTHR.size() + " proteins read");
                }
            }
        }

        log.info(linesRead + " lines read, " + uniprotAndPTHR.size() + " proteins read");
        log.info("File parsed.");

        log.info("Saving map to files...");

        // Then, we write all the files
        long uniprotAndPantherCount = 0;
        Set<String> filenames = new HashSet<>();
        for (String uniprotMatch : uniprotAndPTHR.keySet()) {
            for (String pantherMatch : uniprotAndPTHR.get(uniprotMatch)) {
                // We just use the first 2 characters for the file name to have multiple proteins per file and fewer files overall
                String uniprotIdPrefix = uniprotMatch.substring(0, 2);
                writePair(outputDir.toPath(), uniprotIdPrefix, uniprotMatch, pantherMatch);
                filenames.add(uniprotIdPrefix);
            }
            uniprotAndPantherCount += uniprotAndPTHR.get(uniprotMatch).size();
            if (uniprotAndPantherCount % 25_000 == 0) {
                log.info(uniprotAndPantherCount + " proteins saved in " + filenames.size() + " files");
            }
        }

        log.info("All protein files saved.");
        log.info("Number of Panther identifiers: " + uniprotAndPantherCount);
        log.info("Number of files: " + filenames.size());
    }

    private static void writePair(Path dirPath, String filename, String uniprotId, String pantherId) throws IOException {

        Path filePath = dirPath.resolve(filename);
        try (FileWriter fileWriter = new FileWriter(filePath.toFile(), true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(uniprotId + "," + pantherId);
            bufferedWriter.newLine();
        }
    }
}