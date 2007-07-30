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
package uk.ac.ebi.intact.dataexchange.psimi.xml.enricher;

import org.junit.Test;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

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

        PsiEnricher.enrichPsi(is, writer, config);

        System.out.println(writer.toString());
    }
}