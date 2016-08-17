package uk.ac.ebi.intact.export.mutation.helper.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class MutationExportLine {

    private String featureAc;
    private String featureShortlabel;
    private List<ExportRange> exportRange = new ArrayList<>();
    private String featureType;
    private String annotations;
    private String affectedProteinAc;
    private String affectedProteinSymbol;
    private String affectedProteinFullName;
    private String affectedProteinOrganism;
    private String participants;
    private String pubmedId;
    private String figureLegend;
    private String interactionAc;

    public String getFeatureAc() {
        return featureAc;
    }

    public void setFeatureAc(String featureAc) {
        this.featureAc = featureAc;
    }

    public String getFeatureShortlabel() {
        return featureShortlabel;
    }

    public void setFeatureShortlabel(String featureShortlabel) {
        this.featureShortlabel = featureShortlabel;
    }

    public List<ExportRange> getExportRange() {
        return exportRange;
    }

    public void setExportRange(List<ExportRange> exportRange) {
        this.exportRange = exportRange;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    public String getAffectedProteinAc() {
        return affectedProteinAc;
    }

    public void setAffectedProteinAc(String affectedProteinAc) {
        this.affectedProteinAc = affectedProteinAc;
    }

    public String getAffectedProteinSymbol() {
        return affectedProteinSymbol;
    }

    public void setAffectedProteinSymbol(String affectedProteinSymbol) {
        this.affectedProteinSymbol = affectedProteinSymbol;
    }

    public String getAffectedProteinFullName() {
        return affectedProteinFullName;
    }

    public void setAffectedProteinFullName(String affectedProteinFullName) {
        this.affectedProteinFullName = affectedProteinFullName;
    }

    public String getAffectedProteinOrganism() {
        return affectedProteinOrganism;
    }

    public void setAffectedProteinOrganism(String affectedProteinOrganism) {
        this.affectedProteinOrganism = affectedProteinOrganism;
    }

    public String getParticipants() {
        return participants;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }

    public String getPubmedId() {
        return pubmedId;
    }

    public void setPubmedId(String pubmedId) {
        this.pubmedId = pubmedId;
    }

    public String getFigureLegend() {
        return figureLegend;
    }

    public void setFigureLegend(String figureLegend) {
        this.figureLegend = figureLegend;
    }

    public String getInteractionAc() {
        return interactionAc;
    }

    public void setInteractionAc(String interactionAc) {
        this.interactionAc = interactionAc;
    }
}
