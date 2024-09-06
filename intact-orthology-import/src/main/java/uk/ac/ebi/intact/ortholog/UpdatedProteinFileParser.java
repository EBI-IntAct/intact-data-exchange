package uk.ac.ebi.intact.ortholog;

import lombok.extern.log4j.Log4j;
import java.io.*;
import java.util.*;


@Log4j
public class UpdatedProteinFileParser {
    public static Collection<String> parseFile() {
        Collection <String> alreadyUpdatedProteins = new HashSet<>();
        log.info("Parsing already updated proteins file...");
        try (BufferedReader reader = new BufferedReader(new FileReader("UpdatedProteins.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                alreadyUpdatedProteins.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("File parsed.");
        log.info("Number of proteins already updated: " + alreadyUpdatedProteins.size());
        return alreadyUpdatedProteins;
    }
}