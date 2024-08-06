package uk.ac.ebi.intact.ortholog;

import lombok.extern.log4j.Log4j;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import javax.annotation.Resource;
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

        int counter = 0;

        for (IntactProtein protein : intactProteins) {
            String pantherId = uniprotIdAndPanther.get(protein.getUniprotkb());
            if (pantherId != null) {
                counter += 1;
                intactProteinAndPanther.put(protein, pantherId);
                System.out.println(protein.getUniprotkb() + " -> " + pantherId + " index = " + counter);
            }
        }
        log.info("Number of protein associated to Panther identifier: " + intactProteinAndPanther.size());
        return intactProteinAndPanther;
    }
}