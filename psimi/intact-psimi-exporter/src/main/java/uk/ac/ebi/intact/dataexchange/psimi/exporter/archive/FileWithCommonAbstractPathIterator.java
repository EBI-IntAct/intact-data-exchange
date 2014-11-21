package uk.ac.ebi.intact.dataexchange.psimi.exporter.archive;

import java.io.File;

/**
 * TThis iterator will read FileUnit and the unit name will be the common abstract pathName
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18/10/11</pre>
 */

public class FileWithCommonAbstractPathIterator extends FileWithCommonNameIterator {

    public FileWithCommonAbstractPathIterator(File directory, NameTruncation nameTruncation, String[] extensions, boolean recursive) {
        super(directory, nameTruncation, extensions, recursive);
    }

    @Override
    protected String extractCommonName(File firstFile) {
        String commonName;
        if (fileNameTruncation == null){
            commonName = firstFile.getAbsolutePath().substring(0, firstFile.getAbsolutePath().lastIndexOf("."));
        }
        else {
            String fileName = firstFile.getName();
            String totalPath = firstFile.getAbsolutePath();
            String pathToFile = totalPath.substring(0, totalPath.lastIndexOf(File.separator));
            commonName = pathToFile + File.separator + fileNameTruncation.truncate(fileName.substring(0, fileName.lastIndexOf(".")));
        }

        return commonName;
    }
}
