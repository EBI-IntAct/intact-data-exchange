package uk.ac.ebi.intact.ortholog;

import lombok.extern.log4j.Log4j;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j
public class OrthologsProteinAssociation {

    @Resource(name="intactDao")
    private final IntactDao intactDao;

    public OrthologsProteinAssociation(IntactDao intactDao) {
        this.intactDao = intactDao;
    }

    public Collection<IntactProtein> getIntactProtein(){
        log.info("Fetching all Intact Proteins...");
        return intactDao.getProteinDao().getAll();
    }

    // Method below is just for testing

    public Collection<IntactProtein> getFewIntactProtein() {
        log.info("Fetching few Intact Proteins...");
        List<IntactProtein> fewIntactProteins = new ArrayList<>();
        List<String> proteinsToFetch = Arrays.asList("P38153", "Q01217", "P38116", "P32449", "Q12406", "P43561", "P15646", "P43561", "P17710", "P19659");

        var proteinDao = intactDao.getProteinDao();
        for (String proteinToFetch : proteinsToFetch) {
            fewIntactProteins.addAll(proteinDao.getByXref(proteinToFetch));
        }
        return fewIntactProteins;
    }

    public Map<IntactProtein, String> associateAllProteinsToPantherId(Map<String, String> uniprotIdAndPanther, Collection<IntactProtein> intactProteins) {
        log.info("Associating Intact proteins to Panther identifier...");
        Map<IntactProtein, String> intactProteinAndPanther = new HashMap<>();
//        List<IntactProtein> intactProteinList = new ArrayList<>(intactProteins);

        int batchSize = 1000;

        AtomicInteger counter = new AtomicInteger(0);
        Stream<IntactProtein> intactProteinStream = intactProteins.stream();
        Stream<List<IntactProtein>> batches = batchStream(intactProteinStream, batchSize);

        batches.forEach(batch -> {
            for (int i = 0; i < batchSize; i++) {
                counter.addAndGet(1);
                IntactProtein protein = batch.get(i);
                String proteinId = protein.getUniprotkb();
                String pantherId = uniprotIdAndPanther.get(proteinId);
                if (pantherId != null) {
                    intactProteinAndPanther.put(protein, pantherId);
                    dataWriter(proteinId + "," + pantherId + "," + intactProteinAndPanther.size() + "," + counter);
                }
            }
            log.info("Finished processing batch, total processed proteins :" + intactProteinAndPanther.size());
        });

//        int counter = 0;
//        int index = 0;
//        int nProteinProcessed = 100; // to avoid log at each processed protein
//
//        String[] lastSave= fetchFromStopped();
//
//        if (lastSave != null){
//            index = Integer.parseInt(lastSave[3]);
//            counter = Integer.parseInt(lastSave[2]);
//        }
//
//        for (int i = index; i < intactProteinList.size(); i++) {
//            IntactProtein protein = intactProteinList.get(i);
//            String proteinId = protein.getUniprotkb();
//            String pantherId = uniprotIdAndPanther.get(proteinId);
//            if (pantherId != null) {
//                counter += 1;
////                intactProteinAndPanther.put(protein, pantherId);
//                // is the system.out creating the memory issue?
//                if ((counter) % nProteinProcessed == 0) {
//                    log.info("Protein processed: " + counter );
//                }
//                dataWriter(proteinId + "," + pantherId + "," + counter + "," + i + "\n");
//            }
//        }
//        log.info("Number of protein associated to Panther identifier: " + counter);
        return intactProteinAndPanther;
    }

     public Stream<List<IntactProtein>> batchStream(Stream<IntactProtein> proteinStream, int batchSize) {
        List<IntactProtein> proteins = proteinStream.collect(Collectors.toList());
        int size = proteins.size();
        int numberOfBatches = (size + batchSize - 1) / batchSize;

        return Stream.iterate(0, n -> n + 1)
                .limit(numberOfBatches)
                .map(i -> proteins.subList(i * batchSize, (i + 1) * batchSize));

     }

    public String[] fetchFromStopped() {
     String line = dataReader();
     if (line != null) {
         return line.split(",");
     }
     return null;
    }

    public Map<IntactProtein, String> associateOneProteinToPantherID(Map<String, String> proteinAndPanther, IntactProtein protein){
        String proteinId = protein.getUniprotkb();
        String pantherId = proteinAndPanther.get(proteinId);
        Map<IntactProtein, String> intactProteinAndPantherId = new HashMap<>();
        if (pantherId != null) {
            intactProteinAndPantherId.put(protein, pantherId);
            dataWriter(proteinId + "," + pantherId);
            log.info(proteinId + "," + pantherId);
            return intactProteinAndPantherId;
        }
        return intactProteinAndPantherId;
    }

    public String dataReader() {
        String lastLine = null;
        try (BufferedReader reader = new BufferedReader(new FileReader("proteinAndPanther.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    lastLine = line;
                }
            }
            reader.close();
            return lastLine;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void dataWriter(String toWrite) {
        try (FileWriter fileWriter = new FileWriter("proteinAndPantherBatches.txt", true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
             bufferedWriter.write(toWrite);
             bufferedWriter.newLine(); // Optionally add a newline after the text

        } catch (IOException e) {
            e.printStackTrace(); // Print the stack trace to help with debugging
        }
    }

    public static String associateOneProteinToPantherId(String filePath, IntactProtein protein) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String proteinId = parts[0];
                    if (proteinId.equals(protein.getUniprotkb())) {
                        return parts[1];
                    }

                }
            }
        }
        return null;
    }
}