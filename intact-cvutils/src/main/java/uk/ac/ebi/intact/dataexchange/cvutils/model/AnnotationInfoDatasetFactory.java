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
package uk.ac.ebi.intact.dataexchange.cvutils.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;


/**
 * Creates CvAnnotationDatasets
 * 
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public final class AnnotationInfoDatasetFactory {

    private AnnotationInfoDatasetFactory() {
    }

    public static AnnotationInfoDataset buildFromTabResource(InputStream is) throws IOException {
        AnnotationInfoDataset annotInfoDataset = new AnnotationInfoDataset();

        BufferedReader in = null;

        in = new BufferedReader(new InputStreamReader(is));
        String line;
        int lineCount = 0;
        while ((line = in.readLine()) != null) {

            lineCount++;
            line = line.trim();

            // skip comments
            if (line.startsWith("#")) {
                continue;
            }

            // skip empty lines
            if (line.length() == 0) {
                continue;
            }

            // process line
            StringTokenizer stringTokenizer = new StringTokenizer(line, "\t");

            final String shorltabel = stringTokenizer.nextToken();           // 1. shortlabel
            final String fullname = stringTokenizer.nextToken();             // 2. fullname
            final String type = stringTokenizer.nextToken();                 // 3. type
            final String mi = stringTokenizer.nextToken();                   // 4. mi
            final String topic = stringTokenizer.nextToken();                // 5. topic
            final String reason = stringTokenizer.nextToken();               // 6. exclusion reason
            final String applyToChildrenValue = stringTokenizer.nextToken(); // 7. apply to children

            boolean applyToChildren = false;
            if (Boolean.parseBoolean(applyToChildrenValue.trim())) {
                applyToChildren = true;
            }

            AnnotationInfo annotInfo = new AnnotationInfo(shorltabel, fullname, type, mi, topic, reason, applyToChildren);
            annotInfoDataset.addCvAnnotation(annotInfo);
        }

        return annotInfoDataset;
    }
}