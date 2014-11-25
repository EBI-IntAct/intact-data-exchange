package uk.ac.ebi.intact.dataexchange.psimi.exporter.species.classification;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This row mapper can extract publication id, publication date, species, biosource taxid and the total number of interactions for this publication from a query.
 * The query must return a column pubid (contains publication id), year (created date), species (the species existing in this experiment), taxid
 * (the taxid of the interactor)
 * and total_interactions (total number of interactions for this publication)
 *
 * All columns must returns values of type 'String' excepted the year column which must return an object of type int
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/09/11</pre>
 */

public class PublicationSpeciesRowMapper implements RowMapper {
    public static final String PUBID_COLUMN = "pubid";
    public static final String CREATED_COLUMN = "year";
    public static final String SPECIES_COLUMN = "species";
    public static final String TAXID_COLUMN = "taxid";
    public static final String INTERACTION_NUMBER_COLUMN = "number_interactions";

    @Override
    public PublicationSpeciesUnit mapRow(ResultSet rs, int rowNum) throws SQLException {

        return new PublicationSpeciesUnit(rs.getString(PUBID_COLUMN), rs.getInt(CREATED_COLUMN), rs.getString(SPECIES_COLUMN), rs.getString(TAXID_COLUMN),
                rs.getInt(INTERACTION_NUMBER_COLUMN));

    }
}
