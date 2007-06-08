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
package uk.ac.ebi.intact.dataexchange.psimi.xml.persister.util;

import uk.ac.ebi.intact.context.IntactContext;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PersisterConfig {

    private static final String DRY_RUN_ATT = PersisterConfig.class + ".DRY_RUN";

    private PersisterConfig() {
    }

    public static boolean isDryRun(IntactContext context) {
        return (Boolean) context.getSession().getAttribute(DRY_RUN_ATT);
    }

    public static void setDryRun(IntactContext context, boolean isDryRun) {
        context.getSession().setAttribute(DRY_RUN_ATT, isDryRun);
    }
}