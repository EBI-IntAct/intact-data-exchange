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
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */

public class ExperimentEnricherTest extends EnricherBasicTestCase {

    @Autowired
    private ExperimentEnricher enricher;

    @Test
    public void enrich_pub() {
        final String pubmedId = "15733859";

        Publication publication = getMockBuilder().createPublication(pubmedId);
        Experiment experiment = getMockBuilder().createExperimentEmpty();
        experiment.setShortLabel("lalalssla");
        experiment.setPublication(publication);

        experiment.setOwner(new Institution("ucla"));

        enricher.enrich(experiment);

        Assert.assertEquals("kang-2005", experiment.getShortLabel());
        Assert.assertEquals("The flexible loop of Bcl-2 is required for molecular interaction with immunosuppressant FK-506 binding protein 38 (FKBP38).", experiment.getFullName());
        Assert.assertEquals(2, experiment.getAnnotations().size());

        Assert.assertEquals("dip", experiment.getOwner().getShortLabel());
    }

    @Test
    public void enrich_pub_repeatedAnnots() {
        final String pubmedId = "15733859";

        Publication publication = getMockBuilder().createPublication(pubmedId);
        Experiment experiment = getMockBuilder().createExperimentEmpty();
        experiment.setPublication(publication);

        CvTopic publicationYearTopic = CvObjectUtils.createCvObject(experiment.getOwner(), CvTopic.class, CvTopic.PUBLICATION_YEAR_MI_REF, CvTopic.PUBLICATION_YEAR);
        experiment.addAnnotation(new Annotation(experiment.getOwner(), publicationYearTopic,"2005"));

        enricher.enrich(experiment);

        Assert.assertEquals(2, experiment.getAnnotations().size());
    }

    @Test
    public void enrich_hostOrganism() {
        Experiment experiment = getMockBuilder().createExperimentEmpty();
        experiment.getBioSource().setTaxId("83333");

        enricher.enrich(experiment);

        Assert.assertEquals("strain k12", experiment.getBioSource().getShortLabel());
    }

    @Test
    public void enrich_noDetectionMethod() {
        Experiment experiment = getMockBuilder().createExperimentRandom(2);
        experiment.getBioSource().setTaxId("83333");
        experiment.setCvIdentification(null);

        Assert.assertNull(experiment.getCvIdentification());

        enricher.enrich(experiment);

        Assert.assertNotNull(experiment.getCvIdentification());
        Assert.assertEquals(CvIdentification.PREDETERMINED_MI_REF, experiment.getCvIdentification().getMiIdentifier());
    }

    @Test
    public void enrich_wrongPubmedXrefQual() {
        Experiment experiment = getMockBuilder().createExperimentEmpty();
        experiment.setShortLabel("lalalssla");
        experiment.getBioSource().setTaxId("83333");
        experiment.setPublication(null);
        experiment.getXrefs().clear();

        CvDatabase pubmed = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.PUBMED_MI_REF, CvDatabase.PUBMED);
        final ExperimentXref aXref = getMockBuilder().createIdentityXref( experiment, "15733859", pubmed );
        aXref.setCvXrefQualifier( null );
        experiment.addXref( aXref );

        enricher.enrich(experiment);

        Assert.assertEquals("kang-2005", experiment.getShortLabel());
        Assert.assertEquals(CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, experiment.getXrefs().iterator().next().getCvXrefQualifier().getIdentifier());
    }

    @Test
    public void enrich_alreadySyncedLabel() {
        Experiment experiment = getMockBuilder().createExperimentEmpty();
        experiment.setShortLabel("lala-2010-1");

        CvDatabase pubmed = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.PUBMED_MI_REF, CvDatabase.PUBMED);
        final ExperimentXref aXref = getMockBuilder().createIdentityXref( experiment, "15733859", pubmed );
        aXref.setCvXrefQualifier( null );
        experiment.addXref( aXref );

        enricher.enrich(experiment);

        Assert.assertEquals("lala-2010-1", experiment.getShortLabel());
    }
}