package uk.ac.ebi.intact.dataexchange.psimi.exporter.dataset;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This row mapper can extract publication id, publication date and dataset from a query.
 * The query must return a column pubid (contains publication id), year (created date)
 * and dataset (the dataset value for this publication).
 *
 * The first and last column must returns values of type 'String' and the year column must return an object of type int
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/09/11</pre>
 */

public class PublicationDatasetRowMapper implements RowMapper {

    public static final String PUBID_COLUMN = "pubid";
    public static final String DATE_COLUMN = "year";
    public static final String DATASET_COLUMN = "dataset";

    @Override
    public PublicationDatasetUnit mapRow(ResultSet resultSet, int i) throws SQLException {
        return new PublicationDatasetUnit(resultSet.getString(PUBID_COLUMN), resultSet.getInt(DATE_COLUMN), resultSet.getString(DATASET_COLUMN));
    }
}
