package uk.ac.ebi.intact.dataexchange.psimi.solr.ontology;

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
    String RELATIONSHIP_TYPE = "reltype";
    String CYCLIC = "cyclic";
}
