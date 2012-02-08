package uk.ac.ebi.intact.dataexchange.cvutils.model;

/**
 * This interface is for the configuration of external ontologies used in IntAct
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06/02/12</pre>
 */

public interface ExternalOntologyConfig {
    
    public String getDefaultNamespace();
    public String getShortLabelSynonymCategory();
    public String getAliasSynonymCategory();
    
    public String getAliasSynonymDefinition();
    public String getShortLabelSynonymDefinition();
}
