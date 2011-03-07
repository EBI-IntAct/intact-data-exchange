package uk.ac.ebi.intact.util.uniprotExport;

/**
 * Represents a binary interaction composed with an interactor and a boolean value to know if this interactor interacts
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>07/03/11</pre>
 */

public class InteractingProtein implements Comparable<InteractingProtein>{

    private String interactor;
    private boolean doesInteract;

    public InteractingProtein(String interactor, boolean doesInteract){
        this.interactor = interactor;
        this.doesInteract = doesInteract;
    }

    public String getInteractor() {
        return interactor;
    }

    public boolean doesInteract() {
        return doesInteract;
    }

    @Override
    public int compareTo(InteractingProtein o) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if ( this == o ) return EQUAL;

        int comparison = this.interactor.compareTo(o.getInteractor());

        if ( comparison != EQUAL ) {
            return comparison;
        }

        if (this.doesInteract() == o.doesInteract()){
            return EQUAL;
        }
        else if (this.doesInteract() && !o.doesInteract()){
            return BEFORE;
        }
        else {
            return AFTER;
        }
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

        InteractingProtein bi2 = (InteractingProtein) o;

        if (!getInteractor().equalsIgnoreCase(bi2.getInteractor()))
        {
            return false;
        }

        if (doesInteract() != bi2.doesInteract()){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = interactor.hashCode();
        result = 31 * result + Boolean.valueOf(doesInteract()).hashCode();
        return result;
    }
}
