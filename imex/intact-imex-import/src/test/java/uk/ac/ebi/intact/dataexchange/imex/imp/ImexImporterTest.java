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
import org.joda.time.DateTime;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.meta.ImexImport;
import uk.ac.ebi.intact.model.meta.ImexImportPublication;
import uk.ac.ebi.intact.model.meta.ImexImportPublicationStatus;

import java.io.File;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImexImporterTest extends IntactBasicTestCase {

    private Repository repository;

    @Before
    public void before() throws Exception {
        new IntactUnit().createSchema();

        repository = createRepositoryWithMint();
    }

    @After
    public void after() throws Exception {
        repository.close();
    }

    @Test
    public void importNew() throws Exception {
        Institution mint = new Institution("mint");

        ImexImport imexImport = new ImexImport();
        imexImport.setImportDate(new DateTime().minusDays(1));

        imexImport.getImexImportPublications().add(
                new ImexImportPublication(imexImport, "0", mint, ImexImportPublicationStatus.OK));
        imexImport.getImexImportPublications().add(
                new ImexImportPublication(imexImport, "1", mint, ImexImportPublicationStatus.ERROR));
        persistImexImport(imexImport);

        ImexImporter imexImporter = new ImexImporter(repository);

        ImexImport imexImportLoaded = imexImporter.importNew();

        Assert.assertEquals(3, imexImportLoaded.getCountTotal());
        Assert.assertEquals(0, imexImportLoaded.getCountFailed());
    }

    @Test
    public void importFailed() throws Exception {
        Institution mint = new Institution("mint");

        ImexImport imexImport = new ImexImport();
        imexImport.getImexImportPublications().add(
                new ImexImportPublication(imexImport, "15733859", mint, ImexImportPublicationStatus.OK));
        imexImport.getImexImportPublications().add(
                new ImexImportPublication(imexImport, "15733864", mint, ImexImportPublicationStatus.ERROR));
        imexImport.getImexImportPublications().add(
                new ImexImportPublication(imexImport, "15757671", mint, ImexImportPublicationStatus.ERROR));
        persistImexImport(imexImport);

        Assert.assertEquals(ImexImportPublicationStatus.OK, getDaoFactory().getImexImportPublicationDao().getByPmid("15733859").get(0).getStatus());
        Assert.assertEquals(ImexImportPublicationStatus.ERROR, getDaoFactory().getImexImportPublicationDao().getByPmid("15733864").get(0).getStatus());
        Assert.assertEquals(ImexImportPublicationStatus.ERROR, getDaoFactory().getImexImportPublicationDao().getByPmid("15757671").get(0).getStatus());

        ImexImporter imexImporter = new ImexImporter(repository);

        // first import - should import the two failed
        ImexImport imexImportLoaded = imexImporter.importFailed();

        Assert.assertEquals(2, imexImportLoaded.getCountTotal());
        Assert.assertEquals(0, imexImportLoaded.getCountFailed());

        // a second import of failed - should not import anything
        ImexImport imexImportLoaded2 = imexImporter.importFailed();

        Assert.assertEquals(0, imexImportLoaded2.getCountTotal());
        Assert.assertEquals(0, imexImportLoaded2.getCountFailed());

        int totalCount = 0;
        int totalFailed = 0;

        for (ImexImportPublication imexImportPublication : getDaoFactory().getImexImportPublicationDao().getAll()) {
            totalCount++;

            if (imexImportPublication.getStatus() == ImexImportPublicationStatus.ERROR) {
                totalFailed++;
            }
        }

        Assert.assertEquals(5, totalCount);
        Assert.assertEquals(2, totalFailed);
    }

    @Test
    public void importFailed_notFound() throws Exception {
        Institution mint = new Institution("mint");

        ImexImport imexImport = new ImexImport();
        imexImport.getImexImportPublications().add(
                new ImexImportPublication(imexImport, "0", mint, ImexImportPublicationStatus.ERROR));
        persistImexImport(imexImport);

        ImexImporter imexImporter = new ImexImporter(repository);

        ImexImport imexImportLoaded = imexImporter.importFailed();

        Assert.assertEquals(1, imexImportLoaded.getCountTotal());
        Assert.assertEquals(0, imexImportLoaded.getCountFailed());
        Assert.assertEquals(1, imexImportLoaded.getCountNotFound());
    }

    /**
     * This repository contains three pmids:
     * 15733859 - mint - OK - IMPORTABLE
     * 15733864 - mint - OK - IMPORTABLE
     * 15757671 - mint - OK - IMPORTABLE
     */
    private static Repository createRepositoryWithMint() throws Exception{
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "imex-test-repo-"+System.currentTimeMillis());
        FileUtils.deleteDirectory(tempDir);

        Repository repo = ImexRepositoryContext.openRepository(tempDir.getAbsolutePath());

        File psiMi = new File(ImexImporterTest.class.getResource("/xml/mint_2006-07-18.xml").getFile());
        repo.storeEntrySet(psiMi, "mint");

        return repo;
    }

    private void persistImexImport(ImexImport imexImport) {
        for (ImexImportPublication iip : imexImport.getImexImportPublications()) {
            getDaoFactory().getInstitutionDao().saveOrUpdate(iip.getProvider());
        }
        beginTransaction();
        getDaoFactory().getImexImportDao().persist(imexImport);
        commitTransaction();
    }
}