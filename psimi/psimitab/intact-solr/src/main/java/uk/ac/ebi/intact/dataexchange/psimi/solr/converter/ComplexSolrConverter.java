package uk.ac.ebi.intact.dataexchange.psimi.solr.converter;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import org.springframework.batch.core.ChunkListener;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.TreeComplexComponents;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.ComplexSolrEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.model.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.*;

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
                        solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF_ONTOLOGY, ID ) ;
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

    protected SolrInputDocument ComplexID ( InteractionImpl complex,
                                            SolrInputDocument solrDocument ) throws Exception {
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getAc ( ) ) ;
        if ( complex.getOwner ( ) != null ) {
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getOwner ( ) .getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getOwner ( ) .getShortLabel ( ) + ":" + complex.getAc ( ) ) ;
        }
        String db, id ;
        for ( Xref xref : complex.getXrefs ( ) ) {
            id = xref.getPrimaryId ( ) ;
            db = xref.getCvDatabase ( ) .getShortLabel ( ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, id ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, db ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, new StringBuilder ( )
                    .append ( db ) .append ( ":" ) .append ( id ) .toString ( ) ) ;
        }
        return solrDocument ;
    }

    protected SolrInputDocument ComplexAliasName ( InteractionImpl complex,
                                                   SolrInputDocument solrDocument ) throws Exception {
        String a_name, a_type;
        CvAliasType alias_type;
        String[] names = new String[4] ;

        solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, complex.getShortLabel ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, complex.getFullName ( ) ) ;
        names[3] = complex.getShortLabel ( ) ;

        for ( Alias alias : complex.getAliases ( ) ) {
            a_name = alias.getName ( ) ;
            a_type = alias.getCvAliasType ( ) .getShortLabel ( ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, a_name ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, a_type ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, new StringBuilder ( )
                    .append ( a_type ) .append ( ":" ) .append ( a_name ) .toString ( ) ) ;
            alias_type = alias.getCvAliasType();
            //  We have to change getShortLabel to a static
            // Check for the recommended name > systematic name > first synonym > short label
            if ( alias_type. getShortLabel ( ) .equals ( "recommended name" ) ) {
                names[0] = alias.getName ( ) ;
            }
            else {
                if ( alias_type. getShortLabel ( ) .equals ( "systematic name" ) ) {
                    names[1] = alias.getName ( ) ;
                }
            }
        }

        // Only is necessary search the first synonym if we have not a recommended name or a systematic name
        if ( names[0] == null && names[1] == null ) {
            OntologyTerm ontologyTerm = complexSolrEnricher.findOntologyTerm ( complex ) ;
            if ( ontologyTerm != null ) {
                for ( OntologyTerm synonym : ontologyTerm.getSynonyms ( ) ) {
                    if ( synonym != null ) {
                        names[2] = synonym.getName ( ) ;
                        break ;
                    }
                }
            }
        }

        // Assign the value for complex_name
        int i = 0;
        while ( i < 4 ) {
            if ( names[i] != null ) break ;
            ++i ;
        }
        solrDocument.addField ( ComplexFieldNames.COMPLEX_NAME, names[i] ) ;
        return solrDocument ;
    }

    protected SolrInputDocument ComplexSourceOrganismName ( InteractionImpl complex,
                                                            SolrInputDocument solrDocument ) throws Exception {
        CvObject cvObject = null ;
        for ( Experiment experiment : complex.getExperiments ( ) ) {
            cvObject = experiment.getCvInteraction ( ) ;
            solrDocument.addField ( ComplexFieldNames.SOURCE, cvObject.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.SOURCE_F, cvObject.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.SOURCE, cvObject.getFullName ( ) ) ;

            OntologyTerm ontologyTerm = complexSolrEnricher.findOntologyTermByName ( experiment.getBioSource ( ) .getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM, ontologyTerm.getName ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM_F, ontologyTerm.getName ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.ORGANISM_NAME, ontologyTerm.getName ( ) ) ;
        }
        return solrDocument ;
    }

    protected SolrInputDocument ComplexInteractionType ( InteractionImpl complex,
                                                        SolrInputDocument solrDocument ) throws Exception {
        CvInteractionType type = complex.getCvInteractionType ( ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, type.getShortLabel ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, type.getFullName ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, type.getIdentifier ( ) ) ;
        for ( Alias alias : type.getAliases ( ) ) {
            solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, alias.getName ( ) ) ;
        }
        // add info to interaction_type_f field:
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE_F, type.getFullName ( ) ) ;
        return solrDocument ;
    }

    protected SolrInputDocument InteractorBiorole ( InteractionImpl complex,
                                                    SolrInputDocument solrDocument ) throws Exception {
        CvBiologicalRole biologicalRole = null ;
        CvFeatureType featureType = null ;
        String a_name, a_type;
        for ( Component component : complex.getComponents ( ) ) {
            biologicalRole = component.getCvBiologicalRole ( ) ;
            solrDocument.addField ( ComplexFieldNames.BIOROLE, biologicalRole.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.BIOROLE_F, biologicalRole.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.BIOROLE, biologicalRole.getFullName ( ) ) ;
            for ( Feature feature : component.getFeatures ( ) ) {
                featureType = feature.getCvFeatureType ( ) ;
                solrDocument.addField ( ComplexFieldNames.FEATURES, featureType.getShortLabel ( ) ) ;
                solrDocument.addField ( ComplexFieldNames.FEATURES_F, featureType.getShortLabel ( ) ) ;
                solrDocument.addField ( ComplexFieldNames.FEATURES, featureType.getFullName ( ) ) ;
            }

            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, component.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, component.getFullName ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS_F, component.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS_F, component.getFullName ( ) ) ;
            for ( Alias alias : component.getAliases ( ) ) {
                a_name = alias.getName ( ) ;
                a_type = alias.getCvAliasType ( ) .getShortLabel() ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, a_name ) ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, a_type ) ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, new StringBuilder ( )
                        .append ( a_type ) .append ( ":" ) .append(a_name) .toString() ) ;
            }
        }
        return solrDocument ;
    }

    protected SolrInputDocument PublicationID ( InteractionImpl complex,
                                                SolrInputDocument solrDocument ) throws Exception {

        // that was the old code
        /*InteractionImpl interaction = IntactContext.getCurrentInstance ( )
                                        .getDaoFactory ( )
                                        .getInteractionDao ( )
                                        .getByAc ( complex.getAc ( ) ) ;
        for ( Experiment experiment : interaction.getExperiments ( ) ) {
            for ( Xref xref : experiment.getPublication ( ) .getXrefs ( ) ) {
                if ( xref.hasValidPrimaryId ( ) ) {
                    solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, xref.getPrimaryId ( ) ) ;
                }
            }
        }
        */
        String DB = null ;
        String ID = null ;
        HashMap < String, String > map = new HashMap < String, String > ( ) ;
        String list = null ;
        // Make the chunks for batching
        for ( Xref xref : complex.getXrefs ( ) ) {
            DB = xref.getCvDatabase ( ).getShortLabel ( ) ;
            ID = xref.getPrimaryId ( ) ;
            if ( xref.getCvXrefQualifier ( ) .equals ( "exp_evidence" ) ){
                System.err.println( xref.toString ( ) + ": " + DB + ", " + ID );
                if ( DB != null && ID != null ) {
                    if ( ! map.containsKey ( DB ) ) list = "" ;
                    else list = map.get ( DB ) ;
                    list += " " + ID  ;
                    map.put(DB, list) ;
                }
            }
        }

        // that is the new code
        DefaultPsicquicRegistryClient defaultPsicquicRegistryClient = new DefaultPsicquicRegistryClient ( ) ;
        PsicquicSimpleClient psicquicSimpleClient = null ;
        PsimiTabReader psimiTabReader = new PsimiTabReader ( ) ;
        for ( ServiceType serviceType : defaultPsicquicRegistryClient.listActiveServices ( ) ) {
            psicquicSimpleClient = new PsicquicSimpleClient ( serviceType.getRestUrl ( ) ) ;
            InputStream inputStream = null ;
            try{
                inputStream = psicquicSimpleClient.getByQuery("interactor_id:" + map.get(serviceType.getName())) ;
                for ( BinaryInteraction interaction : psimiTabReader.read ( inputStream ) ){
                    for ( Object crossReference : interaction.getPublications ( ) ) {
                        DB = ((CrossReference)crossReference).getDatabase ( ) ;
                        ID = ((CrossReference)crossReference).getIdentifier ( ) ;
                        solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, DB ) ;
                        solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, ID ) ;
                        solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, new StringBuilder ( )
                                .append ( DB )
                                .append ( ":" )
                                .append ( ID )
                                .toString ( )
                        ) ;
                    }
                }
            }
            catch (SocketTimeoutException e) {
                // we have to log that
            }
            finally {
                if ( inputStream != null ) inputStream.close ( ) ;
            }
        }
        return solrDocument ;
    }

    /*****************************/
    /*      Convert Methods      */
    /*****************************/
    public SolrInputDocument convertComplexToSolrDocument (
            InteractionImpl complex,
            SolrInputDocument solrDocument )
            throws Exception {

        //////////////////////////
        ///   COMPLEX FIELDS   ///
        //////////////////////////

        // add info to publication_id and complex_id field: ac, owner, (db, id and db:id) from xrefs
        solrDocument = ComplexID ( complex, solrDocument ) ;

        // add info to complex_alias field: short label, full name, (name and type) from  alias
        // and add info to complex_name
        solrDocument = ComplexAliasName ( complex, solrDocument ) ;

        // add info to source and source_f, complex_organism, complex_organism_f and organism_name fields:
        solrDocument = ComplexSourceOrganismName ( complex, solrDocument ) ;

        // add info to complex_organism_ontology field:
        // It will do by the enricher
        // add info to interaction_type field:
        solrDocument = ComplexInteractionType ( complex, solrDocument ) ;

        // add info to interaction_type_ontology field:
        // It will do by the enricher
        // add info to complex_xref field:
        // It will do by the enricher
        // add info to complex_xref_ontology field:
        // It will do by the enricher
        // add info to complex_AC field:
        solrDocument.addField ( ComplexFieldNames.COMPLEX_AC, complex.getAc ( ) ) ;
        // add info to description field:
        CvTopic cvTopic = null ;
        for ( Annotation annotation : complex.getAnnotations ( ) ) {
            cvTopic = annotation != null ? annotation.getCvTopic ( ) : null ;
            if ( cvTopic != null && cvTopic.getShortLabel ( ) .equals ( "curated-complex" ) ) {
                solrDocument.addField ( ComplexFieldNames.DESCRIPTION, annotation.getAnnotationText ( ) ) ;
                break ; // We only want the first one
            }
        }
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
                        solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF_ONTOLOGY, ID ) ;
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
        TreeComplexComponents tree = new TreeComplexComponents(complex);

        // add info to interactor_id, interactor_xref, interactor_xref_ontology fields:
        solrDocument = tree.setFields(solrDocument);

        // add info to STC field:
        solrDocument.addField ( ComplexFieldNames.STC, tree.getSTC() ) ;

        // add info to param field:
        solrDocument.addField ( ComplexFieldNames.PARAM, complex.getParameters ( ) .isEmpty ( ) ) ;

        // add info to features, features_f, biorole, biorole_f, interactor_alias and interactor_alias_f fields:
        solrDocument = InteractorBiorole ( complex, solrDocument ) ;
        // add info to interactor_type field:
        solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE,
                complex.getCvInteractionType ( ) .getFullName ( ) ) ;
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

        // add info to source_ontology field:
        // It will do by the enricher
        // add info to number_participants field:
        solrDocument.addField ( ComplexFieldNames.NUMBER_PARTICIPANTS, tree.getNumberOfParticipants() ) ;
        //add info to publication_id

        //add info to update
        solrDocument.addField ( ComplexFieldNames.UDATE, complex.getUpdated ( ) .getDate ( ) ) ;

        //add info to publication_id
        solrDocument = PublicationID ( complex, solrDocument ) ;

        /////////////////////////
        ///   ENRICH FIELDS   ///
        /////////////////////////

        // Enrich the Solr Document and return that
        BufferedWriter traceFile = new BufferedWriter( new FileWriter("TraceInfo.txt", true));
        traceFile.write("TraceInfo: "+solrDocument.toString() + "\n");
        traceFile.close();
        return complexSolrEnricher.enrich ( complex, solrDocument ) ;
    }

    public SolrInputDocument convertComplexToSolrDocument ( InteractionImpl complex ) throws Exception {
        return convertComplexToSolrDocument ( complex, new SolrInputDocument ( ) ) ;
    }


}
