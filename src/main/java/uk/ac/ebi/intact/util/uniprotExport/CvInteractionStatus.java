package uk.ac.ebi.intact.util.uniprotExport;

/**
 * The status of a CV detection method
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public class CvInteractionStatus {

    // Method status
    public static final int EXPORT = 0;
    public static final int DO_NOT_EXPORT = 1;
    public static final int NOT_SPECIFIED = 2;
    public static final int CONDITIONAL_EXPORT = 3;

    private int status;
    private int minimumOccurence = 1;

    public CvInteractionStatus(int status) {
        this.status = status;
    }

    public CvInteractionStatus(int status, int minimumOccurence) {
        this.minimumOccurence = minimumOccurence;
        this.status = status;
    }

    public int getMinimumOccurence() {
        return minimumOccurence;
    }

    public boolean doExport() {
        return status == EXPORT;
    }

    public boolean doNotExport() {
        return status == DO_NOT_EXPORT;
    }

    public boolean isNotSpecified() {
        return status == NOT_SPECIFIED;
    }

    public boolean isConditionalExport() {
        return status == CONDITIONAL_EXPORT;
    }

    public String toString() {

        StringBuffer sb = new StringBuffer(128);

        sb.append("CvInteractionStatus{ minimumOccurence=").append(minimumOccurence);

        sb.append(" status=");
        switch (status) {
            case EXPORT:
                sb.append("EXPORT");
                break;
            case DO_NOT_EXPORT:
                sb.append("DO_NOT_EXPORT");
                break;
            case NOT_SPECIFIED:
                sb.append("NOT_SPECIFIED");
                break;
            case CONDITIONAL_EXPORT:
                sb.append("CONDITIONAL_EXPORT");
                break;
            default:
                sb.append("UNKNOWN VALUE !!!!!!!!!!!!!!!!!");
        }
        sb.append(" }");

        return sb.toString();
    }
}
