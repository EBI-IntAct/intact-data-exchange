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
package uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.enricher;

import org.junit.Test;
import org.junit.Assert;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.model.Experiment;

import java.io.*;

import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.ExperimentDescription;
import psidev.psi.mi.stylesheets.XslTransformerUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiEnricherTest {

    private static final String DIP_FILE = "/xml/dip_2006-11-01.xml";

    @Test
    public void enrichFile() throws Exception {
        InputStream is = PsiEnricherTest.class.getResourceAsStream(DIP_FILE);

        Writer writer = new StringWriter();

        EnricherConfig config = new EnricherConfig();
        config.setUpdateInteractionShortLabels(true);

        PsiEnricher.enrichPsiXml(is, writer, config);

        //System.out.println(writer.toString());

        // TODO not asserting anything !
    }

    @Test
    public void enrichFile_similarExp() throws Exception {
        final String pathToXml = "/xml/similarExperiments.dip.raw.xml";

        PsimiXmlReader reader = new PsimiXmlReader();
        EntrySet originalSet = reader.read(PsiEnricherTest.class.getResourceAsStream(pathToXml));

        Assert.assertEquals(1, originalSet.getEntries().size());
        Entry originalEntry = originalSet.getEntries().iterator().next();

        Assert.assertEquals(3, originalEntry.getInteractions().size());
        Assert.assertEquals(3, originalEntry.getExperiments().size());
        Assert.assertEquals(3, originalEntry.getInteractors().size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(baos);

        EnricherConfig config = new EnricherConfig();
        config.setUpdateInteractionShortLabels(true);

        // the actual method
        PsiEnricher.enrichPsiXml(PsiEnricherTest.class.getResourceAsStream(pathToXml), writer, config);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ByteArrayOutputStream expandedBaos = new ByteArrayOutputStream();

        XslTransformerUtils.compactPsiMi25(bais, expandedBaos);

        ByteArrayInputStream expandedBais = new ByteArrayInputStream(expandedBaos.toByteArray());

        reader = new PsimiXmlReader();
        EntrySet enrichedSet = reader.read(expandedBais);

        Entry entry = enrichedSet.getEntries().iterator().next();
        
        Assert.assertEquals(3, entry.getInteractions().size());
        Assert.assertEquals(3, entry.getExperiments().size());
        Assert.assertEquals(3, entry.getInteractors().size());
    }
}