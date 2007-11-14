/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.CvMapping;

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
    

    private static final Log log = LogFactory.getLog(ExperimentListGeneratorTest.class);
     /*
    public void testGenerateListGavin()
    {
        ExperimentListGenerator gen = new ExperimentListGenerator("gavin%");

        List<ExperimentListItem> eliSpecies = gen.generateClassificationBySpecies();
        //log.debug("By species: "+eliSpecies.size());



        assertEquals(3, eliSpecies.size());
        assertEquals("species"+ File.separator +"yeast_small-01.xml gavin-2006-1", eliSpecies.get(0).toString());
        assertEquals("species"+ File.separator +"yeast_small-02.xml gavin-2002-1", eliSpecies.get(1).toString());
        assertEquals("species"+ File.separator +"yeast_small-03.xml gavin-2006-2", eliSpecies.get(2).toString());


        List<ExperimentListItem> eliPublications = gen.generateClassificationByPublications();
        //log.debug("By publications: "+eliPublications.size());

        assertEquals(3, eliPublications.size());
        assertEquals("pmid"+ File.separator +"2003"+ File.separator +"11805826.xml gavin-2002-1", eliPublications.get(0).toString());
        assertEquals("pmid"+ File.separator +"2005"+ File.separator +"16429126-01.xml gavin-2006-1", eliPublications.get(1).toString());
        assertEquals("pmid"+ File.separator +"2005"+ File.separator +"16429126-02.xml gavin-2006-2", eliPublications.get(2).toString());

        Set<Experiment> negativeExps = gen.getNegativeExperiments();
        assertEquals(0, negativeExps.size());

        List<ExperimentListItem> allItems = gen.generateAllClassifications();
        assertEquals(6, allItems.size());

    }

    public void testGenerateListWithNegative()
    {
        ExperimentListGenerator gen = new ExperimentListGenerator("lim-2006-%");

        List<ExperimentListItem> eliSpecies = gen.generateClassificationBySpecies();

        assertEquals(3, eliSpecies.size());
        assertEquals("species"+ File.separator +"human_small-01.xml lim-2006-2", eliSpecies.get(0).toString());
        assertEquals("species"+ File.separator +"human_small-02.xml lim-2006-3,lim-2006-1", eliSpecies.get(1).toString());
        assertEquals("species"+ File.separator +"human_small-02_negative.xml lim-2006-4", eliSpecies.get(2).toString());

        List<ExperimentListItem> eliPublications = gen.generateClassificationByPublications();

        assertEquals(3, eliPublications.size());
        assertEquals("pmid"+ File.separator +"2006"+ File.separator +"16713569-01.xml lim-2006-2", eliPublications.get(0).toString());
        assertEquals("pmid"+ File.separator +"2006"+ File.separator +"16713569-02.xml lim-2006-3,lim-2006-1", eliPublications.get(1).toString());
        assertEquals("pmid"+ File.separator +"2006"+ File.separator +"16713569-02_negative.xml lim-2006-4", eliPublications.get(2).toString());

        Set<Experiment> negativeExps = gen.getNegativeExperiments();
        assertEquals(1, negativeExps.size());
    }

    public void testGenerateListWithLargeScaleExperiment()
    {
        ExperimentListGenerator gen = new ExperimentListGenerator("giot-2003-%");

        List<ExperimentListItem> eliSpecies = gen.generateClassificationBySpecies();
        //System.out.println("By species: "+eliSpecies);

        assertEquals(11, eliSpecies.size());
        assertEquals("species"+ File.separator +"drome_giot-2003-1_01.xml giot-2003-1 [1,2000]", eliSpecies.get(0).toString());
        assertEquals("species"+ File.separator +"drome_giot-2003-1_10.xml giot-2003-1 [18001,20000]", eliSpecies.get(9).toString());

        List<ExperimentListItem> eliPublications = gen.generateClassificationByPublications();
        //System.out.println("By pub: "+eliPublications);

        assertEquals(11, eliPublications.size());
        assertEquals("pmid"+ File.separator +"2004"+ File.separator +"14605208_giot-2003-1_04.xml giot-2003-1 [6001,8000]", eliPublications.get(3).toString());
        assertEquals("pmid"+ File.separator +"2004"+ File.separator +"14605208_giot-2003-1_10.xml giot-2003-1 [18001,20000]", eliPublications.get(9).toString());
    }

     */
    public void testSearchPatternWithCommas() throws Exception
    {
        File reverseMappingFile = new File(ExperimentListGeneratorTest.class.getResource("/reverseMapping.txt").getFile());

        CvMapping mapping = new CvMapping();
        mapping.loadFile(reverseMappingFile);

        boolean failed = false;
        try
        {
            ExperimentListGenerator gen = new ExperimentListGenerator("jin-2000-3,jin-2000-5,jin-2000-4,jin-2000-2,jin-2000-1");
            gen.generateAllClassifications();
        }
        catch (Exception e)
        {
            failed = true;
        }

        assertTrue("If a search pattern containing commas is provided, the generator should fail", failed);
    }

    
    public void testGenerate_ni1998() throws Exception
    {
        ExperimentListGenerator gen = new ExperimentListGenerator("ni-1998-2");
        gen.setOnlyWithPmid(true);

        List<ExperimentListItem> eliSpecies = gen.generateClassificationBySpecies();
        assertEquals(2, eliSpecies.size());
    }


}
