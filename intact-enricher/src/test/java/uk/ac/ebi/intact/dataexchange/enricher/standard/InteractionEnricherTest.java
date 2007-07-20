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
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactAbstractTestCase;
import uk.ac.ebi.intact.core.unit.IntactUnitDataset;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.unitdataset.PsiTestDatasetProvider;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@IntactUnitDataset(dataset = PsiTestDatasetProvider.INTACT_JUL_06, provider = PsiTestDatasetProvider.class)
public class InteractionEnricherTest extends IntactAbstractTestCase {

    private InteractionEnricher enricher;

    @Before
    public void beforeMethod() {
        enricher = InteractionEnricher.getInstance();
    }

    @After
    public void afterMethod() {
        enricher.close();
        enricher = null;
    }

    @Test
    public void enrich() {
        Interaction interaction = getDaoFactory().getInteractionDao().getAll(0,1).iterator().next();

        assertNotNull("This interaction must exist", interaction);

        enricher.enrich(interaction);

    }

}