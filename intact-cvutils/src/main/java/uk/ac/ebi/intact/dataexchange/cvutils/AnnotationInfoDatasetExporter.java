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
package uk.ac.ebi.intact.dataexchange.cvutils;

import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfo;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Utility to export AnnotationInfo to various file types.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public class AnnotationInfoDatasetExporter {

    /**
     * Exports an AnnotationInfoDataset onto an output stream.
     *
     * @param dataset             the dataset to be exported (not null).
     * @param os                  the output stream onto which we export (not null)
     * @param includeColumnHeader if true, invlude header, otherwise, omit it.
     * @throws IOException
     */
    public void exportCSV( AnnotationInfoDataset dataset, OutputStream os, boolean includeColumnHeader ) throws IOException {

        if ( dataset == null ) {
            throw new NullPointerException( "You must give a non null dataset" );
        }

        if ( os == null ) {
            throw new NullPointerException( "You must give a non null OutputStream" );
        }

        final Collection<AnnotationInfo> infos = dataset.getAllAnnotationInfoSortedByTypeAndLabel();

        if ( includeColumnHeader ) {
            StringBuilder sb = new StringBuilder( 256 );

            sb.append( "\"" ).append( "# Cv shortlabel" ).append( "\"" ).append( ',' );
            sb.append( "\"" ).append( "Cv fullname" ).append( "\"" ).append( ',' );
            sb.append( "\"" ).append( "Cv Class" ).append( "\"" ).append( ',' );
            sb.append( "\"" ).append( "CvTopic shortlabel" ).append( "\"" ).append( ',' );
            sb.append( "\"" ).append( "Annotation's text " ).append( "\"" ).append( ',' );
            sb.append( "\"" ).append( "apply to children " ).append( "\"" );

            sb.append( '\n' );
            os.write( sb.toString().getBytes() );
        }

        for ( AnnotationInfo info : infos ) {
            StringBuilder sb = new StringBuilder( 256 );

            sb.append( "\"" ).append( info.getShortLabel() ).append( "\"" ).append( ',' );
            sb.append( "\"" ).append( info.getFullName() ).append( "\"" ).append( ',' );
            sb.append( "\"" ).append( info.getMi() ).append( "\"" ).append( ',' );
            sb.append( "\"" ).append( info.getType() ).append( "\"" ).append( ',' );
            sb.append( "\"" ).append( info.getTopicShortLabel() ).append( "\"" ).append( ',' );
            sb.append( "\"" ).append( info.getReason() ).append( "\"" ).append( ',' );
            sb.append( "\"" ).append( info.isApplyToChildren() ).append( "\"" ).append( ',' );

            sb.append( '\n' );
            os.write( sb.toString().getBytes() );
        }
    }
}
