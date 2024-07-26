package uk.ac.ebi.intact.ortholog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;

import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.jami.synchronizer.listener.impl.DbSynchronizerStatisticsReporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;


public class OrthologsProteinAssociation {

    @Setter
    @Autowired
    private EntityManager entityManager;

    private final IntactDao intactDao;
    private DbSynchronizerStatisticsReporter synchronizerListener;

    public OrthologsProteinAssociation(IntactDao intactDao) {
        this.intactDao = intactDao;
//        this.intactDao.getSynchronizerContext().initialiseDbSynchronizerListener(synchronizerListener);

    }


    public List<IntactProtein> getIntactProtein(){
//        EntityManager manager = intactDao.getEntityManager();
//        intactDao.getSynchronizerContext().initialiseDbSynchronizerListener(synchronizerListener);

        String proteinID = "P77650";

        return intactDao.getProteinDao().getAll();
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
