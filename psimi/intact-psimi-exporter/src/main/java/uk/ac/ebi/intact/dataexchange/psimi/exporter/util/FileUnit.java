package uk.ac.ebi.intact.dataexchange.psimi.exporter.util;

import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.ReleaseUnit;

import java.io.File;

/**
 * A file unit is a release unit containing a collection of files
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18/10/11</pre>
 */

public class FileUnit extends ReleaseUnit<File> {

    public FileUnit(String name) {
        super(name);
    }
}
