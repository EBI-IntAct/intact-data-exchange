package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.results.MethodAndTypePair;

import java.util.*;

/**
 * Contains utility methods for the writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public class WriterUtils {

    public static final String NEW_LINE = System.getProperty("line.separator");
    public static final String TABULATION = "\t";
    public static final String PUBMED = "pubmed";
    public static final String INTACT = "intact";
    public static final String UNIPROT = "uniprotkb";
    public static final String TAXID = "taxId";
    public static final String CHAIN_PREFIX = "-PRO_";

    /**
     * Invert the map so the value become the key. As the value can have several keys, te new value is a list of previous keys
     * @param mapToInvert
     * @return a map of type String -> List<String>
     */
    public static Map<String, List<String>> invertMapOfTypeStringToString (Map<String, String> mapToInvert){
        Map<String, List<String>> invertedMap = new HashMap<String, List<String>>();

        for (Map.Entry<String, String> entry : mapToInvert.entrySet()){
            String value = entry.getValue();
            String key = entry.getKey();

            if (invertedMap.containsKey(value)){
                List<String> values = invertedMap.get(value);
                values.add(key);
            }
            else{
                List<String> values = new ArrayList<String>();
                values.add(key);
                invertedMap.put(value, values);
            }
        }
        return invertedMap;
    }

    /**
     * Invert the map so the value become the key. As the value can have several keys, te new value is a list of previous keys
     * @param mapToInvert
     * @param keySelection : the list of keys we want to invert
     * @return a map of type MethodAndTypePair -> List<String>
     */
    public static Map<MethodAndTypePair, List<String>> invertMapFromKeySelection (Map<String, MethodAndTypePair> mapToInvert, Set<String> keySelection){
        Map<MethodAndTypePair, List<String>> invertedMap = new HashMap<MethodAndTypePair, List<String>>();

        for (String selectedKey : keySelection){

            if (mapToInvert.containsKey(selectedKey)){
                MethodAndTypePair value = mapToInvert.get(selectedKey);

                if (invertedMap.containsKey(value)){
                    List<String> values = invertedMap.get(value);
                    values.add(selectedKey);
                }
                else{
                    List<String> values = new ArrayList<String>();
                    values.add(selectedKey);
                    invertedMap.put(value, values);
                }
            }
        }
        return invertedMap;
    }
}
