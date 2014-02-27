package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Complex Field Enricher is such as Ontoly Field Enricher
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 05/08/13
 */
public class ComplexSolrEnricher extends AbstractOntologyEnricher{
    /********************************/
    /*      Private attributes      */
    /********************************/
    private static final Log log = LogFactory.getLog ( ComplexSolrEnricher.class );
    private Map<String, PsicquicSimpleClient> mapOfPsicquicClients;
    private String complexProperties=null;
    private PsimiTabReader reader;

    private final static String EXP_EVIDENCE="exp-evidence";
    private final static String INTACT_SECONDARY="intact-secondary";

    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexSolrEnricher ( OntologySearcher ontologySearcher_ ) {
        super ( ontologySearcher_) ;
    }

    /*******************************/
    /*      Protected Methods      */
    /*******************************/
    // is for initialize the default terms to ignore
    protected void initializeOntologyTermsToIgnore(){
        // molecular interaction is root term for psi mi
        getOntologyTermsToIgnore().add("MI:0000");
    }

    protected void initialiseMapOfPsicquicClients() throws IOException {
        Properties prop = new Properties();
        if (complexProperties == null){
            prop.load(ComplexSolrEnricher.class.getResourceAsStream("/META-INF/complex.properties"));
        }
        else{
            prop.load(new FileInputStream(complexProperties));
        }

        this.mapOfPsicquicClients = new HashMap<String, PsicquicSimpleClient>(prop.size());
        for (Map.Entry<Object, Object> client : prop.entrySet()){
            this.mapOfPsicquicClients.put((String)client.getKey(), new PsicquicSimpleClient((String)client.getValue()));
        }
    }

    protected void enrichCvTerm(String fieldName, String facetField, CvDagObject cvDagObject, SolrInputDocument solrDocument) {
        // add all alias to interactor_type
        for ( CvObjectAlias alias : cvDagObject.getAliases ( ) ) {
            if (alias.getName() != null){
                solrDocument.addField(fieldName, alias.getName()) ;
            }
        }
        // add short label, full name and identifier to interactor_type
        solrDocument.addField ( fieldName,  cvDagObject.getShortLabel ( ) ) ;
        solrDocument.addField ( fieldName,  cvDagObject.getFullName   ( ) ) ;
        solrDocument.addField ( fieldName,  cvDagObject.getIdentifier ( ) ) ;

        // add facet field
        if (facetField != null){
            solrDocument.addField(facetField, cvDagObject.getFullName());
        }
    }

    // is for enrich interactor_type* fields and return a SolrDocument
    public void enrichInteractorType(CvDagObject cvDagObject, SolrInputDocument solrDocument) {
        // enrich Cv term exact
        enrichCvTerm(ComplexFieldNames.INTERACTOR_TYPE_EXACT, null, cvDagObject, solrDocument);
        // enrich normal cv term
        enrichCvTerm(ComplexFieldNames.INTERACTOR_TYPE, ComplexFieldNames.INTERACTOR_TYPE_F, cvDagObject, solrDocument);
        // add parents to interaction_type_ontology
        enrichCvTermParents(ComplexFieldNames.INTERACTOR_TYPE, null, cvDagObject, solrDocument);
    }

    protected void enrichCvTermParents(String fieldName, String facetName, CvDagObject cvDagObject, SolrInputDocument solrDocument) {
        for ( CvDagObject parent : cvDagObject.getParents ( ) ) {
            enrichCvTerm(fieldName, facetName, parent, solrDocument);
            if (!parent.getParents().isEmpty()){
                enrichCvTermParents(fieldName, facetName, parent, solrDocument);
            }
        }
    }

    // is for enrich complex_type* fields and return a SolrDocument
    public void enrichComplexType(CvDagObject cvDagObject, SolrInputDocument solrDocument) {
        // enrich Cv term exact
        enrichCvTerm(ComplexFieldNames.COMPLEX_TYPE_EXACT, null, cvDagObject, solrDocument);
        // enrich normal cv term
        enrichCvTerm(ComplexFieldNames.COMPLEX_TYPE, ComplexFieldNames.COMPLEX_TYPE_F, cvDagObject, solrDocument);
        // add parents to interaction_type_ontology
        enrichCvTermParents(ComplexFieldNames.COMPLEX_TYPE, null, cvDagObject, solrDocument);

    }

    public void enrichBiologicalRole(CvDagObject cvDagObject, SolrInputDocument solrDocument) {
        // enrich Cv term exact
        enrichCvTerm(ComplexFieldNames.BIOROLE_EXACT, null, cvDagObject, solrDocument);
        // enrich normal cv term
        enrichCvTerm(ComplexFieldNames.BIOROLE, ComplexFieldNames.BIOROLE_F, cvDagObject, solrDocument);
        // add parents to interaction_type_ontology
        enrichCvTermParents(ComplexFieldNames.BIOROLE, null, cvDagObject, solrDocument);
    }

    public void enrichFeatureType(CvDagObject cvDagObject, SolrInputDocument solrDocument) {
        // enrich Cv term exact
        enrichCvTerm(ComplexFieldNames.FEATURES_EXACT, null, cvDagObject, solrDocument);
        // enrich normal cv term
        enrichCvTerm(ComplexFieldNames.FEATURES, ComplexFieldNames.FEATURES_F, cvDagObject, solrDocument);
        // add parents to interaction_type_ontology
        enrichCvTermParents(ComplexFieldNames.FEATURES, null, cvDagObject, solrDocument);
    }

    public void enrichInteractionType(CvDagObject cvDagObject, SolrInputDocument solrDocument) {
        // enrich Cv term exact
        enrichCvTerm(ComplexFieldNames.INTERACTION_TYPE_EXACT, null, cvDagObject, solrDocument);
        // enrich normal cv term
        enrichCvTerm(ComplexFieldNames.INTERACTION_TYPE, ComplexFieldNames.INTERACTION_TYPE_F, cvDagObject, solrDocument);
        // add parents to interaction_type_ontology
        enrichCvTermParents(ComplexFieldNames.INTERACTION_TYPE, null, cvDagObject, solrDocument);
    }

    // is for enrich complex_organism* fields and return a SolrDocument
    public void enrichOrganism(Interaction interaction, SolrInputDocument solrDocument) throws SolrServerException {
        // retrieve the ontology term for this interaction (using BioSource)
        final OntologyTerm ontologyTerm = findOrganism(interaction) ;
        // add name, all synonyms and tax id to complex_organism
        if ( ontologyTerm != null ) {
            // enrich exact organism
            enrichOrganism(ComplexFieldNames.COMPLEX_ORGANISM_EXACT, null, solrDocument, ontologyTerm);
            // enrich organism for query
            enrichOrganism(ComplexFieldNames.COMPLEX_ORGANISM, ComplexFieldNames.COMPLEX_ORGANISM_F, solrDocument, ontologyTerm);
            // sort field
            solrDocument.addField(ComplexFieldNames.COMPLEX_ORGANISM_SORT, ontologyTerm.getId());
            // stored field to retrieve from the index
            solrDocument.addField(ComplexFieldNames.ORGANISM_NAME, ontologyTerm.getName()+"; "+ontologyTerm.getId());
            // add parents to complex_organism_ontology
            for ( OntologyTerm parent : ontologyTerm.getAllParentsToRoot ( true ) ) {
                enrichOrganism(ComplexFieldNames.COMPLEX_ORGANISM, null, solrDocument, parent);
            }
        }
    }

    protected void enrichOrganism(String fieldName, String facetField, SolrInputDocument solrDocument, OntologyTerm ontologyTerm) {
        // name
        solrDocument.addField ( fieldName, ontologyTerm.getName( ) ) ;
        // synonyms
        for ( OntologyTerm synonym : ontologyTerm.getSynonyms ( ) ) {
            solrDocument.addField ( fieldName, synonym ) ;
        }
        // taxid
        solrDocument.addField ( fieldName, ontologyTerm.getId() ) ;
        // facet field
        if (facetField != null){
            solrDocument.addField(facetField, ontologyTerm.getName()+"("+ontologyTerm.getId()+")");
        }
    }

    // is for enrich complex_xref* fields and return a SolrDocument
    public void enrichInteractionXref(Collection<? extends Xref> interactorXrefs, SolrInputDocument solrDocument) throws SolrServerException {
        enrichXrefs(ComplexFieldNames.COMPLEX_XREF, ComplexFieldNames.COMPLEX_XREF_EXACT, ComplexFieldNames.COMPLEX_ID, interactorXrefs, solrDocument, true);

    }

    public void enrichInteractorXref(Collection<? extends Xref> interactorXrefs, SolrInputDocument solrDocument) throws SolrServerException {
        enrichXrefs(ComplexFieldNames.INTERACTOR_XREF, ComplexFieldNames.INTERACTOR_XREF_EXACT, ComplexFieldNames.INTERACTOR_ID, interactorXrefs, solrDocument, false);

    }

    protected void enrichXrefs(String xrefFieldName, String xrefFieldExact, String idFieldName, Collection<? extends Xref> interactorXrefs, SolrInputDocument solrDocument, boolean checkExpEvidence) throws SolrServerException {
        // needed variables to enrich
        String shortLabel = null ;
        String ID = null ;
        OntologyTerm ontologyTerm_aux = null ;
        if (checkExpEvidence && this.mapOfPsicquicClients == null){
            try {
                initialiseMapOfPsicquicClients();
                this.reader = new PsimiTabReader();
            } catch (IOException e) {
                log.error("ERROR initialising map of PSICQUIC clients. May ignore some psicquic clients.", e);
            }
        }

        // we range all xrefs for the interaction
        for ( Xref interactorXref : interactorXrefs ){
            // get short label and primary id from the xref
            if (interactorXref.getCvDatabase() != null){
                shortLabel = interactorXref.getCvDatabase( ).getShortLabel() ;
                ID = interactorXref.getPrimaryId() ;
                if ( shortLabel != null && ID != null ){
                    if (interactorXref.getCvXrefQualifier() != null){
                        // check experimental evidence
                        if ( interactorXref.getCvXrefQualifier ( ).getShortLabel().equalsIgnoreCase(EXP_EVIDENCE) ){
                            processXref(xrefFieldName, xrefFieldExact, solrDocument, shortLabel, ID);

                            if (checkExpEvidence){
                                if (this.mapOfPsicquicClients.containsKey ( shortLabel ) ){
                                    PsicquicSimpleClient client = this.mapOfPsicquicClients.get(shortLabel);
                                    InputStream stream = null;
                                    try{
                                        stream = client.getByInteraction(ID, PsicquicSimpleClient.MITAB25, 0, 1);
                                        Iterator<psidev.psi.mi.tab.model.BinaryInteraction> binaryIterator = reader.iterate(stream);
                                        if (binaryIterator.hasNext()){
                                            psidev.psi.mi.tab.model.BinaryInteraction<psidev.psi.mi.tab.model.Interactor> binary = binaryIterator.next();
                                            for (CrossReference ref : binary.getPublications()){
                                                String db = ref.getDatabase() ;
                                                String pubid = ref.getIdentifier() ;
                                                if (db != null && pubid != null){
                                                    solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, db ) ;
                                                    solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, pubid ) ;
                                                    solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, db + ":" + pubid ) ;
                                                }
                                            }
                                        }
                                    }
                                    catch(IOException e){
                                        log.error("ERROR with psicquic client serving "+shortLabel+" data. Cannot fetch data.", e);
                                    }
                                    finally {

                                        if (stream != null){
                                            try {
                                                stream.close();
                                            } catch (IOException e) {
                                                log.error(e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // we have an identity, secondary or intact-secondary
                        else if (CvXrefQualifier.IDENTITY_MI_REF.equalsIgnoreCase(interactorXref.getCvXrefQualifier().getIdentifier())
                                || CvXrefQualifier.SECONDARY_AC_MI_REF.equalsIgnoreCase(interactorXref.getCvXrefQualifier().getIdentifier())
                                || INTACT_SECONDARY.equalsIgnoreCase(interactorXref.getCvXrefQualifier().getShortLabel())){
                            // enrich identity xref
                            solrDocument.addField ( idFieldName, shortLabel ) ;
                            solrDocument.addField ( idFieldName, ID ) ;
                            solrDocument.addField ( idFieldName, shortLabel + ":" + ID ) ;
                        }
                        else{
                            processXref(xrefFieldName, xrefFieldExact, solrDocument, shortLabel, ID);
                        }
                    }
                    else{
                        processXref(xrefFieldName, xrefFieldExact, solrDocument, shortLabel, ID);
                    }
                }
            }

            // check if the "db_<id>" can be enriched
            if (isExpandableOntology(shortLabel)) {
                ontologyTerm_aux = findOntologyTerm ( ID, shortLabel );
                // then add short label, id, name and synonyms to complex_xref
                if (ontologyTerm_aux != null){
                    // exact and non exact values
                    solrDocument.addField ( xrefFieldExact, ontologyTerm_aux.getName ( ) ) ;
                    solrDocument.addField ( xrefFieldName, ontologyTerm_aux.getName ( ) ) ;
                    for ( OntologyTerm synonym : ontologyTerm_aux.getSynonyms ( ) ) {
                        solrDocument.addField ( xrefFieldExact, synonym.getName ( ) ) ;
                        solrDocument.addField ( xrefFieldName, synonym.getName ( ) ) ;
                    }
                    // add parents to complex_xref_ontology
                    for ( OntologyTerm parent : ontologyTerm_aux.getAllParentsToRoot ( true ) ) {
                        solrDocument.addField ( xrefFieldName, parent.getId() ) ;
                        solrDocument.addField ( xrefFieldName, parent.getName ( ) ) ;
                        for ( OntologyTerm synonym : parent.getSynonyms ( ) ) {
                            solrDocument.addField ( xrefFieldName, synonym.getName ( ) ) ;
                        }
                    }
                }
            }
        }
    }

    protected void processXref(String xrefFieldName, String xrefFieldExact, SolrInputDocument solrDocument, String shortLabel, String ID) throws SolrServerException {
        OntologyTerm ontologyTerm_aux;// enrich exact xref
        if (xrefFieldExact != null){
            solrDocument.addField ( xrefFieldExact, shortLabel ) ;
            solrDocument.addField ( xrefFieldExact, ID ) ;
            solrDocument.addField ( xrefFieldExact, shortLabel + ":" + ID ) ;
        }
        // enrich normal xref
        solrDocument.addField ( xrefFieldName, shortLabel ) ;
        solrDocument.addField ( xrefFieldName, ID ) ;
        solrDocument.addField ( xrefFieldName, shortLabel + ":" + ID ) ;
    }

    /****************************/
    /*      Enrich Methods      */
    /****************************/
    // enrich fields in the SolrDocument passed as parameter
    public void enrich (
            Interaction interaction,
            SolrInputDocument solrDocument )
            throws Exception {
        // check parameters and information
        if ( interaction == null ) { return ; }

        // Enrich interaction type
        enrichInteractionType(interaction.getCvInteractionType(), solrDocument) ;

        // Enrich Complex Organism fields
        enrichOrganism(interaction, solrDocument) ;

        // Enrich Complex Xref fields
        enrichInteractionXref(interaction.getXrefs(), solrDocument) ;
    }

    // enrich fields in an empty SolrDocument
    public SolrInputDocument enrich ( InteractionImpl interaction ) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        enrich ( interaction, doc) ;
        return doc;
    }

    /**************************/
    /*      Find Methods      */
    /**************************/
    // is for find the ontology term for a specific interaction. Needs to take the host organism coming from experiment
    public OntologyTerm findOrganism(Interaction interaction) throws SolrServerException {
        // get BioSource information from interaction
        Collection<Experiment> exps = interaction.getExperiments();
        if (exps.isEmpty()){
            return null;
        }
        // TODO what do we do when we have several experiments?
        Experiment experiment = exps.iterator().next();
        BioSource biosource = experiment.getBioSource();

        // return an OntologyTerm using tax id and short label
        return biosource != null ? findOntologyTerm(biosource.getTaxId(), biosource.getShortLabel()) : null ;
    }

    public String getComplexProperties() {
        return complexProperties;
    }

    public void setComplexProperties(String complexProperties) throws IOException {
        this.complexProperties = complexProperties;
        initialiseMapOfPsicquicClients();
        this.reader = new PsimiTabReader();
    }
}

