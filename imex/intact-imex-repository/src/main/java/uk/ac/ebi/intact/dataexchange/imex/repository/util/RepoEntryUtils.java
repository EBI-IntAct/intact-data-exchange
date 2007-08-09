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
package uk.ac.ebi.intact.dataexchange.imex.repository.util;

import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.dataexchange.imex.repository.RepositoryHelper;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.UnexpectedError;

import java.io.*;

/**
 * Utilities for the repoEntry class
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RepoEntryUtils {

    private static final String NEW_LINE = System.getProperty( "line.separator" );

    public static void invalidateRepoEntry(RepoEntry repoEntry) {
        repoEntry.setImportable(false);
        repoEntry.setValid(false);
    }

    /**
     * Fail an entry. Change its status to failed and write and error message file
     * @param repoEntry the entry failing
     * @param message error message
     * @param throwable cause
     */
    public static void failEntry(RepoEntry repoEntry, String message, Throwable throwable) {
        UnexpectedError unexpectedError = new UnexpectedError(message, throwable);
        repoEntry.addError(unexpectedError);

        failEntry(repoEntry, unexpectedError);
    }

    /**
     * Fail an entry. Change its status to failed and write and error message file
     * @param repoEntry The entry to fail
     * @param errors Causing errors 
     */
    public static void failEntry(RepoEntry repoEntry, UnexpectedError ... errors) {
        invalidateRepoEntry(repoEntry);

        Repository repository = ImexRepositoryContext.getInstance().getRepository();
        RepositoryHelper helper = new RepositoryHelper(repository);

        File errorFile = helper.getEntryErrorFile(repoEntry);
        try {
            Writer writer = new FileWriter(errorFile);

            for (UnexpectedError error : errors) {
                writer.append(errorFormattedMessage(error.getMessage(), error.getStackTrace()));
            }

            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't create error file: "+errorFile, e);
        }
    }

    /**
     * Fail an entry. Change its status to failed and write and error message file
     * @param repoEntry The entry to fail
     */
    public static void failEntry(RepoEntry repoEntry, String message, String detailedDescription) {
        invalidateRepoEntry(repoEntry);

        Repository repository = ImexRepositoryContext.getInstance().getRepository();
        RepositoryHelper helper = new RepositoryHelper(repository);

        File errorFile = helper.getEntryErrorFile(repoEntry);
        try {
            Writer writer = new FileWriter(errorFile);
            writer.append(errorFormattedMessage(message, detailedDescription));
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't create error file: "+errorFile, e);
        }
    }
    
    private static String errorFormattedMessage(String errorMessage, String errorDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append("# =============================").append(NEW_LINE);
        sb.append("# ERROR: "+errorMessage).append(NEW_LINE);
        sb.append("# =============================").append(NEW_LINE);
        sb.append(errorDescription).append(NEW_LINE);

        return sb.toString();
    }

}