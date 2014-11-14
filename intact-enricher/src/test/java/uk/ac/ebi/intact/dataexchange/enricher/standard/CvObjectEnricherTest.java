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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Participant;
import psidev.psi.mi.jami.model.impl.DefaultCvTerm;
import psidev.psi.mi.jami.utils.CvTermUtils;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvObjectEnricherTest extends EnricherBasicTestCase {

    @Autowired
    @Qualifier("miCvObjectEnricher")
    private MiCvObjectEnricher enricher;

    @Test
    public void enrich_noShortLabel() throws Exception {
        CvTerm bioRole = CvTermUtils.createUnspecifiedRole();
        bioRole.setFullName("unspecified role");
        bioRole.setShortName("test");

        enricher.enrich(bioRole);

        Assert.assertEquals(Participant.UNSPECIFIED_ROLE, bioRole.getShortName());
    }

    @Test
    public void enrich_noShortLabel_cropped() throws Exception {

        CvTerm cvIdentification = new DefaultCvTerm("test", "MI:0396");
        cvIdentification.setFullName("predetermined participant");

        enricher.enrich(cvIdentification);

        Assert.assertEquals("predetermined", cvIdentification.getShortName());
    }

    @Test
    public void enrich_noXrefs() throws Exception {
        CvTerm cvBiologicalRole = new DefaultCvTerm(Participant.ENZYME_ROLE);

        enricher.enrich(cvBiologicalRole);

        Assert.assertNotNull(cvBiologicalRole.getFullName());
        Assert.assertEquals(Participant.ENZYME_ROLE, cvBiologicalRole.getShortName());
        Assert.assertEquals(Participant.ENZYME_ROLE_MI, Participant.ENZYME_ROLE_MI);
    }

}