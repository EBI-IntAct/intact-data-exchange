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
package uk.ac.ebi.intact.psimitab;

import org.junit.Test;
import psidev.psi.mi.search.index.AbstractIndexWriter;
import uk.ac.ebi.intact.psimitab.search.IntActPsimiTabIndexWriter;

import java.io.File;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsimitabToolsTest extends AbstractPsimitabTestCase{

    @Test
    public void buildIndex() throws Exception {
       File psimitabFile = getFileByResources("/mitab_samples/intact.sample-extra.txt", PsimitabToolsTest.class );
//       File psimitabFile = new File(PsimitabToolsTest.class.getResource("/mitab_samples/intact.sample-extra.txt").getFile());
       File indexDirectory = new File("target", "PsimitabToolsTest"+System.currentTimeMillis);

       AbstractIndexWriter indexWriter = new IntActPsimiTabIndexWriter();

       PsimitabTools.buildIndex(indexDirectory, psimitabFile, true, true, indexWriter);
    }

}
