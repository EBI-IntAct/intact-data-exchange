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
package uk.ac.ebi.intact.dataexchange.psimi.xml.exchange;

import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.psixml.generated.EntrySetProcessor;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.PsiProcessReport;

import java.io.InputStream;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Playground {

    public static void main(String[] args) throws Exception {

        InputStream is = Playground.class.getResourceAsStream("/xml/intact_2006-07-19.xml");

        PsimiXmlReader reader = new PsimiXmlReader();

        EntrySet entrySet = reader.read(is);

        System.out.println(entrySet.getEntries().iterator().next().getInteractions().size());


        EntrySetProcessor processor = new EntrySetProcessor();
        PsiProcessReport report = processor.run(entrySet);
    }

}