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

import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Range;
import uk.ac.ebi.intact.model.Feature;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class FeatureConverter extends AbstractAnnotatedObjectConverter<Feature, psidev.psi.mi.xml.model.Feature> {

    public FeatureConverter(Institution institution) {
        super(institution, Feature.class, psidev.psi.mi.xml.model.Feature.class);
    }

    public Feature psiToIntact(psidev.psi.mi.xml.model.Feature psiObject) {
        String shortLabel = IntactConverterUtils.getShortLabelFromNames(psiObject.getNames());

        CvObjectConverter<CvFeatureType,FeatureType> featureTypeConverter =
                new CvObjectConverter<CvFeatureType,FeatureType>(getInstitution(), CvFeatureType.class, FeatureType.class);

        CvFeatureType featureType = featureTypeConverter.psiToIntact(psiObject.getFeatureType());

        // using the empty constructor because we don't have a Component instance to pass
        // to the standard parametrized constructor
        Feature feature = new Feature();
        feature.setOwner(getInstitution());
        feature.setShortLabel(shortLabel);
        feature.setCvFeatureType(featureType);

        FeatureDetectionMethod featureDetMethod = psiObject.getFeatureDetectionMethod();
        if (featureDetMethod != null) {
            CvObjectConverter<CvFeatureIdentification,FeatureDetectionMethod> featureDetMethodConverter =
                new CvObjectConverter<CvFeatureIdentification,FeatureDetectionMethod>(getInstitution(), CvFeatureIdentification.class, FeatureDetectionMethod.class);

            CvFeatureIdentification cvFeatureDetMethod = featureDetMethodConverter.psiToIntact(featureDetMethod);
            feature.setCvFeatureIdentification(cvFeatureDetMethod);
        }

        RangeConverter rangeConverter = new RangeConverter(getInstitution());

        for (psidev.psi.mi.xml.model.Range psiRange : psiObject.getRanges()) {
            Range range = rangeConverter.psiToIntact(psiRange);
            feature.addRange(range);
        }

        return feature;
    }

    public psidev.psi.mi.xml.model.Feature intactToPsi(Feature intactObject) {
        psidev.psi.mi.xml.model.Feature psiFeature = new psidev.psi.mi.xml.model.Feature();
        PsiConverterUtils.populate(intactObject, psiFeature);

        CvObjectConverter<CvFeatureType,FeatureType> featureTypeConverter =
                new CvObjectConverter<CvFeatureType,FeatureType>(getInstitution(), CvFeatureType.class, FeatureType.class);

        if (intactObject.getCvFeatureType() != null) {
            FeatureType featureType = featureTypeConverter.intactToPsi(intactObject.getCvFeatureType());
            psiFeature.setFeatureType(featureType);
        }

        RangeConverter rangeConverter = new RangeConverter(getInstitution());

        for (Range intactRange : intactObject.getRanges()) {
            psidev.psi.mi.xml.model.Range psiRange = rangeConverter.intactToPsi(intactRange);
            psiFeature.getRanges().add(psiRange);
        }

        return psiFeature;
    }

    protected String psiElementKey(psidev.psi.mi.xml.model.Feature psiObject) {
        throw new UnsupportedOperationException();
    }


}