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
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.model.Interaction;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.model.ModelledInteraction;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component(value = "intactInteractionEnricher")
@Lazy
public class InteractionEnricher<T extends Interaction> extends AbstractInteractionEnricher<T> {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(InteractionEnricher.class);

    public InteractionEnricher() {
    }

    @Override
    protected String generateAutomaticShortlabel(T objectToEnrich) {
        if (objectToEnrich instanceof InteractionEvidence){
            return IntactUtils.generateAutomaticInteractionEvidenceShortlabelFor((InteractionEvidence)objectToEnrich, IntactUtils.MAX_SHORT_LABEL_LEN);
        }
        else if (objectToEnrich instanceof ModelledInteraction){
            return IntactUtils.generateAutomaticShortlabelForModelledInteraction((ModelledInteraction)objectToEnrich, IntactUtils.MAX_SHORT_LABEL_LEN);
        }
        else{
            return null;
        }
    }


}
