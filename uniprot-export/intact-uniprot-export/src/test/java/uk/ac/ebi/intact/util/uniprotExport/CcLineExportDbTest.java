/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.uniprotExport;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24-Aug-2006</pre>
 */
public class CcLineExportDbTest extends TestCase {

    private static final Log log = LogFactory.getLog(CcLineExportDbTest.class);


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        IntactContext.getCurrentInstance().getDataContext().commitAllActiveTransactions();
    }

    @Override
    protected void setUp() throws Exception {
        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    public void testGenerateCCLines() throws Exception {
        Collection<String> uniprotIds =
                CCLineExport.getEligibleProteinsFromFile(CcLineExportDbTest.class.getResource("uniprotlinks.dat").getFile());

        Writer ccWriter = new StringWriter();
        Writer goaWriter = new StringWriter();

        LineExportConfig config = new LineExportConfig();
        config.setIgnoreUniprotDrExportAnnotation(true);

        CCLineExport ccLineExport = new CCLineExport(ccWriter, goaWriter, config, System.out);

        //new CcLineExportProgressThread(ccLineExport, uniprotIds.size()).start();

        ccLineExport.generateCCLines(uniprotIds);

        assertEquals(3, ccLineExport.getCcLineCount());
        assertEquals(5, ccLineExport.getGoaLineCount());

        System.out.println(ccWriter.toString());
    }
    /*
  public void testGenerateCCLines_self() throws Exception
  {
      Collection<String> uniprotIds =
              CCLineExport.getEligibleProteinsFromFile(CcLineExportDbTest.class.getResource("uniprotlinks_self.dat").getFile());

      Writer ccWriter = new StringWriter();
      Writer goaWriter = new StringWriter();

      CCLineExport ccLineExport = new CCLineExport(ccWriter, goaWriter);

      ccLineExport.generateCCLines(uniprotIds);

      assertEquals(1, ccLineExport.getCcLineCount());

      System.out.println(ccWriter.toString());
  }
    */
}
