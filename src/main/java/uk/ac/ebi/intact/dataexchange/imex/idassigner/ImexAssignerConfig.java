package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import java.io.File;

/**
 * Imex update configuration.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class ImexAssignerConfig {

    private File updateLogsDirectory;

    public ImexAssignerConfig() {
    }

    public File getUpdateLogsDirectory() {
        return updateLogsDirectory;
    }

    public void setUpdateLogsDirectory( File updateLogsDirectory ) {
        this.updateLogsDirectory = updateLogsDirectory;
    }
}
