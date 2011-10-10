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

import psidev.psi.mi.xml.model.Unit;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.model.AbstractConfidence;
import uk.ac.ebi.intact.model.CvConfidenceType;
import uk.ac.ebi.intact.model.Institution;


/**
 * TODO a confidence can have a list of experiment refs but it does not need to be converted because in intact, one interaction has only one experiment
 * TODO if the interaction can have several experiments, the confidence converter must also be able to convert experiment refs
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractConfidenceConverter<T extends AbstractConfidence> extends AbstractIntactPsiConverter<T, psidev.psi.mi.xml.model.Confidence> {

    private CvObjectConverter<CvConfidenceType,Unit> confidenceTyeConverter;

    public AbstractConfidenceConverter(Institution institution) {
        super( institution );
        confidenceTyeConverter = new CvObjectConverter<CvConfidenceType,Unit>(institution, CvConfidenceType.class, Unit.class);
    }

    public abstract T newConfidenceInstance(String value);

    public T psiToIntact( psidev.psi.mi.xml.model.Confidence psiObject ) {
        String value = psiObject.getValue();
        T confidence = newConfidenceInstance(value);

        CvConfidenceType cvConfType = confidenceTyeConverter.psiToIntact( psiObject.getUnit());
        confidence.setCvConfidenceType( cvConfType);
        return confidence;
    }

    public psidev.psi.mi.xml.model.Confidence intactToPsi( T intactObject ) {
        psidev.psi.mi.xml.model.Confidence confidence = new psidev.psi.mi.xml.model.Confidence();

        confidence.setValue( intactObject.getValue());

        Unit unit = confidenceTyeConverter.intactToPsi(intactObject.getCvConfidenceType());
        confidence.setUnit(unit);

        return confidence;
    }

    @Override
    public void setInstitution(Institution institution)
    {
        super.setInstitution(institution);
        this.confidenceTyeConverter.setInstitution(institution);
    }
}
