package uk.ac.ebi.intact.dataexchange.cvutils;

import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UpdateCVsTest extends IntactBasicTestCase {

    @Test
    public void loadDefault() throws Exception {
        beginTransaction();
        UpdateCVsReport report = UpdateCVs.loadDefaultCVs();
        commitTransaction();

        System.out.println(report.getCreatedTerms().size());
    }

}
