package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import junit.framework.Assert;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 28/08/13
 */
public class ComplexResultIteratorTest {
    private ComplexResultIterator complexResultIterator = null ;
    private SolrDocumentList solrDocumentList = null ;
    @Before
    public void setUp() throws Exception {
        solrDocumentList = new SolrDocumentList ( ) ;
        SolrDocument aux = new SolrDocument ( ) ;
        aux.addField ( ComplexFieldNames.COMPLEX_AC,   "first"  ) ;
        aux.addField ( ComplexFieldNames.COMPLEX_AC,   "another") ;
        aux.addField ( ComplexFieldNames.COMPLEX_NAME, "second" ) ;
        aux.addField ( ComplexFieldNames.DESCRIPTION,  "third"  ) ;
        aux.addField ( ComplexFieldNames.DESCRIPTION,  "maybe"  ) ;
        aux.addField ( ComplexFieldNames.ORGANISM_NAME,"fourth" ) ;
        solrDocumentList.add ( aux ) ;
        aux = new SolrDocument ( ) ;
        aux.addField ( ComplexFieldNames.COMPLEX_AC,   "fifth"  ) ;
        aux.addField ( ComplexFieldNames.COMPLEX_NAME, "sixth"  ) ;
        aux.addField ( ComplexFieldNames.DESCRIPTION,  "seventh") ;
        aux.addField ( ComplexFieldNames.ORGANISM_NAME,"eighth" ) ;
        solrDocumentList.add ( aux ) ;
        this.complexResultIterator = new ComplexResultIterator ( solrDocumentList ) ;
    }

    @Test
    public void testGetIterator() throws Exception {
        Iterator < SolrDocument > iterator = this.complexResultIterator.getIterator ( ) ;
        Assert.assertNotNull("Test not null iterator", iterator) ;
        Assert.assertTrue("Test if iterator has next", iterator.hasNext()) ;
        iterator.next() ;
        Assert.assertFalse("Test if iterator has next", iterator.hasNext()) ;
    }

    @Test
    public void testGetFieldValues() throws Exception {
        Iterator < SolrDocument > iterator = this.solrDocumentList.iterator() ;
        SolrDocument solrDocument = iterator.next ( );
        Assert.assertTrue("Test complex_ac, first", this.complexResultIterator.getFieldValues(solrDocument, ComplexFieldNames.COMPLEX_AC).equals("first another")) ;
        Assert.assertTrue ("Test complex_name, first", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.COMPLEX_NAME ) .equals ( "second" ) ) ;
        Assert.assertTrue ("Test description, first", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.DESCRIPTION ) .equals ( "third maybe" ) ) ;
        Assert.assertTrue ("Test organism_name, first", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.ORGANISM_NAME ) .equals ( "fourth" ) ) ;
        solrDocument = iterator.next ( ) ;
        Assert.assertTrue ("Test complex_ac, second", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.COMPLEX_AC ) .equals ( "fifth" ) ) ;
        Assert.assertTrue ("Test complex_name, second", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.COMPLEX_NAME ) .equals ( "sixth" ) ) ;
        Assert.assertTrue ("Test description, second", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.DESCRIPTION ) .equals ( "seventh" ) ) ;
        Assert.assertTrue ("Test organism_name, second", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.ORGANISM_NAME ) .equals ( "eighth" ) ) ;
    }

    @Test
    public void testHasNext() throws Exception {
        Assert.assertTrue ( "Test has next, true", this.complexResultIterator.hasNext ( ) ) ;
        this.complexResultIterator.next ( ) ;
        Assert.assertTrue ( "Test has next, true", this.complexResultIterator.hasNext ( ) ) ;
        this.complexResultIterator.next ( ) ;
        Assert.assertFalse ( "Test has next, false", this.complexResultIterator.hasNext ( ) ) ;
    }

    @Test
    public void testNext() throws Exception {
        Iterator < SolrDocument > iterator = solrDocumentList.iterator ( ) ;
        SolrDocument solrDocument = iterator.next ( ) ;
        ComplexSearchResults complexSearchResults = this.complexResultIterator.next ( ) ;
        Assert.assertEquals ( "Test complex_ac, first", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.COMPLEX_AC ), complexSearchResults.getComplexAC ( ) ) ;
        Assert.assertEquals ( "Test complex_name, first", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.COMPLEX_NAME ), complexSearchResults.getComplexName ( ) ) ;
        Assert.assertEquals ( "Test description, first", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.DESCRIPTION ), complexSearchResults.getCuratedComplex ( ) ) ;
        Assert.assertEquals ( "Test organism_name, first", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.ORGANISM_NAME ), complexSearchResults.getOrganismName ( ) ) ;
        solrDocument = iterator.next ( ) ;
        complexSearchResults = this.complexResultIterator.next ( ) ;
        Assert.assertEquals ( "Test complex_ac, second", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.COMPLEX_AC ), complexSearchResults.getComplexAC ( ) ) ;
        Assert.assertEquals ( "Test complex_name, second", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.COMPLEX_NAME ), complexSearchResults.getComplexName ( ) ) ;
        Assert.assertEquals ( "Test description, second", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.DESCRIPTION ), complexSearchResults.getCuratedComplex ( ) ) ;
        Assert.assertEquals ( "Test organism_name, second", this.complexResultIterator.getFieldValues ( solrDocument, ComplexFieldNames.ORGANISM_NAME ), complexSearchResults.getOrganismName ( ) ) ;

        Assert.assertNull ( "Test what happens when it does not have next", this.complexResultIterator.next ( ) ) ;
    }

    @Test
    public void testRemove() throws Exception {
        Throwable exception = null ;
        try {
            this.complexResultIterator.remove ( ) ;
        }
        catch ( Throwable e ) {
            exception = e ;
        }
        Assert.assertTrue ( "Test remove method", exception instanceof UnsupportedOperationException ) ;
    }
}
