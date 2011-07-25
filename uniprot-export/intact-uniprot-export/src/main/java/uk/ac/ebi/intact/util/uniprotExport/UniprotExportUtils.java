package uk.ac.ebi.intact.util.uniprotExport;

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
}
