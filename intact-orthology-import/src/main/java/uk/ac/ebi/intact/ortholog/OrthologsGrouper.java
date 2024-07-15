package uk.ac.ebi.intact.ortholog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OrthologsGrouper {
    public static void main(String[] args) {
        String filePath = "/Users/susiehuget/Desktop/Orthologs/test.txt";
        Map<String, String> uniprotAndPanther = parseFile(filePath);
        uniprotToProtein(uniprotAndPanther);

    }

    private static void uniprotToProtein(Map<String, String> uniprotAndPanther) {
        for (Map.Entry<String, String> entry : uniprotAndPanther.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }

    private static Map<String, String> parseFile(String filePath) {
        Pattern uniprotKBRegex = Pattern.compile("UniProtKB=([A-Z0-9]+)");
        Pattern pantherRegex = Pattern.compile("PTHR\\d+");
        Map<String, String> uniprotAndPTHR = new HashMap<>();

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
        return uniprotAndPTHR;
    }


}
