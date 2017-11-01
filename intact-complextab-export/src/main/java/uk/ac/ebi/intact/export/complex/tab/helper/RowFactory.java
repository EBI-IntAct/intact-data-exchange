package uk.ac.ebi.intact.export.complex.tab.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Source;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.export.complex.tab.exception.ComplexExportException;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class RowFactory {
    private static final Log log = LogFactory.getLog(RowFactory.class);

    private static RowFactory rowFactory = new RowFactory();

    public static String[] convertComplexToExportLine(IntactComplex complex) throws ComplexExportException {
        String[] exportLine = new String[18];
        getRowFactory().assignAc(complex, exportLine, 0);
        getRowFactory().assignRecommendedName(complex, exportLine, 1);
        getRowFactory().assignComplexAliases(complex, exportLine, 2);
        getRowFactory().assignTaxId(complex, exportLine, 3);
        getRowFactory().assignParticipants(complex, exportLine, 4);
        getRowFactory().assignXref(complex, exportLine);
        getRowFactory().assignAnnotations(complex, exportLine);
        getRowFactory().assignSource(complex, exportLine, 17);
        return exportLine;
    }

    private static RowFactory getRowFactory() {
        return rowFactory;
    }

    private void assignXref(IntactComplex complex, String[] exportLine) throws ComplexExportException {
        //xrefs contains ALL xrefs
        Collection<Xref> xrefs = complex.getDbXrefs();
        Xref evidenceOntologyXref = null;
        Collection<Xref> experimentalEvidenceList = new ArrayList<>();
        Collection<Xref> goAnnotationList = new ArrayList<>();
        //let's filter all the xref out, which we want to put in a special column
        for (Xref xref : xrefs) {
            if (xref.getDatabase().getShortName().equals("go")) {
                goAnnotationList.add(xref);
            } else if (xref.getQualifier() != null && xref.getQualifier().getShortName().equals("exp-evidence")) {
                experimentalEvidenceList.add(xref);
            } else if (xref.getDatabase().getShortName().equals("evidence ontology")) {
                evidenceOntologyXref = xref;
            }
        }
        //let's remove them all from xrefs. So we can use xrefs to write all the other xref down too.
        xrefs.remove(evidenceOntologyXref);
        xrefs.removeAll(experimentalEvidenceList);
        xrefs.removeAll(goAnnotationList);

        assignEvidenceOntology(complex, evidenceOntologyXref, exportLine, 5);
        assignExperimentalEvidences(experimentalEvidenceList, exportLine, 6);
        assignGoAnnotations(goAnnotationList, exportLine, 7);
        assignOtherXrefs(xrefs, exportLine, 8);
    }

    private void assignAnnotations(IntactComplex complex, String[] exportLine) throws ComplexExportException {
        Collection<Annotation> annotations = complex.getDbAnnotations();
        String curatedComplex = null;
        String complexProperties = null;
        String complexAssembly = null;
        Collection<String> ligands = new ArrayList<>();
        Collection<String> diseases = new ArrayList<>();
        Collection<String> agonists = new ArrayList<>();
        Collection<String> antagonists = new ArrayList<>();
        Collection<String> comments = new ArrayList<>();
        //All the annotations we want to include
        for (Annotation annotation : annotations) {
            switch (annotation.getTopic().getShortName()) {
                case "curated-complex":
                    curatedComplex = annotation.getValue();
                    break;
                case "complex-properties":
                    complexProperties = annotation.getValue();
                    break;
                case "complex-assembly":
                    complexAssembly = annotation.getValue();
                    break;
                case "ligand":
                    ligands.add(annotation.getValue());
                    break;
                case "disease":
                    if (annotation.getValue() != null) {
                        diseases.add(annotation.getValue());
                    }
                    break;
                case "agonist":
                    agonists.add(annotation.getValue());
                    break;
                case "antagonist":
                    antagonists.add(annotation.getValue());
                    break;
                case "comment":
                    comments.add(annotation.getValue());
                    break;
            }
        }
        assignCuratedComplex(curatedComplex, exportLine, 9);
        assignComplexProperties(complexProperties, exportLine, 10);
        assignComplexAssembly(complexAssembly, exportLine, 11);
        assignLigand(ligands, exportLine, 12);
        assignDisease(diseases, exportLine, 13);
        assignAgonist(agonists, exportLine, 14);
        assignAntagonist(antagonists, exportLine, 15);
        assignComment(comments, exportLine, 16);
    }

    private void assignAc(IntactComplex complex, String[] exportLine, int index) throws ComplexExportException {
        try {
            prepareString(complex.getAc(), exportLine, index);
        } catch (NullPointerException e) {
            throw new ComplexExportException("Null value found in column " + (index + 1) + ". Could not find complex ac.", e);
        }
    }

    private void assignRecommendedName(IntactComplex complex, String[] exportLine, int index) throws ComplexExportException {
        try {
            prepareString(complex.getRecommendedName(), exportLine, index);
        } catch (NullPointerException e) {
            throw new ComplexExportException("Null value found in column " + (index + 1) + ". Could not find recommended name.", e);
        }
    }

    private void assignComplexAliases(IntactComplex values, String[] exportLine, int index) throws ComplexExportException {
        try {
            //MI:0673 == complex synonym in OLS
            List<String> list = values.getAliases().stream()
                    .filter(alias -> alias.getType().getMIIdentifier().equals("MI:0673"))
                    .map(Alias::getName).collect(Collectors.toList());
            concatList(list, exportLine, index);
        } catch (NullPointerException e) {
            throw new ComplexExportException("Null value found in column " + (index + 1) + ". Error to parse complex aliases(\"MI:0673\")", e);
        }
    }

    private void assignTaxId(IntactComplex complex, String[] exportLine, int index) throws ComplexExportException {
        try {
            prepareString(String.valueOf(complex.getOrganism().getTaxId()), exportLine, index);
        } catch (NullPointerException e) {
            throw new ComplexExportException("Null value found in column " + (index + 1) + ". Could not find recommended name.", e);
        }
    }

    private void assignParticipants(IntactComplex values, String[] exportLine, int index) throws ComplexExportException {
        try {
            List<String> list = values.getParticipants().stream()
                    .map(modelledParticipant -> modelledParticipant.getInteractor().getPreferredIdentifier().getId()
                            + "(" + modelledParticipant.getStoichiometry().getMaxValue() + ")")
                    .collect(Collectors.toList());
            concatList(list, exportLine, index);
        } catch (NullPointerException e) {
            throw new ComplexExportException("Null value found in column " + (index + 1) + ". Error to parse complex participants.", e);
        }
    }

    private void assignEvidenceOntology(IntactComplex complex, Xref xref, String[] exportLine, int index) throws ComplexExportException {
        try {
            if (xref == null) {
                emptyColumn(exportLine, index);
            } else {
                prepareString(xref.getId() + "(" + complex.getEvidenceType().getShortName() + ")", exportLine, index);
            }
        } catch (NullPointerException e) {
            throw new ComplexExportException("Null value found in column " + (index + 1) + ". Error to parse evidence ontology (xref)", e);
        }
    }

    private void assignExperimentalEvidences(Collection<Xref> values, String[] exportLine, int index) throws ComplexExportException {
        try {
            List<String> list = values.stream()
                    .map(xref -> xref.getDatabase().getShortName() + ":" + xref.getId())
                    .collect(Collectors.toList());
            concatList(list, exportLine, index);
        } catch (NullPointerException e) {
            throw new ComplexExportException("Null value found in column " + (index + 1) + ". Error to parse experimental evidence (xref)", e);
        }
    }

    private void assignGoAnnotations(Collection<Xref> values, String[] exportLine, int index) throws ComplexExportException {
        try {
            List<String> list = new ArrayList<>();
            values.stream().forEach(xref -> {
                InteractorXref goXref = (InteractorXref) xref;
                list.add(goXref.getId() + "(" + goXref.getSecondaryId() + ")");
            });
            concatList(list, exportLine, index);
        } catch (NullPointerException e) {
            throw new ComplexExportException("Null value found in column " + (index + 1) + ". Error to parse go annotations (xref)", e);
        }
    }

    private void assignOtherXrefs(Collection<Xref> values, String[] exportLine, int index) throws NullPointerException, ComplexExportException {
        try {
            List<String> list = values.stream()
                    .map(xref -> xref.getDatabase().getShortName() + ":" + xref.getId() + "(" + xref.getQualifier().getShortName() + ")")
                    .collect(Collectors.toList());
            concatList(list, exportLine, index);
        } catch (NullPointerException e) {
            throw new ComplexExportException("Null value found in column " + (index + 1) + ". Error to parse xrefs (xref)", e);
        }
    }

    private void assignCuratedComplex(String value, String[] exportLine, int index) throws ComplexExportException {
        prepareString(value, exportLine, index);
    }

    private void assignComplexProperties(String value, String[] exportLine, int index) throws ComplexExportException {
        prepareString(value, exportLine, index);
    }

    private void assignComplexAssembly(String value, String[] exportLine, int index) throws ComplexExportException {
        prepareString(value, exportLine, index);
    }

    private void assignLigand(Collection<String> values, String[] exportLine, int index) throws ComplexExportException {
        concatList(values, exportLine, index);
    }

    private void assignDisease(Collection<String> values, String[] exportLine, int index) throws ComplexExportException {
        concatList(values, exportLine, index);
    }

    private void assignAgonist(Collection<String> values, String[] exportLine, int index) throws ComplexExportException {
        concatList(values, exportLine, index);
    }

    private void assignAntagonist(Collection<String> values, String[] exportLine, int index) throws ComplexExportException {
        concatList(values, exportLine, index);
    }

    private void assignComment(Collection<String> values, String[] exportLine, int index) throws ComplexExportException {
        concatList(values, exportLine, index);
    }

    private void assignSource(IntactComplex complex, String[] exportLine, int index) throws ComplexExportException {
        Source source = complex.getSource();
        //We have a few sources, which don't have a MI Id. In that case we parse -
        String miId = source.getMIIdentifier() != null ? source.getMIIdentifier() : "-";
        prepareString("psi-mi:\"" + miId + "\"(" + source.getShortName() + ")", exportLine, index);
    }

    private void concatList(Collection<String> values, String[] exportLine, int index) throws ComplexExportException {
        if (values == null || values.isEmpty()) {
            emptyColumn(exportLine, index);
        } else {
            String value = String.join("|", values);
            prepareString(value, exportLine, index);
        }
    }

    private void prepareString(String value, String[] exportLine, int index) throws ComplexExportException {
        if (value == null || value.isEmpty()) {
            emptyColumn(exportLine, index);
        } else {
            if (value.contains("null")) {
                throw new ComplexExportException("Null value found in column " + (index + 1) + ". (" + value + ")");
            }
            value = value.replaceAll("\n", " ");
            value = value.replaceAll("\t", " ");
            value = value.replaceAll("\\s+", " ");
            exportLine[index] = value;
        }
    }

    private void emptyColumn(String exportLine[], int index) {
        exportLine[index] = "-";
    }
}
