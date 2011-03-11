package uk.ac.ebi.intact.util.uniprotExport.converters;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DefaultDRParameters;

import java.io.IOException;

/**
 * Converts an interactor into a DR line
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class DefaultInteractorToDRLineConverter implements InteractorToDRLineConverter {
    private static final Logger logger = Logger.getLogger(DefaultInteractorToDRLineConverter.class);

    /**
     * Converts an interactor into a DR line
     * @param interactorAc
     * @param numberInteractions
     * @return the converted DRParameter
     * @throws IOException
     */
    public DRParameters convertInteractorIntoDRLine(String interactorAc, int numberInteractions){

        // if the interactor ac is not null, we can create a DRParameter
        if (interactorAc != null){
            logger.debug("Convert DR parameters for " + interactorAc + ", " + numberInteractions);
            return new DefaultDRParameters(interactorAc, numberInteractions);
        }

        logger.warn("interactor Ac is null, cannot convert into DRLines");
        return null;
    }
}
