/*
 * Copyright 2001-2008 The European Bioinformatics Institute.
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
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.XrefUtils;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.annotation.util.AnnotationUtil;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InstitutionEnricher extends AnnotatedObjectEnricher<Institution>{

    private static final Institution INTACT;
    private static final Institution MINT;
    private static final Institution DIP;

    static {
        // IntAct
        INTACT = new Institution("IntAct");

        CvDatabase pubmed = CvObjectUtils.createCvObject(INTACT, CvDatabase.class, CvDatabase.PUBMED_MI_REF, CvDatabase.PUBMED);
        CvTopic postalAddress = CvObjectUtils.createCvObject(INTACT, CvTopic.class, null, "postal address");
        CvTopic email = CvObjectUtils.createCvObject(INTACT, CvTopic.class, CvTopic.CONTACT_EMAIL_MI_REF, CvTopic.CONTACT_EMAIL);
        CvTopic url = CvObjectUtils.createCvObject(INTACT, CvTopic.class, CvTopic.URL_MI_REF, CvTopic.URL);

        INTACT.setFullName("European Bioinformatics Institute");
        INTACT.addXref(XrefUtils.createIdentityXrefPsiMi(INTACT, CvDatabase.INTACT_MI_REF));
        INTACT.addXref(XrefUtils.createIdentityXref(INTACT, "14681455", pubmed));

        final String intactUrl = "http://www.ebi.ac.uk/";
        final String intactAddress = "European Bioinformatics Institute; " +
                                                                 "Wellcome Trust Genome Campus; " +
                                                                 "Hinxton, Cambridge; " +
                                                                 "CB10 1SD; " +
                                                                 "United Kingdom";

        INTACT.setUrl(intactUrl);
        INTACT.setPostalAddress(intactAddress);
        INTACT.addAnnotation(new Annotation(INTACT, postalAddress, intactAddress));
        INTACT.addAnnotation(new Annotation(INTACT, url, intactUrl));

        // MINT
        MINT = new Institution("MINT");
        MINT.setFullName("MINT, Dpt of Biology, University of Rome Tor Vergata");
        MINT.addXref(XrefUtils.createIdentityXrefPsiMi(MINT, CvDatabase.MINT_MI_REF));
        MINT.addXref(XrefUtils.createIdentityXref(MINT, "14681455", pubmed));

        final String mintUrl = "http://mint.bio.uniroma2.it/mint";
        MINT.setUrl(mintUrl);
        MINT.addAnnotation(new Annotation(MINT, url, mintUrl));

        // DIP
        DIP = new Institution("DIP");
        DIP.setFullName("Database of Interacting Proteins");
        DIP.addXref(XrefUtils.createIdentityXrefPsiMi(DIP, CvDatabase.DIP_MI_REF));
        DIP.addXref(XrefUtils.createIdentityXref(DIP, "14681454", pubmed));

        final String dipAddress = "611 Young Drive East; Los Angeles CA 90095; USA";
        final String dipUrl = "http://dip.doe-mbi.ucla.edu/";
        DIP.setPostalAddress(dipAddress);
        DIP.setUrl(dipUrl);
        DIP.addAnnotation(new Annotation(DIP, postalAddress, dipAddress));
        DIP.addAnnotation(new Annotation(DIP, url, dipUrl));
        DIP.addAnnotation(new Annotation(DIP, email, "dip@mbi.ucla.edu"));
    }

    private static ThreadLocal<InstitutionEnricher> instance = new ThreadLocal<InstitutionEnricher>() {
        @Override
        protected InstitutionEnricher initialValue() {
            return new InstitutionEnricher();
        }
    };

    public static InstitutionEnricher getInstance() {
        return instance.get();
    }

    public InstitutionEnricher() {
    }

    public void enrich(Institution objectToEnrich) {

        InstitutionXref psiMiIdentity = XrefUtils.getPsiMiIdentityXref(objectToEnrich);

        if (psiMiIdentity != null) {
            if (CvDatabase.INTACT_MI_REF.equals(psiMiIdentity.getPrimaryId())) {
                enrichInstitutionFromReference(objectToEnrich, INTACT);
            } else if (CvDatabase.MINT_MI_REF.equals(psiMiIdentity.getPrimaryId())) {
                enrichInstitutionFromReference(objectToEnrich, MINT);
            } else if (CvDatabase.DIP_MI_REF.equals(psiMiIdentity.getPrimaryId())) {
                enrichInstitutionFromReference(objectToEnrich, DIP);
            }
        } else {
            if ("ebi".equalsIgnoreCase(objectToEnrich.getShortLabel()) ||
                "intact".equalsIgnoreCase(objectToEnrich.getShortLabel())) {
                enrichInstitutionFromReference(objectToEnrich, INTACT);
            } else if ("mint".equalsIgnoreCase(objectToEnrich.getShortLabel())) {
                enrichInstitutionFromReference(objectToEnrich, MINT);
            } else if ("dip".equalsIgnoreCase(objectToEnrich.getShortLabel()) ||
                "ucla".equalsIgnoreCase(objectToEnrich.getShortLabel())||
                objectToEnrich.getShortLabel().startsWith("http://dip")) {
                enrichInstitutionFromReference(objectToEnrich, DIP);
            }
        }

    }

    protected void enrichInstitutionFromReference(Institution institutionToEnrich, Institution reference) {
        institutionToEnrich.setShortLabel(reference.getShortLabel());
        institutionToEnrich.setFullName(reference.getFullName());
        institutionToEnrich.setPostalAddress(reference.getPostalAddress());
        institutionToEnrich.setUrl(reference.getUrl());
        institutionToEnrich.setXrefs(reference.getXrefs());
        institutionToEnrich.setAliases(reference.getAliases());
        institutionToEnrich.setAnnotations(reference.getAnnotations());
    }

    public void close() {
        // nothing
    }
}