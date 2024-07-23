package uk.ac.ebi.intact.ortholog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.uniprot.UniprotProteinFetcher;
import psidev.psi.mi.jami.model.Protein;

import java.util.Collection;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class OrthologsProteinAssociation {

    private final UniprotProteinFetcher uniprotProteinFetcher;
    Collection<Protein> proteins;

    public Collection<Protein> getIntactProtein() throws BridgeFailedException {
        String proteinID = "P77650";
        proteins = (Collection<Protein>) uniprotProteinFetcher.fetchByIdentifier(proteinID);
        return proteins;
    }

    public void getProtein(){
        System.out.println(proteins.iterator().next());
    }

    private static void uniprotToProtein(Map<String, String> uniprotAndPanther) throws BridgeFailedException {
        for (Map.Entry<String, String> entry : uniprotAndPanther.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }
}
