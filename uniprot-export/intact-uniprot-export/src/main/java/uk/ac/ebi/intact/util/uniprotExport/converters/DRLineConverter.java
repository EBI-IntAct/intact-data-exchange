package uk.ac.ebi.intact.util.uniprotExport.converters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;

/**
 * Interface for DR line converters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public interface DRLineConverter {

    /**
     * Converts an interactor into a DR line
     * @param interactorAc
     * @param numberInteractions
     * @return the converted DRParameter
     * @throws java.io.IOException
     */
    public DRParameters convertInteractorIntoDRLine(String interactorAc, int numberInteractions);
}
