package uk.ac.ebi.intact.export.complex.tab.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.Complex;
import uk.ac.ebi.intact.export.complex.tab.ComplexFlatExportConfig;
import uk.ac.ebi.intact.export.complex.tab.ComplexFlatExportContext;
import uk.ac.ebi.intact.export.complex.tab.exception.ComplexExportException;
import uk.ac.ebi.intact.export.complex.tab.helper.RowFactory;
import uk.ac.ebi.intact.export.complex.tab.writer.ExportWriter;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ComplexFlatExportProcessor {
    private static final Log log = LogFactory.getLog(ComplexFlatExportProcessor.class);

    private final ComplexFlatExportConfig config = ComplexFlatExportContext.getInstance().getConfig();

    @Transactional(value = "jamiTransactionManager", readOnly = true)
    public void exportAll() throws IOException {

        Iterator<Complex> complexes = config.getComplexService().iterateAll();
        while (complexes.hasNext()) {
            Complex complex = complexes.next();

            if (complex instanceof IntactComplex) {

                IntactComplex intactComplex = (IntactComplex) complex;
                System.err.println("\nProcessing Complex " + intactComplex.getComplexAc() + " (" + intactComplex.getAc() + ")");

                int taxId = intactComplex.getOrganism().getTaxId();
                ExportWriter exportFile = config.getFileExportHandler().getExportFile(taxId);
                if (exportFile == null) {
                    exportFile = config.getFileExportHandler().createExportFile(taxId);
                }
                String[] field = null;
                try {
                    field = RowFactory.convertComplexToExportLine(intactComplex);
                } catch (ComplexExportException e) {
                    log.error("Error found in complex:" + intactComplex.getComplexAc() + ": " + e.getMessage());
                    log.info("Complex " + intactComplex.getComplexAc() + " will be excluded from export.");
                }
                if (field != null) {
                    exportFile.writeHeaderIfNecessary("Complex ac", "Recommended name", "Aliases for complex",
                            "Taxonomy identifier", "Identifiers (and stoichiometry) of molecules in complex", "Evidence Code",
                            "Experimental evidence", "Go Annotations", "Cross references", "Description", "Complex properties",
                            "Complex assembly", "Ligand", "Disease", "Agonist", "Antagonist", "Comment", "Source", "Expanded participant list");
                    exportFile.writeColumnValues(field[0], field[1], field[2], field[3], field[4], field[5], field[6],
                            field[7], field[8], field[9], field[10], field[11], field[12], field[13], field[14], field[15],
                            field[16], field[17], field[18]);
                    exportFile.flush();
                }
            }
        }
    }
}