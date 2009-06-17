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

import au.com.bytecode.opencsv.CSVReader;

import java.io.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Creates CvAnnotationDatasets
 * 
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public final class AnnotationInfoDatasetFactory {

    private static final Log log = LogFactory.getLog( AnnotationInfoDatasetFactory.class );

    private AnnotationInfoDatasetFactory() {
    }

    public static AnnotationInfoDataset buildFromCsv( InputStream is) throws IOException {
        return buildFromCsv(is, CSVReader.DEFAULT_SEPARATOR, CSVReader.DEFAULT_QUOTE_CHARACTER);
    }

    public static AnnotationInfoDataset buildFromTabResource(InputStream is) throws IOException {
        return buildFromCsv(is,'\t', '\b');
    }

    public static AnnotationInfoDataset buildFromCsv( InputStream is, char separator, char delimiter ) throws IOException {

        if ( is == null ) {
            throw new NullPointerException( "You must give a non null inputstream" );
        }

        AnnotationInfoDataset annotInfoDataset = new AnnotationInfoDataset();

        InputStreamReader ioreader = new InputStreamReader(is);


        CSVReader csvreader = new CSVReader( ioreader, separator );
        String[] nextLine;

        int lineCount = 0;
        int lineCountExcludingHeader = 0;

        while ( ( nextLine = csvreader.readNext() ) != null ) {
          lineCount++;
            // nextLine[] is an array of values from the line
            // skip comments
            if (nextLine[0].startsWith("#")) {
                continue;
            }

            // skip empty lines
            if (nextLine.length == 0) {
                continue;
            }

           // process line

            if(nextLine.length == 7){
                lineCountExcludingHeader++;

                final String shorltabel = nextLine[0];               // 1. shortlabel
                final String fullname = nextLine[1];                 // 2. fullname
                final String type = nextLine[2];                     // 3. type
                final String mi = nextLine[3];                       // 4. mi
                final String topic = nextLine[4];                    // 5. topic
                final String reason = nextLine[5];                   // 6. exclusion reason
                final String applyToChildrenValue = nextLine[6];     // 7. apply to children

                boolean applyToChildren = false;
                if (Boolean.parseBoolean(applyToChildrenValue.trim())) {
                    applyToChildren = true;
                }

                AnnotationInfo annotInfo = new AnnotationInfo(shorltabel, fullname, type, mi, topic, reason, applyToChildren);
                annotInfoDataset.addCvAnnotation(annotInfo);
            }

        }//end while

        if ( log.isDebugEnabled() ) {
            log.debug( "Total lines mi2cv: " +lineCount);
            log.debug( "Lines excluding header: " +lineCountExcludingHeader);
        }

        return annotInfoDataset;
    }//end method





}