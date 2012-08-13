/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.solr;

import org.apache.solr.common.SolrDocumentList;
import org.hupo.psi.mi.psicquic.model.PsicquicSearchResults;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.converter.ConverterException;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Solr result wrapper, which facilitates getting the interactions;
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSolrSearchResult extends PsicquicSearchResults{

    private PsimiTabReader mitabReader;

    public IntactSolrSearchResult(SolrDocumentList results, String [] fieldNames){
        super(results, fieldNames);
        this.mitabReader = new PsimiTabReader();
    }

    public Collection<BinaryInteraction> getBinaryInteractionList() throws ConverterException, IOException {

        return mitabReader.read(getMitab());
    }

    public Iterator<BinaryInteraction> getBinaryInteractionIterator() throws ConverterException, IOException {

        return mitabReader.iterate(getMitab());
    }
}
