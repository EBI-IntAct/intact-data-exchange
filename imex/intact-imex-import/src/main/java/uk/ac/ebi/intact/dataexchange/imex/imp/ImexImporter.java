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
import org.joda.time.DateTime;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.dataexchange.imex.repository.RepositoryHelper;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.Provider;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.meta.ImexImport;
import uk.ac.ebi.intact.model.meta.ImexImportActivationType;
import uk.ac.ebi.intact.model.meta.ImexImportPublication;
import uk.ac.ebi.intact.model.meta.ImexImportPublicationStatus;
import uk.ac.ebi.intact.persistence.dao.ImexImportDao;
import uk.ac.ebi.intact.persistence.dao.ImexImportPublicationDao;

import javax.persistence.NoResultException;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, Institution> institutions;

    public ImexImporter(Repository repository) {
        this.repository = repository;

        institutions = new HashMap<String, Institution>();
    }

    public ImexImport importNew() {
        ImexImportDao imexObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getImexImportDao();

        DateTime lastUpdateTime = imexObjectDao.getLatestUpdate(ImexImportActivationType.DATE_BASED);

        if (lastUpdateTime == null) {
            lastUpdateTime = new DateTime(1);
        }

        ImexImport imexImport = new ImexImport(repository.getRepositoryDir().getAbsolutePath(),
                                               ImexImportActivationType.DATE_BASED);

        List<RepoEntry> newRepoEntries = repository.findRepoEntriesModifiedAfter(lastUpdateTime);

        for (RepoEntry repoEntry : newRepoEntries) {
            ImexImportPublication imexImportPublication =
                    new ImexImportPublication(imexImport, repoEntry.getPmid(), getInstitution(repoEntry.getRepoEntrySet().getProvider()), ImexImportPublicationStatus.OK);
            imexImportPublication.setReleaseDate(repoEntry.getReleaseDate());
            importRepoEntry(repoEntry, imexImportPublication);

            imexImport.getImexImportPublications().add(imexImportPublication);

        }

        beginTransaction();
        imexObjectDao.persist(imexImport);
        commitTransaction();

        return imexImport;
    }

    /**
     * Tries to re-import from the Repository those PMIDs that have failed in previous imports
     *
     * @return a report of the import
     */
    public ImexImport importFailed() {
        ImexImportDao imexImportDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getImexImportDao();
        ImexImportPublicationDao imexImportPublicationDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getImexImportPublicationDao();

        List<ImexImportPublication> failedImexImports = imexImportPublicationDao.getFailed();

        if (log.isDebugEnabled()) {
            log.debug("Found " + failedImexImports.size() + " pubmed IDs that have failed previously. Will try to import them.");
        }

        ImexImport imexImport = new ImexImport(repository.getRepositoryDir().getAbsolutePath(),
                                               ImexImportActivationType.ONLY_FAILED);

        for (ImexImportPublication imexImportPublication : failedImexImports) {
            // we evict the entity as we are going to change its pk, to generate another entity from it
            imexImportPublicationDao.evict(imexImportPublication);

            // the new ImexImport object
            imexImportPublication.setImexImport(imexImport);

            String pmid = imexImportPublication.getPmid();

            if (log.isInfoEnabled()) log.info("Importing (previously failed) PMID: " + pmid);

            RepoEntry repoEntry;
            try {
                repoEntry = repository.findRepoEntryByPmid(pmid);
            } catch (NoResultException e) {
                if (log.isErrorEnabled()) log.error("Entry with pmid '" + pmid + "' not found in the Repository");

                imexImportPublication.setStatus(ImexImportPublicationStatus.NOT_FOUND);
                imexImportPublication.setMessage("Publication entry not found in repository");
                imexImport.getImexImportPublications().add(imexImportPublication);

                continue;
            }

            importRepoEntry(repoEntry, imexImportPublication);

            if (imexImportPublication.getStatus() == ImexImportPublicationStatus.OK) {
                imexImportPublication.setMessage("This import had failed previously");
            }

            imexImport.getImexImportPublications().add(imexImportPublication);

        }

        beginTransaction();
        imexImportDao.persist(imexImport);
        commitTransaction();

        return imexImport;
    }

    protected void importRepoEntry(RepoEntry repoEntry, ImexImportPublication imexImportPublication) {
        if (imexImportPublication == null) {
            throw new NullPointerException("An ImexImport instance is needed");
        }

        RepositoryHelper helper = new RepositoryHelper(repository);
        File entryFile = helper.getEntryFile(repoEntry);

        imexImportPublication.setOriginalFilename(entryFile.getName());

        final String pmid = repoEntry.getPmid();

        try {
            PsiExchange.importIntoIntact(new FileInputStream(entryFile));

            imexImportPublication.setStatus(ImexImportPublicationStatus.OK);

        } catch (Throwable e) {
            if (log.isErrorEnabled()) log.error("Entry with pmid '" + pmid + "' failed to be imported", e);
            imexImportPublication.setStatus(ImexImportPublicationStatus.ERROR);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.close();

            imexImportPublication.setMessage(sw.toString());

            if (IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getEntityManager().getTransaction().isActive()) {
                try {
                    IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getEntityManager().getTransaction().rollback();
                } catch (Throwable t) {
                    log.error("Error rollbacking transaction: ", t);
                }
            }

            IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getEntityManager().getTransaction().begin();
        }
    }

    protected Institution getInstitution(Provider provider) {
        final String providerName = provider.getName();

        if (institutions.containsKey(providerName)) {
            return institutions.get(providerName);
        }
        beginTransaction();

        Institution institution = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getInstitutionDao().getByShortLabel(providerName, true);

        commitTransaction();

        if (institution != null) {
            institutions.put(providerName, institution);
        } else {
            beginTransaction();
            List<String> institutionsAvailable = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                    .getInstitutionDao().getShortLabelsLike("%");
            commitTransaction();
            throw new RuntimeException("Institution not found for provider: " + providerName + ". Available: " + institutionsAvailable);
        }

        return institution;
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