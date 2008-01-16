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

import org.hibernate.validator.Length;

import javax.persistence.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Entity (name = "message")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", length = 3)
@DiscriminatorValue("MSG")
public class Message extends RepoEntity{

    @Column(length = 4096)
    private String text;

    @Lob
    private String stackTrace;

    @ManyToOne
    private RepoEntry repoEntry;

    @Enumerated(EnumType.STRING)
    private MessageLevel level;

    @Column(length = 4096)
    private String context;


    public Message() {
        level = MessageLevel.INFO;
    }

    public Message(String text, MessageLevel level) {
        this(text, level, null);
    }

    public Message(String text, MessageLevel level, Throwable throwable) {
        this.text = text;
        this.level = level;

        if (throwable != null) {
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            throwable.printStackTrace(pw);
            pw.flush();
            writer.flush();
            this.stackTrace = writer.toString();
        }
    }

    ///////////////////////
    // Getters and Setters

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public MessageLevel getLevel() {
        return level;
    }

    public void setLevel(MessageLevel level) {
        this.level = level;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}