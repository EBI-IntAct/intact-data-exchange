package uk.ac.ebi.intact.task.mitab.clustering;

/**
 * A binary pair contains interactor A and B. Two binary pairs are equal if A and B are equals. A and B are interchangeable 
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06/02/12</pre>
 */

public class BinaryPair implements Comparable<BinaryPair>{

    private String interactorA;
    private String interactorB;
    
    public BinaryPair(String interactorA, String interactorB){
        String [] orderedInteractors = orderInteractors(interactorA, interactorB);
        
        this.interactorA = orderedInteractors[0];
        this.interactorB = orderedInteractors[1];
    }

    public String getInteractorA() {
        return interactorA;
    }

    public String getInteractorB() {
        return interactorB;
    }

    @Override
    public int hashCode() {
        int result;
        
        if (interactorA == null && interactorB == null){
            result = 0;
        }
        else if (interactorA == null){
            result = interactorB.hashCode();
        }
        else if (interactorB == null){
            result = interactorA.hashCode();
        }
        else {
            result = interactorA.hashCode();
            result = 31 * interactorB.hashCode();
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        BinaryPair pair2 = (BinaryPair) o;

        if (interactorA != null && interactorB != null){
            if (pair2.getInteractorA() == null || pair2.getInteractorB() == null){
                return false;
            }
            else if (!interactorA.equals(pair2.getInteractorA()) || !interactorB.equals(pair2.getInteractorB())){
                return false;
            }
        }
        else if (interactorA == null && interactorB == null && (pair2.getInteractorA() != null || pair2.getInteractorB() != null)){
             return false;
        }
        else if (interactorA == null && interactorB != null){
            if (pair2.getInteractorA() != null || pair2.getInteractorB() == null || (pair2.getInteractorB() != null && interactorB.equals(pair2.getInteractorB()))){
                return false;
            }
        }
        else if (interactorB == null && interactorA != null){
            if (pair2.getInteractorB() != null || pair2.getInteractorA() == null || (pair2.getInteractorA() != null && interactorA.equals(pair2.getInteractorA()))){
                return false;
            }
        }

        return true;
    }
    
    private String[] orderInteractors(String inter1, String inter2){
        
        if (inter1 == null && inter2 == null){
            return new String[] {inter1, inter2};
        }
        else if (inter1 == null){
            return new String[] {inter2, inter1};
        }
        else if (inter2 == null){
            return new String[] {inter1, inter2};
        }
        else {
            int comp = inter1.compareTo(inter2);
            
            if (comp == 0){
                return new String[] {inter1, inter2};
            }
            else if (comp < 0){
                return new String[] {inter1, inter2};
            }
            else {
                return new String[] {inter2, inter1};
            }
        }
    }

    @Override
    public int compareTo(BinaryPair binaryPair) {
        final int BEFORE = -1;
        final int AFTER = 1;
        
        int comp1 = compareInteractors(interactorA, binaryPair.getInteractorA());

        if (comp1 == 0){
            return compareInteractors(interactorB, binaryPair.getInteractorB());
        }
        else if (comp1 < 0){
            return BEFORE;
        }
        else {
            return AFTER;
        }
    }

    private int compareInteractors(String inter1, String inter2) {
        final int EQUAL = 0;
        final int BEFORE = -1;
        final int AFTER = 1;
        
        if (inter1 == null && inter2 == null){
            return EQUAL;
        }
        else if (inter1 == null){
            return AFTER;
        }
        else if (inter2 == null){
            return BEFORE;
        }
        else {
            return inter1.compareTo(inter2);
        }
    }
}
