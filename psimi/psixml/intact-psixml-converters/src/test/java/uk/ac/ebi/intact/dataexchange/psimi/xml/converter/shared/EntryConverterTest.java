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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.easymock.classextension.EasyMock.createNiceMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IdSequenceGenerator;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.util.psivalidator.PsiValidator;
import uk.ac.ebi.intact.util.psivalidator.PsiValidatorMessage;
import uk.ac.ebi.intact.util.psivalidator.PsiValidatorReport;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EntryConverterTest extends AbstractConverterTest {

    private static final Log log = LogFactory.getLog(EntryConverterTest.class);

    private Entry entry;
    private EntryConverter entryConverter;

    @Before
    public void setUp() throws Exception {
        entry = createNiceMock(Entry.class);
        entryConverter = new EntryConverter(createNiceMock(Institution.class));
    }

    @After
    public void tearDown() throws Exception {
        entry = null;
        entryConverter = null;

        IdSequenceGenerator.getInstance().reset();
    }

    @Test
    public void mockRountrip() throws Exception {

        Entry beforeRountripEntry = PsiMockFactory.createMockEntry();

        int idNum = IdSequenceGenerator.getInstance().currentId();
        IdSequenceGenerator.getInstance().reset();

        IntactEntry intactEntry = entryConverter.psiToIntact(beforeRountripEntry);
        Entry afterRoundtripEntry = entryConverter.intactToPsi(intactEntry);

        assertEquals(idNum, IdSequenceGenerator.getInstance().currentId());

    }

    @Test
    public void roundtrip_intact() throws Exception {
        File file = getIntactFile();
        assertTrue("Document must be valid: " + file, xmlIsValid(new FileInputStream(file)));

        roundtripWithStream(new FileInputStream(file));
    }

    @Test
    public void roundtrip_mint() throws Exception {
        File file = getMintFile();
        assertTrue("Document must be valid: " + file, xmlIsValid(new FileInputStream(file)));

        roundtripWithStream(new FileInputStream(file));
    }

    @Test
    public void roundtrip_dip() throws Exception {
        File file = getDipFile();
        assertTrue("Document must be valid: " + file, xmlIsValid(new FileInputStream(file)));

        roundtripWithStream(new FileInputStream(file));
    }

    @Test
    public void publicationConversion() throws Exception {
        Entry beforeRountripEntry = PsiMockFactory.createMockEntry();

        IntactEntry intactEntry = entryConverter.psiToIntact(beforeRountripEntry);

        for (Experiment exp : intactEntry.getExperiments()) {
            assertNotNull(exp.getPublication());
        }
    }

    private static void roundtripWithStream(InputStream is) throws Exception {
        PsimiXmlReader reader = new PsimiXmlReader();
        EntrySet entrySet = reader.read(is);

        EntryConverter entryConverter = new EntryConverter(IntactContext.getCurrentInstance().getInstitution());

        Entry beforeRountripEntry = entrySet.getEntries().iterator().next();

        IntactEntry intactEntry = entryConverter.psiToIntact(beforeRountripEntry);
        Entry afterRoundtripEntry = entryConverter.intactToPsi(intactEntry);

        assertTrue("XML created after conversion roundtrip must be valid", xmlIsValid(afterRoundtripEntry));

        assertEquals("Number of interactions should be the same", beforeRountripEntry.getInteractions().size(), afterRoundtripEntry.getInteractions().size());
        assertEquals("Number of experiments should be the same",countExperimentsInEntry(beforeRountripEntry), afterRoundtripEntry.getExperiments().size());
        assertEquals("Number of interactors should be the same",countInteractorsInEntry(beforeRountripEntry), afterRoundtripEntry.getInteractors().size());
       
    }

    private static boolean xmlIsValid(InputStream xml) throws Exception {
        PsiValidatorReport report = PsiValidator.validate(new InputSource(xml));
        return analyzeReport(report);
    }

    private static boolean xmlIsValid(Entry entry) throws Exception {

        PsimiXmlWriter writer = new PsimiXmlWriter();
        String xml = writer.getAsString(new EntrySet(Arrays.asList(entry), 2, 5, 3));

        PsiValidatorReport report = PsiValidator.validate(xml);
        return analyzeReport(report);
    }

    private static boolean analyzeReport(PsiValidatorReport report) throws Exception {
        boolean isValid = report.isValid();

        if (!isValid && log.isErrorEnabled()) {
            for (PsiValidatorMessage message : report.getMessages()) {
                log.error("\t" + message);
            }
        }

        return isValid;
    }

    private static int countExperimentsInEntry(Entry entry) {
        Collection<ExperimentDescription> experiments;

        if (entry.getExperiments() != null && !entry.getExperiments().isEmpty()) {
            experiments = entry.getExperiments();
        } else {
            experiments = new HashSet<ExperimentDescription>();
            for (Interaction i : entry.getInteractions()) {
                experiments.addAll(i.getExperiments());
            }
        }

        return experiments.size();
    }

    private static int countInteractorsInEntry(Entry entry) {
        Collection<Interactor> interactors;

        if (entry.getInteractors() != null && !entry.getInteractors().isEmpty()) {
            interactors = entry.getInteractors();
        } else {
            interactors = new HashSet<Interactor>();
            for (Interaction i : entry.getInteractions()) {
                for (Participant p : i.getParticipants()) {
                    interactors.add(p.getInteractor());
                }
            }
        }

        return interactors.size();
    }
}