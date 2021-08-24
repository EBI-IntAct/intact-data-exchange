package uk.ac.ebi.intact.export.complex.tab.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.export.complex.tab.exception.ComplexExportException;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractorPool;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;
import uk.ac.ebi.intact.jami.utils.comparator.IntactInteractorComparator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class RowFactory {
    private static final Log log = LogFactory.getLog(RowFactory.class);

    private static RowFactory rowFactory = new RowFactory();

    public static String[] convertComplexToExportLine(IntactComplex complex) throws ComplexExportException {
        String[] exportLine = new String[19];
        getRowFactory().assignComplexAc(complex, exportLine, 0);
        getRowFactory().assignRecommendedName(complex, exportLine, 1);
        getRowFactory().assignComplexAliases(complex, exportLine, 2);
        getRowFactory().assignTaxId(complex, exportLine, 3);
        getRowFactory().assignParticipants(complex, exportLine, 4);
        getRowFactory().assignXref(complex, exportLine);
        getRowFactory().assignAnnotations(complex, exportLine);
        getRowFactory().assignSource(complex, exportLine, 17);
        getRowFactory().assignExpandedParticipantsStoichiometry(complex, exportLine, 18);
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

    private void assignComplexAc(IntactComplex complex, String[] exportLine, int index) throws ComplexExportException {
        try {
            prepareString(complex.getComplexAc(), exportLine, index);
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

    private void assignParticipants(IntactComplex complex, String[] exportLine, int index) throws ComplexExportException {
        try {
            Map<Interactor, Integer> interactorStoichioMap = new TreeMap<>(new IntactInteractorComparator());

            for (ModelledParticipant participant : complex.getParticipants()) {
                final Interactor interactor = participant.getInteractor();
                int stoichiometry = participant.getStoichiometry().getMaxValue();
                interactorStoichioMap.compute(interactor, (k, v) -> (v == null) ? stoichiometry : v + stoichiometry);
            }

            List<String> list = new ArrayList<>();
            for (Map.Entry<Interactor, Integer> entry : interactorStoichioMap.entrySet()) {
                String s;
                if (entry.getKey() instanceof InteractorPool) {
                    final InteractorPool moleculeSet = ((InteractorPool) entry.getKey());
                    s = "[" + moleculeSet.stream().map(interactor -> interactor.getPreferredIdentifier().getId())
                            .collect(Collectors.joining(",")) + "]" + "(" + entry.getValue() + ")";

                } else {
                    s = entry.getKey().getPreferredIdentifier().getId() + "(" + entry.getValue() + ")";
                }
                list.add(s);
            }
            Collections.sort(list);

            concatList(list, exportLine, index);
        } catch (NullPointerException e) {
            throw new ComplexExportException("Null value found in column " + (index + 1) + ". Error to parse complex participants.", e);
        }
    }

    private void assignExpandedParticipantsStoichiometry(IntactComplex complex, String[] exportLine, int index) throws ComplexExportException {
        try {
            Map<Interactor, Integer> interactorStoichioMap = new TreeMap<>(new IntactInteractorComparator());
            collectMembersStoichiometry(interactorStoichioMap, complex, 1);

            //we print only proteins or sets
            List<String> list = new ArrayList<>();
            for (Map.Entry<Interactor, Integer> entry : interactorStoichioMap.entrySet()) {
                String s;
                if (entry.getKey() instanceof IntactInteractorPool) {
                    final IntactInteractorPool moleculeSet = (IntactInteractorPool) entry.getKey();
                    s = "[" + moleculeSet.stream().map(interactor -> interactor.getPreferredIdentifier().getId())
                            .collect(Collectors.joining(",")) + "]" + "(" + entry.getValue() + ")";
                    list.add(s);

                } else if (entry.getKey() instanceof Protein) {
                    s = entry.getKey().getPreferredIdentifier().getId() + "(" + entry.getValue() + ")";
                    list.add(s);
                }
            }
            Collections.sort(list);

            concatList(list, exportLine, index);
        } catch (NullPointerException e) {
            throw new ComplexExportException("Null value found in column " + (index + 1) + ". Error to parse complex participants.", e);
        }
    }

    private static void collectMembersStoichiometry(Map<Interactor, Integer> interactorStoichioMap, IntactComplex intactComplex, Integer parentStoichiometry) {

        List<ModelledParticipant> participants = intactComplex.getParticipants().stream()
                .filter(participant -> participant.getInteractor().getInteractorType().getMIIdentifier().equals("MI:0326") || //protein
                participant.getInteractor().getInteractorType().getMIIdentifier().equals("MI:1302") || //complex
                participant.getInteractor().getInteractorType().getMIIdentifier().equals("MI:1304")) //molecule sets
                .collect(Collectors.toList());

        for (ModelledParticipant participant : participants) {
            final Interactor interactor = participant.getInteractor();
            if (interactor instanceof IntactComplex) {
               int complexStoichiometry = participant.getStoichiometry().getMaxValue();
                log.info("\nComplex as interactor " + interactor.getPreferredIdentifier().getId() + " with stoi: " + complexStoichiometry);
                //We removed the complex from the map because it is going to be replace with its components.
                collectMembersStoichiometry(interactorStoichioMap, (IntactComplex) interactor, complexStoichiometry);
            } else { //Proteins and sets
                if (parentStoichiometry != 0) {
                    int stoichiometry = participant.getStoichiometry().getMaxValue() * parentStoichiometry;
                    interactorStoichioMap.compute(interactor, (k, v) -> (v == null) ? stoichiometry : v + stoichiometry);
                } else { //Stoichiometry for the parent is 0, we don not know how many molecules we have in reality
                    interactorStoichioMap.put(interactor, 0);
                }
            }
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

    private void emptyColumn(String[] exportLine, int index) {
        exportLine[index] = "-";
    }
}