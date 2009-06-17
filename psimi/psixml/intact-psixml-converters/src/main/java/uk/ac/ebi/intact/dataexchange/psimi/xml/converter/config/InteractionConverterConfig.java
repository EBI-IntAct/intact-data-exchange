package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config;

import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the configuration of the converter.
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 2.0.0
 */
public class InteractionConverterConfig {

    /**
     * If true, fix those xrefs that are wrongly typed as "identity" but should be "source-reference"
     */
    private boolean autoFixSourceReferences = true;

    ///////////////////
    // Constructor

    public InteractionConverterConfig() {
    }

    public boolean isAutoFixSourceReferences() {
        return autoFixSourceReferences;
    }

    public void setAutoFixSourceReferences(boolean autoFixSourceReferences) {
        this.autoFixSourceReferences = autoFixSourceReferences;
    }
}