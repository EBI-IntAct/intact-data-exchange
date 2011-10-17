/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.ExperimentRef;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.model.*;


/**
 * Base class to convert parameters
 *
 * @author Julie Bourbeillon (julie.bourbeillon@labri.fr)
 * @version $Id$
 * @since 2.0.1
 */
public abstract class ParameterConverter<T extends Parameter> extends AbstractIntactPsiConverter<T, psidev.psi.mi.xml.model.Parameter> {

    private ExperimentConverter experimentConverter;

    public ParameterConverter( Institution institution ) {
        super( institution );
        experimentConverter = new ExperimentConverter(institution);
    }

    public ParameterConverter( Institution institution, ExperimentConverter expConverter ) {
        super( institution );
        if (expConverter != null){
            experimentConverter = expConverter;
        }
        else {
            experimentConverter = new ExperimentConverter(institution);
        }
    }

    public T psiToIntact( psidev.psi.mi.xml.model.Parameter psiObject ) {

        Double factor = psiObject.getFactor();

        CvParameterType cvParameterType = new CvParameterType (getInstitution(), psiObject.getTerm());
        cvParameterType.setIdentifier(psiObject.getTermAc());

        T parameter = newParameterInstance();

        psiStartConversion(psiObject);
        parameter.setOwner(getInstitution());
        parameter.setCvParameterType(cvParameterType);
        parameter.setFactor(factor);

        if ((psiObject.getUnit() != null) || (psiObject.getUnitAc() != null)){
            CvParameterUnit cvParameterUnit = new CvParameterUnit(getInstitution(), psiObject.getUnit());
            cvParameterUnit.setIdentifier(psiObject.getUnitAc());
            parameter.setCvParameterUnit(cvParameterUnit);
        }

        if (psiObject.hasBase() ) {
            Integer base = psiObject.getBase();
            parameter.setBase(base);
        }
        if(psiObject.hasExponent()) {
            Integer exponent = psiObject.getExponent();
            parameter.setExponent(exponent);
        }
        if (psiObject.hasUncertainty()){
            Double uncertainty = psiObject.getUncertainty();
            parameter.setUncertainty(uncertainty);
        }
        if (psiObject.hasExperiment()){
            Experiment experiment = experimentConverter.psiToIntact(psiObject.getExperiment());
            parameter.setExperiment(experiment);
        }

        psiEndConversion(psiObject);
        return parameter;
    }

    public psidev.psi.mi.xml.model.Parameter intactToPsi( T intactObject ) {

        psidev.psi.mi.xml.model.Parameter parameter = new psidev.psi.mi.xml.model.Parameter(intactObject.getCvParameterType().getShortLabel(), intactObject.getFactor());

        intactStartConversation(parameter);

        parameter.setTermAc(intactObject.getCvParameterType().getIdentifier());
        if (intactObject.getBase() != null) {
            parameter.setBase(intactObject.getBase());
        }
        if (intactObject.getBase() != null) {
            parameter.setBase(intactObject.getBase());
        }
        if (intactObject.getExponent() != null) {
            parameter.setExponent(intactObject.getExponent());
        }
        if (intactObject.getUncertainty() != null) {
            parameter.setUncertainty(intactObject.getUncertainty());
        }
        if (intactObject.getCvParameterUnit() != null) {
            parameter.setUnit(intactObject.getCvParameterUnit().getShortLabel());
            parameter.setUnitAc(intactObject.getCvParameterUnit().getIdentifier());
        }
        if(intactObject.getExperiment() != null) {
            parameter.setExperimentRef(new ExperimentRef(experimentConverter.intactToPsi(intactObject.getExperiment()).getId()));
        }

        intactEndConversion(intactObject);
        return parameter;
    }

    protected abstract T newParameterInstance();

    public void setInstitution(Institution institution, boolean setExperimentInstitution)
    {
        super.setInstitution(institution);
        if (setExperimentInstitution){
            experimentConverter.setInstitution(institution, getInstitutionPrimaryId());
        }
    }

    public void setInstitution(Institution institution, boolean setExperimentInstitution, String institutionPrimaryId)
    {
        super.setInstitution(institution, institutionPrimaryId);
        if (setExperimentInstitution){
            experimentConverter.setInstitution(institution, getInstitutionPrimaryId());
        }
    }

    @Override
    public void setCheckInitializedCollections(boolean check){
        super.setCheckInitializedCollections(check);
        this.experimentConverter.setCheckInitializedCollections(check);
    }

    public void setCheckInitializedCollections(boolean check, boolean initializeExperiment){
        super.setCheckInitializedCollections(check);
        if (initializeExperiment){
            this.experimentConverter.setCheckInitializedCollections(check);
        }
    }
}