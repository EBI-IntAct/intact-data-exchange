package uk.ac.ebi.intact.ortholog;

import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;
import uk.ac.ebi.intact.jami.utils.IntactUtils;
import java.util.*;
import java.util.Map;

public class OrthologsXrefWriter {

    private final static String PANTHER_DATABASE_ID = "pantherDbId";
    private final Map<String, IntactCvTerm> cvTermMap = new HashMap<>();

    public void iterateThroughProteins(Map<IntactProtein, String> intactProteinAndPanther){
        for (Map.Entry<IntactProtein, String> entry : intactProteinAndPanther.entrySet()){
            IntactProtein protein = entry.getKey();
            String pantherId = entry.getValue();
            addOrthologyXref(protein, pantherId);
        }
    }

    public void addOrthologyXref(IntactProtein protein, String pantherId){
        InteractorXref xref = newOrthologsXref(pantherId);
        if (!protein.getXrefs().contains(xref)){
            protein.getXrefs().add(xref);
        }
    }

    private InteractorXref newOrthologsXref(String id){
        IntactCvTerm database = findCvTerm(IntactUtils.DATABASE_OBJCLASS, PANTHER_DATABASE_ID);
        IntactCvTerm qualifier = findCvTerm(IntactUtils.QUALIFIER_OBJCLASS, Xref.IDENTITY_MI);
        return new InteractorXref(database, id, qualifier);
    }

    private IntactCvTerm findCvTerm(String clazz, String id) {
        String key = clazz + "_" + id;
        if (cvTermMap.containsKey(key)) {
            return cvTermMap.get(key);
        }
        IntactCvTerm cvTerm = new IntactCvTerm();
//        IntactCvTerm cvTerm = intactComplexService.getCvTerm(clazz, id);
        if (cvTerm != null) {
            cvTermMap.put(key, cvTerm);
            return cvTerm;
        }
//        throw new CvTermNotFoundException("CV Term not found with class '" + clazz + "' and id '" + id + "'");
        return cvTerm;
    }
}