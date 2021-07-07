package uk.ac.ebi.intact.export.complex.uniprot;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;
import uk.ac.ebi.intact.jami.service.ComplexService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Export Gene Product Information Data from complex to GOA.
 */
@Service
public class ComplexExport2CC {

    private static final String NEW_LINE = System.getProperty("line.separator");

    private static String fileName;

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: ComplexExport2CC <file_name>");
            System.exit(1);
        }
        fileName = args[0];

        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext(new String[]{"/META-INF/complex-uniprot-cc-export-config.xml"});
        ComplexExport2CC service = (ComplexExport2CC) springContext.getBean("complexExport2CC");

        service.exportToCC();

    }

    @Transactional(value = "jamiTransactionManager", readOnly = true)
    public void exportToCC() throws IOException {

        ComplexService complexService = ApplicationContextProvider.getBean("complexService");
        Iterator<Complex> complexes = complexService.iterateAll();
        Map<String, String> proteinToComplexes = new TreeMap<>();


        System.err.println("Complexes to export: " + complexService.countAll());

        try (BufferedWriter ccWriter = new BufferedWriter(new FileWriter(fileName))) {

            while (complexes.hasNext()) {
                Complex complex = complexes.next();
                if (complex instanceof IntactComplex) {

                    IntactComplex intactComplex = (IntactComplex) complex;
                    System.err.println("\nProcessing Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ")");

                    ccComplexLine(intactComplex, proteinToComplexes);
                }
            }

            // write lines
            for (Map.Entry<String, String> entry : proteinToComplexes.entrySet()) {
                ccWriter.write("AC   " + entry.getKey());
                ccWriter.newLine();
                ccWriter.write("CC   -!- COMPLEX:");
                ccWriter.newLine();
                ccWriter.write(entry.getValue());
            }

        }
    }

    private static boolean ccComplexLine(IntactComplex intactComplex, Map<String, String> proteinToComplexes) {

        StringBuilder informationSb = new StringBuilder();

        if (intactComplex.getParticipants() != null) {
            String name = intactComplex.getRecommendedName();
            if(name!=null) { // it can be removed for release-db
                name = name.replaceAll("[\n\t\r]", " ");
            }

            informationSb.append("CC       Component of the ")
                    .append(name)
                    .append(" (").append(intactComplex.getComplexAc()).append(") comprising of ");

            /*  proteins(s) */
            Set<Protein> proteins = intactComplex.getParticipants().stream()
                    .map(ModelledParticipant::getInteractor) //proteins
                    .filter(interactor -> interactor.getInteractorType().getMIIdentifier().equals("MI:0326"))
                    .map(Protein.class::cast)
                    .collect(Collectors.toSet());


            Iterator<Protein> prpteinIterator = proteins.iterator();
            //We extract first element to handle commas
            if (prpteinIterator.hasNext()) {
                Protein protein = prpteinIterator.next();
                String line = proteinComponent(protein, intactComplex);
                if (line == null) {
                    return false;
                } else {
                    //Removes duplicated lines due to PRO CHAINS
                    informationSb.append(line);
                }
            }

            while (prpteinIterator.hasNext()) {
                Protein protein = prpteinIterator.next();
                String line = proteinComponent(protein, intactComplex);
                if (line == null) {
                    return false;
                } else {
                    informationSb.append(", ");
                    informationSb.append(line);
                }
            }

            /* other participants */
            Set<Interactor> noProteins = intactComplex.getParticipants().stream()
                    .map(ModelledParticipant::getInteractor)
                    .filter(interactor -> !interactor.getInteractorType().getMIIdentifier().equals("MI:0326")) //no proteins
                    .collect(Collectors.toSet());

            if(!proteins.isEmpty() && !noProteins.isEmpty()){
                //Comma between participants
                informationSb.append(", ");
            }

            Iterator<Interactor> iterator = noProteins.iterator();
            //We extract first element to handle commas
            if (iterator.hasNext()) {
                Interactor noProtein = iterator.next();
                String line = otherComponent(noProtein, intactComplex);
                if (line == null) {
                    return false;
                } else {
                    //Removes duplicated lines due to PRO CHAINS
                    informationSb.append(line);
                }
            }

            while (iterator.hasNext()) {
                Interactor noProtein = iterator.next();
                String line = otherComponent(noProtein, intactComplex);
                if (line == null) {
                    return false;
                } else {
                    informationSb.append(", ");
                    informationSb.append(line);
                }
            }


            /*  complex_assembly(s) */
            Collection< Annotation > assemblies = AnnotationUtils.collectAllAnnotationsHavingTopic(intactComplex.getAnnotations(), "IA:2783", "complex-assembly");
            if( assemblies.size() > 1) {
                System.err.println("\nComplex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") has more than one complex-assembly annotation");
            }
            else if(assemblies.size() == 1){
                informationSb.append(". This complex is a ");
                informationSb.append(assemblies.iterator().next().getValue().toLowerCase());
            }
            informationSb.append(".");
            informationSb.append(NEW_LINE);

            String sentence = informationSb.toString();

            //Collect sentences per protein
            for (Protein protein : proteins) {
                proteinToComplexes.merge(extractUniprotCanonicalId(protein), sentence, String::concat);
            }
        }

        return true;
    }

    private static String proteinComponent(Protein interactor, IntactComplex intactComplex) {

        StringBuilder proteinSb = new StringBuilder();
        Xref identifier = interactor.getPreferredIdentifier();

        if (identifier == null) {
            System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found an interactor that doesn't have any identity: " + interactor);
            return null;

        } else {
            // [gene name] [(AC)],
            if (interactor.getUniprotkb()!= null) {

                final String geneName = interactor.getGeneName();
                if(geneName != null){
                    proteinSb.append(geneName);
                    proteinSb.append(" ");
                }
                proteinSb.append("(");
                proteinSb.append(extractProteinName(interactor));
                proteinSb.append(")");
            }
            else {
                System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found a protein that doesn't have any uniprot id: " + interactor);
                return null;
            }

        }

        return proteinSb.toString();
    }

    private static String otherComponent(Interactor interactor, IntactComplex intactComplex) {

        StringBuilder componentSb = new StringBuilder();
        Xref identifier = interactor.getPreferredIdentifier();

        if (identifier == null) {
            System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found an interactor that doesn't have any identity: " + interactor);
            return null;

        } else {
            // [preferred name] [(preferred identifier)],
            if (interactor.getPreferredIdentifier()!= null) {

                final String geneName = interactor.getPreferredName();
                if(geneName != null){
                    componentSb.append(geneName);
                    componentSb.append(" ");
                }
                componentSb.append("(");
                componentSb.append(interactor.getPreferredIdentifier().getId());
                componentSb.append(")");
            }
            else {
                System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found a protein that doesn't have any preferred id: " + interactor);
                return null;
            }

        }

        return componentSb.toString();
    }

    private static String extractUniprotCanonicalId(Protein protein) {

        String canonicalId = protein.getUniprotkb();

        if (isSpliceVariant(protein) || isFeatureChain(protein)){
            canonicalId = canonicalId.split("-")[0];
        }

        return canonicalId;
    }

    private static String extractProteinName(Protein protein) {

        String proteinName = protein.getUniprotkb();

        if (isFeatureChain(protein)){
            String[] strings = proteinName.split("-");
            proteinName = strings[1] + " ["+ strings[0]+"]";
        }

        return proteinName;
    }

    private static boolean isSpliceVariant(Protein protein) {
        Collection<InteractorXref> xrefs = protein.getXrefs();
        for (InteractorXref xref : xrefs) {
            if (xref.getQualifier() != null) {
                String qualifierIdentity = xref.getQualifier().getMIIdentifier();
                if (Xref.ISOFORM_PARENT_MI.equals(qualifierIdentity)) {
                    return true;
                }
            }
        }
        return false;
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