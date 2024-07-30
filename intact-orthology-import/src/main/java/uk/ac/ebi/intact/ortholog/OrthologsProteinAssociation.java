package uk.ac.ebi.intact.ortholog;

import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import javax.annotation.Resource;
import java.util.*;



public class OrthologsProteinAssociation {

    @Resource(name="intactDao")
    private final IntactDao intactDao;

    public OrthologsProteinAssociation(IntactDao intactDao) {
        this.intactDao = intactDao;
    }

    public Collection<IntactProtein> getIntactProtein(){

        //TODO: see if it is possible to fetch by UniprotKB to not have to fetch the whole db everytime

        System.out.println("Fetching Intact Proteins...");
        return intactDao.getProteinDao().getAll();
    }

    public Map<IntactProtein, String> associateProteinToPantherIndex(Map<String, String> uniprotIdAndPanther, Collection<IntactProtein> intactProteins) {
        System.out.println("Associating Intact proteins to Panther identifier...");
        Map<IntactProtein, String> intactProteinAndPanther = new HashMap<>();
        for (Map.Entry<String, String> entry : uniprotIdAndPanther.entrySet()){
            System.out.println("PANTHER " + entry.getKey() + ": " + entry.getValue());
            for (Iterator<IntactProtein> iterator = intactProteins.iterator(); iterator.hasNext();){
                //TODO see why the uniprotKB cannot be used
                System.out.println(iterator.next().getUniprotkb());
                if (entry.getValue().equals(iterator.next().getUniprotkb())){
                    intactProteinAndPanther.put(iterator.next(), entry.getValue());
                    System.out.println(iterator.next() + " -> " + entry.getValue());
                }
            }
        }
        System.out.println("Intact proteins associated to Panther identifier.");
        return intactProteinAndPanther;
    }
}
