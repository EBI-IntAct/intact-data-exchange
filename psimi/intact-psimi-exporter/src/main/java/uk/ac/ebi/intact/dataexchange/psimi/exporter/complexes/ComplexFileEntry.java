package uk.ac.ebi.intact.dataexchange.psimi.exporter.complexes;

import psidev.psi.mi.jami.model.Complex;

/**
 * The complex file entry contains the complex to write plus some species information.
 * It contains the species name (folder where to write the entry) and the name of the entry (name of the file where to write this entry)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/09/11</pre>
 */

public class ComplexFileEntry implements Comparable<ComplexFileEntry> {

    /**
     * Species name associated with this entry
     */
    private String speciesName;
    /**
     * Name of this entry. It will be the name of the species file
     */
    private String entryName;
    /**
     * The complex to write
     */
    private Complex complex;

    public ComplexFileEntry(String speciesName, String entryName, Complex complex){
        super();
        this.speciesName = speciesName;
        this.entryName = entryName;
        this.complex = complex;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public String getEntryName() {
        return entryName;
    }

    public Complex getComplex() {
        return complex;
    }

    @Override
    public int compareTo(ComplexFileEntry o) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if ( this == o ) return EQUAL;

        int comparison = this.speciesName.compareTo(o.getSpeciesName());
        if ( comparison == EQUAL ) {
            return this.entryName.compareTo(o.getEntryName());
        }

        return comparison;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ComplexFileEntry entry2 = (ComplexFileEntry) o;

        if (!speciesName.equals(entry2.getSpeciesName()))
        {
            return false;
        }
        if (!entryName.equals(entry2.getEntryName()))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = speciesName.hashCode();
        result = 31 * result + entryName.hashCode();
        return result;
    }
}
