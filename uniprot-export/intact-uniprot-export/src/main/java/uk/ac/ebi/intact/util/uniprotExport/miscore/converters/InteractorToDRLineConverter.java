package uk.ac.ebi.intact.util.uniprotExport.miscore.converters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.DRParameters;

import java.io.IOException;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class InteractorToDRLineConverter {

    public DRParameters convertInteractorToDRLine(String interactorAc, int numberInteractions) throws IOException {

        if (interactorAc != null){
            return new DRParameters(interactorAc, numberInteractions);
        }

        return null;
    }
}
