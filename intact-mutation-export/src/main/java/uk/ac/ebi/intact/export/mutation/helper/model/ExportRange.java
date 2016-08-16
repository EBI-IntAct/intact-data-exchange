package uk.ac.ebi.intact.export.mutation.helper.model;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ExportRange {
    private String range;
    private String originalSequence;
    private String resultingSequence;

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getOriginalSequence() {
        return originalSequence;
    }

    public void setOriginalSequence(String originalSequence) {
        this.originalSequence = originalSequence;
    }

    public String getResultingSequence() {
        return resultingSequence;
    }

    public void setResultingSequence(String resultingSequence) {
        this.resultingSequence = resultingSequence;
    }
}
