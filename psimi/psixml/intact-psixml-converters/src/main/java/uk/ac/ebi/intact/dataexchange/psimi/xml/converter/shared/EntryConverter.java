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
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Interaction;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Entry Converter.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EntryConverter extends AbstractIntactPsiConverter<IntactEntry, Entry> {

    private InstitutionConverter institutionConverter;
    private InteractionConverter interactionConverter;
    private InteractorConverter interactorConverter;
    private ExperimentConverter experimentConverter;
    protected AnnotationConverter annotationConverter;

    public EntryConverter() {
        super(null);
        institutionConverter = new InstitutionConverter();
        experimentConverter = new ExperimentConverter(null);
        interactionConverter = new InteractionConverter(null, experimentConverter);
        interactorConverter = new InteractorConverter(null);
        this.annotationConverter = new AnnotationConverter(null);
    }

    @Deprecated
    public EntryConverter(Institution institution) {
        super(institution);
    }

    public IntactEntry psiToIntact(Entry psiObject) {
        psiStartConversion(psiObject);

        Institution institution = institutionConverter.psiToIntact(psiObject.getSource());

        setInstitution(institution);

        Collection<Interaction> interactions = new ArrayList<Interaction>();

        for (psidev.psi.mi.xml.model.Interaction psiInteraction : psiObject.getInteractions()) {
            Interaction interaction = interactionConverter.psiToIntact(psiInteraction);
            interactions.add(interaction);
        }

        IntactEntry ientry = new IntactEntry(interactions);
        ientry.setInstitution(getInstitution());

        IntactConverterUtils.populateAnnotations(psiObject, ientry, institution, this.annotationConverter);

        if (psiObject.getSource().getReleaseDate() != null) {
            ientry.setReleasedDate(psiObject.getSource().getReleaseDate());
        }

        ConversionCache.clear();

        psiEndConversion(psiObject);
        ConverterContext.getInstance().getLocation().resetPosition();

        failIfInconsistentConversion(ientry, psiObject);

        return ientry;
    }

    public Entry intactToPsi(IntactEntry intactObject) {
        intactStartConversation(intactObject);

        Entry entry = new Entry();

        if( intactObject.getInteractions().size() == 0 ) {
            throw new IllegalArgumentException( "You must give an IntactEntry with at least one interaction." );
        }

        if (intactObject.getInstitution() != null){
            setInstitution(intactObject.getInstitution());
        }
        else {
            Interaction firstInteraction = intactObject.getInteractions().iterator().next();

            setInstitution(firstInteraction.getOwner());
        }

        entry.setSource(institutionConverter.intactToPsi(getInstitution()));

        if (intactObject.getReleasedDate() != null){
            entry.getSource().setReleaseDate(intactObject.getReleasedDate());
        }

        // converts all experiments first
        if (ConverterContext.getInstance().isGenerateCompactXml()){
            for ( uk.ac.ebi.intact.model.Experiment e : intactObject.getExperiments() ) {
                final ExperimentDescription experimentDesc = experimentConverter.intactToPsi( e );
                if (!contains(entry.getExperiments(), experimentDesc)) {
                    entry.getExperiments().add( experimentDesc );
                }
            }
        }

        // converts all interactors first
        if (ConverterContext.getInstance().isGenerateCompactXml()){
            for ( uk.ac.ebi.intact.model.Interactor i : intactObject.getInteractors() ) {
                final psidev.psi.mi.xml.model.Interactor interactor = interactorConverter.intactToPsi( i );
                if ( ! contains( entry.getInteractors(), interactor ) ) {
                    entry.getInteractors().add( interactor );
                }
            }
        }

        for (Interaction intactInteracton : intactObject.getInteractions()) {
            psidev.psi.mi.xml.model.Interaction interaction = interactionConverter.intactToPsi(intactInteracton);
            entry.getInteractions().add(interaction);
        }

        ConversionCache.clear();

        intactEndConversion(intactObject);

        failIfInconsistentConversion(intactObject, entry);

        return entry;

    }

    public boolean contains(Collection<? extends HasId> idElements, HasId hasId) {
        for (HasId idElement : idElements) {
            if (idElement != null && idElement.getId() == hasId.getId()) {
                return true;
            }
        }
        return false;
    }

    protected void failIfInconsistentConversion(IntactEntry intactEntry, Entry psiEntry) {
        failIfInconsistentCollectionSize("interaction", intactEntry.getInteractions(), psiEntry.getInteractions());
        failIfInconsistentCollectionSize("experiment", intactEntry.getExperiments(), PsiConverterUtils.nonRedundantExperimentsFromPsiEntry(psiEntry));
        failIfInconsistentCollectionSize("interactor", intactEntry.getInteractors(), PsiConverterUtils.nonRedundantInteractorsFromPsiEntry(psiEntry));
    }

    @Override
    public void setInstitution(Institution institution)
    {
        super.setInstitution(institution);
        institutionConverter.setInstitution(institution, getInstitutionPrimaryId());
        experimentConverter.setInstitution(institution, getInstitutionPrimaryId());
        interactionConverter.setInstitution(institution, false, true, getInstitutionPrimaryId());
        interactorConverter.setInstitution(institution, getInstitutionPrimaryId());
        this.annotationConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setInstitution(Institution institution, String institId){
        super.setInstitution(institution, institId);
        institutionConverter.setInstitution(institution, getInstitutionPrimaryId());
        experimentConverter.setInstitution(institution, getInstitutionPrimaryId());
        interactionConverter.setInstitution(institution, false, true, getInstitutionPrimaryId());
        interactorConverter.setInstitution(institution, getInstitutionPrimaryId());
        this.annotationConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setCheckInitializedCollections(boolean check){
        super.setCheckInitializedCollections(check);
        this.institutionConverter.setCheckInitializedCollections(check);
        this.experimentConverter.setCheckInitializedCollections(check);
        this.interactionConverter.setCheckInitializedCollections(check, false, true);
        this.interactorConverter.setCheckInitializedCollections(check);
        this.annotationConverter.setCheckInitializedCollections(check);
    }
}