package uk.ac.ebi.intact.dataexchange.cvutils;

import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvTerm;
import uk.ac.ebi.intact.model.CvObject;
import org.junit.Test;

import java.util.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvUpdaterTest extends IntactBasicTestCase {

    @Test
    public void createOrUpdateCVs() throws Exception {
        IntactOntology ontology = OboUtils.createOntologyFromOboLatestPsiMi();

        CvUpdater updater = new CvUpdater(ontology);
        //updater.setExcludeObsolete(true);
        updater.createOrUpdateCVs();

        System.out.println("Total: "+getDaoFactory().getCvObjectDao().countAll());
    }

    public boolean containsMi(Collection<CvObject> cvs, String mi) {
        for (CvObject cv : cvs) {
            if (cv.getMiIdentifier().equals(mi)) {
                return true;
            }
        }

        return false;
    }

}
