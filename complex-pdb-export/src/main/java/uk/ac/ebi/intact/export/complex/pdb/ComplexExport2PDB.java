package uk.ac.ebi.intact.export.complex.pdb;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.ComplexDao;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleStatus;
import uk.ac.ebi.intact.jami.service.ComplexService;
import uk.ac.ebi.intact.jami.utils.comparator.IntactInteractorComparator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Export Gene Product Information Data from complex to GOA.
 */
@Service
public class ComplexExport2PDB {

    private static final char SEPARATOR = '\t';

    private static final String NEW_LINE = System.getProperty("line.separator");

    private static String fileNamePrefix;
    private static boolean released;
    private static boolean predicted;

    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.err.println("Usage: ComplexExport2PDB <file_name_prefix> <released data [true|false] [true|false]>");
            System.exit(1);
        }
        fileNamePrefix = args[0];
        released = Boolean.parseBoolean(args[1]);
        predicted = Boolean.parseBoolean(args[2]);

        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext(new String[]{"/META-INF/complex-pdb-export-config.xml"});
        ComplexExport2PDB service = (ComplexExport2PDB) springContext.getBean("complexExport2PDB");

        service.exportToPDB();

    }

    @Transactional(value = "jamiTransactionManager", readOnly = true)
    public void exportToPDB() throws IOException {

        ComplexService complexService = ApplicationContextProvider.getBean("complexService");
        ComplexDao complexDao = complexService.getIntactDao().getComplexDao();

        String query;
        Map< String,Object> queryParameters = new HashMap<>();
        List<IntactComplex> complexes;

        int totalComplexes = (int) complexService.countAll();

        // The queries are adapted for the point in the release pipeline that are called.
        // The update of the complex status happens while processing the release (after the pipeline finishes)
        // however PDB needs all released complexes including those to be released in the running release.
        // In order to cover all the cases we need all complexes released and ready for release to be exported.
        if(!released) {
            query = "select distinct f from IntactComplex f "  +
                    "join f.cvStatus as lcStatus " +
                    "where f.predictedComplex is :predictedComplex " +
                    "and (lcStatus.shortName = :readyForChecking " +
                    "or lcStatus.shortName = :curationInProgress " +
                    "or lcStatus.shortName = :acceptedOnHold)";

            queryParameters.put("predictedComplex", predicted);
            queryParameters.put("readyForChecking", LifeCycleStatus.READY_FOR_CHECKING.shortLabel());
            queryParameters.put("curationInProgress", LifeCycleStatus.CURATION_IN_PROGRESS.shortLabel());
            queryParameters.put("acceptedOnHold", LifeCycleStatus.ACCEPTED_ON_HOLD.shortLabel());
        }
        else {
            query = "select distinct f from IntactComplex f "  +
                    "join f.cvStatus as lcStatus " +
                    "where f.predictedComplex is :predictedComplex " +
                    "and (lcStatus.shortName = :readyForRelease " +
                    "or lcStatus.shortName = :released)";

            queryParameters.put("predictedComplex", predicted);
            queryParameters.put("readyForRelease", LifeCycleStatus.READY_FOR_RELEASE.shortLabel());
            queryParameters.put("released", LifeCycleStatus.RELEASED.shortLabel());
        }

        complexes = complexDao.getByQuery(query, queryParameters, 0, totalComplexes);

        // Format documentation: https://intact.atlassian.net/browse/GENISSUES-76
        try (BufferedWriter complexWriter = new BufferedWriter(new FileWriter(new File(fileNamePrefix + "_complexes.tsv")));
             BufferedWriter componentsWriter = new BufferedWriter(new FileWriter(new File(fileNamePrefix + "_components.tsv")));
             BufferedWriter pdbWriter = new BufferedWriter(new FileWriter(new File(fileNamePrefix + "_xrefs.tsv")))) {

            complexWriter.write("complex_id" + SEPARATOR + "version" + SEPARATOR + "recommended_name" + SEPARATOR + "systematic_name" + SEPARATOR + "complex_assembly");
            complexWriter.write(NEW_LINE);

            componentsWriter.write("complex_id" + SEPARATOR + "version" + SEPARATOR + "database_name" + SEPARATOR + "database_ac" + SEPARATOR + "stoichiometry");
            componentsWriter.write(NEW_LINE);

            pdbWriter.write("complex_id" + SEPARATOR + "version" + SEPARATOR + "pdb_ids");
            pdbWriter.write(NEW_LINE);

            StringBuilder componentsSb = new StringBuilder(2048);
            StringBuilder complexSb = new StringBuilder(2048);
            StringBuilder pdbSb = new StringBuilder(2048);

            for (IntactComplex intactComplex : complexes) {
                System.err.println("\nProcessing Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ")");
                componentsSb.setLength(0);
                complexSb.setLength(0);
                pdbSb.setLength(0);

                if (expandComponents(componentsSb, intactComplex, 1, intactComplex)) { //We maintain the original first level stoichiometries
                    if (complexLine(complexSb, intactComplex)) {
                        componentsWriter.write(componentsSb.toString());
                        componentsWriter.flush();
                        complexWriter.write(complexSb.toString());
                        complexWriter.flush();
                        if (pdbLine(pdbSb, intactComplex)) {
                            pdbWriter.write(pdbSb.toString());
                            pdbWriter.flush();
                        }
                    }
                }
            }
        }
    }

    private static boolean complexLine(StringBuilder informationSb, IntactComplex intactComplex) {

        /*  1 complex_id */
        informationSb.append(intactComplex.getComplexAc()).append(SEPARATOR);

        /*  2 complex_version */
        informationSb.append(intactComplex.getComplexVersion()).append(SEPARATOR);

        /*  3 recommended_name */
        String name = intactComplex.getRecommendedName();
        if(name!=null) { // it can be removed for release-db
            informationSb.append(name.replaceAll("[\n\t\r]", " "));
        }
        informationSb.append(SEPARATOR);

        /*  4 systematic_name */
        informationSb.append(intactComplex.getSystematicName()).append(SEPARATOR);


        /*  5 complex_assembly(s) */
        Collection<Annotation> assemblies = AnnotationUtils.collectAllAnnotationsHavingTopic(intactComplex.getAnnotations(), "IA:2783", "complex-assembly");
        if( assemblies.size() > 1) {
            System.err.println("\nComplex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") has more than one complex-assembly annotation");
        }
        else if(assemblies.size() == 1){
            informationSb.append(assemblies.iterator().next().getValue());
        }

        informationSb.append(NEW_LINE);

        return true;
    }

    private static boolean pdbLine(StringBuilder informationSb, IntactComplex intactComplex) {

        //wwpdb qualifier should be identity or subset, so it can an identifier or a xref
        Collection<Xref> pdbXrefs = XrefUtils.collectAllXrefsHavingDatabase(intactComplex.getIdentifiers(), "MI:0805", "wwpdb");
        pdbXrefs.addAll(XrefUtils.collectAllXrefsHavingDatabase(intactComplex.getXrefs(), "MI:0805", "wwpdb"));

        if(!pdbXrefs.isEmpty()) {
            //We append the line only if there are pdbXrefs

            /*  1 complex_id */
            informationSb.append(intactComplex.getComplexAc()).append(SEPARATOR);

            /*  2 complex_version */
            informationSb.append(intactComplex.getComplexVersion()).append(SEPARATOR);

            /*  3 pdb_ids */
            String pdbIds = pdbXrefs.stream().map(Xref::getId).collect(Collectors.joining(","));
            informationSb.append(pdbIds);

            informationSb.append(NEW_LINE);
        } else {
            return false;
        }

        return true;
    }

    private static boolean expandComponents(StringBuilder informationSb, IntactComplex intactComplex, Integer parentStoichiometry, IntactComplex parentComplex){

        Map<Interactor, Integer> interactorStoichioMap = new TreeMap<>(new IntactInteractorComparator());
        List<ModelledParticipant> participants = intactComplex.getParticipants().stream().filter(
                participant -> participant.getInteractor().getInteractorType().getMIIdentifier().equals("MI:0326") || //protein
                        participant.getInteractor().getInteractorType().getMIIdentifier().equals("MI:1302")) //complex
                .collect(Collectors.toList());

        for (Participant participant : participants) {
            final Interactor interactor = participant.getInteractor();
            if(parentStoichiometry != 0) {
                int stoichiometry = participant.getStoichiometry().getMaxValue() * parentStoichiometry;
                interactorStoichioMap.compute(interactor, (k, v) -> (v == null) ? stoichiometry : v + stoichiometry);
            }
            else { //Stoichiometry for the parent is 0, we don not know how many molecules we have in reality
                interactorStoichioMap.put(interactor, 0);
            }
        }

        for (Map.Entry<Interactor, Integer> interactorStoichioEntry : interactorStoichioMap.entrySet()) {
            Interactor interactor = interactorStoichioEntry.getKey();
            if (!componentLine(informationSb, interactor, interactorStoichioEntry.getValue(), parentComplex)) {
                return false;
            }
        }
        return true;
    }

    private static boolean componentLine(StringBuilder componentSb, Interactor interactor, Integer stoichiometry, IntactComplex intactComplex) {

        if (interactor instanceof IntactComplex) {

            System.err.println("\nComplex as interactor " + interactor.getPreferredIdentifier().getId() + " with stoi: " + stoichiometry);
            expandComponents(componentSb, (IntactComplex) interactor, stoichiometry, intactComplex);

        } else if (interactor instanceof Protein) { //Proteins

            Protein protein = (Protein) interactor;
            Xref identifier = protein.getPreferredIdentifier();

            if (identifier == null) {
                System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found an interactor that doesn't have any identity: " + interactor);
                return false;

            } else {

                if (protein.getUniprotkb() != null) {
                    /*  1 complex_id */
                    componentSb.append(intactComplex.getComplexAc()).append(SEPARATOR);

                    /*  2 complex_version */
                    componentSb.append(intactComplex.getComplexVersion()).append(SEPARATOR);

                    /*  3 database_name */
                    componentSb.append(protein.getPreferredIdentifier().getDatabase().getShortName()).append(SEPARATOR);

                    /*  4 database_accession */
                    componentSb.append(extractUniprotCanonicalId(protein)).append(SEPARATOR);

                    /*  5 stoichiometry */
                    componentSb.append(stoichiometry);

                    componentSb.append(NEW_LINE);

                } else {
                    System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found a protein that doesn't have any uniprot id: " + interactor);
                    return false;
                }
            }

        } else {
            System.err.println("\nProcessing interactor " + interactor.getPreferredIdentifier() + " that is not protein or complex");
        }

        return true;
    }

    private static String extractUniprotCanonicalId(Protein protein) {

        String canonicalId = protein.getUniprotkb();

        if (isFeatureChain(protein)){
            canonicalId = canonicalId.split("-")[0];
        }

        return canonicalId;
    }

    private static boolean isFeatureChain(Protein protein) {

        Collection<InteractorXref> xrefs = protein.getXrefs();

        for (InteractorXref xref : xrefs) {
            if (xref.getQualifier() != null) {
                String qualifierIdentity = xref.getQualifier().getMIIdentifier();
                if (Xref.CHAIN_PARENT_MI.equals(qualifierIdentity)) {
                    return true;
                }
            }
        }

        return false;
    }
}
