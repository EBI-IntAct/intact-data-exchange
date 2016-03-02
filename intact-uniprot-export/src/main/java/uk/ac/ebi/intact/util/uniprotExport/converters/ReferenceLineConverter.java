package uk.ac.ebi.intact.util.uniprotExport.converters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.referencelineparameters.ReferenceParameters;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public interface ReferenceLineConverter {

    ReferenceParameters convertInteractorIntoReferenceLine(String interactorAc);

}
