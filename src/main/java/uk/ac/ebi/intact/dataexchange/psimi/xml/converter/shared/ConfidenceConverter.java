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
import uk.ac.ebi.intact.model.Confidence;
import uk.ac.ebi.intact.model.CvConfidenceType;
import uk.ac.ebi.intact.model.Institution;


/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *               06-Dec-2007
 *               </pre>
 */
public class ConfidenceConverter extends AbstractIntactPsiConverter<Confidence, psidev.psi.mi.xml.model.Confidence> {

    public ConfidenceConverter( Institution institution ) {
        super( institution );
    }

    public uk.ac.ebi.intact.model.Confidence psiToIntact( psidev.psi.mi.xml.model.Confidence psiObject ) {
        String value = psiObject.getValue();
        Confidence confidence = new Confidence( getInstitution(), value );
        CvObjectConverter<CvConfidenceType,Unit> confidenceTyeConverter =
                        new CvObjectConverter<CvConfidenceType,Unit>(getInstitution(), CvConfidenceType.class, Unit.class);
        CvConfidenceType cvConfType = confidenceTyeConverter.psiToIntact( psiObject.getUnit());
        confidence.setCvConfidenceType( cvConfType);
        return confidence;
    }

    public psidev.psi.mi.xml.model.Confidence intactToPsi( uk.ac.ebi.intact.model.Confidence intactObject ) {
        psidev.psi.mi.xml.model.Confidence confidence = new psidev.psi.mi.xml.model.Confidence();

        confidence.setValue( intactObject.getValue());

        CvObjectConverter<CvConfidenceType,Unit> confidenceTyeConverter =
                        new CvObjectConverter<CvConfidenceType,Unit>(getInstitution(), CvConfidenceType.class, Unit.class);

        Unit unit = confidenceTyeConverter.intactToPsi(intactObject.getCvConfidenceType());
        confidence.setUnit(unit);

        return confidence;
    }

}
