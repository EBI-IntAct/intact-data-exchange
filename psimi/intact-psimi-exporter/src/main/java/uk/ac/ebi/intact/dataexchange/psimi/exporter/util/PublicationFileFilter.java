package uk.ac.ebi.intact.dataexchange.psimi.exporter.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * This filter will only select xml files and do a selection of the publication id
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/09/11</pre>
 */

public class PublicationFileFilter implements FilenameFilter {

    private String publicationId;
    // the separator
    private String separator = "_";
    // the extension
    private String extension = null;

    @Override
    public boolean accept(File dir, String name) {
        if (publicationId != null) {

            if (name.startsWith(publicationId + separator) || name.startsWith(publicationId + ".")) {
                return true;
            }
        }

        if (extension != null && name.endsWith(extension)) {
            return true;
        }

        return false;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
