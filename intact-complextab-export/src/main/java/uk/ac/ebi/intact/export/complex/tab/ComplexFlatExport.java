package uk.ac.ebi.intact.export.complex.tab;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.export.complex.tab.helper.ComplexFlatExportDaoImpl;
import uk.ac.ebi.intact.export.complex.tab.processor.ComplexFlatExportProcessor;
import uk.ac.ebi.intact.export.complex.tab.writer.FileExportHandler;

import java.io.File;
import java.io.IOException;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ComplexFlatExport {
    private static final Log log = LogFactory.getLog(ComplexFlatExport.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: ComplexFlatExport <folder>");
            System.exit(1);
        }
        final String filename = args[0];

        ComplexFlatExportConfig config = ComplexFlatExportContext.getInstance().getConfig();
        config.setComplexFlatExportDao(new ComplexFlatExportDaoImpl());
        try {
            config.setFileExportHandler(new FileExportHandler(new File(filename)));
            ComplexFlatExportProcessor complexFlatExportProcessor = new ComplexFlatExportProcessor();
            log.info("Starting the complex flat export");
            complexFlatExportProcessor.exportAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}