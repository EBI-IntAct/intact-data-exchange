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
package uk.ac.ebi.intact.dataexchange.psimi.xml.persister.standard;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.AbstractPersister;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersisterException;
import uk.ac.ebi.intact.model.*;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractAnnotatedObjectPersister<T extends AnnotatedObject> extends AbstractPersister<T> {

    protected AbstractAnnotatedObjectPersister(IntactContext intactContext) {
        super(intactContext);
    }

    @Override
    protected void saveOrUpdateAttributes(T intactObject) throws PersisterException {
        CvObjectPersister cvPersister = CvObjectPersister.getInstance();

        for (Xref xref : (Collection<Xref>) intactObject.getXrefs()) {
            cvPersister.saveOrUpdate(xref.getCvDatabase());

            if (xref.getCvXrefQualifier() != null) {
                cvPersister.saveOrUpdate(xref.getCvXrefQualifier());
            }
        }
        for (Alias alias : (Collection<Alias>) intactObject.getAliases()) {
            cvPersister.saveOrUpdate(alias.getCvAliasType());
        }
        for (Annotation annotation : (Collection<Annotation>) intactObject.getAnnotations()) {
            cvPersister.saveOrUpdate(annotation.getCvTopic());
        }
    }

    @Override
    protected T syncAttributes(T intactObject) {
        CvObjectPersister cvPersister = CvObjectPersister.getInstance();

        try {
            for (Xref xref : (Collection<Xref>) intactObject.getXrefs()) {
                CvDatabase cvDb = (CvDatabase) cvPersister.syncIfTransient(xref.getCvDatabase());
                xref.setCvDatabase(cvDb);

                if (xref.getCvXrefQualifier() != null) {
                    CvXrefQualifier cvXrefQual = (CvXrefQualifier) cvPersister.syncIfTransient(xref.getCvXrefQualifier());
                    xref.setCvXrefQualifier(cvXrefQual);
                }
            }
            for (Alias alias : (Collection<Alias>) intactObject.getAliases()) {
                CvAliasType cvAliasType = (CvAliasType) cvPersister.syncIfTransient(alias.getCvAliasType());
                alias.setCvAliasType(cvAliasType);
                alias.setOwner(getIntactContext().getInstitution());
            }
            for (Annotation annotation : (Collection<Annotation>) intactObject.getAnnotations()) {
                CvTopic cvTopic = (CvTopic) cvPersister.syncIfTransient(annotation.getCvTopic());
                annotation.setCvTopic(cvTopic);
                annotation.setOwner(getIntactContext().getInstitution());
            }
        } catch (Throwable t) {
            throw new IntactException("Exception syncing: "+intactObject.getShortLabel()+" ("+intactObject.getAc()+")", t);
        }

        return intactObject;
    }

    @Override
    protected boolean syncedAndCandidateAreEqual(T synced, T candidate) {
        // return true by default
        return true;
    }

    @Override
    protected boolean update(T objectToUpdate, T existingObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected T syncIfTransient(T intactObject) {
        return super.syncIfTransient(intactObject);
    }
}