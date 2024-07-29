package uk.ac.ebi.intact.ortholog;

import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrthologsProteinAssociation {

    private final IntactDao intactDao;

    public OrthologsProteinAssociation(IntactDao intactDao) {
        this.intactDao = intactDao;
    }

    public Collection<IntactProtein> getIntactProtein(){
        String proteinID = "P77650";
//        Collection<IntactProtein> proteins = intactDao.getProteinDao().getAll();
//        return intactDao.getProteinDao().getAll();
        return (Collection<IntactProtein>) intactDao.getProteinDao().getByAc(proteinID);
    }

    public Map<IntactProtein, String> associateProteinToPantherIndex(Map<String, String> uniprotAndPanther, List<IntactProtein> intactProteins) {
        Map<IntactProtein, String> intactProteinAndPanther = new HashMap<>();
        for (IntactProtein intactProtein : intactProteins) {
            for (Map.Entry<String, String> entry : uniprotAndPanther.entrySet()){
                if (entry.getValue().equals(intactProtein.getAc())){
                    intactProteinAndPanther.put(intactProtein, entry.getValue());
                }
            }
        }
        return intactProteinAndPanther;
    }
}
