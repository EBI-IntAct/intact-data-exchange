package uk.ac.ebi.intact.psimitab.converters;

import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.irefindex.seguid.RigDataModel;

/**
 * This unit contains the converted interactor and the rogid of the interactor if it is a protein.
 * This should allow the interaction converter to create a rigid from the rogids of all its interactors
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/08/12</pre>
 */

public class MitabInteractor {

    private Interactor interactor;
    private RigDataModel rigDataModel;

    public MitabInteractor(Interactor interactor, RigDataModel rigDataModel){
         this.interactor = interactor;
        this.rigDataModel = rigDataModel;
    }

    public MitabInteractor(Interactor interactor){
        this.interactor = interactor;
        this.rigDataModel = null;
    }

    public Interactor getInteractor() {
        return interactor;
    }

    public RigDataModel getRigDataModel() {
        return rigDataModel;
    }

    public void setRigDataModel(RigDataModel rigDataModel) {
        this.rigDataModel = rigDataModel;
    }
}
