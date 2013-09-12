package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;


import org.apache.commons.collections15.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.model.*;

import java.util.*;

/**
 * Complex Field Enricher is such as Ontoly Field Enricher
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 05/08/13
 */
public class ComplexSolrEnricher  extends BaseFieldEnricher {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private static final Log log = LogFactory.getLog ( ComplexSolrEnricher.class );
    public final OntologySearcher ontologySearcher;
    private final Map < String, Collection < OntologyTerm > > cvCache ;
    private final Map < String, OntologyTerm > ontologyTermCache ;
    private Set < String > expandableOntologies  = null ;
    private Set < String > ontologyTermsToIgnore = null ;

    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexSolrEnricher ( OntologySearcher ontologySearcher_ ) {
        super ( ) ;
        this.ontologySearcher       = ontologySearcher_          ;
        this.cvCache                = new LRUMap ( 50000 )       ;
        this.ontologyTermCache      = new LRUMap ( 10000 )       ;
        this.ontologyTermsToIgnore  = new HashSet < String > ( ) ;
    }

    /*******************************/
    /*      Protected Methods      */
    /*******************************/
    // is for initialize the default terms to ignore
    protected void initializeOntologyTermsToIgnore ( ) {

    }

    // is for enrich interactor_type* fields and return a SolrDocument
    protected SolrInputDocument InteractorPartEnrich ( CvDagObject cvDagObject, SolrInputDocument solrDocument ) throws Exception {
        // add all alias to interactor_type
        for ( CvObjectAlias Alias : cvDagObject.getAliases ( ) ) {
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE, Alias.getName ( ) ) ;
        }
        // add short label, full name and identifier to interactor_type
        solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE,  cvDagObject.getShortLabel ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE,  cvDagObject.getFullName   ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE,  cvDagObject.getIdentifier ( ) ) ;

        // add parents to interaction_type_ontology
        for ( CvDagObject parent : cvDagObject.getParents ( ) ) {
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE_ONTOLOGY, parent.getIdentifier ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE_ONTOLOGY, parent.getShortLabel ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE_ONTOLOGY, parent.getFullName ( ) ) ;
        }
        return solrDocument ;
    }

    // is for enrich complex_organism* fields and return a SolrDocument
    protected SolrInputDocument OrganismPartEnrich ( InteractionImpl interaction, SolrInputDocument solrDocument ) throws Exception {
        // retrieve the ontology term for this interaction (using BioSource)
        final OntologyTerm ontologyTerm = findOntologyTerm ( interaction ) ;
        // add name, all synonyms and tax id to complex_organism
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM, ontologyTerm.getName( ) ) ;
        for ( OntologyTerm synonym : ontologyTerm.getSynonyms ( ) ) {
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM, synonym ) ;
        }
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM, interaction.getBioSource ( ) .getTaxId ( ) ) ;
        // add parents to complex_organism_ontology
        for ( OntologyTerm parent : ontologyTerm.getAllParentsToRoot ( true ) ) {
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM_ONTOLOGY, parent.getId ( ) ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM_ONTOLOGY, parent.getName ( ) ) ;
        }
        return solrDocument ;
    }

    // is for enrich complex_xref* fields and return a SolrDocument
    protected SolrInputDocument XrefPartEnrich ( Collection < InteractorXref > interactorXrefs, SolrInputDocument solrDocument ) throws Exception {
        // needed variables to enrich
        String shortLabel = null ;
        String ID = null ;
        OntologyTerm ontologyTerm_aux = null ;
        // we range all xrefs for the interaction
        for ( InteractorXref interactorXref : interactorXrefs ){
            // only enrich if that xref has a valid primary id
            if ( interactorXref.hasValidPrimaryId ( ) ) {
                // get short label and primary id from the xref
                shortLabel = interactorXref.getCvDatabase ( ) .getShortLabel ( ) ;
                ID = interactorXref.getPrimaryId ( ) ;
                ontologyTerm_aux = findOntologyTerm ( ID, shortLabel ) ;
                // check if the "db_<id>" is GO
                if ( CvDatabase.GO_MI_REF.equals ( ID ) ) {
                    // then add short label, id, name and synonyms to complex_xref
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF, shortLabel ) ;
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF, ID ) ;
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF, shortLabel + ":" + ID ) ;
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF, ontologyTerm_aux.getName ( ) ) ;
                    for ( OntologyTerm synonym : ontologyTerm_aux.getSynonyms ( ) ) {
                        solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF, synonym.getName ( ) ) ;
                    }
                    // add parents to complex_xref_ontology
                    for ( OntologyTerm parent : ontologyTerm_aux.getAllParentsToRoot ( true ) ) {
                        solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF_ONTOLOGY, parent.getId ( ) ) ;
                        solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF_ONTOLOGY, parent.getName ( ) ) ;
                        // add parent synosyms to complex_xref_ontology
                        for ( OntologyTerm synonym : parent.getSynonyms ( ) ) {
                            solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF_ONTOLOGY, synonym.getName ( ) ) ;
                        }
                    }
                }
            }
        }
        return solrDocument ;
    }

    /****************************/
    /*      Enrich Methods      */
    /****************************/
    // enrich fields in the SolrDocument passed as parameter
    public SolrInputDocument enrich (
            InteractionImpl interaction,
            SolrInputDocument solrDocument )
            throws Exception {
        // check parameters and information
        if ( interaction == null ) { return null ; }
        String value = interaction.getShortLabel ( ) ;
        if ( ontologyTermsToIgnore.contains ( value ) ) { return null ; }

        // Enrich Interactor fields
        solrDocument = InteractorPartEnrich ( interaction.getCvInteractionType ( ), solrDocument ) ;

        // Enrich Complex Organism fields
        solrDocument = OrganismPartEnrich ( interaction, solrDocument ) ;

        // Enrich Complex Xref fields
        return XrefPartEnrich ( interaction.getXrefs ( ), solrDocument ) ;
    }

    // enrich fields in an empty SolrDocument
    public SolrInputDocument enrich ( InteractionImpl interaction ) throws Exception {
        return enrich ( interaction, new SolrInputDocument ( ) ) ;
    }

    /**************************/
    /*      Find Methods      */
    /**************************/
    // is for find the ontology term for a specific interaction
    public OntologyTerm findOntologyTerm ( InteractionImpl interaction ) throws SolrServerException {
        // get BioSource information from interaction
        BioSource bioSource = interaction.getBioSource ( ) ;
        // return an OntologyTerm using tax id and short label
        return findOntologyTerm(bioSource.getTaxId(), bioSource.getShortLabel()) ;
    }

    // is for find the ontology term using the tax id and te short name
    private OntologyTerm findOntologyTerm(String taxid, String name) throws SolrServerException {
        // check if the ontology search, tax id and the name are not null
        if ( ontologySearcher == null || taxid == null || name == null ) {
            return null ;
        }
        // create the key to search/store in the cache
        final String cacheKey = new StringBuilder ( ) .append ( taxid )
                .append(":") .append ( name ) .toString ( ) ;
        // check if the ontology term is in the cache
        if ( ontologyTermCache.containsKey ( cacheKey ) ) {
            return ontologyTermCache.get ( cacheKey ) ;
        }
        // get the ontology term for this interaction and store that in the cache
        final LazyLoadedOntologyTerm term = new LazyLoadedOntologyTerm ( ontologySearcher, taxid, name ) ;
        ontologyTermCache.put ( cacheKey, term );
        return term ;
    }

    // is for find the ontology term using only the short name
    public OntologyTerm findOntologyTermByName ( String name ) throws  SolrServerException {
        // check if the name is not null
        if ( name == null ) { return null ; }
        // create the key to search/store in the cache
        final String cacheKey = new StringBuilder ( ) .append ( ":" ) .append ( name ) .toString ( ) ;
        // check if the ontology term is in the cache
        if ( ontologyTermCache.containsKey ( cacheKey ) ) { return ontologyTermCache.get(cacheKey) ; }
        // get the ontology term for this interaction and store that in the cache
        final LazyLoadedOntologyTerm term = new LazyLoadedOntologyTerm ( ontologySearcher, null, name ) ;
        ontologyTermCache.put ( cacheKey, term ) ;
        return term ;
    }


    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    // get a set of ontology terms to ignore
    public Set < String > getOntologyTermsToIgnore ( ) { return ontologyTermsToIgnore ; }

    // get all parents as a collection
    public Collection < OntologyTerm > getAllParents ( InteractionImpl interaction, boolean includeItself ) throws SolrServerException {
        return getAllParents ( interaction, includeItself, true);
    }

    // get all parents as a collection
    private Collection < OntologyTerm > getAllParents ( InteractionImpl interaction, boolean includeItself, boolean includeSynonyms ) throws SolrServerException {
        // required variables to get all parents
        final Set < OntologyTerm > allParents ;
        final String type = interaction.getCvInteractionType ( ) .toString() ;
        final String identifier = interaction.getShortLabel ( ) ;
        // check if the ontology server is not null and if is not an ontology term to ignore
        if ( ontologySearcher == null ||
                ontologyTermsToIgnore.contains ( identifier ) ) {
            return Collections.EMPTY_SET ;
        }
        // check if the ontology term is in the cache
        if ( cvCache.containsKey ( identifier ) ) {
            return cvCache.get(identifier) ;
        }
        // find the ontology term for this interaction and get all parents include synonyms and itself.
        final OntologyTerm ontologyTerm = findOntologyTerm ( interaction ) ;
        allParents = ontologyTerm.getAllParentsToRoot ( includeSynonyms );
        if ( includeItself ){
            allParents.add ( ontologyTerm ) ;
        }
        // store in the cache this information
        cvCache.put ( identifier, allParents ) ;
        return allParents != null ? allParents : Collections.EMPTY_SET ;
    }

    /*****************************/
    /*      Override Method      */
    /*****************************/
    @Override
    public boolean isExpandableOntology(String name) {
        if ( expandableOntologies == null ) {
            if ( ontologySearcher == null ) {
                expandableOntologies = new HashSet < String > ( ) ;
            }
            else {
                try {
                    expandableOntologies = ontologySearcher.getOntologyNames ( ) ;
                }
                catch ( SolrServerException e ) {
                    if ( log.isErrorEnabled ( ) ) {
                        log.error ( new StringBuilder ( ) .append( "Problem getting list of ontology names: " )
                                .append ( e.getMessage ( ) ) .toString ( ), e );
                    }
                    return false ;
                }
            }
        }
        return expandableOntologies.contains ( name ) ;
    }
}

