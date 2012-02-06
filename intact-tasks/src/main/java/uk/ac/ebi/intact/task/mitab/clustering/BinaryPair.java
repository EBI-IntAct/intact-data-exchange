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
        this.interactorA = interactorA;
        this.interactorB = interactorB;
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
            int compare = interactorA.compareTo(interactorB);

            if (compare == 0){
                result = interactorA.hashCode();
                result = 31 * interactorB.hashCode();
            }
            // interactor A is before interactor B
            else if (compare < 0){
                result = interactorA.hashCode();
                result = 31 * interactorB.hashCode();
            }
            // interactor B is before interactor A
            else {
                result = interactorB.hashCode();
                result = 31 * interactorA.hashCode(); 
            }
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
            else if (interactorA.equals(pair2.getInteractorA())){
                return interactorB.equals(pair2.getInteractorB());
            }
            else if (interactorA.equals(pair2.getInteractorB())){
                return interactorB.equals(pair2.getInteractorA());
            }
            else {
                return false;
            }
        }
        else if (interactorA == null && interactorB == null){
             if (pair2.getInteractorA() != null || pair2.getInteractorB() != null){
                 return false;
             }
        }
        else if (interactorA == null && interactorB != null){
            if (pair2.getInteractorA() == null){
                if (pair2.getInteractorB() == null){
                    return false;
                }
                else {
                    return pair2.getInteractorB().equals(interactorB);
                }
            }
            else if (pair2.getInteractorB() == null){
                if (pair2.getInteractorA() == null){
                    return false;
                }
                else {
                    return pair2.getInteractorA().equals(interactorB);
                }
            }
            else {
                return false;
            }
        }
        else {
            if (pair2.getInteractorA() == null){
                if (pair2.getInteractorB() == null){
                    return false;
                }
                else {
                    return pair2.getInteractorB().equals(interactorA);
                }
            }
            else if (pair2.getInteractorB() == null){
                if (pair2.getInteractorA() == null){
                    return false;
                }
                else {
                    return pair2.getInteractorA().equals(interactorA);
                }
            }
            else {
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
        
        String [] interactors1 = orderInteractors(interactorA, interactorB);
        String [] interactors2 = orderInteractors(binaryPair.getInteractorA(), binaryPair.getInteractorB());
        
        String firstInteractor1 = interactors1[0];
        String firstInteractor2 = interactors2[0];
        String secondInteractor1 = interactors1[1];
        String secondInteractor2 = interactors2[1];
        
        int comp1 = compareInteractors(firstInteractor1, firstInteractor2);

        if (comp1 == 0){
            return compareInteractors(secondInteractor1, secondInteractor2);
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
