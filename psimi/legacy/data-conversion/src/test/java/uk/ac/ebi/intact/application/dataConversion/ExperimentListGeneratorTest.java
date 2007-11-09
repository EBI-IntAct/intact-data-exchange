/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.CvMapping;
import uk.ac.ebi.intact.business.IntactException;

import java.io.File;
import java.util.List;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09-Aug-2006</pre>
 */
public class ExperimentListGeneratorTest extends DataConversionAbstractTest
{

    public void testSearchPatternWithCommas() throws Exception
    {
        ExperimentListGenerator gen = new ExperimentListGenerator("jin-2000-3,jin-2000-5,jin-2000-4,jin-2000-2,jin-2000-1");
        gen.generateAllClassifications();
    }

    @Test
    public void testGenerate_ni1998() throws Exception
    {
        ExperimentListGenerator gen = new ExperimentListGenerator("ni-1998-2");
        gen.setOnlyWithPmid(true);

        List<ExperimentListItem> eliSpecies = gen.generateClassificationBySpecies();
        Assert.assertEquals(2, eliSpecies.size());
    }


}
