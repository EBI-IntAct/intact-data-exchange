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

import psidev.psi.mi.tab.formatter.LineFormatter;
import psidev.psi.mi.tab.formatter.TabulatedLineFormatter;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactLineFormatter extends TabulatedLineFormatter {

    public IntactLineFormatter() {
        super();

        final IntActColumnHandler columnHandler = new IntActColumnHandler();
        columnHandler.setGoTermNameAutoCompletion(true);
        columnHandler.setInterproNameAutoCompletion(true);

        setColumnHandler(columnHandler);
        setBinaryInteractionClass( IntActBinaryInteraction.class );
    }
}
