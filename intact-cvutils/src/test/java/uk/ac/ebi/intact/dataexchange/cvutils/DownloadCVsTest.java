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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.config.CvPrimer;
import uk.ac.ebi.intact.config.impl.SmallCvPrimer;
import uk.ac.ebi.intact.core.persister.PersisterException;
import uk.ac.ebi.intact.core.persister.standard.CvObjectPersister;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;
import uk.ac.ebi.intact.model.CvBiologicalRole;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.util.CvObjectBuilder;
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
        new IntactUnit().createSchema();

        CvPrimer cvPrimer = new DownloadCvPrimer(getDaoFactory());
        cvPrimer.createCVs();

        beginTransaction();
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
            System.out.println(cv.getShortLabel()+" ("+cv.getObjClass()+") - Xrefs: "+cv.getXrefs());
        }

        commitTransaction();
        
        PSILoader loader = new PSILoader(System.out);
        IntactOntology ontology = loader.parseOboFile(new ByteArrayInputStream(oboOutput.getBytes()));

        Assert.assertEquals(21, ontology.getCvTerms().size());
    }

    private class DownloadCvPrimer extends SmallCvPrimer {

        public DownloadCvPrimer(DaoFactory daoFactory) {
            super(daoFactory);
        }

        @Override
        public void createCVs() {
            // FIXME: creating identity and database CVs won't be necessary when using
            // intact-core > 1.6.1
            beginTransaction();
            
            CvObjectBuilder builder = new CvObjectBuilder();
            getDaoFactory().getCvObjectDao().persist(builder.createIdentityCvXrefQualifier(getIntactContext().getInstitution()));
            getDaoFactory().getCvObjectDao().persist(builder.createPsiMiCvDatabase(getIntactContext().getInstitution()));

            commitTransaction();
            beginTransaction();

            // create the default CVs from the SmallCvPrimer
            super.createCVs();

            commitTransaction();
            beginTransaction();

            // create additional CVs needed by DownloadCVs
            CvObjectPersister cvPersister = CvObjectPersister.getInstance();

            CvObject definition = CvObjectUtils.createCvObject(getIntactContext().getInstitution(), CvTopic.class, "IA:0241", CvTopic.DEFINITION);
            CvExperimentalRole expRole = CvObjectUtils.createCvObject(getIntactContext().getInstitution(), CvExperimentalRole.class,
                                                                      CvExperimentalRole.UNSPECIFIED_PSI_REF, CvExperimentalRole.UNSPECIFIED);
            CvBiologicalRole bioRole = CvObjectUtils.createCvObject(getIntactContext().getInstitution(), CvBiologicalRole.class,
                                                                      CvBiologicalRole.UNSPECIFIED_PSI_REF, CvBiologicalRole.UNSPECIFIED);

            try {
                cvPersister.saveOrUpdate(definition);
                cvPersister.saveOrUpdate(expRole);
                cvPersister.saveOrUpdate(bioRole);
                cvPersister.commit();
            } catch (PersisterException e) {
                e.printStackTrace();
            }

            commitTransaction();

        }
    }
}