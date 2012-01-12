package uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters;

/**
 * GO parameters must provide uniprot acs of the first and second protein and a list of pubmed Ids
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public interface GOParameters {

    /**
     * The uniprot ac of the first protein
     * @return
     */
    public String getFirstProtein();

    /**
     * The uniprot ac of the second protein
     * @return
     */
    public String getSecondProtein();
}
