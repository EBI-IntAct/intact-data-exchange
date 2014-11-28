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
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Experiment;
import psidev.psi.mi.jami.model.Publication;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.model.impl.DefaultExperiment;
import psidev.psi.mi.jami.model.impl.DefaultOrganism;
import psidev.psi.mi.jami.model.impl.DefaultPublication;
import psidev.psi.mi.jami.model.impl.DefaultSource;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;

import javax.annotation.Resource;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */

public class ExperimentEnricherTest extends EnricherBasicTestCase {

    @Resource(name = "intactExperimentEnricher")
    private ExperimentEnricher enricher;

    @Test
    public void enrich_pub() throws EnricherException{
        final String pubmedId = "15733859";

        Publication publication = new DefaultPublication(pubmedId);
        Experiment experiment = new DefaultExperiment(publication);

        publication.setSource(new DefaultSource("ucla"));

        enricher.enrich(experiment);

        Assert.assertEquals("The flexible loop of Bcl-2 is required for molecular interaction with immunosuppressant FK-506 binding protein 38 (FKBP38).",
                publication.getTitle());
        Assert.assertEquals(3, experiment.getAnnotations().size());

        Assert.assertEquals("ucla", publication.getSource().getShortName());
    }

    @Test
    public void enrich_pub_repeatedAnnots() throws EnricherException{
        final String pubmedId = "15733859";

        Publication publication = new DefaultPublication(pubmedId);
        Experiment experiment = new DefaultExperiment(publication);

        experiment.getAnnotations().add(AnnotationUtils.createAnnotation(Annotation.PUBLICATION_YEAR, Annotation.PUBLICATION_YEAR_MI, "2005"));

        enricher.enrich(experiment);

        Assert.assertEquals(3, experiment.getAnnotations().size());
    }

    @Test
    public void enrich_hostOrganism() throws EnricherException{
        Experiment experiment = new DefaultExperiment(null);
        experiment.setHostOrganism(new DefaultOrganism(83333));

        enricher.enrich(experiment);

        Assert.assertEquals("ecoli", experiment.getHostOrganism().getCommonName());
    }

    @Test
    public void enrich_noDetectionMethod() throws EnricherException{
        Experiment experiment = new DefaultExperiment(null);
        experiment.setHostOrganism(new DefaultOrganism(83333));

        enricher.enrich(experiment);

        Assert.assertNotNull(experiment.getInteractionDetectionMethod());
        Assert.assertEquals(Experiment.UNSPECIFIED_METHOD_MI, experiment.getInteractionDetectionMethod().getMIIdentifier());
    }

    @Test
    public void enrich_wrongPubmedXrefQual() throws EnricherException{
        Experiment experiment = new DefaultExperiment(new DefaultPublication("15733859"));
        experiment.setHostOrganism(new DefaultOrganism(83333));

        enricher.enrich(experiment);

        Assert.assertEquals(Xref.PRIMARY_MI, experiment.getXrefs().iterator().next().getQualifier().getMIIdentifier());
    }
}