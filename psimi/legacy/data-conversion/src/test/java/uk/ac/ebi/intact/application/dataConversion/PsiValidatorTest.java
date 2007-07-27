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

/**
 * Test for PsiValidator
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18-Aug-2006</pre>
 */
public class PsiValidatorTest extends TestCase
{

    private static final Log log = LogFactory.getLog(PsiValidatorTest.class);

    public void testValidate_Error() throws Exception
    {
        File xmlWithError = new File(PsiValidatorTest.class.getResource("/validator/psi1_error.xml").getFile());

        PsiValidatorReport report = PsiValidator.validate(xmlWithError);

        assertNotNull(report);
        assertFalse(report.isValid());
        assertEquals(1, report.getMessages().size());
    }

    public void testValidate_Ok() throws Exception
    {
        File xmlOk = new File(PsiValidatorTest.class.getResource("/validator/psi1_ok.xml").getFile());

        PsiValidatorReport report = PsiValidator.validate(xmlOk);

        assertTrue(report.isValid());
        assertEquals(0, report.getMessages().size());
    }

    public void testValidate253_Ok() throws Exception
    {
        File xmlOk = new File(PsiValidatorTest.class.getResource("/validator/psi253_ok.xml").getFile());

        PsiValidatorReport report = PsiValidator.validate(xmlOk);

        assertTrue(report.isValid());
        assertEquals(0, report.getMessages().size());
    }
}
