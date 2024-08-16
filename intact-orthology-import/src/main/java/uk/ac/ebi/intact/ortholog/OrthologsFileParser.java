package uk.ac.ebi.intact.ortholog;

import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j
public class OrthologsFileParser {

    public static Map<String, String> parseFile(String filePath) {
        Pattern uniprotKBRegex = Pattern.compile("UniProtKB=([A-Z0-9]+)");
        Pattern pantherRegex = Pattern.compile("PTHR\\d+");
        Map<String, String> uniprotAndPTHR = new HashMap<>();
        log.info("Parsing file...");

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
        log.info("File parsed.");
        log.info("Number of Panther identifiers: " + uniprotAndPTHR.size());
        return uniprotAndPTHR;
    }

    public static void parseFileAndSave(String inputFilePath, String outputDirPath) throws IOException {
        Pattern uniprotKBRegex = Pattern.compile("UniProtKB=([A-Z0-9]+)");
        Pattern pantherRegex = Pattern.compile("PTHR\\d+");
        long uniprotAndPantherCount = 0;
        log.info("Parsing file...");

        File outputDir = new File(outputDirPath);
        // First, we empty de directory to start clean
        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }
        outputDir.mkdirs();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                ArrayList<String> uniprotMatches = new ArrayList<>();

                Matcher uniprotMatcher = uniprotKBRegex.matcher(line);
                Matcher pantherMatcher = pantherRegex.matcher(line);

                while (uniprotMatcher.find()) {
                    uniprotMatches.add(uniprotMatcher.group(1));
                }
                while (pantherMatcher.find()) {
                    for (String uniprotMatch : uniprotMatches) {
                        writePair(outputDir.toPath(), uniprotMatch, pantherMatcher.group());
                        uniprotAndPantherCount += uniprotMatches.size();
                    }
                }
            }
        }

        log.info("File parsed.");
        log.info("Number of Panther identifiers: " + uniprotAndPantherCount);
    }

    private static void writePair(Path dirPath, String uniprotId, String pantherId) throws IOException {
        Path filePath = dirPath.resolve(uniprotId);
        try (FileWriter fileWriter = new FileWriter(filePath.toFile(), true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(uniprotId + "," + pantherId);
            bufferedWriter.newLine(); // Optionally add a newline after the text
        }
    }
}