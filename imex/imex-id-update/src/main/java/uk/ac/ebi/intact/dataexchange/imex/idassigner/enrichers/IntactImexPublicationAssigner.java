package uk.ac.ebi.intact.dataexchange.imex.idassigner.enrichers;

import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.imex.ImexPublicationAssigner;
import psidev.psi.mi.jami.model.Publication;
import psidev.psi.mi.jami.model.Source;
import psidev.psi.mi.jami.model.impl.DefaultSource;
import psidev.psi.mi.jami.utils.comparator.cv.DefaultCvTermComparator;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexCentralManager;

/**
 * This enricher will update a publication having IMEx id and synchronize it with IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/10/14</pre>
 */

public class IntactImexPublicationAssigner extends ImexPublicationAssigner {

    public IntactImexPublicationAssigner(ImexCentralClient fetcher) {
        super(fetcher);
    }

    @Override
    protected boolean isEntitledToAssignImexId(Publication publicationToEnrich, ImexPublication imexPublication) throws EnricherException {

        Source source = publicationToEnrich.getSource();
        Source source2 = imexPublication.getSource();
        if (source2 == null){
            source2 = new DefaultSource(imexPublication.getOwner());
        }
        if (source != null && !DefaultCvTermComparator.areEquals(source, imexPublication.getSource())){
            if (imexPublication.getOwner().equalsIgnoreCase(ImexCentralManager.INTACT_CURATOR)){
                return true;
            }
            return false;
        }
        return source != null && DefaultCvTermComparator.areEquals(source, imexPublication.getSource());
    }
}


