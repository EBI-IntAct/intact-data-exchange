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
public class ComplexExport2GPAVersion2 {

    private static final char TAB = '\t';
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final Format DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    private static final String COMPONENT_MI_REF = "MI:0354";
    private static final String FUNCTION_MI_REF = "MI:0355";
    private static final String PROCESS_MI_REF = "MI:0359";
    private static final String COMPLEX_PORTAL = "ComplexPortal";
    private static final String TAXON_DB = "NCBITaxon";
    private static final String PMID = "PMID";

    private static String fileName;

    private static final List<String> ecoForPubMed = new ArrayList<>(
            Arrays.asList(
            "ECO:0000269",
            "ECO:0000314",
            "ECO:0000315",
            "ECO:0000353",
            "ECO:0005543",
            "ECO:0005547"));

    private static final List<String> ecoForCPX = new ArrayList<>(
            Arrays.asList(
                    "ECO:0005610",
                    "ECO:0005544",
                    "ECO:0005546"));

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: ComplexExport2GPAVersion2 <file_name>");
            System.exit(1);
        }
        fileName = args[0];
        // TODO: feedback from Alex Ignatchenko alexsign@ebi.ac.uk
        // I would recommend to add properties column to GPAD with contributor-id and internal annotation record
        // id if available. This is not mandatory but surely will help with data exchange.
        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext(new String[]{"/META-INF/complex-go-export-config.xml"});
        ComplexExport2GPAVersion2 service = (ComplexExport2GPAVersion2) springContext.getBean("complexExport2GPAVersion2");

        service.exportToGPA();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true)
    public void exportToGPA() throws IOException {

        ComplexService complexService = ApplicationContextProvider.getBean("complexService");
        Iterator<Complex> complexes = complexService.iterateAll();

        System.err.println("Complexes to export: " + complexService.countAll());

        // Format documentation: https://github.com/geneontology/go-annotation/blob/master/specs/gpad-gpi-2-0.md
        File file = new File(fileName);
        //Creates parent directories if they don't exist
        file.getParentFile().mkdirs();

        try (BufferedWriter associationWriter = new BufferedWriter(new FileWriter(new File(fileName)))) {
            associationWriter.write("!gpa-version: 2.0");
            associationWriter.write(NEW_LINE);
            associationWriter.write("!generated-by: ComplexPortal");
            associationWriter.write(NEW_LINE);
            associationWriter.write("!date-generated: " + DATE_FORMATTER.format(new Date()));
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
        }
    }

    private static boolean associationLine(StringBuilder associationSb, IntactComplex intactComplex) {

        // Prepare the gp_association (GO term lines)
        Collection<ComplexGOXref> goTerms = collectGoXrefs(intactComplex);
        String relation;
        String eco;
        String reference;

        if (!goTerms.isEmpty()) {
            for (ComplexGOXref goTerm : goTerms) {
                relation = null;
                eco = null;
                reference = null;
                if (goTerm.getQualifier() != null) {
                    relation = collectRelation(goTerm);
                }
                if (goTerm.getEvidenceType() != null) {
                    eco = collectEco(goTerm, intactComplex);
                }
                if (goTerm.getPubmed() != null) {
                    reference = collectReference(goTerm, intactComplex);
                }

                if (relation != null && eco != null && reference != null) {
                    /*  1 DB_Object_ID ::= ID e.g. UniProtKB:P11678 */
                    associationSb.append(COMPLEX_PORTAL).append(":").append(intactComplex.getComplexAc()).append(TAB);
                    /*  2 Negation ::= 'NOT' e.g. NOT */
                    associationSb.append(TAB);
                    /*  3 Relation ::= OBO_ID Relations Ontology (subset) e.g. RO:0002263 */
                    associationSb.append(relation).append(TAB);
                    /*  4 Ontology_Class_ID ::= OBO_ID e.g. GO:0050803 */
                    associationSb.append(goTerm.getId()).append(TAB);
                    if (reference.startsWith(COMPLEX_PORTAL)) {
                        //This should be fix in the editor in the future, for now we move the reference to the with/from column in the export
                        /*  5 Reference ::= [ID] ('|' ID)* */
                        associationSb.append("GO_REF:0000114").append(TAB);
                        /*  6 Evidence_type ::= OBO_ID */
                        associationSb.append(eco).append(TAB);
                        /*  7 With_or_From ::= [ID] ('|' | ‘,’ ID)**/
                        associationSb.append(reference).append(TAB);
                    } else { //We assume PUBMED,
                        /*  5 Reference ::= [ID] ('|' ID)* */
                        associationSb.append(reference).append(TAB);
                        /*  6 Evidence_type ::= OBO_ID */
                        associationSb.append(eco).append(TAB);
                        /*  7 With_or_From ::= [ID] ('|' | ‘,’ ID) */
                        associationSb.append(TAB);
                    }
                    // Note a complex can contain more than one species, For now we export the complex species selected by the curator
                    /*  8 Interacting_taxon_ID ::= ['NCBITaxon:'Taxon_ID] ('|' | ‘,’ 'NCBITaxon:'[Taxon_ID])* e.g. NCBITaxon:5476 */
                    associationSb.append(TAXON_DB).append(":").append(intactComplex.getOrganism().getTaxId()).append(TAB);
                    /*  9 Date ::= YYYY-MM-DD */
                    associationSb.append(DATE_FORMATTER.format(intactComplex.getUpdated())).append(TAB);
                    /* 10 Assigned_by ::= Prefix */
                    associationSb.append(COMPLEX_PORTAL).append(TAB);
                    /* 11 Annotation_Extensions ::= [Extension_Conj] ('|' Extension_Conj)* e.g. BFO:0000066(GO:0005829)	*/
                    associationSb.append(TAB);
                    /* 12 Annotation_Properties ::= [Property_Value_Pair] ('|' Property_Value_Pair)* e.g. contributor-id=https://orcid.org/0000-0002-1478-7671 */
                    // Note: In the future we can add the curator ORCID, for the moment we don't have that information stored
                    // e.g. contributor-id=https://orcid.org/0000-0002-1706-4196
                    associationSb.append(TAB);

                    associationSb.append(NEW_LINE);
                } else {
                    System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") without proper complex GO Xref to export");
                    return false;
                }
            }
        } else if (!intactComplex.isPredictedComplex()) {
            // Go xrefs are only expected for curated complexes
            System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") [ " + intactComplex.getOrganism().getScientificName() + "] has empty complex GO Xref");
            return false;
        }

        return true;
    }

    private static String collectRelation(ComplexGOXref goTerm) {
        String qualifier = null;
        switch (goTerm.getQualifier().getMIIdentifier()) {
            case COMPONENT_MI_REF:
                //"located in";
                qualifier = "RO:0001025";
                break;
            case PROCESS_MI_REF:
                //"involved_in";
                qualifier = "RO:0002331";
                break;
            case FUNCTION_MI_REF:
                //"enables";
                qualifier = "RO:0002327";
                break;
        }
        return qualifier;
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

    private static String collectReference(ComplexGOXref goTerm, IntactComplex intactComplex) {

        String reference = null;
        String pubmed = goTerm.getPubmed();
        String evidence = collectEco(goTerm, intactComplex);

        if (pubmed != null) {
            if (pubmed.startsWith("EBI-")) {
                //reference = "IntAct:" + pubmed;
                // This case shouldn't happen, wrong annotation in the complex portal - report to curators
                System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found a go term with EBI evidence " + pubmed + " " + goTerm.getId() + " (" + evidence + ")");
            } else if (pubmed.startsWith("CPX-")) {
                reference = COMPLEX_PORTAL + ":" + pubmed;
                if (!ecoForCPX.contains(evidence)) {
                    System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found a go term with CPX evidence " + pubmed + " with wrong ECO code: " + goTerm.getId() + " (" + evidence + ")");
                }
            } else {
                reference = PMID + ":" + pubmed;
                if (!ecoForPubMed.contains(evidence)) {
                    System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found a go term with PubMed evidence " + pubmed + " with wrong ECO code: " + goTerm.getId() + " (" + evidence + ")");
                }
            }
        } else {
            System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found a go term that doesn't have a proper pubmed id (i.e. " + goTerm.getId() + ")");
        }

        return reference;
    }

    private static String collectEco(ComplexGOXref goTerm, IntactComplex intactComplex) {

        String eco = null;

        if (goTerm.getEvidenceType() != null) {
            Collection<Xref> ecoRefs = XrefUtils.collectAllXrefsHavingDatabase(goTerm.getEvidenceType().getIdentifiers(), Complex.ECO_MI, Complex.ECO);
            if (ecoRefs.size() == 1) {
                eco = ecoRefs.iterator().next().getId();
            } else {
                System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found a go term that doesn't have a proper eco code ("+ goTerm.getAc() + " " + goTerm.getId() + ")");
            }
        } else {
            System.err.println("ERROR: Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ") found a go term that doesn't have an eco code ("+ goTerm.getAc() + " " + goTerm.getId() + ")");
        }

        return eco;
    }
}
