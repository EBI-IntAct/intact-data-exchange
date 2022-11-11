/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import static junit.framework.Assert.*;
import org.junit.*;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Interaction;
import psidev.psi.mi.xml.model.Names;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IdSequenceGenerator;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ExportProfile;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.IntactEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * EntryConverter Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EntryConverterTest extends AbstractConverterTest {

    private EntryConverter entryConverter;

    @Before
    public void setUp() {
        entryConverter = new EntryConverter();
        ConversionCache.clear();
        output = false;

        IdSequenceGenerator.getInstance().reset();
    }

    @After
    public void tearDown() {
        entryConverter = null;

        IdSequenceGenerator.getInstance().reset();
    }

    @Test
    public void mockRountrip() {
        int idNum = IdSequenceGenerator.getInstance().currentId();
        IdSequenceGenerator.getInstance().reset();

        assertEquals(idNum, IdSequenceGenerator.getInstance().currentId());

    }

    @Test
    public void roundtrip_intact() throws Exception {
        File file = getIntactFile();

        roundtripWithStream(new FileInputStream(file), "European Bioinformatics Institute");
    }

    static boolean output = false;

    @Test
    public void roundtrip_dip() throws Exception {
        File file = getDipFile();

        //output = true;
        roundtripWithStream(new FileInputStream(file), "DIP");
    }

    @Test
    public void roundtrip_mint() throws Exception {
        File file = getMintFile();

        roundtripWithStream(new FileInputStream(file), "MINT");
    }

    @Test
    public void roundtrip_similarInteractions() throws Exception {
        File file = new File(EntryConverterTest.class.getResource("/xml/dupes.xml").getFile());

        PsimiXmlReader reader = new PsimiXmlReader();
        EntrySet entrySet = reader.read(new FileInputStream(file));

        EntryConverter entryConverter = new EntryConverter();
        IntactEntry intactEntry = entryConverter.psiToIntact(entrySet.getEntries().iterator().next());

        Assert.assertEquals(8, intactEntry.getInteractions().size());
    }

    @Test
    public void roundtrip_similarExperiments() throws Exception {
        final String resource = "/xml/similarExperiments.dip.raw.xml";
        InputStream is = EntryConverterTest.class.getResourceAsStream(resource);

        roundtripWithStream(is, "DIP");
    }

    @Test
    public void roundtrip_mint_selfInteraction_2comp_1interactor() throws Exception {
        final String resource = "/xml/15733864.mint.raw.xml";
        InputStream is = EntryConverterTest.class.getResourceAsStream(resource);

        File file = new File(EntryConverterTest.class.getResource(resource).getFile());

        roundtripWithStream(is, "MINT");
    }

    @Test
    public void roundtrip_intact_redundantExperiments() throws Exception {
        final String resource = "/xml/16267818.intact.raw.xml";
        InputStream is = EntryConverterTest.class.getResourceAsStream(resource);

        roundtripWithStream(is, "European Bioinformatics Institute");
    }

    @Test
    public void roundtrip_similarInteractions_sameLabel() throws Exception {
        File file = new File(EntryConverterTest.class.getResource("/xml/dupes.xml").getFile());

        PsimiXmlReader reader = new PsimiXmlReader();
        EntrySet entrySet = reader.read(new FileInputStream(file));

        for (Entry entry : entrySet.getEntries()) {
            for (Interaction psiInteraction : entry.getInteractions()) {
                Names names = new Names();
                names.setShortLabel("sameLabel");
                psiInteraction.setNames(names);
            }
        }

        EntryConverter entryConverter = new EntryConverter();
        IntactEntry intactEntry = entryConverter.psiToIntact(entrySet.getEntries().iterator().next());

        Assert.assertEquals(8, intactEntry.getInteractions().size());
    }

    @Test
    public void publicationConversion() throws Exception {
        Entry beforeRountripEntry = PsiMockFactory.createMockEntry();

        IntactEntry intactEntry = entryConverter.psiToIntact(beforeRountripEntry);

        for (Experiment exp : intactEntry.getExperiments()) {
            assertNotNull(exp.getPublication());
        }
    }

    private static void roundtripWithStream(InputStream is, String institutionShortLabel) throws Exception {
        PsimiXmlReader reader = new PsimiXmlReader();
        EntrySet entrySet = reader.read(is);

        final ConverterContext context = ConverterContext.getInstance();
        context.configure( new ExportProfile() {
            public void configure( ConverterContext context ) {
                context.setGenerateExpandedXml( true );
            }
        } );

        EntryConverter entryConverter = new EntryConverter();

        for (Entry beforeRountripEntry : entrySet.getEntries())  {
            IntactEntry intactEntry = entryConverter.psiToIntact(beforeRountripEntry);
            Entry afterRoundtripEntry = entryConverter.intactToPsi(intactEntry);

            Assert.assertEquals(institutionShortLabel, intactEntry.getInstitution().getShortLabel());
            Assert.assertEquals(institutionShortLabel, intactEntry.getInteractions().iterator().next().getOwner().getShortLabel());
            Assert.assertEquals(institutionShortLabel, intactEntry.getExperiments().iterator().next().getOwner().getShortLabel());
            Assert.assertEquals(institutionShortLabel, intactEntry.getInteractors().iterator().next().getOwner().getShortLabel());

            assertEquals("Number of interactions should be the same", beforeRountripEntry.getInteractions().size(), afterRoundtripEntry.getInteractions().size());
            assertEquals("Number of experiments should be the same",
                         PsiConverterUtils.nonRedundantExperimentsFromPsiEntry( beforeRountripEntry ).size(),
                         PsiConverterUtils.nonRedundantExperimentsFromPsiEntry( afterRoundtripEntry ).size());
            assertEquals("Number of interactors should be the same",
                         PsiConverterUtils.nonRedundantInteractorsFromPsiEntry(beforeRountripEntry).size(),
                         PsiConverterUtils.nonRedundantInteractorsFromPsiEntry(afterRoundtripEntry).size());
        }
       
    }
}
