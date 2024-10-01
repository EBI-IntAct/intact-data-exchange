package uk.ac.ebi.intact.ortholog;

import lombok.extern.log4j.Log4j;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import javax.annotation.Resource;
import javax.persistence.Query;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

@Log4j
public class OrthologsProteinAssociation {

    @Resource(name="intactDao")
    private final IntactDao intactDao;

    public OrthologsProteinAssociation(IntactDao intactDao) {
        this.intactDao = intactDao;
    }

    public List<Integer> getProteinAcs() {
        String sqlQuery = "select CAST(REPLACE(ac,'EBI-','') as integer) as numberAC from intact.ia_interactor p where category = 'protein' order by numberAC asc";
        Query query = intactDao.getEntityManager().createNativeQuery(sqlQuery);
        return query.getResultList();
    }

    public List<IntactProtein> fetchProteins(Integer startAc, Integer endAc) {
        String sqlQuery = "select p FROM IntactProtein p where CAST(REPLACE(ac,'EBI-','') as integer) BETWEEN :startAc and :endAc";
        Query query = intactDao.getEntityManager().createQuery(sqlQuery);
        query.setParameter("startAc", startAc);
        query.setParameter("endAc", endAc);
        return query.getResultList();
    }

    public static Collection<String> associateOneProteinToPantherIds(String dirPath, IntactProtein protein) throws IOException {
        String proteinAc = protein.getUniprotkb();
        List<String> pantherIds = new ArrayList<>();
        if (proteinAc != null) {
            // We just use the first 2 characters for the file name, with each file containing data for multiple proteins
            String uniprotIdPrefix = protein.getUniprotkb().substring(0, 2);
            Path filePath = Path.of(dirPath).resolve(uniprotIdPrefix);
            if (filePath.toFile().exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (parts.length == 2) {
                            String proteinId = parts[0];
                            if (proteinId.equals(protein.getUniprotkb())) {
                                pantherIds.add(parts[1]);
                            }

                        }
                    }
                }
                return pantherIds;
            }
        }
        return pantherIds;
    }
}