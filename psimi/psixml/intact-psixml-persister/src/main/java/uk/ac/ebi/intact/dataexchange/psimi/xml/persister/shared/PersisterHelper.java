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
package uk.ac.ebi.intact.dataexchange.psimi.xml.persister.shared;

import net.sf.ehcache.Element;
import uk.ac.ebi.intact.config.impl.AbstractHibernateDataConfig;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersisterException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersisterReport;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.util.CacheContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.util.PersisterConfig;
import uk.ac.ebi.intact.model.*;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PersisterHelper {

    private PersisterHelper() {
    }

    public static PersisterReport syncAnnotatedObject(AnnotatedObject intactObject, IntactContext context) throws PersisterException {
        CvPersister cvPersister = new CvPersister(context, PersisterConfig.isDryRun(context));

        if (isAlreadySynced(intactObject, context)) {
            return cvPersister.getReport();
        } else {
            markAsSynched(intactObject, context);
        }

        try {
            for (Xref xref : (Collection<Xref>) intactObject.getXrefs()) {
                CvDatabase cvDb = (CvDatabase) cvPersister.saveOrUpdate(xref.getCvDatabase());
                xref.setCvDatabase(cvDb);

                if (xref.getCvXrefQualifier() != null) {
                    CvXrefQualifier cvXrefQual = (CvXrefQualifier) cvPersister.saveOrUpdate(xref.getCvXrefQualifier());
                    xref.setCvXrefQualifier(cvXrefQual);
                }
            }
            for (Alias alias : (Collection<Alias>) intactObject.getAliases()) {
                CvAliasType cvAliasType = (CvAliasType) cvPersister.saveOrUpdate(alias.getCvAliasType());
                alias.setCvAliasType(cvAliasType);
                alias.setOwner(context.getInstitution());
            }
            for (Annotation annotation : (Collection<Annotation>) intactObject.getAnnotations()) {
                CvTopic cvTopic = (CvTopic) cvPersister.saveOrUpdate(annotation.getCvTopic());
                annotation.setCvTopic(cvTopic);
                annotation.setOwner(context.getInstitution());
            }
        } catch (Throwable t) {
            throw new PersisterException("Exception syncing: "+intactObject.getShortLabel()+" ("+intactObject.getAc()+")", t);
        }

        return cvPersister.getReport();
    }

    public static boolean doesNotContainAc(IntactObject intactObject) {
        return intactObject.getAc() == null;
    }

    public static boolean isTransient(IntactObject intactObject, IntactContext intactContext) {
        AbstractHibernateDataConfig dataConfig = (AbstractHibernateDataConfig) intactContext.getConfig().getDefaultDataConfig();
        return !dataConfig.getSessionFactory().getCurrentSession().contains(intactObject);
    }

    private static boolean isAlreadySynced(IntactObject intactObject, IntactContext intactContext) {
        if (doesNotContainAc(intactObject)) return false;

        return (CacheContext.getInstance(intactContext).getSyncObjectsCache().isKeyInCache(intactObject.getAc()));
    }

    private static void markAsSynched(IntactObject intactObject, IntactContext intactContext) {
        if (doesNotContainAc(intactObject)) return;

        CacheContext.getInstance(intactContext).getSyncObjectsCache().put(new Element(intactObject.getAc(), true));
    }

}