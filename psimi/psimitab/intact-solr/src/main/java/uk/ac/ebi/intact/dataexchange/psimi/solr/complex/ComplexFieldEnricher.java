package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;


import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.BaseFieldEnricher;
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
public class ComplexFieldEnricher  extends BaseFieldEnricher {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private static final Log log = LogFactory.getLog ( ComplexFieldEnricher.class );
    public final OntologySearcher ontologySearcher;
    private final Map < String, Collection < OntologyTerm > > cvCache ;
    private final Map < String, OntologyTerm > ontologyTermCache ;
    private Set < String > expandableOntologies  = null ;
    private Set < String > ontologyTermsToIgnore = null ;

    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexFieldEnricher ( OntologySearcher ontologySearcher_ ) {
        super ( ) ;
        this.ontologySearcher = ontologySearcher_ ;
        this.cvCache = new LRUMap ( 50000 ) ;
        this.ontologyTermCache = new LRUMap ( 10000 ) ;
        ontologyTermsToIgnore = new HashSet < String > ( ) ;
    }

    /*******************************/
    /*      Protected Methods      */
    /*******************************/
    protected void initializeOntologyTermsToIgnore ( ) {

    }

    protected SolrDocument InteractorPartEnrich ( CvDagObject cvDagObject, SolrDocument solrDocument ) throws Exception {
        // CvInteractionType enrich to store in interaction_type
        for ( CvObjectAlias Alias : cvDagObject.getAliases ( ) ) {
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE, Alias.getName ( ) ) ;
        }
        solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE,  cvDagObject.getShortLabel ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE,  cvDagObject.getFullName   ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE,  cvDagObject.getIdentifier ( ) ) ;
        // and get parents to store in interaction_type_ontology
        for ( CvDagObject parent : cvDagObject.getParents ( ) ) {
            solrDocument.addField ( ComplexFieldNames.INTERACTOR_TYPE_ONTOLOGY, parent ) ;
        }

        return solrDocument ;
    }
    protected SolrDocument OrganismPartEnrich ( InteractionImpl interaction, SolrDocument solrDocument ) throws Exception {
        // BioSource enrich to store in complex_organism
        final OntologyTerm ontologyTerm = findOntologyTerm ( interaction ) ;
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM, ontologyTerm.getName( ) ) ;
        for ( OntologyTerm synonym : ontologyTerm.getSynonyms ( ) ) {
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM, synonym ) ;
        }
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM, interaction.getBioSource ( ) .getTaxId ( ) ) ;
        // and get all parents to store in complex_organism_ontology
        for ( OntologyTerm parent : ontologyTerm.getAllParentsToRoot ( true ) ) {
            solrDocument.addField ( ComplexFieldNames.COMPLEX_ORGANISM_ONTOLOGY, parent ) ;
        }

        return solrDocument ;
    }
    protected SolrDocument XrefPartEnrich ( Collection < InteractorXref > interactorXrefs, SolrDocument solrDocument ) throws Exception {

        // Xrefs enrich to store in complex_xref
        String shortLabel = null ;
        String ID = null ;
        OntologyTerm ontologyTerm_aux = null ;
        for ( InteractorXref interactorXref : interactorXrefs ){
            shortLabel = interactorXref.getCvDatabase ( ) .getShortLabel ( ) ;
            solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF, shortLabel ) ;
            if ( interactorXref.hasValidPrimaryId ( ) ) {
                ID = interactorXref.getPrimaryId ( ) ;
                solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF, ID ) ;
                ontologyTerm_aux = findOntologyTerm ( ID, shortLabel ) ;
                solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF, ontologyTerm_aux.getName ( ) ) ;
                for ( OntologyTerm synonym : ontologyTerm_aux.getSynonyms ( ) ) {
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF, synonym ) ;
                }
                for ( OntologyTerm parent : ontologyTerm_aux.getAllParentsToRoot ( true ) ) {
                    solrDocument.addField ( ComplexFieldNames.COMPLEX_XREF_ONTOLOGY, parent ) ;
                }
            }
        }

        return solrDocument ;
    }


    /****************************/
    /*      Enrich Methods      */
    /****************************/
    public SolrDocument enrich (
            InteractionImpl interaction,
            SolrDocument solrDocument )
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

    public SolrDocument enrich ( InteractionImpl interaction ) throws Exception {
        return enrich ( interaction, new SolrDocument ( ) ) ;
    }

    /**************************/
    /*      Find Methods      */
    /**************************/
    public OntologyTerm findOntologyTerm ( InteractionImpl interaction ) throws SolrServerException {
        BioSource bioSource = interaction.getBioSource ( ) ;
        return findOntologyTerm(bioSource.getTaxId(), bioSource.getShortLabel()) ;    }

    private OntologyTerm findOntologyTerm(String taxid, String name) throws SolrServerException {
        if ( ontologySearcher == null ) {
            return null ;
        }
        String cacheKey = new StringBuilder ( ) .append ( taxid )
                .append("_") .append ( name ) .toString ( ) ;
        if ( ontologyTermCache.containsKey ( cacheKey ) ) {
            return ontologyTermCache.get ( cacheKey ) ;
        }
        final LazyLoadedOntologyTerm term = new LazyLoadedOntologyTerm ( ontologySearcher, taxid, name ) ;
        ontologyTermCache.put ( cacheKey, term );
        return term ;
    }

    public OntologyTerm findOntologyTermByName ( String name ) throws  SolrServerException {
        if ( name == null ) { return null ; }
        String cacheKey = new StringBuilder ( ) .append ( "_" ) .append ( name ) .toString ( ) ;
        if ( ontologyTermCache.containsKey ( cacheKey ) ) { return ontologyTermCache.get(cacheKey) ; }
        final LazyLoadedOntologyTerm term = new LazyLoadedOntologyTerm ( ontologySearcher, null, name ) ;
        ontologyTermCache.put ( cacheKey, term ) ;
        return term ;
    }


    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public Set < String > getOntologyTermsToIgnore ( ) { return ontologyTermsToIgnore ; }

    public Collection < OntologyTerm > getAllParents ( InteractionImpl interaction, boolean includeItself ) throws SolrServerException {
        return getAllParents ( interaction, includeItself, true);
    }

    private Collection < OntologyTerm > getAllParents ( InteractionImpl interaction, boolean includeItself, boolean includeSynonyms ) throws SolrServerException {
        Set<OntologyTerm> allParents = null ;
        final String type = interaction.getCvInteractionType ( ) .toString() ;
        final String identifier = interaction.getShortLabel ( ) ;
        if ( ontologySearcher == null ||
                ontologyTermsToIgnore.contains ( identifier ) ) {
            return Collections.EMPTY_LIST ;
        }
        if ( cvCache.containsKey ( identifier ) ) {
            return cvCache.get(identifier) ;
        }
        final OntologyTerm ontologyTerm = findOntologyTerm ( interaction ) ;
        allParents = ontologyTerm.getAllParentsToRoot ( includeSynonyms );
        if ( includeItself ){
            allParents.add ( ontologyTerm ) ;
        }
        cvCache.put ( identifier, allParents ) ;
        return allParents != null ? allParents : Collections.EMPTY_LIST ;
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

