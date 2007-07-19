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
package uk.ac.ebi.intact.dataexchange.psimi.xml.exchange;

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactUnit;

import java.io.InputStream;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Playground {

    public static void main(String[] args) throws Exception {

        IntactUnit iu = new IntactUnit();
        iu.createSchema();

        InputStream is = Playground.class.getResourceAsStream("/xml/dip_2007-02-15.xml");
        PsiExchange.importIntoIntact(is, false);

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        System.out.println("Interactions DB: "+ IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
        .getInteractionDao().countAll());
        System.out.println("Experiments DB: "+IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
        .getExperimentDao().countAll());

        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }

}