package uk.ac.ebi.intact.util.uniprotExport.filters;

import uk.ac.ebi.intact.util.uniprotExport.exporters.QueryBuilder;

import java.util.List;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class UniProtReferenceLineFilter {

    private static QueryBuilder queryProvider = new QueryBuilder();
    
    public static List<String> UniProtReferenceLineResult(String uniprotAc){
        return queryProvider.getReferenceLineInformation(uniprotAc);
    }
}
