package uk.ac.ebi.intact.dataexchange.psimi.solr;

/**
 * Names of the Fields.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface FieldNames {

    String EXACT_PREFIX = "_exact";

    String IDENTIFIER = "id";
    String ID_A = "idA";
    String ID_B = "idB";
    String ALTID_A = "altidA";
    String ALTID_B = "altidB";
    String ALIAS_A = "aliasA";
    String ALIAS_B = "aliasB";
    String DETMETHOD = "detmethod";
    String DETMETHOD_EXACT = "detmethod"+EXACT_PREFIX;
    String PUBAUTH = "pubauth";
    String PUBID = "pubid";
    String TAXID_A = "taxidA";
    String TAXID_A_EXACT = "taxidA"+EXACT_PREFIX;
    String TAXID_B = "taxidB";
    String TAXID_B_EXACT = "taxidB"+EXACT_PREFIX;
    String TYPE = "type";
    String TYPE_EXACT = "type"+EXACT_PREFIX;
    String SOURCE = "source";
    String INTERACTION_ID = "interaction_id";
    String CONFIDENCE = "confidence";
    String EXPERIMENTAL_ROLE_A = "experimentalRoleA";
    String EXPERIMENTAL_ROLE_A_EXACT = "experimentalRoleA"+EXACT_PREFIX;
    String EXPERIMENTAL_ROLE_B = "experimentalRoleB";
    String EXPERIMENTAL_ROLE_B_EXACT = "experimentalRoleB"+EXACT_PREFIX;
    String BIOLOGICAL_ROLE_A = "biologicalRoleA";
    String BIOLOGICAL_ROLE_A_EXACT = "biologicalRoleA"+EXACT_PREFIX;
    String BIOLOGICAL_ROLE_B = "biologicalRoleB";
    String BIOLOGICAL_ROLE_B_EXACT = "biologicalRoleB"+EXACT_PREFIX;
    String PROPERTIES_A = "propertiesA";
    String PROPERTIES_A_EXACT = "propertiesA"+EXACT_PREFIX;
    String PROPERTIES_B = "propertiesB";
    String PROPERTIES_B_EXACT = "propertiesB"+EXACT_PREFIX;
    String TYPE_A = "typeA";
    String TYPE_A_EXACT = "typeA"+EXACT_PREFIX;
    String TYPE_B = "typeB";
    String TYPE_B_EXACT = "typeB"+EXACT_PREFIX;
    String HOST_ORGANISM = "hostOrganism";
    String HOST_ORGANISM_EXACT = "hostOrganism"+EXACT_PREFIX;
    String EXPANSION = "expansion";
    String DATASET = "dataset";
    String ANNOTATION_A = "annotationA";
    String ANNOTATION_A_EXACT = "annotationA"+EXACT_PREFIX;
    String ANNOTATION_B = "annotationB";
    String ANNOTATION_B_EXACT = "annotationB"+EXACT_PREFIX;
    String PARAMETER_A = "parameterA";
    String PARAMETER_B = "parameterB";
    String PARAMETER_INTERACTION = "parameterInteraction";

    String DB_GO = "go";
    String DB_INTERPRO = "interpro";
    String DB_PSIMI = "psi-mi";
    String DB_CHEBI = "chebi";

    String LINE = "line";

    String PKEY = "pkey";
    String RIGID = "rigid";
    String RELEVANCE_SCORE = "relevancescore";
    String EVIDENCES = "evidences";

    String INTACT_BY_INTERACTOR_TYPE_PREFIX = "intact_byInteractorType_";
    String GENE_NAME = "geneName";

    String[] DATA_FIELDS = new String[] {
         ID_A, ID_B, ALTID_A, ALTID_B, ALIAS_A, ALIAS_B, DETMETHOD_EXACT, PUBAUTH, PUBID, TAXID_A_EXACT, TAXID_B_EXACT,
            TYPE_EXACT, SOURCE, INTERACTION_ID, CONFIDENCE,
            EXPERIMENTAL_ROLE_A_EXACT, EXPERIMENTAL_ROLE_B_EXACT, BIOLOGICAL_ROLE_A_EXACT, BIOLOGICAL_ROLE_B_EXACT, PROPERTIES_A_EXACT, PROPERTIES_B_EXACT,
            TYPE_A_EXACT, TYPE_B_EXACT, HOST_ORGANISM_EXACT, EXPANSION, DATASET, ANNOTATION_A, ANNOTATION_B,
            PARAMETER_A, PARAMETER_B, PARAMETER_INTERACTION
    };


}
