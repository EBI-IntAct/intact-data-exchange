/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.psimitab.search;

import psidev.psi.mi.search.index.impl.InteractorIndexWriter;
import psidev.psi.mi.search.engine.SearchEngine;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.utils.BinaryInteractionHandler;
import psidev.psi.mi.tab.utils.OnlyOneInteractorHandler;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.util.IntactOnlyOneInteractorHandler;
import uk.ac.ebi.intact.psimitab.util.IntactBinaryInteractionHandler;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import org.apache.lucene.store.Directory;

import java.io.IOException;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactInteractorIndexWriter extends InteractorIndexWriter {

    public IntactInteractorIndexWriter() {
        super(new IntactDocumentBuilder(), new IntactOnlyOneInteractorHandler());
    }

    @Override
    protected BinaryInteraction mergeBinaryInteractions( BinaryInteraction source, BinaryInteraction target ) {
        IntactBinaryInteractionHandler handler = new IntactOnlyOneInteractorHandler();
        return handler.merge((IntactBinaryInteraction) source, (IntactBinaryInteraction) target);
    }

    @Override
    protected SearchEngine createSearchEngine(Directory directory) throws IOException {
        return new IntactSearchEngine(directory);
    }

}
