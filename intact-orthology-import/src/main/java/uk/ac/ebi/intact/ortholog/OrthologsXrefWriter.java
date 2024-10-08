package uk.ac.ebi.intact.ortholog;

import lombok.RequiredArgsConstructor;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;
import uk.ac.ebi.intact.jami.utils.IntactUtils;
import java.util.*;

@RequiredArgsConstructor
public class OrthologsXrefWriter {

    private final static String PANTHER_DATABASE_MI = "MI:0702";
    private final static String ORTHOLOGY_MI = "MI:2426";
    private final Map<String, IntactCvTerm> cvTermMap = new HashMap<>();
    private final IntactDao intactDao;


    @Override
    public int hashCode() {
        return Objects.hash(cvTermMap, intactDao);
    }

    public void addOrthologyXrefs(IntactProtein protein, Collection<String> pantherIds) throws Exception{
        for (String pantherId: pantherIds) {
            addOrthologyXref(protein, pantherId);
        }
    }

    public void addOrthologyXref(IntactProtein protein, String pantherId) throws Exception{
        InteractorXref xref = newOrthologsXref(pantherId);
        if (!protein.getXrefs().contains(xref)){
            protein.getXrefs().add(xref);
        }
    }

    private InteractorXref newOrthologsXref(String id) throws Exception{
        IntactCvTerm database = findCvTerm(IntactUtils.DATABASE_OBJCLASS, PANTHER_DATABASE_MI);
        IntactCvTerm qualifier = findCvTerm(IntactUtils.QUALIFIER_OBJCLASS, ORTHOLOGY_MI);
        return new InteractorXref(database, id, qualifier);
    }

    private IntactCvTerm findCvTerm(String clazz, String id) throws Exception {
        String key = clazz + "_" + id;
        if (cvTermMap.containsKey(key)) {
            return cvTermMap.get(key);
        }
        IntactCvTerm cvTerm = intactDao.getCvTermDao().getByMIIdentifier(id,clazz);
        if (cvTerm != null) {
            cvTermMap.put(key, cvTerm);
            return cvTerm;
        }
        throw new Exception("CV Term not found with class '" + clazz + "' and id '" + id + "'");
    }
}