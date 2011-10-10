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

import psidev.psi.mi.xml.model.FeatureDetectionMethod;
import psidev.psi.mi.xml.model.FeatureType;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.model.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class FeatureConverter extends AbstractAnnotatedObjectConverter<Feature, psidev.psi.mi.xml.model.Feature> {

    private CvObjectConverter<CvFeatureType,FeatureType> featureTypeConverter;
    private CvObjectConverter<CvFeatureIdentification,FeatureDetectionMethod> featureDetMethodConverter;
    private RangeConverter rangeConverter;

    public FeatureConverter(Institution institution) {
        super(institution, Feature.class, psidev.psi.mi.xml.model.Feature.class);
        featureTypeConverter = new CvObjectConverter<CvFeatureType,FeatureType>(institution, CvFeatureType.class, FeatureType.class);
        featureDetMethodConverter = new CvObjectConverter<CvFeatureIdentification,FeatureDetectionMethod>(institution, CvFeatureIdentification.class, FeatureDetectionMethod.class);
        rangeConverter = new RangeConverter(institution);
    }

    public Feature psiToIntact(psidev.psi.mi.xml.model.Feature psiObject) {
        //String shortLabel = IntactConverterUtils.getShortLabelFromNames(psiObject.getNames());

        // using the empty constructor because we don't have a Component instance to pass
        // to the standard parametrized constructor
        Feature feature = super.psiToIntact(psiObject);
        if (!isNewIntactObjectCreated()) {
            return feature;
        }

        psiStartConversion(psiObject);

        feature.setOwner(getInstitution());
        IntactConverterUtils.populateNames(psiObject.getNames(), feature, aliasConverter);
        IntactConverterUtils.populateXref(psiObject.getXref(), feature, xrefConverter);
        //feature.setShortLabel(shortLabel);

        if (psiObject.getFeatureType() != null) {

            CvFeatureType featureType = featureTypeConverter.psiToIntact(psiObject.getFeatureType());
            feature.setCvFeatureType(featureType);
        }

        FeatureDetectionMethod featureDetMethod = psiObject.getFeatureDetectionMethod();
        if (featureDetMethod != null) {

            CvFeatureIdentification cvFeatureDetMethod = featureDetMethodConverter.psiToIntact(featureDetMethod);
            feature.setCvFeatureIdentification(cvFeatureDetMethod);
        }

        for (psidev.psi.mi.xml.model.Range psiRange : psiObject.getRanges()) {
            Range range = rangeConverter.psiToIntact(psiRange);
            feature.addRange(range);
        }
        psiEndConversion(psiObject);


        return feature;
    }

    public psidev.psi.mi.xml.model.Feature intactToPsi(Feature intactObject) {
        psidev.psi.mi.xml.model.Feature psiFeature = super.intactToPsi(intactObject);

        if (!isNewPsiObjectCreated()) {
            return psiFeature;
        }

        intactStartConversation(intactObject);

        if (intactObject.getCvFeatureIdentification()!= null) {
            FeatureDetectionMethod featureMethod = featureDetMethodConverter.intactToPsi(intactObject.getCvFeatureIdentification());
            psiFeature.setFeatureDetectionMethod(featureMethod);
        }

        if (intactObject.getCvFeatureType() != null){
            FeatureType featureType = featureTypeConverter.intactToPsi(intactObject.getCvFeatureType());
            psiFeature.setFeatureType(featureType);
        }

        for (Range intactRange : IntactCore.ensureInitializedRanges(intactObject)) {
            psidev.psi.mi.xml.model.Range psiRange = rangeConverter.intactToPsi(intactRange);
            psiFeature.getRanges().add(psiRange);
        }

        intactEndConversion(intactObject);

        return psiFeature;
    }

    protected Object psiElementKey(psidev.psi.mi.xml.model.Feature psiObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInstitution(Institution institution)
    {
        super.setInstitution(institution);
        featureTypeConverter.setInstitution(institution);
        featureDetMethodConverter.setInstitution(institution);
        rangeConverter.setInstitution(institution);
    }
}