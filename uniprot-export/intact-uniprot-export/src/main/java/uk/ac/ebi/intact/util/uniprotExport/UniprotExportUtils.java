package uk.ac.ebi.intact.util.uniprotExport;

import uk.ac.ebi.intact.util.uniprotExport.results.contexts.IntactTransSplicedProteins;

import java.util.Set;

/**
 * This class contains utility methods for uniprot export
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25/07/11</pre>
 */

public class UniprotExportUtils {

    public static boolean isMasterProtein(String uniprotAc){

        if (uniprotAc.length() == 6 && !uniprotAc.contains("-") && !uniprotAc.contains("PRO")){
            return true;
        }
        return false;
    }

    public static boolean isIsoformProtein(String uniprotAc){

        if (uniprotAc.contains("-") && !uniprotAc.contains("PRO")){
            return true;
        }
        return false;
    }

    public static boolean isFeatureChainProtein(String uniprotAc){

        if (uniprotAc.contains("PRO")){
            return true;
        }
        return false;
    }

    public static String extractMasterProteinFrom(String uniprotAc){

        if (uniprotAc.contains("-")){
             return uniprotAc.substring(0, uniprotAc.indexOf("-"));
        }

        return uniprotAc;
    }

    /**
     *
     * @param firstInteractor : uniprot ac of the master uniprot
     * @param interactor : uniprot ac of the interactor
     * @param transSplicedProteins : set of trans spliced proteins which can be associated with the master uniprot entry
     * @return true if this interactor is from the same uniprot entry as the master uniprot ac, false otherwise
     */
    public static boolean isFromSameUniprotEntry(String firstInteractor, String interactor, Set<IntactTransSplicedProteins> transSplicedProteins){

        // the interactor starts with master uniprot so we consider it as the first interactor
        if (interactor.startsWith(firstInteractor)){
            return true;
        }
        // if proteins from this uniprot entry are trans spliced variants
        else if (transSplicedProteins != null){
            for (IntactTransSplicedProteins prot : transSplicedProteins){
                // the interactor is a transpliced variant of this uniprot entry so we consider it as the first interactor
                if (interactor.equalsIgnoreCase(prot.getUniprotAc())){
                    return true;
                }
            }
        }
        return false;
    }
}
