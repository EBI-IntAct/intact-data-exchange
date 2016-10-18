package uk.ac.ebi.intact.export.complex.flat;

import uk.ac.ebi.intact.export.complex.flat.helper.ComplexFlatExportDao;
import uk.ac.ebi.intact.export.complex.flat.helper.ComplexFlatExportDaoImpl;
import uk.ac.ebi.intact.export.complex.flat.writer.FileExportHandler;
import uk.ac.ebi.intact.jami.dao.IntactDao;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ComplexFlatExportConfig {
    private FileExportHandler fileExportHandler;
    private IntactDao intactDao;
    private ComplexFlatExportDao flatExportDao;
    private ComplexFlatExportDao complexFlatExportDao;

    public FileExportHandler getFileExportHandler() {
        return fileExportHandler;
    }

    public void setFileExportHandler(FileExportHandler fileExportHandler) {
        this.fileExportHandler = fileExportHandler;
    }

    public IntactDao getIntactDao() {
        return intactDao;
    }

    public void setIntactDao(IntactDao intactDao) {
        this.intactDao = intactDao;
    }

    public ComplexFlatExportDao getFlatExportDao() {
        return flatExportDao;
    }

    public void setFlatExportDao(ComplexFlatExportDao flatExportDao) {
        this.flatExportDao = flatExportDao;
    }

    public ComplexFlatExportDao getComplexFlatExportDao() {
        return complexFlatExportDao;
    }

    public void setComplexFlatExportDao(ComplexFlatExportDaoImpl complexFlatExportDao) {
        this.complexFlatExportDao = complexFlatExportDao;
    }
}
