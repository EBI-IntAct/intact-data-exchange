package uk.ac.ebi.intact.export.complex.go;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.service.ComplexService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Export Gene Product Information Data from complex to GOA.
 */
@Service
public class ComplexExport2GPIVersion2 {

    private static final char TAB = '\t';
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String COMPLEX_PORTAL = "ComplexPortal";
    private static final String PROTEIN_CONTAINING_COMPLEX_GO = "GO:0032991";
    private static final String INTACT = "IntAct";
    private static final String TAXON_DB = "NCBITaxon";
    private static final Format DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    private static String fileName;

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: ComplexExport2GPIVersion2 <file_name>");
            System.exit(1);
        }
        fileName = args[0];

        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext(new String[]{"/META-INF/complex-go-export-config.xml"});
        ComplexExport2GPIVersion2 service = (ComplexExport2GPIVersion2) springContext.getBean("complexExport2GPIVersion2");

        service.exportToGPI();

    }

    @Transactional(value = "jamiTransactionManager", readOnly = true)
    public void exportToGPI() throws IOException {

        ComplexService complexService = ApplicationContextProvider.getBean("complexService");
        Iterator<Complex> complexes = complexService.iterateAll();

        System.err.println("Complexes to export: " + complexService.countAll());

        // Format documentation: https://github.com/geneontology/go-annotation/blob/master/specs/gpad-gpi-2-0.md

        File file = new File(fileName);
        //Creates parent directories if they don't exist
        file.getParentFile().mkdirs();

        try (BufferedWriter informationWriter = new BufferedWriter(new FileWriter(file))) {
            informationWriter.write("!gpi-version: 2.0");
            informationWriter.write(NEW_LINE);
            informationWriter.write("!generated-by: ComplexPortal");
            informationWriter.write(NEW_LINE);
            informationWriter.write("!date-generated: " + DATE_FORMATTER.format(new Date()));
            informationWriter.write(NEW_LINE);

            StringBuilder informationSb = new StringBuilder(2048);

            while (complexes.hasNext()) {
                Complex complex = complexes.next();
                if (complex instanceof IntactComplex) {

                    IntactComplex intactComplex = (IntactComplex) complex;
                    System.err.println("\nProcessing Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ")");

                    informationSb.setLength(0);

                    // We don't need to expand the components of the complex as new GPI lines, however the 2.0 format
                    // allows to list the identifiers in column 11.
                    boolean writeInformation = informationLine(informationSb, intactComplex);

                    if (writeInformation) {
                        informationWriter.write(informationSb.toString());
                        informationWriter.flush();
                    }
                }
            }
        }
    }

    private static boolean informationLine(StringBuilder informationSb, IntactComplex intactComplex) {

        // Prepare the gp_information file (complex participant) https://github.com/geneontology/go-annotation/blob/master/specs/gpad-gpi-2-0.md

        /* 1 DB_Object_ID ::= ID CURIE syntax e.g. UniProtKB:P56704 */
        informationSb.append(COMPLEX_PORTAL).append(":").append(intactComplex.getComplexAc()).append(TAB);

        /* 2 DB_Object_Symbol ::= xxxx e.g. AMOT */
        informationSb.append(intactComplex.getShortName()).append(TAB);

        /* 3 DB_Object_Name ::= xxxx e.g. Angiomotin */
        String name = intactComplex.getRecommendedName();
        if (name != null) { // it can be removed for release-db
            informationSb.append(name.replaceAll("[\n\t\r]", " "));
        }
        informationSb.append(TAB);

        /* 4 DB_Object_Synonym(s) ::= [Label] ('|' Label)* e.g AMOT|KIAA1071 */
        if (!intactComplex.getAliases().isEmpty()) {
            String synonyms = intactComplex.getAliases().stream()
                    .map(Alias::getName)
                    .filter(aName -> aName != null && !aName.equals(name))
                    .map(aName -> aName.replaceAll("[\n\t\r]", " "))
                    .collect(Collectors.joining("|"));

            informationSb.append(synonyms);
        }
        informationSb.append(TAB);

        /* 5 DB_Object_Type ::= OBO_ID from Sequence Ontology OR Protein Ontology OR Gene Ontology e.g. PR:000000001 */
        // Note: following recommendations we have chosen protein-containing complex GO:0032991
        informationSb.append(PROTEIN_CONTAINING_COMPLEX_GO).append(TAB);

        /* 6 DB_Object_Taxon ::= NCBITaxon:[Taxon_ID] e.g NCBITaxon:9606 */
        informationSb.append(TAXON_DB).append(":").append(intactComplex.getOrganism().getTaxId()).append(TAB);

        /* 7 Encoded_By ::= [ID] ('|' ID)* for proteins and transcripts,
        this refers to the gene id that encodes those entities e.g. HGNC:17810*/
        // Not applicable for complexes
        informationSb.append(TAB);

        /* 8 Parent_Protein ::= [ID] ('|' ID)* when column 1 refers to a protein isoform or modified protein,
        this column refers to the gene-centric reference protein accession of the column 1 entry.*/
        //There is no need to add parent complexes here, only for master proteins.
        informationSb.append(TAB);

        /* 9 Protein_Containing_Complex_Members ::= [ID] ('|' ID)* e.g. UniProtKB:Q15021|UniProtKB:Q15003 */
        Set<String> components = new TreeSet<>();
        collectMembers(components, intactComplex);
        informationSb.append(String.join("|", components)).append(TAB);

        /* 10 DB_Xrefs ::= [ID] ('|' ID)* */
        informationSb.append(TAB);

        /* 11 Gene_Product_Properties ::= [Property_Value_Pair] ('|' Property_Value_Pair)* e.g. db-subset=Swiss-Prot */
        informationSb.append(TAB);

        informationSb.append(NEW_LINE);

        return true;
    }

    private static boolean collectMembers(Set<String> componentSb, IntactComplex intactComplex) {
        for (ModelledParticipant participant : intactComplex.getParticipants()) {
            final Interactor interactor = participant.getInteractor();
            if (interactor instanceof IntactComplex) {
                if (!collectMembers(componentSb, (IntactComplex) interactor)) {
                    return false;
                }
            }
            else if (interactor instanceof Protein) {
                if (!idExtraction(componentSb, interactor)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean idExtraction(Set<String> componentSb, Interactor interactor) {

        Xref identifier = interactor.getPreferredIdentifier();

        if (identifier == null) {
            System.err.println("ERROR: Found an interactor that doesn't have any identity: " + interactor);
            return false;

        } else {
            String id = identifier.getId();
            String dbName = identifier.getDatabase().getShortName();

            if ("uniprotkb".equals(dbName)) {
                dbName = "UniProtKB";
                if (id.contains("-PRO_")) {
                    id = id.replace("-", ":");
                }
            }
            if ("intact".equals(dbName)) {
                dbName = INTACT;
            }
            /* 1 DB_Object_ID ::= ID CURIE syntax e.g. UniProtKB:P56704 */
            componentSb.add((dbName) + (":") + (id));
        }

        return true;
    }
}
