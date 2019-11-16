package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

/**
 * SecondCCParametersVersion1 contains the information of a basic Second CC parameters plus the number of interaction evidences
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public interface SecondCCParametersVersion1 extends SecondCCParameters {

    /**
     * the number of interaction evidences
     * @return
     */
    public int getNumberOfInteractionEvidences();

    public void setGeneName(String name);

    public void setNumberOfInteractionEvidences(int number);
}
