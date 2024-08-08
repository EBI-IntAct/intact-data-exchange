package uk.ac.ebi.intact.ortholog;

import lombok.extern.log4j.Log4j;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import javax.annotation.Resource;
import java.io.*;
import java.util.*;

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

    // Method below are just for testing

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
        List<IntactProtein> intactProteinList = new ArrayList<>(intactProteins);

        int counter = 0;
        int index = 0;
        String[] lastSave= fetchFromStopped();

        if (lastSave != null){
            index = Integer.parseInt(lastSave[3]);
            counter = Integer.parseInt(lastSave[2]);
        }

        for (int i = index; i < intactProteinList.size(); i++) {
            IntactProtein protein = intactProteinList.get(i);
            String proteinId = protein.getUniprotkb();
            String pantherId = uniprotIdAndPanther.get(proteinId);
            if (pantherId != null) {
                counter += 1;
//                intactProteinAndPanther.put(protein, pantherId);
                System.out.println(proteinId + " -> " + pantherId + " index = " + counter);
                dataWriter(proteinId + "," + pantherId + "," + counter + "," + i + "\n");
            }
        }
        log.info("Number of protein associated to Panther identifier: " + counter);
        return intactProteinAndPanther;
    }

    public String[] fetchFromStopped() {
     String line = dataReader();
     if (line != null) {
         return line.split(",");
     }
     return null;
    }

    public static String dataReader() {
        String lastLine = null;
        try (BufferedReader reader = new BufferedReader(new FileReader("proteinAndPanther.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    lastLine = line;
                }
            }
            return lastLine;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void dataWriter(String toWrite){
        try {
            FileWriter fileWriter = new FileWriter("proteinAndPanther.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(toWrite);
            bufferedWriter.close();
        }
        catch (Exception e) {
            e.getStackTrace();
        }
    }
}