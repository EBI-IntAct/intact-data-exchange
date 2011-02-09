package uk.ac.ebi.intact.util.uniprotExport.miscore.extension;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/02/11</pre>
 */

public class ComparativeValues {

    int numberOfInteractionsA;
    int numberOfInteractionsB;

    public ComparativeValues(int numberOfInteractionsA, int numberOfInteractionsB){
        this.numberOfInteractionsA = numberOfInteractionsA;
        this.numberOfInteractionsB = numberOfInteractionsB;
    }

    public int getNumberOfInteractionsA() {
        return numberOfInteractionsA;
    }

    public int getNumberOfInteractionsB() {
        return numberOfInteractionsB;
    }
}
