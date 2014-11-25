package uk.ac.ebi.intact.dataexchange.psimi.exporter.species.classification;

/**
 * This publication unit contains the publication id, year, species, taxid and number of interactions for this publication.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/09/11</pre>
 */

public class PublicationSpeciesUnit {

    private String publicationId;
    private int year;
    private String species;
    private int taxid;
    private int numberInteractions;

    public PublicationSpeciesUnit(String pubId, int year, String species, String taxid, int numberInteractions){
        this.species = species;
        this.year = year;
        this.publicationId = pubId;
        try{
            this.taxid = Integer.parseInt(taxid);
        }
        catch (NumberFormatException e){
            throw new IllegalStateException("Cannot read species taxid because not a number: "+taxid, e);
        }

        this.numberInteractions = numberInteractions;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public int getYear() {
        return year;
    }

    public String getSpecies() {
        return species;
    }

    public int getTaxid() {
        return taxid;
    }

    public int getNumberInteractions() {
        return numberInteractions;
    }
}
