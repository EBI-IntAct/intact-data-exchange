package uk.ac.ebi.intact.dataexchange.psimi.solr.converter;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.ComplexSolrEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.model.*;

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
        this.complexSolrEnricher = new ComplexSolrEnricher ( ontologySearcher ) ;
    }
    public ComplexSolrConverter ( SolrServer solrServer ) {
        this ( new OntologySearcher ( solrServer ) ) ;
    }

    /*****************************/
    /*      Convert Methods      */
    /*****************************/
    public SolrInputDocument convertComplexToSolrDocument (
            InteractionImpl complex,
            SolrInputDocument solrDocument )
            throws  Exception {

        //////////////////////////
        ///   COMPLEX FIELDS   ///
        //////////////////////////

        // add info to publication_id and complex_id field: ac, owner, (db, id and db_id) from xrefs
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getAc ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getOwner ( ) .getShortLabel() ) ;
        String db, id ;
        for ( Xref xref : complex.getXrefs ( ) ) {
            id = xref.getPrimaryId ( ) ;
            db = xref.getCvDatabase ( ) .getShortLabel ( ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, id ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, db ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, new StringBuilder ( )
                        .append ( db ) .append ( "_" ) .append ( id ) .toString ( ) ) ;
            if ( xref.getCvDatabase ( ) .equals ( "pubmed" )
                    && xref.getCvXrefQualifier ( ) .equals ( "see_also" ) ) {
                solrDocument.addField ( ComplexFieldNames.PUBLICATION_ID, xref.getPrimaryId ( ) ) ;
            }
        }
        // add info to complex_alias field: short label, full name, (name and type) from  alias
        // and add info to complex_name
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, complex.getShortLabel ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, complex.getFullName ( ) ) ;
        String a_name, a_type;
        boolean one = false;
        for ( Alias alias : complex.getAliases ( ) ) {
            a_name = alias.getName ( ) ;
            a_type = alias.getCvAliasType ( ) .getShortLabel ( ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, a_name ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, a_type ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, new StringBuilder ( )
                        .append ( a_type ) .append ( "_" ) .append ( a_name ) .toString ( ) ) ;
            if ( ! one && alias.getCvAliasType ( ) . getShortLabel ( ) .equals ( "recommended name" ) ) {
                one = true ;
                solrDocument.addField ( ComplexFieldNames.COMPLEX_NAME, alias.getName ( ) ) ;
            }
        }
        if ( ! one ) {
            solrDocument.addField ( ComplexFieldNames.COMPLEX_NAME, complex.getShortLabel ( ) ) ;
        }
        // add info to complex_organism, complex_organism_f and organism_name fields:
        for ( Experiment experiment : complex.getExperiments ( ) ) {
            OntologyTerm ontologyTerm = complexSolrEnricher.findOntologyTermByName ( experiment.getBioSource ( ) .getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM, ontologyTerm.getName ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM_F, ontologyTerm.getName ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.ORGANISM_NAME, ontologyTerm.getName ( ) ) ;
        }
        // add info to complex_organism_ontology field:
        // It will do by the enricher
        // add info to interaction_type field:
        CvInteractionType type = complex.getCvInteractionType ( ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, type.getShortLabel ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, type.getFullName ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, type.getIdentifier ( ) ) ;
        for ( Alias alias : type.getAliases ( ) ) {
            solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE, alias.getName ( ) ) ;
        }
        // add info to interaction_type_f field:
        solrDocument.addField ( ComplexFieldNames.INTERACTION_TYPE_F, type.getFullName ( ) ) ;
        // add info to interaction_type_ontology field:
        // It will do by the enricher
        // add info to complex_xref field:
        // It will do by the enricher
        // add info to complex_xref_ontology field:
        // It will do by the enricher
        // add info to complex_AC field:
        solrDocument.addField ( ComplexFieldNames.COMPLEX_AC, complex.getAc ( ) ) ;
        // add info to description field:
        for ( Annotation annotation : complex.getAnnotations ( ) ) {
            if ( annotation.getCvTopic ( ) .getShortLabel ( ) .equals ( "curated_complex" ) ) {
                solrDocument.addField ( ComplexFieldNames.DESCRIPTION, annotation.getAnnotationText ( ) ) ;
                break ; // We only want the first one
            }
        }

        /////////////////////////////
        ///   INTERACTOR FIELDS   ///
        /////////////////////////////

        // add info to interactor_id field:
        // to avoid stack overflow problems I change the recursive algorithm to this iterative algorithm
        Stack < Interactor > stack = new Stack < Interactor > ( ) ;
        stack.push ( complex ) ;
        Set < String > indexed = new HashSet < String > ( ) ;
        indexed.add ( complex.getAc ( ) ) ;
        Interactor interactorAux;
        float number_participants = 0 , stoichiometry = 0 ;
        do {
            interactorAux = stack.pop ( ) ;
            // if interactorAux is an instance of InteractionImpl we need to get all its components
            if ( interactorAux instanceof InteractionImpl ) {
                String AC = null ;
                for ( Component component : ( ( InteractionImpl) interactorAux ) .getComponents ( ) ) {
                    AC = component.getAc ( ) ;
                    if ( ! indexed.contains ( AC ) ) {
                        stack.push ( component.getInteractor ( ) ) ;
                        stoichiometry = component.getStoichiometry ( ) ;
                        number_participants += stoichiometry == 0.0f ? 1 : stoichiometry ;
                        indexed.add ( AC ) ;
                    }
                }
            }
            // now, we get the information of the interactorAux
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, interactorAux.getAc ( ) ) ;
            for ( InteractorXref xref : interactorAux.getXrefs ( ) ) {
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, xref.getAc ( ) ) ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, xref.getPrimaryId ( ) ) ;
            }
        } while ( ! stack.isEmpty ( ) ) ;


        // add info to interactor_alias and interactor_alias_f fields:
        for ( Component component : complex.getComponents ( ) ) {
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
                        .append ( a_type ) .append ( "_" ) .append ( a_name ) .toString ( ) ) ;
            }
        }
        // add info to interactor_type field:
        solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE,
                complex.getCvInteractionType ( ) .getFullName ( ) ) ;
        // add info to interactor_type_ontology field:
        // It will do by the enricher

        ///////////////////////////
        ///   PSICQUIC FIELDS   ///
        ///////////////////////////

        // add info to features, features_f, biorole and biorole_f fields:
        for ( Component component : complex.getComponents ( ) ) {
            CvBiologicalRole biologicalRole = component.getCvBiologicalRole ( ) ;
            solrDocument.addField ( ComplexFieldNames.BIOROLE, biologicalRole.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.BIOROLE_F, biologicalRole.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.BIOROLE, biologicalRole.getFullName ( ) ) ;
            for ( Feature feature : component.getFeatures ( ) ) {
                CvFeatureType featureType = feature.getCvFeatureType ( ) ;
                solrDocument.addField ( ComplexFieldNames.FEATURES, featureType.getShortLabel ( ) ) ;
                solrDocument.addField ( ComplexFieldNames.FEATURES_F, featureType.getShortLabel ( ) ) ;
                solrDocument.addField ( ComplexFieldNames.FEATURES, featureType.getFullName ( ) ) ;
            }
        }
        // add info to biorole_ontology field:
        // It will do by the enricher
        // add info to features_ontology field:
        // It will do by the enricher

        ////////////////////////
        ///   OTHER FIELDS   ///
        ////////////////////////

        // add info to source and source_f fields:
        for ( Experiment experiment : complex.getExperiments ( ) ) {
            CvObject cvObject = null ;
            solrDocument.addField ( ComplexFieldNames.SOURCE, cvObject.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.SOURCE_F, cvObject.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.SOURCE, cvObject.getFullName ( ) ) ;
        }
        // add info to source_ontology field:
        // It will do by the enricher
        // add info to number_participants field:
        solrDocument.addField ( ComplexFieldNames.NUMBER_PARTICIPANTS, number_participants ) ;

        /////////////////////////
        ///   ENRICH FIELDS   ///
        /////////////////////////

        // Enrich the Solr Document and return that
        return complexSolrEnricher.enrich ( complex, solrDocument ) ;
    }

    public SolrInputDocument convertComplexToSolrDocument ( InteractionImpl complex ) throws Exception {
        return convertComplexToSolrDocument ( complex, new SolrInputDocument ( ) ) ;
    }


}
