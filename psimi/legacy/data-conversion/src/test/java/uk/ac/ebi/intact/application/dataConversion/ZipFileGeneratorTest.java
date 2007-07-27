/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileFilter;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18-Aug-2006</pre>
 */
public class ZipFileGeneratorTest extends TestCase
{

    private static final Log log = LogFactory.getLog(ZipFileGeneratorTest.class);

    public void testClusterAllXmlFilesFromDirectory()
    {
        File baseDir = new File(ZipFileGeneratorTest.class.getResource("/zip/base").getFile());
        assertTrue(baseDir.isDirectory());

        ZipFileGenerator.clusterAllXmlFilesFromDirectory(baseDir, true);

        File targetDir = new File(baseDir, "pmid/2006");
        assertTrue(targetDir.isDirectory());

        File[] zipFiles = targetDir.listFiles(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.getName().endsWith(".zip");
            }
        });

        assertEquals(3, zipFiles.length);
    }
}
