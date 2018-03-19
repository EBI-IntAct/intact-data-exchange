package uk.ac.ebi.intact.export.complex.go;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;
import uk.ac.ebi.intact.jami.service.ComplexService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * Export Gene Product Information Data from complex to GOA.
 */
@Service
public class ComplexExport2GPI {

    private static final char TAB = '\t';
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String COMPLEX_PORTAL = "ComplexPortal";
    private static final String INTACT = "IntAct";

    private static String folder;

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: ComplexExport2GPI <folder>");
            System.exit(1);
        }
        folder = args[0];

        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext(new String[]{"/META-INF/complex-go-export-config.xml"});
        ComplexExport2GPI service = (ComplexExport2GPI) springContext.getBean("complexExport2GPI");

        service.exportToGPI();

    }

    @Transactional(value = "jamiTransactionManager", readOnly = true)
    public void exportToGPI() throws IOException {

        ComplexService complexService = ApplicationContextProvider.getBean("complexService");
        Iterator<Complex> complexes = complexService.iterateAll();

        System.err.println("Complexes to export: " + complexService.countAll());

        // Format documentation: ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/UNIPROT/gp_association_readme
        BufferedWriter informationWriter = null;

        try {
            informationWriter = new BufferedWriter(new FileWriter(new File(folder + "complex_portal.gpi")));
            informationWriter.write("!gpi-version: 1.2");
            informationWriter.write(NEW_LINE);

            StringBuilder informationSb = new StringBuilder( 2048 );

            while (complexes.hasNext()) {
                Complex complex = complexes.next();
                if (complex instanceof IntactComplex) {

                    IntactComplex intactComplex = (IntactComplex) complex;
                    System.err.println("\nProcessing Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ")");

                    informationSb.setLength(0);

                    //For now we don't need to write the components of the complex
                    boolean writeInformation = informationLine(informationSb, intactComplex, false, null);

                    if (writeInformation) {
                        informationWriter.write(informationSb.toString());
                        informationWriter.flush();
                    }
                }
            }
        } finally {
            if (informationWriter != null) {
                informationWriter.close();
            }
        }
    }

    private static boolean informationLine(StringBuilder informationSb, IntactComplex intactComplex, boolean writeComponents, String parentAc) {

        // Prepare the gp_information file (complex participant) http://wiki.geneontology.org/index.php/Proposed_GPI1.2_format

        /*  1 DB */
        informationSb.append(COMPLEX_PORTAL).append(TAB);

        /*  2 DB_Object_ID */
        informationSb.append(intactComplex.getComplexAc()).append(TAB);

        /*  3 DB_Object_Symbol */
        informationSb.append(intactComplex.getShortName()).append(TAB);

        /*  4 DB_Object_Name */
        String name = intactComplex.getRecommendedName();
        if(name!=null) { // it can be removed for release-db
            informationSb.append(name.replaceAll("[\n\t\r]", " "));
        }
        informationSb.append(TAB);

        /*  5 DB_Object_Synonym(s) */
        if (!intactComplex.getAliases().isEmpty()) {
            String aliases = intactComplex.getAliases().stream()
                    .map(Alias::getName)
                    .filter(aName -> aName != null && !aName.equals(name))
                    .map(aName -> aName.replaceAll("[\n\t\r]", " "))
                    .collect(Collectors.joining("|"));

            informationSb.append(aliases);
        }
        informationSb.append(TAB);

        /*  6 DB_Object_Type */
        informationSb.append("protein_complex").append(TAB);

        /*  7 Taxon */
        informationSb.append("taxon:").append(intactComplex.getOrganism().getTaxId()).append(TAB);

        /*  8 Parent_Object_ID */
        if (parentAc != null){
            informationSb.append(COMPLEX_PORTAL).append(":").append(parentAc);
        }
        informationSb.append(TAB);

        /* 9 DB_Xref(s) */
        informationSb.append(TAB);

        /* 10 Properties */
        informationSb.append(TAB);

        informationSb.append(NEW_LINE);

        if (writeComponents) {
            for (Participant participant : intactComplex.getParticipants()) {
                final Interactor interactor = participant.getInteractor();
                if (interactor instanceof IntactComplex) {
                    if (!informationLine(informationSb, (IntactComplex) interactor, false, intactComplex.getComplexAc())) {
                        return false;
                    }
                } else {
                    if (!componentLine(informationSb, interactor, intactComplex.getComplexAc())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean componentLine(StringBuilder componentSb, Interactor interactor, String parentAc) {

        Xref identifier = interactor.getPreferredIdentifier();

        if (identifier == null) {
            System.err.println("Found an interactor that doesn't have any identity: " + interactor);
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
            /*  1 DB */
            componentSb.append(dbName).append(TAB);

            /*  2 DB_Object_ID */
            componentSb.append(id).append(TAB);

            String objectSymbol = interactor.getShortName();
            /*  3 DB_Object_Symbol */
            if(interactor instanceof Protein){
                objectSymbol = ((Protein)interactor).getGeneName();
            }

            componentSb.append(objectSymbol).append(TAB);

            /*  4 DB_Object_Name */
            componentSb.append(interactor.getFullName()).append(TAB);

            /*  5 DB_Object_Synonym(s) */
            final String name = objectSymbol;
            if (!interactor.getAliases().isEmpty()) {
                String aliases = interactor.getAliases().stream()
                        .map(Alias::getName)
                        .filter(aName -> aName != null && !aName.equals(name))
                        .map(aName -> aName.replaceAll("[\n\t\r]", " "))
                        .collect(Collectors.joining("|"));

                componentSb.append(aliases);
            }
            componentSb.append(TAB);

            /*  6 DB_Object_Type */
            String moleculeType = interactor.getInteractorType().getShortName();
            if (interactor instanceof Protein) {
                if (isSpliceVariant((Protein) interactor)) {
                    moleculeType = "isoform";
                } else if (isFeatureChain((Protein) interactor)) {
                    moleculeType = "feature";
                }
            }
            componentSb.append(moleculeType).append(TAB);

            /*  7 Taxon */
            if (interactor.getOrganism() != null) {
                componentSb.append("taxon:").append(interactor.getOrganism().getTaxId()).append(TAB);
            } else {
                componentSb.append(TAB);
            }
            /*  8 Parent_Object_ID */
            if (parentAc != null){
                componentSb.append(COMPLEX_PORTAL).append(":").append(parentAc);
            }
            componentSb.append(TAB);

            /* 9 DB_Xref(s) */
            componentSb.append(TAB);
            /* 10 Properties */
            componentSb.append(TAB);

            componentSb.append(NEW_LINE);
        }

        return true;
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
