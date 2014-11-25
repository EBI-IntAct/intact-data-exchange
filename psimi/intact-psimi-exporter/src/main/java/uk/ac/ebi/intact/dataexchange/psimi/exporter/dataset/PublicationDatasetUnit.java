package uk.ac.ebi.intact.dataexchange.psimi.exporter.dataset;

/**
 * This publication dataset unit contains a publication id, dataset name and a publication year.
 *
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/09/11</pre>
 */

public class PublicationDatasetUnit {

    private String publicationId;
    private String dataset;
    private int publicationYear;

    public PublicationDatasetUnit(String pubId, int created, String dataset){
        this.dataset = dataset;
        this.publicationYear = created;

        this.publicationId = pubId;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public String getDataset() {
        return dataset;
    }

    public int getPublicationYear() {
        return publicationYear;
    }
}
