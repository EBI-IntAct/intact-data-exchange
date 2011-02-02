package uk.ac.ebi.intact.util.uniprotExport.writers;

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

    public static Map<Map.Entry<String, String>, List<String>> invertMapFromKeySelection (Map<String, Map.Entry<String, String>> mapToInvert, Set<String> keySelection){
        Map<Map.Entry<String, String>, List<String>> invertedMap = new HashMap<Map.Entry<String, String>, List<String>>();

        for (String selectedKey : keySelection){

            if (mapToInvert.containsKey(selectedKey)){
                Map.Entry<String, String> value = mapToInvert.get(selectedKey);

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
