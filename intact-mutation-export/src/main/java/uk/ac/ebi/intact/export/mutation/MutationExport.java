package uk.ac.ebi.intact.export.mutation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.export.mutation.helper.Exporter;
import uk.ac.ebi.intact.export.mutation.helper.MutationExportDaoImpl;
import uk.ac.ebi.intact.export.mutation.helper.Consumer;
import uk.ac.ebi.intact.export.mutation.processor.MutationExportProcessor;
import uk.ac.ebi.intact.export.mutation.writer.FileExportHandler;

import java.io.File;
import java.io.IOException;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class MutationExport {
    private static final Log log = LogFactory.getLog(MutationExport.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: MutationExport <folder>");
            System.exit(1);
        }
        final String filename = args[0];

        MutationExportConfig config = MutationExportContext.getInstance().getConfig();
        config.setMutationExportDao(new MutationExportDaoImpl());

        try {
            MutationExportProcessor mutationExportProcessor = new MutationExportProcessor();
            config.setFileExportHandler(new FileExportHandler(new File(filename)));
            Exporter exporter = new Exporter(config.getFileExportHandler());
            config.setExporter(exporter);
            log.info("Starting the mutation export");
            mutationExportProcessor.exportAll();
        } catch (IOException e) {
            log.error("The repository " + filename + " cannot be found. We cannot write log files and so we cannot run a global mutation update.");
            e.printStackTrace();
        }
    }
}