/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.externalservices.searchengine;

import uk.ac.ebi.intact.model.AnnotatedObject;

import java.io.IOException;

/**
 * What an Index Exporter can do.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23-Nov-2006</pre>
 */
public interface IndexExporter<T extends AnnotatedObject> {

    ////////////////////////
    // Export methods

    /**
     * export header of the index.
     *
     * @throws IOException
     */
    public void exportHeader() throws IndexerException;

    /**
     * Export the beginning of the list of entries;
     *
     * @throws IOException
     */
    public void exportEntryListStart() throws IndexerException;

    /**
     * Export the given entry.
     *
     * @param object the entry to export.
     *
     * @throws IOException
     */
    public void exportEntry( T object ) throws IndexerException;

    /**
     * Export all entries.
     *
     * @throws IOException
     */
    public void exportEntries() throws IndexerException;

    /**
     * Export the end of the list of entries of the index.
     *
     * @throws IOException
     */
    public void exportEntryListEnd() throws IndexerException;

    /**
     * Export the footer of the index.
     *
     * @throws IOException
     */
    public void exportFooter() throws IndexerException;

    /**
     * Builds a complete index.
     *
     * @throws IOException
     */
    public void buildIndex() throws IndexerException;

    /////////////////////
    // Counting

    /**
     * Returns the count of entries to be exported.
     *
     * @return
     */
    public int getEntryCount() throws IndexerException;
}