package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Collection;

/**
 * A way to write a ComplexDocument into a Solr Document.
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 24/07/13
 */
public class ComplexIndexWriter {

    /********************************/
    /*      Private attributes      */
    /********************************/
    private IndexWriter indexWriter;

    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexIndexWriter(Directory directory, boolean create) throws IOException {
        indexWriter = new IndexWriter(directory, new StandardAnalyzer(), create);
    }

    /****************************************/
    /*      Methods for work with Solr      */
    /****************************************/

    public IndexWriter getIndexWriter()         { return indexWriter; }
    //public void flush()      throws IOException { indexWriter.flush(); }
    public void optimize()   throws IOException { indexWriter.optimize(); }
    public void close()      throws IOException { indexWriter.close(); }

    @Override
    public void addDocument(ComplexDocument complexDocument) throws IOException {
        Document doc = new Document();

        // Begin to add all information available in complexDocument to doc
        // First of all, we must add the required information
        doc.add(new Field(ComplexFieldName.ID,             complexDocument.getID(),            Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(ComplexFieldName.COMPLEX_NAME,   complexDocument.getComplexName(),   Field.Store.YES, Field.Index.NO));
        doc.add(new Field(ComplexFieldName.COMPLEX_AC,     complexDocument.getComplexAC(),     Field.Store.YES, Field.Index.NO));
        doc.add(new Field(ComplexFieldName.DESCRIPTION,    complexDocument.getDescription(),   Field.Store.YES, Field.Index.NO));

        // Right now, we must add other information available in the complexDocument
        // Adding all Complex IDs
        Collection<String> aux = complexDocument.getComplexID();
        if ( aux != null )
        for ( String ID : aux )
            doc.add(new Field(ComplexFieldName.COMPLEX_ID,     ID,     Field.Store.YES, Field.Index.ANALYZED));

        // Adding all Complex Alias
        aux = complexDocument.getComplexAlias();
        if ( aux != null ){
           for ( String Alias : aux )
               doc.add(new Field(ComplexFieldName.COMPLEX_ALIAS, Alias, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Complex Types
        aux = complexDocument.getComplexType();
        if ( aux != null ){
            for ( String Type : aux )
                doc.add(new Field(ComplexFieldName.COMPLEX_TYPE, Type, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Complex Cross References
        aux = complexDocument.getComplexXref();
        if ( aux != null ){
            for ( String Xref : aux )
                doc.add(new Field(ComplexFieldName.COMPLEX_XREF, Xref, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Interactors IDs
        aux = complexDocument.getInteractorID();
        if ( aux != null ){
            for ( String ID : aux )
                doc.add(new Field(ComplexFieldName.INTERACTOR_ID, ID, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Interactors Alias
        aux = complexDocument.getInteractorAlias();
        if ( aux != null ){
            for ( String Alias : aux )
                doc.add(new Field(ComplexFieldName.INTERACTOR_ALIAS, Alias, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Interactors Types
        aux = complexDocument.getInteractorType();
        if ( aux != null ){
            for ( String Type : aux )
                doc.add(new Field(ComplexFieldName.INTERACTOR_TYPE, Type, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Biological Roles
        aux = complexDocument.getBiorole();
        if ( aux != null ){
            for ( String Rol : aux )
                doc.add(new Field(ComplexFieldName.BIOROLE, Rol, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Features
        aux = complexDocument.getFeatures();
        if ( aux != null ){
            for ( String Feature : aux )
                doc.add(new Field(ComplexFieldName.FEATURES, Feature, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Sources
        aux = complexDocument.getSource();
        if ( aux != null ){
            for ( String Source : aux )
                doc.add(new Field(ComplexFieldName.SOURCE, Source, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Number of Participants
        aux = complexDocument.getNumberParticipants();
        if ( aux != null ){
            for ( String Number : aux )
                doc.add(new Field(ComplexFieldName.NUMBER_PARTICIPANTS, Number, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Pathway Cross Reference
        aux = complexDocument.getPathwayXref();
        if ( aux != null ){
            for ( String Xref : aux )
                doc.add(new Field(ComplexFieldName.PATHWAY_XREF, Xref, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Eco Cross Reference
        aux = complexDocument.getInteractorType();
        if ( aux != null ){
            for ( String Type : aux )
                doc.add(new Field(ComplexFieldName.INTERACTOR_TYPE, Type, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Adding all Publication IDs
        aux = complexDocument.getPublicationID();
        if ( aux != null ){
            for ( String ID : aux )
                doc.add(new Field(ComplexFieldName.PUBLICATION_ID, ID, Field.Store.NO, Field.Index.ANALYZED));
        }

        // Right now we must add that Document to our IndexWriter
        this.indexWriter.addDocument(doc);
    }

}
