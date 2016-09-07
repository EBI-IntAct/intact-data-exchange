package uk.ac.ebi.intact.export.complex.flat.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.export.complex.flat.ComplexFlatExportConfig;
import uk.ac.ebi.intact.export.complex.flat.ComplexFlatExportContext;
import uk.ac.ebi.intact.export.complex.flat.exception.ComplexExportException;
import uk.ac.ebi.intact.export.complex.flat.helper.RowFactory;
import uk.ac.ebi.intact.export.complex.flat.writer.ExportWriter;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;

import java.io.IOException;
import java.util.List;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ComplexFlatExportProcessor {
    private static final Log log = LogFactory.getLog(ComplexFlatExportProcessor.class);

    private ComplexFlatExportConfig config = ComplexFlatExportContext.getInstance().getConfig();

    public void exportAll() throws IOException {
        List<IntactComplex> complexList = getAllComplexes();
        log.info("Retrieved " + complexList.size() + " complexes.");
        for (IntactComplex intactComplex : complexList) {
            int taxId = intactComplex.getOrganism().getTaxId();
            ExportWriter exportFile = config.getFileExportHandler().getExportFile(taxId);
            if (exportFile == null) {
                exportFile = config.getFileExportHandler().createExportFile(taxId, intactComplex.getOrganism().getScientificName());
            }
            String[] field = null;
            try {
                field = RowFactory.convertComplexToExportLine(intactComplex);
            } catch (ComplexExportException e) {
                log.error("Error found in complex:" + intactComplex.getAc() + ": " + e.getMessage());
                log.info("Complex " + intactComplex.getAc() + " will be excluded from export.");
            }
            if (field != null){
                exportFile.writeHeaderIfNecessary("Complex ac", "Recommended name", "Aliases for complex", "Taxonomy identifier", "Identifiers (and stoichiometry) of molecules in complex", "Confidence", "Experimental evidence", "Go Annotations", "Cross references", "Description", "Complex properties", "Complex assembly", "Ligand", "Disease", "Agonist", "Antagonist", "Comment", "Source");
                exportFile.writeColumnValues(field[0], field[1], field[2], field[3], field[4], field[5], field[6], field[7], field[8], field[9], field[10], field[11], field[12], field[13], field[14], field[15], field[16], field[17]);
                exportFile.flush();
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    private List<IntactComplex> getAllComplexes() {
        return config.getComplexFlatExportDao().getAllComplexes();
    }
}