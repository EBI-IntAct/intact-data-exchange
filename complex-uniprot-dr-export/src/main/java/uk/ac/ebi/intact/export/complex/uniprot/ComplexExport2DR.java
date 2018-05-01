package uk.ac.ebi.intact.export.complex.uniprot;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.ModelledParticipant;
import psidev.psi.mi.jami.model.Protein;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;
import uk.ac.ebi.intact.jami.service.ComplexService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Export Gene Product Information Data from complex to GOA.
 */
@Service
public class ComplexExport2DR {

    private static final char TAB = '\t';
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String COMPLEX_PORTAL = "ComplexPortal";
    private static final String INTACT = "IntAct";

    private static String fileName;

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: ComplexExport2DR <file_name>");
            System.exit(1);
        }
        fileName = args[0];

        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext(new String[]{"/META-INF/complex-uniprot-dr-export-config.xml"});
        ComplexExport2DR service = (ComplexExport2DR) springContext.getBean("complexExport2DR");

        service.exportToDR();

    }

    @Transactional(value = "jamiTransactionManager", readOnly = true)
    public void exportToDR() throws IOException {

        ComplexService complexService = ApplicationContextProvider.getBean("complexService");
        Iterator<Complex> complexes = complexService.iterateAll();

        System.err.println("Complexes to export: " + complexService.countAll());

        BufferedWriter drWriter = null;

        try {
            drWriter = new BufferedWriter(new FileWriter(new File(fileName)));

            StringBuilder informationSb = new StringBuilder( 2048 );

            while (complexes.hasNext()) {
                Complex complex = complexes.next();
                if (complex instanceof IntactComplex) {

                    IntactComplex intactComplex = (IntactComplex) complex;
                    System.err.println("\nProcessing Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ")");

                    informationSb.setLength(0);

                    //For now we don't need to write the components of the complex
                    boolean writeLine = drComplexLines(informationSb, intactComplex, false);

                    if (writeLine) {
                        drWriter.write(informationSb.toString());
                        drWriter.flush();
                    }
                }
            }
        } finally {
            if (drWriter != null) {
                drWriter.close();
            }
        }
    }

    private static boolean drComplexLines(StringBuilder informationSb, IntactComplex intactComplex, boolean writeParentComplex) {

        if (intactComplex.getParticipants() != null) {

            Set<String> uniqueLines = new HashSet<>();

            Set<Protein> proteins = intactComplex.getParticipants().stream()
                .filter(participant -> participant.getInteractor().getInteractorType().getMIIdentifier().equals("MI:0326")) //proteins
                .map(ModelledParticipant::getInteractor)
                .map(Protein.class::cast)
                .collect(Collectors.toSet());

            for (Protein protein : proteins) {
                String line = componentLine(protein, intactComplex);
                if (line == null) {
                    return false;
                }
                else {
                    //Removes duplicated lines due to PRO CHAINS
                    uniqueLines.add(line);
                }
            }

            if (writeParentComplex) {
                //TODO. Implement in the future when the reference to parent complexes are in the data and website
            }

            for (String uniqueLine : uniqueLines) {
                informationSb.append(uniqueLine);
            }
        }
        return true;
    }

    private static String componentLine(Protein interactor, Complex intactComplex) {

        StringBuilder componentSb = new StringBuilder();
        Xref identifier = interactor.getPreferredIdentifier();

        if (identifier == null) {
            System.err.println("ERROR: Found an interactor that doesn't have any identity: " + interactor);
            return null;

        } else {

            if (interactor.getUniprotkb()!= null) {

                /*  1 Uniprot ID */
                componentSb.append(extractUniprotCanonicalId(interactor)).append(TAB);

                /*  2 Complex Portal AC */
                componentSb.append(intactComplex.getComplexAc()).append(TAB);

                /*  3 Complex Name */
                String name = intactComplex.getRecommendedName();
                if(name!=null) { // it can be removed for release-db
                    componentSb.append(name.replaceAll("[\n\t\r]", " "));
                }
                componentSb.append(TAB);

                /*  4 Isoforms/Process chains IDs */
                if(isSpliceVariant(interactor)) {
                    componentSb.append(interactor.getUniprotkb());
                }
                componentSb.append(TAB);

                componentSb.append(NEW_LINE);
            }
            else {
                System.err.println("ERROR: Found a protein that doesn't have any uniprot id: " + interactor);
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
