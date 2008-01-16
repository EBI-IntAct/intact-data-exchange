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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.UnsupportedConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.MessageLevel;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentConverter extends AbstractAnnotatedObjectConverter<Experiment, ExperimentDescription> {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ExperimentConverter.class);

    public ExperimentConverter(Institution institution) {
        super(institution, Experiment.class, ExperimentDescription.class);
    }

    public Experiment psiToIntact(ExperimentDescription psiObject) {
        Experiment experiment = super.psiToIntact(psiObject);

        if (!isNewIntactObjectCreated()) {
            return experiment;
        }

        String shortLabel;

        if (psiObject.getNames() != null) {
           shortLabel = IntactConverterUtils.getShortLabelFromNames(psiObject.getNames());
        } else {
           shortLabel = IntactConverterUtils.createExperimentTempShortLabel(); 
        }

        Organism hostOrganism = psiObject.getHostOrganisms().iterator().next();
        BioSource bioSource = new OrganismConverter(experiment.getOwner()).psiToIntact(hostOrganism);

        InteractionDetectionMethod idm = psiObject.getInteractionDetectionMethod();
        CvInteraction cvInteractionDetectionMethod = new InteractionDetectionMethodConverter(getInstitution()).psiToIntact(idm);

        experiment.setOwner(getInstitution());
        experiment.setShortLabel(shortLabel);
        experiment.setBioSource(bioSource);

        IntactConverterUtils.populateNames(psiObject.getNames(), experiment);
        IntactConverterUtils.populateXref(psiObject.getXref(), experiment, new XrefConverter<ExperimentXref>(getInstitution(), ExperimentXref.class));

        // fail if the primary reference does not point to Pubmed and primary-reference
        final DbReference primaryRef = psiObject.getBibref().getXref().getPrimaryRef();
        if (!CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(primaryRef.getRefTypeAc()) ||
            !CvDatabase.PUBMED_MI_REF.equals(primaryRef.getDbAc())) {
            final String message = "Bibref in ExperimentDescription [PSI Id=" + psiObject.getId() + "] " +
                                   "should be a primary-reference (refTypeAc=" + CvXrefQualifier.PRIMARY_REFERENCE_MI_REF + ") " +
                                   "with points to Pubmed (dbAc=" + CvDatabase.PUBMED_MI_REF + "): " + primaryRef;
            log.error(message);

            addMessageToContext(MessageLevel.WARN, message+". Fixed.", true);
        }

        IntactConverterUtils.populateXref(psiObject.getBibref().getXref(), experiment, new XrefConverter<ExperimentXref>(getInstitution(), ExperimentXref.class));
        IntactConverterUtils.populateAnnotations(psiObject, experiment, getInstitution());
        
        experiment.setCvInteraction(cvInteractionDetectionMethod);

        ParticipantIdentificationMethod pim = psiObject.getParticipantIdentificationMethod();
        if (pim != null) {
            CvIdentification cvParticipantIdentification = new ParticipantIdentificationMethodConverter(getInstitution()).psiToIntact(pim);
            experiment.setCvIdentification(cvParticipantIdentification);
        }

        Publication publication = createPublication(psiObject);
        experiment.setPublication(publication);

        return experiment;
    }

    public ExperimentDescription intactToPsi(Experiment intactObject) {
        ExperimentDescription expDesc = super.intactToPsi(intactObject);

        if (!isNewPsiObjectCreated()) {
            return expDesc;
        }

        Bibref bibref = new Bibref();

        try {
            PsiConverterUtils.populate(intactObject, bibref);
        } catch (UnsupportedConversionException e) {
            throw new UnsupportedConversionException("No Bibref could be found for Experiment with Xrefs: "+intactObject.getXrefs(), e);
        }

        InteractionDetectionMethodConverter detMethodConverter = new InteractionDetectionMethodConverter(getInstitution());
        InteractionDetectionMethod detMethod = (InteractionDetectionMethod) PsiConverterUtils.toCvType(intactObject.getCvInteraction(), detMethodConverter);

        expDesc.setBibref(bibref);
        expDesc.setInteractionDetectionMethod(detMethod);

        PsiConverterUtils.populate(intactObject, expDesc);

        if (intactObject.getCvIdentification() != null) {
            ParticipantIdentificationMethod identMethod = (ParticipantIdentificationMethod)
                    PsiConverterUtils.toCvType(intactObject.getCvIdentification(), new ParticipantIdentificationMethodConverter(getInstitution()));
            expDesc.setParticipantIdentificationMethod(identMethod);
        }

        Organism organism = new OrganismConverter(getInstitution()).intactToPsi(intactObject.getBioSource());
        expDesc.getHostOrganisms().add(organism);

        return expDesc;
    }

    private Publication createPublication(ExperimentDescription experiment) {
        final DbReference primaryRef = experiment.getBibref().getXref().getPrimaryRef();

        if (!CvDatabase.PUBMED_MI_REF.equals(primaryRef.getDbAc())) {
            throw new UnsupportedConversionException("The bibref (bibliographic xref) of the experiment is not pointing to Pubmed " +
                                                     "(dbAc="+ CvDatabase.PUBMED_MI_REF+"). Experiment PSI Id: "+experiment.getId()+", ref found: " +
                                                     "db="+primaryRef.getDb()+", dbAc="+primaryRef.getDbAc()+", id="+primaryRef.getId());
        }

        String pubId = primaryRef.getId();

        Publication publication = (Publication) ConversionCache.getElement("pub:"+pubId);

        if (publication != null) {
            return publication;
        }

        publication = new Publication(getInstitution(), pubId);
        ConversionCache.putElement("pub:"+pubId, publication);

        return publication;
    }
}