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
package uk.ac.ebi.intact.psimitab;

import org.apache.lucene.store.Directory;
import psidev.psi.mi.search.Searcher;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.xml.converter.ConverterException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Tools to manage PSIMITAB files
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsimitabTools {

    /**
     * Do not instantiate PsimitabTools.
     */
    private PsimitabTools() {
    }

    /**
     * Builds a Lucene index from the PSIMITAB file, delegating the work to the <code>psidev.psi.mi.search.Searcher</code> class
     */
    public static Directory buildIndex(Directory indexDirectory, InputStream psimiTabData, boolean createIndex, boolean hasHeader) throws IOException, ConverterException, MitabLineException {
        return Searcher.buildIndex(indexDirectory, psimiTabData, createIndex, hasHeader);
    }

    /**
     * Builds a Lucene index from the PSIMITAB file, delegating the work to the <code>psidev.psi.mi.search.Searcher</code> class
     */
    public static Directory buildIndex(File indexDirectory, File psimiTabData, boolean createIndex, boolean hasHeader) throws IOException, ConverterException, MitabLineException {
        return Searcher.buildIndex(indexDirectory, psimiTabData, createIndex, hasHeader);
    }

    /**
     * Builds a Lucene index from the PSIMITAB file, delegating the work to the <code>psidev.psi.mi.search.Searcher</code> class
     */
    public static Directory buildIndex(File indexDirectory, InputStream psimiTabData, boolean createIndex, boolean hasHeader) throws IOException, ConverterException, MitabLineException {
        return Searcher.buildIndex(indexDirectory, psimiTabData, createIndex, hasHeader);
    }

    /**
     * Builds a Lucene index from the PSIMITAB file, delegating the work to the <code>psidev.psi.mi.search.Searcher</code> class
     */
    public static Directory buildIndex(String indexDirectory, InputStream psimiTabData, boolean createIndex, boolean hasHeader) throws IOException, ConverterException, MitabLineException {
        return Searcher.buildIndex(indexDirectory, psimiTabData, createIndex, hasHeader);
    }

    /**
     * Builds a Lucene index from the PSIMITAB file, delegating the work to the <code>psidev.psi.mi.search.Searcher</code> class
     */
    public static Directory buildIndex(String indexDirectory, String psimiTabData, boolean createIndex, boolean hasHeader) throws IOException, ConverterException, MitabLineException {
        return Searcher.buildIndex(indexDirectory, psimiTabData, createIndex, hasHeader);
    }

    /**
     * Builds a Lucene index from the PSIMITAB file, delegating the work to the <code>psidev.psi.mi.search.Searcher</code> class
     */
    public static Directory buildIndexInMemory(InputStream psimiTabData, boolean createIndex, boolean hasHeader) throws IOException, ConverterException, MitabLineException {
        return Searcher.buildIndexInMemory(psimiTabData, createIndex, hasHeader);
    }
}