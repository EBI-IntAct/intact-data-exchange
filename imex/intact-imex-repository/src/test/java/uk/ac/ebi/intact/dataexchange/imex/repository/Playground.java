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
package uk.ac.ebi.intact.dataexchange.imex.repository;

import org.apache.commons.io.FileUtils;
import uk.ac.ebi.intact.dataexchange.imex.repository.dao.RepoEntryService;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.UnexpectedError;

import java.io.File;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Playground {

    public static void main(String[] args) throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "myRepo/");
        FileUtils.deleteDirectory(tempDir);

        Repository repo = ImexRepositoryContext.openRepository(tempDir.getAbsolutePath());

        //File empty = new File("/homes/baranda/projects/temp/2007-07-05.dip.xml");
        //repo.storeEntrySet(empty, "dip");

        File psiMi = new File(Playground.class.getResource("/xml/mint_2006-07-18.xml").getFile());
        repo.storeEntrySet(psiMi, "mint");


        RepoEntryService repoEntryService = ImexRepositoryContext.getInstance().getImexServiceProvider().getRepoEntryService();
        for (RepoEntry repoEntry : repoEntryService.findAllRepoEntries()) {
            System.out.println(repoEntry.getName()+" - "+repoEntry.getRepoEntrySet().getProvider().getName()+" - "+(repoEntry.isValid()? "OK" : "ERROR")+
            (repoEntry.isImportable()? " - IMPORTABLE" : ""));

            if (!repoEntry.isValid()) {
                for (UnexpectedError error : repoEntry.getErrors()) {
                    System.out.println("\tError: "+error.getMessage());
                }
            }
        }

    }


}