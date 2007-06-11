/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.dbutil.cv;

import uk.ac.ebi.intact.dbutil.cv.model.CvTerm;
import uk.ac.ebi.intact.dbutil.cv.model.IntactOntology;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.ook.model.implementation.TermBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Report for the UpdateCVs task
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UpdateCVsReport implements Serializable
{
    private IntactOntology ontology;
    private Collection<CvTerm> obsoleteTerms;
    private Collection<CvTerm> orphanTerms;
    private Collection<CvObject> updatedTerms;
    private Collection<CvObject> createdTerms;
    private Collection<TermBean> invalidTerms;

    public UpdateCVsReport()
    {
        this.orphanTerms = new ArrayList<CvTerm>();
        this.obsoleteTerms = new ArrayList<CvTerm>();
        this.updatedTerms = new ArrayList<CvObject>();
        this.createdTerms = new ArrayList<CvObject>();
        this.invalidTerms = new ArrayList<TermBean>();
    }

    public IntactOntology getOntology()
    {
        return ontology;
    }

    public void setOntology(IntactOntology ontology)
    {
        this.ontology = ontology;
    }

    public Collection<CvTerm> getOrphanTerms()
    {
        return orphanTerms;
    }

    public void setOrphanTerms(Collection<CvTerm> orphanTerms)
    {
        this.orphanTerms = orphanTerms;
    }

    public Collection<CvObject> getUpdatedTerms()
    {
        return updatedTerms;
    }

    public void setUpdatedTerms(Collection<CvObject> updatedTerms)
    {
        this.updatedTerms = updatedTerms;
    }

    public Collection<CvObject> getCreatedTerms()
    {
        return createdTerms;
    }

    public void setCreatedTerms(Collection<CvObject> createdTerms)
    {
        this.createdTerms = createdTerms;
    }

    public boolean addUpdatedTerm(CvObject o)
    {
        return updatedTerms.add(o);
    }

    public boolean addCreatedTerm(CvObject o)
    {
        return createdTerms.add(o);
    }

    public Collection<CvTerm> getObsoleteTerms()
    {
        return obsoleteTerms;
    }

    public void setObsoleteTerms(Collection<CvTerm> obsoleteTerms)
    {
        this.obsoleteTerms = obsoleteTerms;
    }

    public Collection<TermBean> getInvalidTerms()
    {
        return invalidTerms;
    }

    public void setInvalidTerms(Collection<TermBean> invalidTerms)
    {
        this.invalidTerms = invalidTerms;
    }
}
