/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dataexchange.cvutils;

/**
 * Configuration for the UpdateCVs task
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UpdateCVsConfig {

    private boolean ignoreObsoletionOfObsolete;

    /**
     * Constructs a new UpdateCVsConfig.
     */
    public UpdateCVsConfig() {
    }

    /**
     * When true, don't let the obsolete term to be obsoleted, ignoring what OBO says
     *
     * @return Value for property 'allowObsoletionOfObsolete'.
     */
    public boolean isIgnoreObsoletionOfObsolete() {
        return ignoreObsoletionOfObsolete;
    }

    /**
     * Setter for property 'allowObsoletionOfObsolete'.
     *
     * @param ignoreObsoletionOfObsolete Value to set for property 'allowObsoletionOfObsolete'.
     */
    public void setIgnoreObsoletionOfObsolete(boolean ignoreObsoletionOfObsolete) {
        this.ignoreObsoletionOfObsolete = ignoreObsoletionOfObsolete;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("[ignoreObsoletionOfObsolete=").append(ignoreObsoletionOfObsolete);
        sb.append(']');
        return sb.toString();
    }
}