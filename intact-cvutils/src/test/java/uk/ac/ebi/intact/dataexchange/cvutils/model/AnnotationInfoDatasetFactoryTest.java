/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.cvutils.model;

import org.junit.Test;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotationInfoDatasetFactoryTest {

    @Test
    public void buildFromCsvTest() throws IOException {
        InputStream is = AnnotationInfoDatasetFactoryTest.class.getResourceAsStream("/additional-annotations.csv");
       
        AnnotationInfoDataset annotationDataset = AnnotationInfoDatasetFactory.buildFromCsv( is );
        Assert.assertEquals( 317, annotationDataset.getAll().size() );

        final AnnotationInfo urlAnnotation = annotationDataset.getCvAnnotation("MI:0614");
        Assert.assertFalse(urlAnnotation.getReason().startsWith("\""));
    }

}
