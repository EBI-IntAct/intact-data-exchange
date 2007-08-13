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
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.model.CvBiologicalRole;
import uk.ac.ebi.intact.model.CvIdentification;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvObjectEnricherTest {

    @Test
    public void enrich_noShortLabel() throws Exception {
        IntactMockBuilder mockBuilder = new IntactMockBuilder();

        CvBiologicalRole bioRole = mockBuilder.createCvObject(CvBiologicalRole.class, CvBiologicalRole.UNSPECIFIED_PSI_REF, null);
        bioRole.setFullName("unspecified role");

        Assert.assertNull(bioRole.getShortLabel());

        CvObjectEnricher cvObjectEnricher = CvObjectEnricher.getInstance();
        cvObjectEnricher.enrich(bioRole);

        Assert.assertNotNull(bioRole.getShortLabel());
        Assert.assertEquals(CvBiologicalRole.UNSPECIFIED, bioRole.getShortLabel());
    }

    @Test
    public void enrich_noShortLabel_cropped() throws Exception {
        IntactMockBuilder mockBuilder = new IntactMockBuilder();

        CvIdentification cvIdentification = mockBuilder.createCvObject(CvIdentification.class, CvIdentification.PREDETERMINED_MI_REF, null);
        cvIdentification.setFullName("predetermined participant");

        Assert.assertNull(cvIdentification.getShortLabel());

        CvObjectEnricher cvObjectEnricher = CvObjectEnricher.getInstance();
        cvObjectEnricher.enrich(cvIdentification);

        Assert.assertNotNull(cvIdentification.getShortLabel());
        Assert.assertEquals(CvIdentification.PREDETERMINED, cvIdentification.getShortLabel());
    }

    @Test
    public void enrich_noXrefs() throws Exception {
        IntactMockBuilder mockBuilder = new IntactMockBuilder();

        CvBiologicalRole cvBiologicalRole = mockBuilder.createCvObject(CvBiologicalRole.class, CvBiologicalRole.ENZYME_PSI_REF, CvBiologicalRole.ENZYME);
        cvBiologicalRole.getXrefs().clear();

        Assert.assertNull(CvObjectUtils.getPsiMiIdentityXref(cvBiologicalRole));

        CvObjectEnricher cvObjectEnricher = CvObjectEnricher.getInstance();
        cvObjectEnricher.enrich(cvBiologicalRole);

        Assert.assertNotNull(cvBiologicalRole.getShortLabel());
        Assert.assertNotNull(cvBiologicalRole.getFullName());
        Assert.assertEquals(CvBiologicalRole.ENZYME, cvBiologicalRole.getShortLabel());
        Assert.assertEquals(CvBiologicalRole.ENZYME_PSI_REF, CvObjectUtils.getPsiMiIdentityXref(cvBiologicalRole).getPrimaryId());
    }

}