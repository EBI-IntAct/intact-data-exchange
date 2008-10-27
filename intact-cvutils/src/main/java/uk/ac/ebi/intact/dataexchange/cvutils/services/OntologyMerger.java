package uk.ac.ebi.intact.dataexchange.cvutils.services;

import org.obo.datamodel.OBOSession;

/**
 * TODO comment that class header
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public interface OntologyMerger {


    OBOSession merge(OntologyMergeConfig omc, OBOSession source,OBOSession target);
    

}
