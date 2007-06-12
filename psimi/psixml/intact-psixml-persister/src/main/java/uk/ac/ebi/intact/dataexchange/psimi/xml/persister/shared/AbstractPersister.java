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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.Persister;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersisterException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersisterReport;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.ReportedIntactObject;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.service.AbstractService;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.util.PersisterConfig;
import uk.ac.ebi.intact.model.AnnotatedObject;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractPersister<T extends AnnotatedObject> implements Persister<T> {

    private static final Log log = LogFactory.getLog(AbstractPersister.class);

    private IntactContext intactContext;

    public AbstractPersister(IntactContext intactContext, boolean dryRun) {
        this.intactContext = intactContext;

        PersisterConfig.setDryRun(intactContext, dryRun);

        intactContext.getConfig().getDefaultDataConfig().setAutoFlush(false);
    }

    protected IntactContext getIntactContext() {
        return intactContext;
    }

    protected void persist(T intactObject, PersisterReport report) throws PersisterException {
        if (!PersisterConfig.isDryRun(getIntactContext())) {
            getService().persist(intactObject);
        }
        report.addCreated(new ReportedIntactObject(intactObject));
    }

    protected boolean isDryRun() {
        return PersisterConfig.isDryRun(getIntactContext());
    }

    protected abstract AbstractService getService();
}