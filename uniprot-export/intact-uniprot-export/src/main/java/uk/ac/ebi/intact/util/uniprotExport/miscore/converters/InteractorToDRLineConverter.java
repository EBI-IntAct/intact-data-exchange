package uk.ac.ebi.intact.util.uniprotExport.miscore.converters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.DRParametersImpl;

import java.io.IOException;

/**
 * Converts an interactor into a DR line
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class InteractorToDRLineConverter {

    /**
     * Converts an interactor into a DR line
     * @param interactorAc
     * @param numberInteractions
     * @return the converted DRParameter
     * @throws IOException
     */
    public DRParameters convertInteractorToDRLine(String interactorAc, int numberInteractions) throws IOException {

        // if the interactor ac is not null, we can create a DRParameter
        if (interactorAc != null){
            return new DRParametersImpl(interactorAc, numberInteractions);
        }

        return null;
    }
}
