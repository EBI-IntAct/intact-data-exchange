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
package uk.ac.ebi.intact.dataexchange.enricher.fetch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvTerm;
import uk.ac.ebi.intact.model.CvExperimentalRole;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvObjectFetcherTest {

    private CvObjectFetcher fetcher;

    @Before
    public void before() throws Exception {
        fetcher = CvObjectFetcher.getInstance();
    }

    @Test
    public void fetchByTermId_short() {
        CvTerm term = fetcher.fetchByTermId("MI:0326");
        Assert.assertEquals("protein", term.getShortName());
    }

    @Test
    public void fetchByTermId_long() {
        CvTerm term = fetcher.fetchByTermId("MI:0001");
        Assert.assertEquals("interaction detect", term.getShortName());
        Assert.assertEquals("interaction detection method", term.getFullName());
    }

    @Test
    public void fetchByTermShortLabel() {
        CvTerm term = fetcher.fetchByShortLabel(CvExperimentalRole.class, CvExperimentalRole.UNSPECIFIED);
        Assert.assertEquals(CvExperimentalRole.UNSPECIFIED, term.getShortName());
        Assert.assertEquals("unspecified role", term.getFullName());
        Assert.assertEquals(CvExperimentalRole.UNSPECIFIED_PSI_REF, term.getId());
    }
}