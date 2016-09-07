package uk.ac.ebi.intact.export.complex.flat.helper;

import uk.ac.ebi.intact.export.complex.flat.ComplexFlatExportConfig;
import uk.ac.ebi.intact.export.complex.flat.ComplexFlatExportContext;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;

import java.util.List;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ComplexFlatExportDaoImpl implements ComplexFlatExportDao {
    private ComplexFlatExportConfig config = ComplexFlatExportContext.getInstance().getConfig();

    public List<IntactComplex> getAllComplexes() {
        return config.getIntactDao().getComplexDao().getAll();
    }
}
