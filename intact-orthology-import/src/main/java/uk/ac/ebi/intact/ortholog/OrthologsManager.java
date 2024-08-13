package uk.ac.ebi.intact.ortholog;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.io.IOException;

// This class is just for testing

@RequiredArgsConstructor
public class OrthologsManager {

    public static void main(String[] args) throws IOException, BridgeFailedException {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:/META-INF/orthology-import-spring.xml");

        OrthologsFileReader orthologsFileReader = context.getBean("orthologsFileReader", OrthologsFileReader.class);
        OrthologsFileParser orthologsFileParser = context.getBean("orthologsFileParser", OrthologsFileParser.class);
        OrthologsProteinAssociation orthologsProteinAssociation = context.getBean("orthologsProteinAssociation", OrthologsProteinAssociation.class);
        OrthologsXrefWriter orthologsXrefWriter = context.getBean("orthologsXrefWriter", OrthologsXrefWriter.class);

        String reportFile = "orthologsReport.txt";
        String filePath = "orthologsData.txt";
        String urlPanther = "http://data.pantherdb.org/ftp/ortholog/current_release/AllOrthologs.tar.gz";

        String report = "";

//        OrthologsFileReader.decompressGzip(urlPanther,filePath);// working

        Map<String, String> uniprotAndPanther = OrthologsFileParser.parseFile(filePath);// working
//        System.out.println("Total number of orthologs parsed: " + uniprotAndPanther.size());
//        report += "Total number of orthologs parsed: " + uniprotAndPanther.size() + "\n";

        Collection<IntactProtein> proteins = orthologsProteinAssociation.getIntactProtein();// working
//        Collection<IntactProtein> proteins = orthologsProteinAssociation.getFewIntactProtein();
//        System.out.println("Total number of Intact proteins: " + proteins.size());
//        report += "Total number of Intact proteins: " + proteins.size() + "\n";

        Map<IntactProtein, String> proteinAndPanther = orthologsProteinAssociation.associateAllProteinsToPantherId(uniprotAndPanther, proteins);
//        System.out.println("Number of protein associated to Panther id: " + proteinAndPanther.size());
//        report += "Number of protein associated to Panther id: " + proteinAndPanther.size() + "\n";

//        orthologsXrefWriter.iterateThroughProteins(proteinAndPanther);

//        reportWriter(reportFile, report);
    }

    public static void reportWriter(String reportFile, String toWrite){
        try {
            FileWriter fileWriter = new FileWriter(reportFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(toWrite);
            bufferedWriter.close();
        }
        catch (Exception e) {
            e.getStackTrace();
        }
    }
}
