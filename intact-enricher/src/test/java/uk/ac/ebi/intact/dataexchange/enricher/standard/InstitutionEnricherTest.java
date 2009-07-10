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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */

public class InstitutionEnricherTest extends IntactBasicTestCase {

    private InstitutionEnricher enricher;

    @Before
    public void beforeMethod() {
        enricher = InstitutionEnricher.getInstance();
    }

    @After
    public void afterMethod() {
        enricher.close();
        enricher = null;
    }


    @Test
    public void enrich_intact() {
        Institution ebi = new Institution("ebi");

        enricher.enrich(ebi);

        Assert.assertEquals("IntAct", ebi.getShortLabel());
        Assert.assertEquals(CvDatabase.INTACT_MI_REF, XrefUtils.getPsiMiIdentityXref(ebi).getPrimaryId());
    }

    @Test
    public void enrich_mint() {
        Institution ebi = new Institution("mint");

        enricher.enrich(ebi);

        Assert.assertEquals("MINT", ebi.getShortLabel());
        Assert.assertEquals(CvDatabase.MINT_MI_REF, XrefUtils.getPsiMiIdentityXref(ebi).getPrimaryId());
    }
    
    @Test
    public void enrich_dip() {
        Institution ebi = new Institution("ucla");

        enricher.enrich(ebi);

        Assert.assertEquals("DIP", ebi.getShortLabel());
        Assert.assertEquals(CvDatabase.DIP_MI_REF, XrefUtils.getPsiMiIdentityXref(ebi).getPrimaryId());
    }

}