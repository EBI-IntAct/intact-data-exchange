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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.ExperimentDescription;
import psidev.psi.mi.xml.model.HasId;
import psidev.psi.mi.xml.model.Participant;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Interaction;

import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EntryConverter extends AbstractIntactPsiConverter<IntactEntry, Entry> {

    public EntryConverter(Institution institution) {
        super(institution);
    }

    public IntactEntry psiToIntact(Entry psiObject) {

        Collection<Interaction> interactions = new ArrayList<Interaction>();

        InteractionConverter interactionConverter = new InteractionConverter(getInstitution());

        for (psidev.psi.mi.xml.model.Interaction psiInteraction : psiObject.getInteractions()) {
            Interaction interaction = interactionConverter.psiToIntact(psiInteraction);
            interactions.add(interaction);
        }

        IntactEntry ientry = new IntactEntry(interactions);

        ConversionCache.clear();

        return ientry;
    }

    public Entry intactToPsi(IntactEntry intactObject) {
        Entry entry = new Entry();

        InteractionConverter interactionConverter = new InteractionConverter(getInstitution());

        for (Interaction intactInteracton : intactObject.getInteractions()) {
            psidev.psi.mi.xml.model.Interaction interaction = interactionConverter.intactToPsi(intactInteracton);
            entry.getInteractions().add(interaction);

            for (Participant participant : interaction.getParticipants()) {
                if (!contains(entry.getInteractors(), participant.getInteractor())) {
                    entry.getInteractors().add(participant.getInteractor());
                }
            }

            for (ExperimentDescription experimentDesc : interaction.getExperiments()) {
                if (!contains(entry.getExperiments(), experimentDesc)) {
                    entry.getExperiments().add(experimentDesc);
                }
            }

        }

        ConversionCache.clear();

        return entry;

    }

    public boolean contains(Collection<? extends HasId> idElements, HasId hasId) {
        for (HasId idElement : idElements) {
            if (idElement.getId() == hasId.getId()) {
                return true;
            }
        }
        return false;
    }

}