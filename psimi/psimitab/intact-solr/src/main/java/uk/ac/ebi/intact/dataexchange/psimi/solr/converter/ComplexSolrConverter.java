package uk.ac.ebi.intact.dataexchange.psimi.solr.converter;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.TreeComplexComponents;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.ComplexSolrEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.model.*;

import java.text.SimpleDateFormat;

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

    /******************************************/
    /*      Protected methods to convert      */
    /******************************************/
    /*
    // recursive method to get participants and their sons.
    protected SolrInputDocument participants (
            InteractionImpl complex,
            SolrInputDocument solrDocument )
            throws Exception {

        Map < String, Integer > numbers = new HashMap < String, Integer > ( ) ;
        Set < String > indexed = new HashSet < String > ( ) ;
        boolean stc = false;

        solrDocument = participants ( complex, solrDocument, numbers, indexed) ;

        solrDocument.addField ( ComplexFieldNames.PARAM, complex.getParameters ( ) .isEmpty ( ) ) ;

        return solrDocument ;
    }

    protected SolrInputDocument participants (
            Interactor complex,
            SolrInputDocument solrDocument,
            Map < String, Integer > numbers,
            Set < String > indexed )
            throws Exception {

        float number_participants = 0.0f, number_recursive = 0.0f, stoichiometry = 0.0f ;
        boolean stc = false;

        // if interactorAux is an instance of InteractionImpl we need to get all its components
        if ( complex instanceof InteractionImpl ) {
            for ( Component component : ( ( InteractionImpl ) complex ) .getComponents() ) {
                stoichiometry = component.getStoichiometry ( ) ;
                stc |= stoichiometry != 0.0f ;
                number_participants += stoichiometry == 0.0f ? 1.0f : stoichiometry ;
                number_recursive += number_participants;
                participants(component.getInteractor ( ), solrDocument, numbers, indexed);
            }
        }

        String AC = complex.getAc ( ) ;
        if ( ! indexed.contains ( AC ) ) {
            // now, we get the information of the interactorAux
            String shortLabel = null ;
            String ID = null;
            CvXrefQualifier cvXrefQualifier = null ;
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, AC ) ;
            for ( InteractorXref xref : complex.getXrefs ( ) ) {
                cvXrefQualifier = xref.getCvXrefQualifier ( ) ;
                if ( cvXrefQualifier != null &&
                        (  cvXrefQualifier.getIdentifier ( ) .equals ( CvXrefQualifier.IDENTITY_MI_REF )
                                || cvXrefQualifier.getIdentifier ( ) .equals ( CvXrefQualifier.SECONDARY_AC_MI_REF )
                                || cvXrefQualifier.getShortLabel ( ) .equalsIgnoreCase ( "intact-secondary" )
                        )
                    ) {
                    shortLabel = xref.getCvDatabase ( ) .getShortLabel ( ) ;
                    ID = xref.getPrimaryId() ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, ID ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, shortLabel ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID,
                            new StringBuilder ( ) .append ( shortLabel ) .append ( ":" ) .append ( ID ) .toString ( ) ) ;
                }
                else {
                    if ( xref.getCvDatabase ( ) .getIdentifier() .equals ( CvDatabase.GO_MI_REF ) ) {
                        solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF_EXACT, ID ) ;
                    }
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, ID ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, shortLabel ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF,
                            new StringBuilder ( ) .append ( shortLabel ) .append ( ":" ) .append ( ID ) .toString ( ) ) ;
                    if ( xref.getCvXrefQualifier ( ) != null ){
                        solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, cvXrefQualifier.getShortLabel ( ) ) ;
                    }
                }
            }
            indexed.add ( AC ) ;
        }
        solrDocument.addField ( ComplexFieldNames.STC, stc ) ;
        return solrDocument ;
    }
    */

    protected void indexComplexAC(InteractionImpl complex,
                                  SolrInputDocument solrDocument) throws Exception {
        // stored field
        solrDocument.addField(ComplexFieldNames.COMPLEX_AC, complex.getAc());
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
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, type.getShortLabel() ) ;
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, alias.getName() ) ;
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, type.getShortLabel()+":"+alias.getName()) ;
                    if (firstRecommended == null && COMPLEX_RECOMMENDED_NAME_MI.equals(type.getIdentifier())){
                        firstRecommended = alias.getName();
                    }
                    else if (firstSystematic == null && COMPLEX_SYSTEMATIC_NAME_MI.equals(type.getIdentifier())){
                        firstSystematic = alias.getName();
                    }
                    else if (firstComplexSynonym == null && COMPLEX_SYNONYM.equals(type.getIdentifier())){
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
    public void toSolrDocument(
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
            this.complexSolrEnricher.enrichInteractorType(complex.getCvInteractorType(), solrDocument);
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

        // add info to interactor_id field:
        // to avoid stack overflow problems I change the recursive algorithm to this iterative algorithm
        // I decided to make a recursive algorithm can be easier, because this algorithm does not calculate
        //  the number of participants right.
        /*Stack < Interactor > stack = new Stack < Interactor > ( ) ;
        stack.push ( complex ) ;
        Set < String > indexed = new HashSet < String > ( ) ;
        indexed.add ( complex.getAc ( ) ) ;
        Interactor interactorAux;
        boolean stc = false;
        float stoichiometry = 0.0f ;
        int number_participants = 0 ;
        do {
            interactorAux = stack.pop ( ) ;
            // if interactorAux is an instance of InteractionImpl we need to get all its components
            if ( interactorAux instanceof InteractionImpl ) {
                String AC = null ;
                for ( Component component : ( ( InteractionImpl) interactorAux ) .getComponents ( ) ) {
                    AC = component.getAc ( ) ;
                    stoichiometry = component.getStoichiometry ( ) ;
                    if ( ! indexed.contains ( AC ) ) {
                        stack.push ( component.getInteractor ( ) ) ;
                        stc |= stoichiometry != 0.0f ;
                        indexed.add ( AC ) ;
                    }
                    number_participants += stoichiometry == 0.0f ? 1 : (int) stoichiometry ;
                }
            }
            // now, we get the information of the interactorAux
            String shortLabel = null ;
            String ID = null;
            CvXrefQualifier cvXrefQualifier = null ;
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, interactorAux.getAc ( ) ) ;
            for ( InteractorXref xref : interactorAux.getXrefs ( ) ) {
                cvXrefQualifier = xref.getCvXrefQualifier ( ) ;
                if ( cvXrefQualifier != null &&
                        (  cvXrefQualifier.getIdentifier ( ) .equals ( CvXrefQualifier.IDENTITY_MI_REF )
                        || cvXrefQualifier.getIdentifier ( ) .equals ( CvXrefQualifier.SECONDARY_AC_MI_REF )
                        || cvXrefQualifier.getShortLabel ( ) .equalsIgnoreCase ( "intact-secondary" ) ) ) {
                    shortLabel = xref.getCvDatabase ( ) .getShortLabel ( ) ;
                    ID = xref.getPrimaryId() ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, ID ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, shortLabel ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID,
                           new StringBuilder ( ) .append ( shortLabel ) .append ( ":" ) .append ( ID ) .toString ( ) ) ;
                }
                else {
                    if ( xref.getCvDatabase ( ) .getIdentifier() .equals(CvDatabase.GO_MI_REF) ) {
                        solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF_EXACT, ID ) ;
                    }
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, ID ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, shortLabel ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF,
                           new StringBuilder ( ) .append ( shortLabel ) .append ( ":" ) .append ( ID ) .toString ( ) ) ;
                    if ( xref.getCvXrefQualifier ( ) != null ){
                        solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, cvXrefQualifier.getShortLabel() ) ;
                    }
                }
            }
        } while ( ! stack.isEmpty ( ) ) ;
        */

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

        ////////////////////////
        ///   OTHER FIELDS   ///
        ////////////////////////


        /////////////////////////
        ///   ENRICH FIELDS   ///
        /////////////////////////

        // Enrich the Solr Document and return that
        /*BufferedWriter traceFile = new BufferedWriter( new FileWriter("TraceInfo.txt", true));
        traceFile.write("TraceInfo: "+solrDocument.toString() + "\n");
        traceFile.close();*/
    }

    public SolrInputDocument toSolrDocument(InteractionImpl complex) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        toSolrDocument(complex, doc) ;
        return doc;
    }


}
