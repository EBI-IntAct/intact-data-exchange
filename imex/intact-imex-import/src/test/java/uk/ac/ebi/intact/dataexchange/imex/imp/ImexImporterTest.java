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
package uk.ac.ebi.intact.dataexchange.imex.imp;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.model.meta.ImexObject;
import uk.ac.ebi.intact.model.meta.ImexObjectStatus;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImexImporterTest extends IntactBasicTestCase {

    @Before
    public void before() throws Exception {
        new IntactUnit().createSchema();
        beginTransaction();
    }

    @After
    public void after() throws Exception {
        commitTransaction();
    }

    @Test
    public void reimportFailed() throws Exception {
        persistImexObject("15733859", ImexObjectStatus.OK);
        persistImexObject("15733864", ImexObjectStatus.ERROR);
        persistImexObject("15757671", ImexObjectStatus.ERROR);

        commitTransaction();

        beginTransaction();
        Assert.assertEquals(ImexObjectStatus.OK, getDaoFactory().getImexObjectDao().getByPmid("15733859").getStatus());
        Assert.assertEquals(ImexObjectStatus.ERROR, getDaoFactory().getImexObjectDao().getByPmid("15733864").getStatus());
        Assert.assertEquals(ImexObjectStatus.ERROR, getDaoFactory().getImexObjectDao().getByPmid("15757671").getStatus());
        commitTransaction();

        Repository repo = createRepositoryWithMint();

        ImexImporter imexImporter = new ImexImporter(repo);
        ImportReport report = imexImporter.reimportFailed();

        System.out.println(report);

        for (Map.Entry<String,Throwable> entry : report.getFailedPmids().entrySet()) {
            System.out.println(entry.getKey()+":");
            entry.getValue().printStackTrace();
        }

        Assert.assertEquals(2, report.getSucessfullPmids().size());
        Assert.assertEquals(0, report.getFailedPmids().size());
        Assert.assertEquals(0, report.getPmidsNotFoundInRepo().size());

        beginTransaction();

        for (ImexObject imexObject : (List<ImexObject>)getDaoFactory().getImexObjectDao().getAll()) {

            Assert.assertEquals(ImexObjectStatus.OK,imexObject.getStatus());
        }

        commitTransaction();
    }

    @Test
    public void reimportFailed_notFound() throws Exception {
        persistImexObject("0", ImexObjectStatus.ERROR);

        commitTransaction();

        Repository repo = createRepositoryWithMint();

        ImexImporter imexImporter = new ImexImporter(repo);
        ImportReport report = imexImporter.reimportFailed();

        Assert.assertEquals(0, report.getSucessfullPmids().size());
        Assert.assertEquals(0, report.getFailedPmids().size());
        Assert.assertEquals(1, report.getPmidsNotFoundInRepo().size());

        beginTransaction();
        Assert.assertEquals(ImexObjectStatus.ERROR, getDaoFactory().getImexObjectDao().getByPmid("0").getStatus());
        commitTransaction();
    }

    /**
     * This repository contains three pmids:
     * 15733859 - mint - OK - IMPORTABLE
     * 15733864 - mint - OK - IMPORTABLE
     * 15757671 - mint - OK - IMPORTABLE
     */
    private static Repository createRepositoryWithMint() throws Exception{
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "imex-test-repo/");
        FileUtils.deleteDirectory(tempDir);

        Repository repo = ImexRepositoryContext.openRepository(tempDir.getAbsolutePath());

        File psiMi = new File(ImexImporterTest.class.getResource("/xml/mint_2006-07-18.xml").getFile());
        repo.storeEntrySet(psiMi, "mint");

        return repo;
    }

    private void persistImexObject(String pmid, ImexObjectStatus status) {
        getDaoFactory().getImexObjectDao().persist(
                new ImexObject(getIntactContext().getInstitution(), pmid, status));
    }
}