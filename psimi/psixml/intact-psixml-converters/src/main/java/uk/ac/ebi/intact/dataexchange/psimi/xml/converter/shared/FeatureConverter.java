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
import psidev.psi.mi.xml.model.FeatureDetectionMethod;
import psidev.psi.mi.xml.model.FeatureType;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;

import java.util.Collection;

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
    private static final Log log = LogFactory.getLog(FeatureConverter.class);

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

        // export xrefs, names and annotations
        IntactConverterUtils.populateNames(psiObject.getNames(), feature, aliasConverter);
        IntactConverterUtils.populateXref(psiObject.getXref(), feature, xrefConverter);
        IntactConverterUtils.populateAnnotations(psiObject, feature, getInstitution(), annotationConverter);

        //feature.setShortLabel(shortLabel);

        if (psiObject.getFeatureType() != null) {

            CvFeatureType featureType = featureTypeConverter.psiToIntact(psiObject.getFeatureType());
            feature.setCvFeatureType(featureType);
        }
        else {
            log.error("Feature without feature type : " + feature.getShortLabel());
        }

        FeatureDetectionMethod featureDetMethod = psiObject.getFeatureDetectionMethod();
        if (featureDetMethod != null) {

            CvFeatureIdentification cvFeatureDetMethod = featureDetMethodConverter.psiToIntact(featureDetMethod);
            feature.setCvFeatureIdentification(cvFeatureDetMethod);
        }

        if (psiObject.getRanges().isEmpty()){
            log.error("Feature without any ranges : " + feature.getShortLabel());
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

        // Set id, annotations, xrefs and aliases
        PsiConverterUtils.populateId(psiFeature);
        PsiConverterUtils.populateNames(intactObject, psiFeature, aliasConverter);
        PsiConverterUtils.populateXref(intactObject, psiFeature, xrefConverter);
        PsiConverterUtils.populateAttributes(intactObject, psiFeature, annotationConverter);

        if (intactObject.getCvFeatureIdentification()!= null) {
            FeatureDetectionMethod featureMethod = featureDetMethodConverter.intactToPsi(intactObject.getCvFeatureIdentification());
            psiFeature.setFeatureDetectionMethod(featureMethod);
        }

        if (intactObject.getCvFeatureType() != null){
            FeatureType featureType = featureTypeConverter.intactToPsi(intactObject.getCvFeatureType());
            psiFeature.setFeatureType(featureType);
        }
        else {
            log.error("Feature without feature type " + intactObject.getShortLabel());
        }

        Collection<Range> ranges;
        if (isCheckInitializedCollections()){
            ranges = IntactCore.ensureInitializedRanges(intactObject);
        }
        else {
            ranges = intactObject.getRanges();
        }

        for (Range intactRange : ranges) {
            psidev.psi.mi.xml.model.Range psiRange = rangeConverter.intactToPsi(intactRange);
            psiFeature.getRanges().add(psiRange);
        }

        if (ranges.isEmpty()){
            log.error("Feature without any ranges : " + intactObject.getShortLabel());
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
        featureTypeConverter.setInstitution(institution, getInstitutionPrimaryId());
        featureDetMethodConverter.setInstitution(institution, getInstitutionPrimaryId());
        rangeConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setInstitution(Institution institution, String institId){
        super.setInstitution(institution, institId);
        featureTypeConverter.setInstitution(institution, getInstitutionPrimaryId());
        featureDetMethodConverter.setInstitution(institution, getInstitutionPrimaryId());
        rangeConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setCheckInitializedCollections(boolean check){
        super.setCheckInitializedCollections(check);
        this.featureTypeConverter.setCheckInitializedCollections(check);
        this.featureDetMethodConverter.setCheckInitializedCollections(check);
        this.rangeConverter.setCheckInitializedCollections(check);
    }
}