package uk.ac.ebi.intact.util.uniprotExport.filters;

import org.junit.Test;
import uk.ac.ebi.intact.util.uniprotExport.exporters.QueryBuilder;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class UniProtReferenceLineFilterTest {
    
    @Test
    
    public void testReferenceLineInformation(){
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.getReferenceLineInformation("A0A024R5S0");
        
    }
}
