package uk.ac.ebi.intact.psimitab.converters.expansion;

import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.psimitab.converters.MitabInteractor;

/**
 * A Mitab interaction contains a binary interaction and the rigdata model of the two interactors  (so we can compute the rigid of interaction)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/08/12</pre>
 */

public class MitabExpandedInteraction {

    private BinaryInteraction binaryInteraction;
    private MitabInteractor mitabInteractorA;
    private MitabInteractor mitabInteractorB;

    public MitabExpandedInteraction(BinaryInteraction binaryInteraction, MitabInteractor mitabInteractorA, MitabInteractor mitabInteractorB){
        this.binaryInteraction = binaryInteraction;
        this.mitabInteractorA = mitabInteractorA;
        this.mitabInteractorB = mitabInteractorB;
    }

    public BinaryInteraction getBinaryInteraction() {
        return binaryInteraction;
    }

    public MitabInteractor getMitabInteractorA() {
        return mitabInteractorA;
    }

    public MitabInteractor getMitabInteractorB() {
        return mitabInteractorB;
    }
}
