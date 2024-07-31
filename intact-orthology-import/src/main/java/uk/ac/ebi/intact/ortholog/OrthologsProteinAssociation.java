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

    public Map<Collection <IntactProtein>, String> associateProteinToPantherId(Map<String,String> uniprotIdAndPanther) {
        Map<Collection <IntactProtein>, String> intactProteinAndPanther = new HashMap<>();
        for (Map.Entry<String,String> entry : uniprotIdAndPanther.entrySet()) {
            Collection <IntactProtein> intactProtein = this.intactDao.getProteinDao().getByXref(entry.getKey());
            if (!intactProtein.isEmpty()) {
                intactProteinAndPanther.put(intactProtein, entry.getValue());
            }
        }
        return intactProteinAndPanther;
    }

//    public Collection<IntactProtein> getIntactProtein(){
//        System.out.println("Fetching Intact Proteins...");
//        return intactDao.getProteinDao().getAll();
//    }

//    public Map<IntactProtein, String> associateAllProteinsToPantherId(Map<String, String> uniprotIdAndPanther, Collection<IntactProtein> intactProteins) {
//        System.out.println("Associating Intact proteins to Panther identifier...");
//        Map<IntactProtein, String> intactProteinAndPanther = new HashMap<>();
//            for (Iterator<IntactProtein> iterator = intactProteins.iterator(); iterator.hasNext();){
//                System.out.println(iterator.next().getUniprotkb());
//                System.out.println(iterator.next().getIdentifiers());
//                System.out.println(uniprotIdAndPanther.get(iterator.next().getUniprotkb()));
//                if (uniprotIdAndPanther.get(iterator.next().getUniprotkb()) != null){
//                    intactProteinAndPanther.put(iterator.next(),uniprotIdAndPanther.get(iterator.next().getUniprotkb()));
//                    System.out.println(iterator.next() + " -> " + uniprotIdAndPanther.get(iterator.next().getUniprotkb()));
//                }
//
//                System.out.println(iterator.next().getUniprotkb());
//                if (entry.getValue().equals(iterator.next().getUniprotkb())){
//                    intactProteinAndPanther.put(iterator.next(), entry.getValue());
//                    System.out.println(iterator.next() + " -> " + entry.getValue());
//                }
//            }
//        System.out.println("Intact proteins associated to Panther identifier.");
//        return intactProteinAndPanther;
//    }
}
