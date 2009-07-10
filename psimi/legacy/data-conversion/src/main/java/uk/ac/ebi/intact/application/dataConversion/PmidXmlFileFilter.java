/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Filters the files that follow this pattern %PMID%.xml
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16-Aug-2006</pre>
 */
public class PmidXmlFileFilter implements FileFilter
{

    private static final Log log = LogFactory.getLog(PmidXmlFileFilter.class);

    /**
     * Pattern matching the filename of PSI-XML file generated using the pubmed classification.
     * <p/>
     * It matches 12345.xml, 12345_author-2006-1.xml
     */
    public static final String PMID_XML_FILE_PATTERN = "^((?:unassigned)?(?:\\d+))\\D.*xml";

    /**
     * Compiled pattern.
     */
    public static Pattern PMID_FILENAME_PATTERN = Pattern.compile( PMID_XML_FILE_PATTERN );

    public boolean accept(File pathname)
    {
        if (pathname.isDirectory())
        {
            return true;
        }
        return PMID_FILENAME_PATTERN.matcher(pathname.getName()).find();
    }
}
