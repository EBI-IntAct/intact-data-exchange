package uk.ac.ebi.intact.dataexchange.psimi.exporter.species;

import psidev.psi.mi.jami.model.InteractionEvidence;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A species file unit contain a list of files, the name of the species and the taxid
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/09/11</pre>
 */

public class SpeciesFileUnit {

    private List<File> positiveIndexedEntries = new ArrayList<File>();
    private List<File> negativeIndexEntries = new ArrayList<File>();
    private String species;
    private int taxid;
    private int numberInteractions=0;
    private Map<String, Object> dataSourceOptions;

    public List<File> getPositiveIndexedEntries() {
        return positiveIndexedEntries;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public List<File> getNegativeIndexedEntries() {
        return negativeIndexEntries;
    }

    public int getTaxid() {
        return taxid;
    }

    public void setTaxid(int taxid) {
        this.taxid = taxid;
    }

    public Iterator<InteractionEvidence> getPositiveInteractionIterator(){
        return new SpeciesFileInteractionIterator(positiveIndexedEntries, taxid, dataSourceOptions);
    }

    public Iterator<InteractionEvidence> getNegativeInteractionIterator(){
        return new SpeciesFileInteractionIterator(negativeIndexEntries, taxid, dataSourceOptions);
    }

    public Map<String, Object> getDataSourceOptions() {
        return dataSourceOptions;
    }

    public void setDataSourceOptions(Map<String, Object> dataSourceOptions) {
        this.dataSourceOptions = dataSourceOptions;
    }

    public int getNumberInteractions() {
        return numberInteractions;
    }

    public void setNumberInteractions(int numberInteractions) {
        this.numberInteractions = numberInteractions;
    }
}
