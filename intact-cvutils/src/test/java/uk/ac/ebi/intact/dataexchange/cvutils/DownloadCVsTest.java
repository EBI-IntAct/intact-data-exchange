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
package uk.ac.ebi.intact.dataexchange.cvutils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.config.CvPrimer;
import uk.ac.ebi.intact.config.impl.SmallCvPrimer;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterException;
import uk.ac.ebi.intact.core.persister.standard.CvObjectPersister;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DownloadCVsTest extends IntactBasicTestCase {

    @Before
    public void prepare() throws Exception {
        IntactContext.getCurrentInstance().getConfig().setReadOnlyApp(false);

        new IntactUnit().createSchema();

        CvPrimer cvPrimer = new DownloadCvPrimer(getDaoFactory());
        cvPrimer.createCVs();

        beginTransaction();
    }

    @After
    public void after() throws Exception {
        commitTransaction();
    }

    @Test
    public void download_default() throws Exception {

        StringWriter writer = new StringWriter();
        BufferedWriter bufWriter = new BufferedWriter(writer);

        DownloadCVs downloadCVs = new DownloadCVs();
        downloadCVs.download(bufWriter);

        bufWriter.flush();

        String oboOutput = writer.toString();

        commitTransaction();

        beginTransaction();

        Assert.assertEquals(21, getDaoFactory().getCvObjectDao().countAll());

        for (CvObject cv : getDaoFactory().getCvObjectDao().getAll()) {
            Assert.assertFalse(cv.getXrefs().isEmpty());
        }

        commitTransaction();
        
        PSILoader loader = new PSILoader(System.out);
        IntactOntology ontology = loader.parseOboFile(new ByteArrayInputStream(oboOutput.getBytes()));

        Assert.assertEquals(21, ontology.getCvTerms().size());
    }

    @Test
    public void download_unexpectedCV() throws Exception {
        CvDatabase wrongDb = getMockBuilder().createCvObject(CvDatabase.class, "no", "uniprot");
        wrongDb.getXrefs().clear();

        beginTransaction();
        CvObjectPersister.getInstance().saveOrUpdate(wrongDb);
        CvObjectPersister.getInstance().commit();
        commitTransaction();
        
        // --- testing

        StringWriter writer = new StringWriter();
        BufferedWriter bufWriter = new BufferedWriter(writer);

        beginTransaction();

        DownloadCVs downloadCVs = new DownloadCVs();
        downloadCVs.download(bufWriter);

        bufWriter.flush();

        String oboOutput = writer.toString();

        commitTransaction();

        beginTransaction();

        Assert.assertEquals(22, getDaoFactory().getCvObjectDao().countAll());

        commitTransaction();

        PSILoader loader = new PSILoader(System.out);
        IntactOntology ontology = loader.parseOboFile(new ByteArrayInputStream(oboOutput.getBytes()));

        Assert.assertEquals(22, ontology.getCvTerms().size());
    }

    @Test (expected = IntactException.class)
    public void download_readOnly_someCvsNotExisting() throws Exception {
        IntactContext.getCurrentInstance().getConfig().setReadOnlyApp(true);

        StringWriter writer = new StringWriter();
        BufferedWriter bufWriter = new BufferedWriter(writer);

        beginTransaction();

        DownloadCVs downloadCVs = new DownloadCVs();
        downloadCVs.download(bufWriter);
    }
    
    @Test (expected = IntactException.class)
    public void download_readOnly_someCvsWithoutXrefs() throws Exception {
        CvDatabase wrongDb = getMockBuilder().createCvObject(CvDatabase.class, "no", "uniprot");
        wrongDb.getXrefs().clear();

        beginTransaction();
        CvObjectPersister.getInstance().saveOrUpdate(wrongDb);
        CvObjectPersister.getInstance().commit();
        commitTransaction();

        // --- testing

        IntactContext.getCurrentInstance().getConfig().setReadOnlyApp(true);

        StringWriter writer = new StringWriter();
        BufferedWriter bufWriter = new BufferedWriter(writer);

        beginTransaction();

        DownloadCVs downloadCVs = new DownloadCVs();
        downloadCVs.download(bufWriter);
    }


    private class DownloadCvPrimer extends SmallCvPrimer {

        public DownloadCvPrimer(DaoFactory daoFactory) {
            super(daoFactory);
        }

        @Override
        public void createCVs() {
            // this first block can be removed when using intact-core > 1.6.3-SNAPSHOT
            beginTransaction();
            try {
                CvObjectPersister.getInstance().saveOrUpdate(getMockBuilder().createCvObject(CvXrefQualifier.class, CvXrefQualifier.GO_DEFINITION_REF_MI_REF, CvXrefQualifier.GO_DEFINITION_REF));
                CvObjectPersister.getInstance().commit();
            } catch (PersisterException e) {
                e.printStackTrace();
            }
            commitTransaction();
            // end of possible remove
            
            beginTransaction();

            // create the default CVs from the SmallCvPrimer
            super.createCVs();

            commitTransaction();
            beginTransaction();

            // create additional CVs needed by DownloadCVs
            CvObjectPersister cvPersister = CvObjectPersister.getInstance();

            CvObject definition = CvObjectUtils.createCvObject(getIntactContext().getInstitution(), CvTopic.class, "IA:0241", CvTopic.DEFINITION);
            CvExperimentalRole expRoleUnspecified = CvObjectUtils.createCvObject(getIntactContext().getInstitution(), CvExperimentalRole.class,
                                                                      CvExperimentalRole.UNSPECIFIED_PSI_REF, CvExperimentalRole.UNSPECIFIED);
            CvBiologicalRole bioRoleUnspecified = CvObjectUtils.createCvObject(getIntactContext().getInstitution(), CvBiologicalRole.class,
                                                                      CvBiologicalRole.UNSPECIFIED_PSI_REF, CvBiologicalRole.UNSPECIFIED);

            try {
                cvPersister.saveOrUpdate(definition);
                cvPersister.saveOrUpdate(expRoleUnspecified);
                cvPersister.saveOrUpdate(bioRoleUnspecified);
                cvPersister.commit();
            } catch (PersisterException e) {
                e.printStackTrace();
            }

            commitTransaction();



        }
    }
}