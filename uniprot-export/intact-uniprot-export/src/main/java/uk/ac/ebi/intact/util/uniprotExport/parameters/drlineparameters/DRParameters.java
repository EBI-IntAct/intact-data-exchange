package uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public interface DRParameters {

    /**
     * The uniprot ac
     * @return
     */
    public String getUniprotAc();

    /**
     * The number of interactions
     * @return
     */
    public int getNumberOfInteractions();
}
