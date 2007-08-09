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
package uk.ac.ebi.intact.dataexchange.imex.repository.model;

import javax.persistence.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class UnexpectedError extends RepoEntity{

    String message;

    @Lob
    String stackTrace;

    @ManyToOne
    RepoEntry repoEntry;

    public UnexpectedError() {
    }

    public UnexpectedError(String message, Throwable throwable) {
        this.message = message;

        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        throwable.printStackTrace(pw);
        pw.flush();
        writer.flush();
        this.stackTrace = writer.toString();
    }

    ///////////////////////
    // Getters and Setters

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RepoEntry getRepoEntry() {
        return repoEntry;
    }

    public void setRepoEntry(RepoEntry repoEntry) {
        this.repoEntry = repoEntry;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
}