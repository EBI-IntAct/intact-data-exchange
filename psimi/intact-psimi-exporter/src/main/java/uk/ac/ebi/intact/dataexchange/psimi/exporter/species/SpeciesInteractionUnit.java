package uk.ac.ebi.intact.dataexchange.psimi.exporter.species;

/**
 * A species interaction unit contain an iterator of positive and negative interactions and the species name
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/09/11</pre>
 */

public class SpeciesInteractionUnit {

    private String species;
    private InteractionEvidenceChunkIterator positiveInteractionIterator;
    private InteractionEvidenceChunkIterator negativeInteractionIterator;

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public InteractionEvidenceChunkIterator getPositiveInteractionIterator() {
        return positiveInteractionIterator;
    }

    public void setPositiveInteractionIterator(InteractionEvidenceChunkIterator positiveInteractionIterator) {
        this.positiveInteractionIterator = positiveInteractionIterator;
    }

    public InteractionEvidenceChunkIterator getNegativeInteractionIterator() {
        return negativeInteractionIterator;
    }

    public void setNegativeInteractionIterator(InteractionEvidenceChunkIterator negativeInteractionIterator) {
        this.negativeInteractionIterator = negativeInteractionIterator;
    }
}
