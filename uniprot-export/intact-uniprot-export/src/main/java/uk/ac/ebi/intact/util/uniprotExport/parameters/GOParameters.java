package uk.ac.ebi.intact.util.uniprotExport.parameters;

import java.util.Set;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public interface GOParameters {

    public String getFirstProtein();

    public String getSecondProtein();

    public Set<String> getPubmedIds();
}
