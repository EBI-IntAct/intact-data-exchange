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
import org.junit.Test;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Participant;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;

import javax.annotation.Resource;

/**
 * MiCvObjectFetcher Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class MiCvObjectFetcherTest extends EnricherBasicTestCase {

    @Resource(name = "miCvObjectFetcher")
    private MiCvObjectFetcher fetcher;

    @Test
    public void fetchByTermId_short() throws BridgeFailedException {
        CvTerm term = fetcher.fetchByIdentifier("MI:0326", CvTerm.PSI_MI);
        Assert.assertEquals("protein", term.getShortName());
    }

    @Test
    public void fetchByTermId_long() throws BridgeFailedException {
        CvTerm term = fetcher.fetchByIdentifier("MI:0001", CvTerm.PSI_MI);
        Assert.assertNotNull(term);
        Assert.assertEquals("interaction detect", term.getShortName());
        Assert.assertEquals("interaction detection method", term.getFullName());
    }

    @Test
    public void fetchByTermShortLabel() throws BridgeFailedException {
        CvTerm term = fetcher.fetchByName(Participant.UNSPECIFIED_ROLE, CvTerm.PSI_MI);
        Assert.assertNotNull(term);
        Assert.assertEquals(Participant.UNSPECIFIED_ROLE, term.getShortName());
        Assert.assertEquals("unspecified role", term.getFullName());
        Assert.assertEquals(Participant.UNSPECIFIED_ROLE_MI, term.getMIIdentifier());
    }
}