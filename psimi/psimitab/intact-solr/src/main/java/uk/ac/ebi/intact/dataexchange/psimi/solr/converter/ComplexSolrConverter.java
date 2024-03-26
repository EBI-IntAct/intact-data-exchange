package uk.ac.ebi.intact.dataexchange.psimi.solr.converter;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.TreeComplexComponents;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.ComplexSolrEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.util.ComplexUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 14/08/13
 */
public class ComplexSolrConverter {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private ComplexSolrEnricher complexSolrEnricher ;
    private static final String COMPLEX_RECOMMENDED_NAME= "complex recommended name";
    private static final String COMPLEX_RECOMMENDED_NAME_MI= "MI:1315";
    private static final String COMPLEX_SYSTEMATIC_NAME= "complex systematic name";
    private static final String COMPLEX_SYSTEMATIC_NAME_MI= "MI:1316";
    private static final String COMPLEX_SYNONYM= "complex synonym";
    private static final String COMPLEX_SYNONYM_MI= "MI:0673";
    private static final String CURATED_COMPLEX="curated-complex";

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexSolrConverter ( OntologySearcher ontologySearcher ) {
        this.complexSolrEnricher = new ComplexSolrEnricher( ontologySearcher ) ;
    }
    public ComplexSolrConverter ( SolrServer solrServer ) {
        this ( new OntologySearcher ( solrServer ) ) ;
    }

    public void setComplexPropertiesFile(String file) throws IOException {
        this.complexSolrEnricher.setComplexProperties(file);
    }

    /******************************************/
    /*      Protected methods to convert      */
    /******************************************/

    protected void indexComplexAC(InteractionImpl complex,
                                  SolrInputDocument solrDocument) throws Exception {
        Xref complexPrimaryXref = ComplexUtils.getComplexPrimaryXref(complex);
        // stored field
        solrDocument.addField(ComplexFieldNames.AC, complex.getAc()); // for old ac
        if(complexPrimaryXref!=null){
            solrDocument.addField(ComplexFieldNames.COMPLEX_AC,complexPrimaryXref.getPrimaryId());
            solrDocument.addField(ComplexFieldNames.COMPLEX_VERSION,complexPrimaryXref.getDbRelease());
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complexPrimaryXref.getPrimaryId() ) ;
        }
        // search fields
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getAc ( ) ) ;
        // index source of complex id
        if ( complex.getOwner ( ) != null ) {
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getOwner ( ).getShortLabel() ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getOwner ( ).getShortLabel() + ":" + complex.getAc ( ) ) ;
        }
    }

    protected void indexComplexNames(InteractionImpl complex,
                                     SolrInputDocument solrDocument) throws Exception {

        // shortname
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, complex.getShortLabel ( ) ) ;
        // fullname
        if (complex.getFullName() != null){
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, complex.getFullName ( ) ) ;
        }

        String firstRecommended=null;
        String firstSystematic=null;
        String firstComplexSynonym=null;
        String firstAlias=null;

        for ( Alias alias : complex.getAliases ( ) ) {
            if (alias.getName() != null){
                if (alias.getCvAliasType() != null){
                    CvAliasType type = alias.getCvAliasType();
//                    solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, type.getShortLabel() ) ;
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, alias.getName() ) ;
//                    solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, type.getShortLabel()+":"+alias.getName()) ;
                    if (firstRecommended == null && COMPLEX_RECOMMENDED_NAME_MI.equals(type.getIdentifier())){
                        firstRecommended = alias.getName();
                    }
                    else if (firstSystematic == null && COMPLEX_SYSTEMATIC_NAME_MI.equals(type.getIdentifier())){
                        firstSystematic = alias.getName();
                    }
                    else if (firstComplexSynonym == null && COMPLEX_SYNONYM_MI.equals(type.getIdentifier())){
                        firstComplexSynonym = alias.getName();
                    }
                }
                else if (firstAlias == null){
                    firstAlias = alias.getName();
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, alias.getName());
                }
                else{
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, alias.getName());
                }
            }
        }

        // we index complex name
        if ( firstRecommended != null ) {
            solrDocument.addField(ComplexFieldNames.COMPLEX_NAME, firstRecommended);
        }
        else if ( firstSystematic != null ) {
            solrDocument.addField(ComplexFieldNames.COMPLEX_NAME, firstSystematic);
        }
        else if ( firstComplexSynonym != null ) {
            solrDocument.addField(ComplexFieldNames.COMPLEX_NAME, firstComplexSynonym);
        }
        else if ( firstAlias != null ) {
            solrDocument.addField(ComplexFieldNames.COMPLEX_NAME, firstAlias);
        }
        else{
            solrDocument.addField(ComplexFieldNames.COMPLEX_NAME, complex.getShortLabel());
        }
    }

    protected void indexComplexSource(InteractionImpl complex,
                                                   SolrInputDocument solrDocument) throws Exception {
        if (complex.getOwner() != null){
            Institution owner = complex.getOwner();
            // short name
            solrDocument.addField ( ComplexFieldNames.SOURCE, owner.getShortLabel ( ) ) ;
            // facet field
            solrDocument.addField ( ComplexFieldNames.SOURCE_F, owner.getShortLabel() ) ;
            // full name
            solrDocument.addField ( ComplexFieldNames.SOURCE, owner.getFullName ( ) ) ;
        }
    }

    protected void indexUpdateDate(InteractionImpl complex,
                                      SolrInputDocument solrDocument) throws Exception {
        if (complex.getUpdated() != null){
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy/MM/dd");
            String formattedDate = simpleFormat.format(complex.getUpdated());
            solrDocument.addField(ComplexFieldNames.UDATE, Integer.parseInt(formattedDate.replace("/", ""))); //int representation of date
        }
    }

    /*****************************/
    /*      Convert Methods      */
    /*****************************/
    protected void toSolrDocument(
            InteractionImpl complex,
            SolrInputDocument solrDocument)
            throws Exception {

        //////////////////////////
        ///   COMPLEX FIELDS   ///
        //////////////////////////

        // add info to complex_id field: ac, owner, (db, id and db:id) from xrefs
        indexComplexAC(complex, solrDocument) ;

        // add info to complex_alias field: short label, full name, (name and type) from  alias
        // and add info to complex_name
        indexComplexNames(complex, solrDocument) ;

        // add info to source and source_f, complex_organism, complex_organism_f and organism_name fields:
        indexComplexSource(complex, solrDocument) ;

        // add update date
        indexUpdateDate(complex, solrDocument);

        // add info to complex_organism_ontology field:
        // It will do by the enricher
        // add info to interaction_type field:
        if (complex.getCvInteractionType() != null){
            this.complexSolrEnricher.enrichInteractionType(complex.getCvInteractionType(), solrDocument);
        }

        if (complex.getCvInteractorType() != null){
            this.complexSolrEnricher.enrichComplexType(complex.getCvInteractorType(), solrDocument);
        }

        // add info to interaction_type_ontology field:
        // It will do by the enricher
        // add info to complex_xref field:
        // It will do by the enricher
        // add info to complex_xref_ontology field:
        // Enrich Complex Organism fields
        this.complexSolrEnricher.enrichOrganism(complex, solrDocument) ;

        // Enrich Complex Xref fields
        this.complexSolrEnricher.enrichInteractionXref(complex.getXrefs(), solrDocument) ;

        // add info to description field:
        CvTopic cvTopic = null ;
        for ( Annotation annotation : complex.getAnnotations ( ) ) {
            cvTopic = annotation != null ? annotation.getCvTopic ( ) : null ;
            if ( cvTopic != null && cvTopic.getShortLabel ( ) .equalsIgnoreCase( CURATED_COMPLEX ) && annotation.getAnnotationText() != null) {
                solrDocument.addField ( ComplexFieldNames.DESCRIPTION, annotation.getAnnotationText ( ) ) ;
                break ; // We only want the first one
            }
        }

        // add info to param field:
        solrDocument.addField ( ComplexFieldNames.PARAM, !complex.getParameters ( ).isEmpty ( ) ) ;

        /////////////////////////////
        ///   INTERACTOR FIELDS   ///
        /////////////////////////////

        // Create a TreeComplexComponents
        TreeComplexComponents tree = new TreeComplexComponents(complex, this.complexSolrEnricher);

        // add info to interactor_id, interactor_xref, interactor_xref_ontology fields, biorole, feature:
        tree.indexFields(solrDocument);

        // add info to STC field:
        solrDocument.addField ( ComplexFieldNames.STC, tree.getSTC() ) ;

        // add info to source_ontology field:
        // It will do by the enricher
        // add info to number_participants field:
        solrDocument.addField ( ComplexFieldNames.NUMBER_PARTICIPANTS, tree.getNumberOfParticipants() ) ;

        // store serialised participants
        for (Component participant : ComplexUtils.mergeParticipants(complex.getComponents())) {
            complexSolrEnricher.enrichSerialisedParticipant(participant, solrDocument);
        }

        // add info to interactor_type_ontology field:
        // It will do by the enricher

        ///////////////////////////
        ///   PSICQUIC FIELDS   ///
        ///////////////////////////
        // Most of psicquic fields have been indexed above

        // add info to biorole_ontology field:
        // It will do by the enricher
        // add info to features_ontology field:
        // It will do by the enricher

    }

    public SolrInputDocument toSolrDocument(InteractionImpl complex) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        toSolrDocument(complex, doc) ;
        return doc;
    }

}
