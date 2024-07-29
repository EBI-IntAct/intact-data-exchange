package uk.ac.ebi.intact.ortholog;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import java.util.*;
import java.io.IOException;

@RequiredArgsConstructor
public class OrthologsManager {

    private final IntactDao intactDao;
    private final OrthologsProteinAssociation orthologsProteinAssociation;

    public static void main(String[] args) throws IOException, BridgeFailedException {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:/META-INF/orthology-import-spring.xml");

        OrthologsFileReader orthologsFileReader = context.getBean("orthologsFileReader", OrthologsFileReader.class);
        OrthologsFileParser orthologsFileParser = context.getBean("orthologsFileParser", OrthologsFileParser.class);
        OrthologsProteinAssociation orthologsProteinAssociation = context.getBean("orthologsProteinAssociation", OrthologsProteinAssociation.class);

        String filePath = "orthologsData.txt";
        String urlPanther = "http://data.pantherdb.org/ftp/ortholog/current_release/AllOrthologs.tar.gz";

//        OrthologsFileReader.decompressGzip(urlPanther,filePath); // working ✅
//        Map<String, String> uniprotAndPanther = OrthologsFileParser.parseFile(filePath); // working ✅

        Collection<IntactProtein> proteins = orthologsProteinAssociation.getIntactProtein();
        System.out.println(proteins);
    }
}

