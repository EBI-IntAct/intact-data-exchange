package uk.ac.ebi.intact.export.complex.go;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.model.extension.ComplexGOXref;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.service.ComplexService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Export Gene Product Association Data from complex to GOA.
 */
@Service
public class ComplexExport2GPA {

    private static final char TAB = '\t';
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final Format DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");

    private static final String COMPONENT_MI_REF = "MI:0354";
    private static final String FUNCTION_MI_REF = "MI:0355";
    private static final String PROCESS_MI_REF = "MI:0359";
    private static final String COMPLEX_PORTAL = "ComplexPortal";
    private static final String INTACT = "IntAct";
    private static final String PMID = "PMID";

    private static List<String> ecoForPubMed = new ArrayList<>(
            Arrays.asList(
            "ECO:0000269",
            "ECO:0000314",
            "ECO:0000315",
            "ECO:0000316",
            "ECO:0000353",
            "ECO:0005543",
            "ECO:0005547",
            "ECO:0000315",
            "ECO:0000316"));

    private static List<String> ecoForCPX = new ArrayList<>(
            Arrays.asList(
                    "ECO:0005610",
                    "ECO:0005544",
                    "ECO:0005546"));

    public static void main(String[] args) throws IOException {

        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext(new String[]{"/META-INF/complex-go-export-config.xml"});
        ComplexExport2GPA service = (ComplexExport2GPA) springContext.getBean("complexExport2GPA");

        service.exportToGPA();

    }

    @Transactional(value = "jamiTransactionManager", readOnly = true)
    public void exportToGPA() throws IOException {

        ComplexService complexService = ApplicationContextProvider.getBean("complexService");
        Iterator<Complex> complexes = complexService.iterateAll();

        System.err.println("Complexes to export: " + complexService.countAll());

        // Format documentation: ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/UNIPROT/gp_association_readme
        BufferedWriter associationWriter = null;

        try {
            associationWriter = new BufferedWriter(new FileWriter(new File("complex_portal.gpa")));
            associationWriter.write("!gpa-version: 1.1");
            associationWriter.write(NEW_LINE);

            StringBuilder associationSb = new StringBuilder(2048);

            while (complexes.hasNext()) {
                Complex complex = complexes.next();

                if (complex instanceof IntactComplex) {

                    IntactComplex intactComplex = (IntactComplex) complex;
                    System.err.println("\nProcessing Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ")");

                    associationSb.setLength(0);

                    boolean writeAssociation = associationLine(associationSb, intactComplex);

                    if (writeAssociation) {
                        associationWriter.write(associationSb.toString());
                        associationWriter.flush();
                    }
                }
            }
        } finally {
            if (associationWriter != null) {
                associationWriter.close();
            }
        }
    }

    private static boolean associationLine(StringBuilder associationSb, IntactComplex intactComplex) {

        // Prepare the gp_association (GO term lines)
        Collection<ComplexGOXref> goTerms = collectGoXrefs(intactComplex);
        String qualifier;
        String eco;
        String reference;

        if (!goTerms.isEmpty()) {
            for (ComplexGOXref goTerm : goTerms) {
                qualifier = null;
                eco = null;
                reference = null;
                if (goTerm.getQualifier() != null) {
                    switch (goTerm.getQualifier().getMIIdentifier()) {
                        case COMPONENT_MI_REF:
                            qualifier = "part_of";
                            break;
                        case PROCESS_MI_REF:
                            qualifier = "involved_in";
                            break;
                        case FUNCTION_MI_REF:
                            qualifier = "enables";
                            break;
                    }
                }
                if (goTerm.getEvidenceType() != null) {
                    eco = collectEco(goTerm);
                }
                if (goTerm.getPubmed() != null) {
                    reference = collectReference(goTerm);
                }
                if (qualifier != null && eco != null && reference != null) {
                    /*  1 DB */
                    associationSb.append(COMPLEX_PORTAL).append(TAB);
                    /*  2 DB Object ID */
                    associationSb.append(intactComplex.getComplexAc()).append(TAB);
                    /*  3 Qualifier */
                    associationSb.append(qualifier).append(TAB);
                    /*  4 GO ID */
                    associationSb.append(goTerm.getId()).append(TAB);
                    if (reference.startsWith(COMPLEX_PORTAL)) {
                        //This should be fix in the editor in the future, for now we move the reference to the with/from column in the export
                        /*  5 DB:Reference(s) */
                        associationSb.append("GO_REF:0000024").append(TAB);//TODO To review the GO_REF
                        /*  6 Evidence code */
                        associationSb.append(eco).append(TAB);
                        /*  7 With (or) From */
                        associationSb.append(reference).append(TAB);
                    } else { //We assume PUBMED,
                        /*  5 DB:Reference(s) */
                        associationSb.append(reference).append(TAB);
                        /*  6 Evidence code */
                        associationSb.append(eco).append(TAB);
                        /*  7 With (or) From */
                        associationSb.append(TAB);
                    }
                    /*  8 Interacting taxon ID */
                    associationSb.append(intactComplex.getOrganism().getTaxId()).append(TAB);
                    /*  9 Date */
                    associationSb.append(DATE_FORMATTER.format(intactComplex.getUpdated())).append(TAB);
                    /* 10 Assigned by */
                    associationSb.append(COMPLEX_PORTAL).append(TAB);
                    /* 11 Annotation Extension */
                    associationSb.append(TAB);
                    /* 12 Annotation Properties */
                    associationSb.append(TAB);
                    /* 13 */
                    associationSb.append(NEW_LINE);
                } else {
                    System.err.println("Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") without proper complex GO Xref to export");
                    //TODO Inferred from other xrefs
                    return false;
                }
            }
        } else {
            System.err.println("Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") with empty complex GO Xref");
            //TODO Inferred from other xrefs
            return false;
        }

        return true;
    }

    private static Collection<ComplexGOXref> collectGoXrefs(IntactComplex complex) {

        Set<ComplexGOXref> goTerms = new HashSet<>(complex.getXrefs().size());

        final Collection<Xref> xrefs = XrefUtils.collectAllXrefsHavingDatabase(complex.getXrefs(), Xref.GO_MI, Xref.GO);
        for (Xref xref : xrefs) {
            if (xref instanceof ComplexGOXref) {
                goTerms.add((ComplexGOXref) xref);
            }
//            else {
//                System.err.println(((AbstractIntactXref) xref).getAc() + " is a term from GO (" + xref.getId() + ") that it is not a ComplexGOXref");
//            }
        }

        return goTerms;
    }

    private static String collectReference(ComplexGOXref goTerm) {

        String reference = null;
        String pubmed = goTerm.getPubmed();
        String evidence = collectEco(goTerm);

        if (pubmed != null) {
            if (pubmed.startsWith("EBI-")) {
                //reference = "IntAct:" + pubmed;
                // This case shouldn't happen wrong annotation in the complex portal - report to curators
                reference = null;
                System.err.println("Found a go term with EBI evidence " + pubmed + " " + goTerm.getId() + " (" + evidence + ")");
            } else if (pubmed.startsWith("CPX-")) {
                reference = COMPLEX_PORTAL + ":" + pubmed;
                if (!ecoForCPX.contains(evidence)) {
                    System.err.println("Found a go term with CPX evidence " + pubmed + " with wrong ECO code: " + goTerm.getId() + " (" + evidence + ")");
                }
            } else {
                reference = PMID + ":" + pubmed;
                if (!ecoForPubMed.contains(evidence)) {
                    System.err.println("Found a go term with PubMed evidence " + pubmed + " with wrong ECO code: " + goTerm.getId() + " (" + evidence + ")");
                }
            }
        } else {
            System.err.println("Found a go term that doesn't have a proper pubmed id (i.e. " + goTerm.getId() + ")");
        }

        return reference;
    }

    private static String collectEco(ComplexGOXref goTerm) {

        String eco = null;

        if (goTerm.getEvidenceType() != null) {
            Collection<Xref> ecoRefs = XrefUtils.collectAllXrefsHavingDatabase(goTerm.getEvidenceType().getIdentifiers(), Complex.ECO_MI, Complex.ECO);
            if (!ecoRefs.isEmpty() && ecoRefs.size() == 1) {
                eco = ecoRefs.iterator().next().getId();
            } else {
                System.err.println("ERROR: Found a go term that doesn't have a proper eco code ("+ goTerm.getAc() + " " + goTerm.getId() + ")");
            }
        } else {
            System.err.println("ERROR: Found a go term that doesn't have a eco code ("+ goTerm.getAc() + " " + goTerm.getId() + ")");
        }

        return eco;
    }
}
