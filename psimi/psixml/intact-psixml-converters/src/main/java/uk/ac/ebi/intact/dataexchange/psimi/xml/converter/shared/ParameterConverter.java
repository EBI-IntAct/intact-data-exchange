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

    public ParameterConverter( Institution institution ) {
        super( institution );
    }

    public T psiToIntact( psidev.psi.mi.xml.model.Parameter psiObject ) {

        Double factor = psiObject.getFactor();

        CvParameterType cvParameterType = new CvParameterType (getInstitution(), psiObject.getTerm());
        cvParameterType.setIdentifier(psiObject.getTermAc());

        T parameter = newParameterInstance();
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
            ExperimentConverter experimentConverter = new ExperimentConverter(getInstitution());
            Experiment experiment = experimentConverter.psiToIntact(psiObject.getExperiment());
            parameter.setExperiment(experiment);
        }
        return parameter;
    }

    public psidev.psi.mi.xml.model.Parameter intactToPsi( T intactObject ) {

        psidev.psi.mi.xml.model.Parameter parameter = new psidev.psi.mi.xml.model.Parameter(intactObject.getCvParameterType().getShortLabel(), intactObject.getFactor());
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
            ExperimentConverter experimentConverter = new ExperimentConverter(getInstitution());
            parameter.setExperiment(experimentConverter.intactToPsi(intactObject.getExperiment()));
        }
        return parameter;
    }

    protected abstract T newParameterInstance();

}