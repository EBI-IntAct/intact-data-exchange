package uk.ac.ebi.intact.util.uniprotExport.miscore.converters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.DRParametersImpl;

import java.io.IOException;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class InteractorToDRLineConverter {

    public DRParametersImpl convertInteractorToDRLine(String interactorAc, int numberInteractions) throws IOException {

        if (interactorAc != null){
            return new DRParametersImpl(interactorAc, numberInteractions);
        }

        return null;
    }
}
