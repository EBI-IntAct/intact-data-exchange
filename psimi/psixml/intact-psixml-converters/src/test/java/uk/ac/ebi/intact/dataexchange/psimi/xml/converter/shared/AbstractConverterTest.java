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

import org.junit.Before;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.model.Institution;

import java.io.File;
import java.io.InputStream;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AbstractConverterTest extends IntactBasicTestCase {

    private static final String INTACT_FILE = "/xml/intact_2006-07-19.xml";
    private static final String MINT_FILE = "/xml/mint_2006-07-18.xml";
    private static final String DIP_FILE = "/xml/dip_2006-11-01.xml";

    protected File getIntactFile() {
        return new File(AbstractConverterTest.class.getResource(INTACT_FILE).getFile());
    }

    protected File getMintFile() {
        return new File(AbstractConverterTest.class.getResource(MINT_FILE).getFile());
    }

    protected File getDipFile() {
        return new File(AbstractConverterTest.class.getResource(DIP_FILE).getFile());
    }

    protected InputStream getIntactStream() {
         return AbstractConverterTest.class.getResourceAsStream(INTACT_FILE);
    }

    protected InputStream getMintStream() {
         return AbstractConverterTest.class.getResourceAsStream(MINT_FILE);
    }

    protected InputStream getDipStream() {
         return AbstractConverterTest.class.getResourceAsStream(DIP_FILE);
    }

    protected EntrySet getIntactEntrySet() throws Exception{
        PsimiXmlReader reader = new PsimiXmlReader();
        return reader.read(getIntactStream());
    }

    protected EntrySet getMintEntrySet() throws Exception{
        PsimiXmlReader reader = new PsimiXmlReader();
        return reader.read(getMintStream());
    }

    protected EntrySet getDipEntrySet() throws Exception{
        PsimiXmlReader reader = new PsimiXmlReader();
        return reader.read(getDipStream());
    }

    protected Institution getMockInstitution() {
        return new Institution("testInstitution");
    }

    @Deprecated
    protected IntactMockBuilder getIntactMockBuilder() {
        return getMockBuilder();
    }
}