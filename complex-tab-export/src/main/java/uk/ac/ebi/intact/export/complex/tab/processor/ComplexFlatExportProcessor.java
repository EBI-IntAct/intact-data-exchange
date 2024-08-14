package uk.ac.ebi.intact.export.complex.tab.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.Complex;
import uk.ac.ebi.intact.export.complex.tab.ComplexFlatExportConfig;
import uk.ac.ebi.intact.export.complex.tab.ComplexFlatExportContext;
import uk.ac.ebi.intact.export.complex.tab.exception.ComplexExportException;
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
                try {
                    exportFile.writeComplex(intactComplex);
                } catch (ComplexExportException e) {
                    log.error("Error found in complex:" + complex.getComplexAc() + ": " + e.getMessage());
                    log.info("Complex " + complex.getComplexAc() + " will be excluded from export.");
                }
            }
        }
    }
}