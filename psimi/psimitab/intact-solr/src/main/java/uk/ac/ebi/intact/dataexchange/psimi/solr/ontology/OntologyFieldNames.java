package uk.ac.ebi.intact.dataexchange.psimi.solr.ontology;

import uk.ac.ebi.intact.bridges.ontologies.FieldName;

/**
 * Names of the fields in the ontology index.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface OntologyFieldNames {

    String ID = "id";
    String ONTOLOGY = "ontology";
    String PARENT_ID = "pid";
    String PARENT_NAME = "pname";
    String CHILD_ID = "cid";
    String CHILD_NAME = "cname";
    //String RELATIONSHIP_TYPE = "reltype";
    //String CYCLIC = "cyclic";
    String PARENT_SYNONYMS = FieldName.PARENT_SYNONYMS;
    String CHILDREN_SYNONYMS = FieldName.CHILDREN_SYNONYMS;
}
