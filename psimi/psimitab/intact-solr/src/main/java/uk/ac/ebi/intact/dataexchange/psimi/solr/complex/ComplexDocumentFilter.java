package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

/**
 * Used to filter complex documents when indexing.
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 24/07/13
 */
public interface ComplexDocumentFilter {

    boolean accept(ComplexDocument complexDocument);

}
