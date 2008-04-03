/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import uk.ac.ebi.intact.config.impl.SmallCvPrimer;
import uk.ac.ebi.intact.context.DataContext;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.plugins.dbupdate.targetspecies.UpdateTargetSpecies;

import java.io.File;
import java.util.Iterator;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class DataConversionAbstractTest extends IntactBasicTestCase {


    @After
    public void tearDown() throws Exception {
    }

    @BeforeClass
    public static void prepare() throws Exception {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        dataContext.beginTransaction();
        DataConversionCvPrimer primer = new DataConversionCvPrimer(dataContext.getDaoFactory());
        primer.createCVs();
        dataContext.commitTransaction();

        dataContext.beginTransaction();
        IntactMockBuilder mockBuilder = new IntactMockBuilder();

        Experiment exp1 = mockBuilder.createDeterministicExperiment();
        exp1.setShortLabel("mahajan-2000-1");
        exp1.addInteraction(mockBuilder.createInteractionRandomBinary());

        PersisterHelper.saveOrUpdate(exp1);

        dataContext.commitTransaction();
        dataContext.beginTransaction();

        Experiment exp2 = mockBuilder.createExperimentEmpty("ni-1998-1", "1234");
        exp2.addInteraction(mockBuilder.createInteractionRandomBinary());
        Iterator<Component> compIter = exp2.getInteractions().iterator().next().getComponents().iterator();
        Component c1 = compIter.next();
        Component c2 = compIter.next();

        c1.setExpressedIn(mockBuilder.createBioSource(9606, "human"));
        c2.setExpressedIn(mockBuilder.createBioSource(-1, "in vitro"));

        PersisterHelper.saveOrUpdate(exp2);

        dataContext.commitTransaction();

        // update target species
        new UpdateTargetSpecies().updateAllExperiments();

        File file = new File("reverseMapping.txt.ser");
        if (file.exists()) file.delete();
    }

    @AfterClass
    public void theAftermath() {
        IntactContext.closeCurrentInstance();
    }

    private static class DataConversionCvPrimer extends SmallCvPrimer {

        public DataConversionCvPrimer(DaoFactory daoFactory) {
            super(daoFactory);
        }

        @Override
        public void createCVs() {
            super.createCVs();

            getCvObject(CvXrefQualifier.class, CvXrefQualifier.TARGET_SPECIES);
        }
    }
}
