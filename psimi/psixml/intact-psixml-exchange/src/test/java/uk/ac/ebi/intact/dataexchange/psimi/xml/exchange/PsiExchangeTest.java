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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.persistence.dao.entry.IntactEntryFactory;

import java.io.StringWriter;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiExchangeTest {

    private IntactContext context;

    @Before
    public void setUp() throws Exception {
        context = IntactContext.getCurrentInstance();
        context.getDataContext().beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        context.getDataContext().commitAllActiveTransactions();
        context = null;
    }

    @Test
    @Ignore
    public void exportXml() {

        IntactEntry entry = IntactEntryFactory.createIntactEntry(context)
                .addExperimentWithShortLabel("zhang-2006-1");

        StringWriter writer = new StringWriter();

        PsiExchange.exportToPsiXml(writer, entry);

        System.out.println(writer.toString());
    }
}