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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterException;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.dataexchange.imex.repository.RepositoryHelper;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.meta.ImexObject;
import uk.ac.ebi.intact.model.meta.ImexObjectStatus;
import uk.ac.ebi.intact.persistence.dao.ImexObjectDao;

import javax.persistence.NoResultException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Imports from the IntAct-IMEx repository into the database
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:ImexImporter.java 9285 2007-08-02 10:33:06Z baranda $
 */
public class ImexImporter {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ImexImporter.class);

    private Repository repository;

    public ImexImporter(Repository repository) {
        this.repository = repository;
    }

    public ImportReport reimportFailed() throws IOException {
        ImexObjectDao imexObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getImexObjectDao();

        beginTransaction();
        List<ImexObject> failedImexObjects = imexObjectDao.getFailed();
        commitTransaction();

        if (log.isDebugEnabled()) {
            log.debug("Found "+failedImexObjects.size()+" pubmed IDs that have failed previously. Will try to import them.");
        }

        ImportReport report = new ImportReport();

        for (ImexObject imexObject : failedImexObjects) {
            String pmid = imexObject.getPmid();

            if (log.isInfoEnabled()) log.info("Importing (previously failed) PMID: "+pmid);

            RepoEntry repoEntry;
            try {
                repoEntry = repository.findRepoEntryByPmid(pmid);
            } catch (NoResultException e) {
                report.getPmidsNotFoundInRepo().add(pmid);
                continue;
            }

            RepositoryHelper helper = new RepositoryHelper(repository);
            File entryFile = helper.getEntryFile(repoEntry);



            try {
                PsiExchange.importIntoIntact(new FileInputStream(entryFile), false);
                report.getSucessfullPmids().add(pmid);

                imexObject.setStatus(ImexObjectStatus.OK);

            } catch (PersisterException e) {
                report.getFailedPmids().put(pmid, e);
                imexObject.setStatus(ImexObjectStatus.ERROR);
            }

            beginTransaction();
            imexObjectDao.merge(imexObject);
            commitTransaction();
        }

        return report;
    }

    private void beginTransaction() {
        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    private void commitTransaction() {
        try {
            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        } catch (IntactTransactionException e) {
            throw new ImportException(e);
        }
    }
}