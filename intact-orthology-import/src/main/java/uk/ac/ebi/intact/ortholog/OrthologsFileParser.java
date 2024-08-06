package uk.ac.ebi.intact.ortholog;

import lombok.extern.log4j.Log4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
}