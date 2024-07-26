package uk.ac.ebi.intact.ortholog;

import lombok.RequiredArgsConstructor;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.dao.impl.IntactDaoImpl;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.*;
import java.util.*;


@RequiredArgsConstructor
public class OrthologsManager {

    private final IntactDao intactDao;

    public static void main(String[] args) throws IOException, BridgeFailedException {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:/META-INF/orthology-import-spring.xml");
        // Retrieve beans and use them
        OrthologsFileReader orthologsFileReader = context.getBean("orthologsFileReader", OrthologsFileReader.class);
        OrthologsFileParser orthologsFileParser = context.getBean("orthologsFileParser", OrthologsFileParser.class);
        OrthologsProteinAssociation orthologsProteinAssociation = context.getBean("orthologsProteinAssociation", OrthologsProteinAssociation.class);


        String filePath = "orthologsData.txt";
        String urlPanther = "http://data.pantherdb.org/ftp/ortholog/current_release/AllOrthologs.tar.gz";

//        OrthologsFileReader.decompressGzip(urlPanther,filePath); // working ✅
//        Map<String, String> uniprotAndPanther = OrthologsFileParser.parseFile(filePath); // working ✅

//        OrthologsProteinAssociation orthologsProteinAssociation = new OrthologsProteinAssociation(new IntactDaoImpl());
//        List<IntactProtein> proteins = orthologsProteinAssociation.getIntactProtein();
        List<IntactProtein> prot = orthologsProteinAssociation.getIntactProtein();

    }
}

