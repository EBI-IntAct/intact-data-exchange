package uk.ac.ebi.intact.ortholog;

import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;

import javax.annotation.Resource;
import java.util.*;
import java.util.logging.Logger;

public class OrthologsProteinAssociation {

    private static final Logger logger = Logger.getLogger(OrthologsProteinAssociation.class.getName());

    @Resource(name="intactDao")
    private final IntactDao intactDao;

    public OrthologsProteinAssociation(IntactDao intactDao) {
        this.intactDao = intactDao;
    }

    public Collection<IntactProtein> getIntactProtein(){
        logger.info("Fetching all Intact Proteins...");
        return intactDao.getProteinDao().getAll();
    }

    public Collection<IntactProtein> getSpecificIntactProtein(String proteinId) {
        return intactDao.getProteinDao().getByXref(proteinId);
    }

    // Method below are just for testing

    public Collection<IntactProtein> getFewIntactProtein() {
        logger.info("Fetching few Intact Proteins...");
        List<IntactProtein> fewIntactProteins = new ArrayList<>();
        List<String> proteinsToFetch = Arrays.asList("P38153", "Q01217", "P38116", "P32449", "Q12406", "P43561", "P15646", "P43561", "P17710", "P19659");

        var proteinDao = intactDao.getProteinDao();
        for (String proteinToFetch : proteinsToFetch) {
            fewIntactProteins.addAll(proteinDao.getByXref(proteinToFetch));
        }
        return fewIntactProteins;
    }

    public Map<IntactProtein, String> associateAllProteinsToPantherId(Map<String, String> uniprotIdAndPanther, Collection<IntactProtein> intactProteins) {
        logger.info("Associating Intact proteins to Panther identifier...");
        Map<IntactProtein, String> intactProteinAndPanther = new HashMap<>();

        for (IntactProtein protein : intactProteins) {
            String pantherId = uniprotIdAndPanther.get(protein.getUniprotkb());
            if (pantherId != null) {
                intactProteinAndPanther.put(protein, pantherId);
                System.out.println(protein.getUniprotkb() + " -> " + pantherId);
            }
        }
        logger.info("Intact proteins associated to Panther identifier.");
        return intactProteinAndPanther;
    }
}