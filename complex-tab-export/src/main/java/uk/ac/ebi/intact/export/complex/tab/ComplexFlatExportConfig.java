package uk.ac.ebi.intact.export.complex.tab;

import uk.ac.ebi.intact.export.complex.tab.writer.FileExportHandler;
import uk.ac.ebi.intact.jami.service.ComplexService;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ComplexFlatExportConfig {
    private FileExportHandler fileExportHandler;
    private ComplexService complexService;

    public FileExportHandler getFileExportHandler() {
        return fileExportHandler;
    }

    public void setFileExportHandler(FileExportHandler fileExportHandler) {
        this.fileExportHandler = fileExportHandler;
    }

    public ComplexService getComplexService() {
        return complexService;
    }

    public void setComplexService(ComplexService complexService) {
        this.complexService = complexService;
    }
}
