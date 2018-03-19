package uk.ac.ebi.intact.export.complex.tab.helper;

import uk.ac.ebi.intact.jami.model.extension.IntactComplex;

import java.util.List;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public interface ComplexFlatExportDao {

    public List<IntactComplex> getAllComplexes();
}
