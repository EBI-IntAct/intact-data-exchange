package uk.ac.ebi.intact.ortholog;

import lombok.RequiredArgsConstructor;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.fetcher.InteractorFetcher;
import psidev.psi.mi.jami.bridges.fetcher.ProteinFetcher;
import psidev.psi.mi.jami.bridges.uniprot.UniprotProteinFetcher;
import java.io.*;
import java.util.*;

@RequiredArgsConstructor
public class OrthologsManager {

    UniprotProteinFetcher uniprotProteinFetcher;

    public OrthologsManager(UniprotProteinFetcher uniprotProteinFetcher) {
        this.uniprotProteinFetcher = new UniprotProteinFetcher();
    }

    public static void main(String[] args) throws IOException, BridgeFailedException {
        String filePath = "orthologsData.txt";
        String urlPanther = "http://data.pantherdb.org/ftp/ortholog/current_release/AllOrthologs.tar.gz";

        OrthologsManager orthologsManager = new OrthologsManager();

//        OrthologsFileReader.decompressGzip(urlPanther,filePath);
//        Map<String, String> uniprotAndPanther = OrthologsFileParser.parseFile(filePath);

//        System.out.println(orthologsManager.uniprotProteinFetcher.fetchByIdentifier("P77650")); // return null

//
//        OrthologsProteinAssociation orthologsProteinAssociation = new OrthologsProteinAssociation(new UniprotProteinFetcher());
//        orthologsProteinAssociation.getProtein();
    }
}

