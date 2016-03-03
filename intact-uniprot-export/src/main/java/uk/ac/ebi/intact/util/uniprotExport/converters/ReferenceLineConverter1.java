package uk.ac.ebi.intact.util.uniprotExport.converters;

import uk.ac.ebi.intact.util.uniprotExport.filters.UniProtReferenceLineFilter;
import uk.ac.ebi.intact.util.uniprotExport.parameters.referencelineparameters.ReferenceParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.referencelineparameters.ReferenceParameters1;

import java.util.List;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ReferenceLineConverter1 implements ReferenceLineConverter {


    @Override
    public ReferenceParameters convertInteractorIntoReferenceLine(String interactorAc) {
        if (interactorAc != null) {
            List<String> results = UniProtReferenceLineFilter.UniProtReferenceLineResult(interactorAc);
            if(results.size() == 4) {
                return new ReferenceParameters1(results.get(0), results.get(1), results.get(2), results.get(3));
            }
        }
        return null;
    }
}
