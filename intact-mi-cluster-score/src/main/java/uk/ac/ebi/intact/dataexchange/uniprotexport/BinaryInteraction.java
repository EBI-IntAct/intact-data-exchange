package uk.ac.ebi.intact.dataexchange.uniprotexport;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/02/11</pre>
 */

public class BinaryInteraction {

    private String interactorA;
    private String interactorB;

    public BinaryInteraction(String interactorA, String interactorB){
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

        BinaryInteraction pair2 = (BinaryInteraction) o;

        if (this.interactorA != null && this.interactorB != null){
            if (pair2.getInteractorA() != null && pair2.getInteractorB() != null){
                if ((this.interactorA.equalsIgnoreCase(pair2.getInteractorA()) && this.interactorB.equalsIgnoreCase(pair2.getInteractorB()))
                        || (this.interactorA.equalsIgnoreCase(pair2.getInteractorB()) && this.interactorB.equalsIgnoreCase(pair2.getInteractorA()))){
                     return true;
                }
            }
        }
        else if (this.interactorA == null && this.interactorB == null){
            if (pair2.getInteractorA() == null && pair2.getInteractorB() == null){
                return true;
            }
        }
        else if (this.interactorA == null){
            if (pair2.getInteractorA() == null && pair2.getInteractorB() != null){
                if (this.interactorB.equalsIgnoreCase(pair2.getInteractorB())){
                    return true;
                }
            }
            else if (pair2.getInteractorA() != null && pair2.getInteractorB() == null){
                if (this.interactorB.equalsIgnoreCase(pair2.getInteractorA())){
                    return true;
                }
            }
        }
        else if (this.interactorB == null){
            if (pair2.getInteractorA() == null && pair2.getInteractorB() != null){
                if (this.interactorA.equalsIgnoreCase(pair2.getInteractorB())){
                    return true;
                }
            }
            else if (pair2.getInteractorA() != null && pair2.getInteractorB() == null){
                if (this.interactorA.equalsIgnoreCase(pair2.getInteractorA())){
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = (interactorA != null ? interactorA.hashCode() : 0);
        result += (interactorB != null ? interactorB.hashCode() : 0);
        return result;
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();

        if(this.interactorA == null){
            buffer.append("-");
        }
        else {
            buffer.append(this.interactorA);
        }

        buffer.append("\t");

        if(this.interactorB == null){
            buffer.append("-");
        }
        else {
            buffer.append(this.interactorB);
        }

        return buffer.toString();
    }
}
