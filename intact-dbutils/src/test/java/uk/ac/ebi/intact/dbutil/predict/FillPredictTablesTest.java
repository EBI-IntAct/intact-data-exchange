package uk.ac.ebi.intact.dbutil.predict;

import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactAbstractTestCase;
import uk.ac.ebi.intact.core.unit.IntactUnitDataset;
import uk.ac.ebi.intact.unitdataset.PsiTestDatasetProvider;

/**
 * FillPredictTables Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>11/15/2006</pre>
 */
public class FillPredictTablesTest extends IntactAbstractTestCase {

    @Test
    @IntactUnitDataset(dataset = PsiTestDatasetProvider.INTACT_JUL_06, provider = PsiTestDatasetProvider.class)
    public void testRunTask() throws Exception {

        FillPredictTables.runTask(System.out);
    }
}
