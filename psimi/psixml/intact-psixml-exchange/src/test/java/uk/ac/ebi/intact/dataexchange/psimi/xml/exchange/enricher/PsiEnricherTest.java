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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import psidev.psi.mi.xml.PsimiXmlForm;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.converter.config.PsimiXmlConverterConfig;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.stylesheets.XslTransformerUtils;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;

import java.io.*;

/**
 * PsiEnricher Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiEnricherTest extends IntactBasicTestCase {

    private static final String DIP_FILE = "/xml/dip_2006-11-01.xml";

    @Autowired
    private PsiEnricher psiEnricher;

    @Test
    public void enrichFile() throws Exception {
        InputStream is = PsiEnricherTest.class.getResourceAsStream(DIP_FILE);

        Writer writer = new StringWriter();

        EnricherConfig config = new EnricherConfig();
        config.setUpdateInteractionShortLabels(true);
        config.setOboUrl("https://raw.githubusercontent.com/HUPO-PSI/psi-mi-CV/master/psi-mi.obo");

        psiEnricher.enrichPsiXml(is, writer, config);

        //System.out.println(writer.toString());

        // TODO not asserting anything !
    }

    @Test
    public void enrichFile_similarExp() throws Exception {
        final String pathToXml = "/xml/similarExperiments.dip.raw.xml";  // compact XML

        PsimiXmlReader reader = new PsimiXmlReader();
        EntrySet originalSet = reader.read(PsiEnricherTest.class.getResourceAsStream(pathToXml));

        Assert.assertEquals(1, originalSet.getEntries().size());
        Entry originalEntry = originalSet.getEntries().iterator().next();

        Assert.assertEquals(3, originalEntry.getInteractions().size());
        Assert.assertEquals(3, originalEntry.getExperiments().size());
        Assert.assertEquals(3, originalEntry.getInteractors().size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(baos);

        Writer sw = new StringWriter();

        EnricherConfig config = new EnricherConfig();
        config.setUpdateInteractionShortLabels(true);
        config.setOboUrl("https://raw.githubusercontent.com/HUPO-PSI/psi-mi-CV/master/psi-mi.obo");

        // the actual method
        System.out.println( "ConverterContext.getInstance().isGenerateExpandedXml(): " + ConverterContext.getInstance().isGenerateExpandedXml() );
//        ConverterContext.getInstance().setGenerateExpandedXml( true );
        psiEnricher.enrichPsiXml(PsiEnricherTest.class.getResourceAsStream(pathToXml), sw, config);
        System.out.println(sw.toString());

        if( true ) return;

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        ByteArrayOutputStream expandedBaos = new ByteArrayOutputStream();

        XslTransformerUtils.compactPsiMi25(bais, expandedBaos);

        ByteArrayInputStream expandedBais = new ByteArrayInputStream(expandedBaos.toByteArray());

        reader = new PsimiXmlReader();
        PsimiXmlConverterConfig converterConfig = new PsimiXmlConverterConfig();
        System.out.println("converterConfig.getXmlForm(): " + converterConfig.getXmlForm() );
		 converterConfig.setXmlForm( PsimiXmlForm.FORM_COMPACT);
		psidev.psi.mi.xml.converter.ConverterContext.getInstance().setConverterConfig(converterConfig);

        EntrySet enrichedSet = reader.read(expandedBais);
        Entry entry = enrichedSet.getEntries().iterator().next();

        Assert.assertEquals(3, entry.getInteractions().size());
        Assert.assertEquals(3, entry.getExperiments().size());
        Assert.assertEquals(3, entry.getInteractors().size());
    }

    private void printInputStream( InputStream bais ) throws IOException {
        InputStreamReader myreader = new InputStreamReader( bais );
        BufferedReader br = new BufferedReader( myreader );
        String line = null;
        while( (line = br.readLine()) != null ) {
            System.out.println( line );
        }
    }
}