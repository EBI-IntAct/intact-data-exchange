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

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.CvTopic;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiExchange_BasicTest extends IntactBasicTestCase {

    @Before
    public void prepare() throws Exception {
        IntactUnit iu = new IntactUnit();
        iu.createSchema();
    }

    @After
    public void endTest() throws Exception {
        commitTransaction();
    }

    @Test
    public void importIntoIntact_default() throws Exception {
        Institution institution = getMockBuilder().createInstitution("IA:0000", "lalaInstitution");
        institution.getAnnotations().add(getMockBuilder().createAnnotation("nowhere", CvTopic.CONTACT_EMAIL_MI_REF, CvTopic.CONTACT_EMAIL));

        Experiment experiment = new IntactMockBuilder(institution).createExperimentRandom(3);
        IntactEntry entry = new IntactEntry(experiment.getInteractions());
        entry.setInstitution(institution);

        beginTransaction();
        PsiExchange.importIntoIntact(entry, false);
        commitTransaction();
        
        beginTransaction();
        Assert.assertEquals(3, getDaoFactory().getInteractionDao().countAll());
        Assert.assertEquals(1, getDaoFactory().getExperimentDao().countAll());
        commitTransaction();
    }

}