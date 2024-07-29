package uk.ac.ebi.intact.ortholog;

import org.springframework.batch.item.ItemWriter;
import psidev.psi.mi.jami.model.Protein;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;
import uk.ac.ebi.intact.jami.utils.IntactUtils;
//import uk.ac.ebi.complex.service.service.IntactComplexService;

import java.util.*;
import java.util.Map;


public class OrthologsXrefWriter {

    private final static String PANTHER_DATABASE_ID = "test";
    private final Map<String, IntactCvTerm> cvTermMap = new HashMap<>();

//    private final IntactComplexService intactComplexService;

    public void iterateThroughProteins(){

    }

    public void addOrthologyXref(Map<IntactProtein, String> proteinAndPanther){
        IntactProtein protein = (IntactProtein) proteinAndPanther.keySet();
        String pantherId = proteinAndPanther.get(protein);
        InteractorXref xref = newOrthologsXref(pantherId);
        if (!protein.getXrefs().contains(xref)){
            protein.getXrefs().add(xref);
        }
    }

    private InteractorXref newOrthologsXref(String id){
        IntactCvTerm database = findCvTerm(IntactUtils.DATABASE_OBJCLASS, PANTHER_DATABASE_ID);
        // Currently we use identity as qualifier, as we are only importing exact matches.
        // If we merge curated complexes with partial matches, we need to add a different qualifier (subset, see-also, etc.).
        IntactCvTerm qualifier = findCvTerm(IntactUtils.QUALIFIER_OBJCLASS, Xref.IDENTITY_MI);
        return new InteractorXref(database, id, qualifier);
    }

    private IntactCvTerm findCvTerm(String clazz, String id){
        String key = clazz + "_" + id;
        if (cvTermMap.containsKey(key)) {
            return cvTermMap.get(key);
        }
        IntactCvTerm cvTerm = new IntactCvTerm();
//        IntactCvTerm cvTerm = intactComplexService.getCvTerm(clazz, id);
//        if (cvTerm != null) {
//            cvTermMap.put(key, cvTerm);
//            return cvTerm;
//        }
//        throw new CvTermNotFoundException("CV Term not found with class '" + clazz + "' and id '" + id + "'");
        return cvTerm;
    }
}