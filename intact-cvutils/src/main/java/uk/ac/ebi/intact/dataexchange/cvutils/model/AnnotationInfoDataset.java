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

import java.util.*;


/**
 * Stores an indexed collection of AnnotationInfo.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class AnnotationInfoDataset {

    private Map<String, AnnotationInfo> cvAnnotations;

    public AnnotationInfoDataset() {
        this.cvAnnotations = new HashMap<String, AnnotationInfo>();
    }

    public void addCvAnnotation( AnnotationInfo annotInfo ) {
        cvAnnotations.put( annotInfo.getMi(), annotInfo );
    }

    public AnnotationInfo getCvAnnotation( String mi ) {
        return cvAnnotations.get( mi );
    }

    public boolean containsCvAnnotation( String mi ) {
        return cvAnnotations.containsKey( mi );
    }

    /**
     * Collect all available AnnotationInfo in no particular order.
     *
     * @return a non null list of AnnotationInfo.
     * @since 2.0.1
     */
    public Collection<AnnotationInfo> getAll() {
        return new ArrayList<AnnotationInfo>( cvAnnotations.values() );
    }

    /**
     * Retreive the complete list of AnnotationInfo ordered according to the given comparator.
     *
     * @param comparator how to order the objects.
     * @return a non null list.
     * @since 2.0.1
     */
    public Collection<AnnotationInfo> getAllAnnotationInfoSorted( Comparator<AnnotationInfo> comparator ) {

        if ( comparator == null ) {
            throw new NullPointerException( "You must give a non null comparator" );
        }

        final List<AnnotationInfo> infos = new ArrayList<AnnotationInfo>( cvAnnotations.values() );
        Collections.sort( infos, comparator );
        return infos;
    }

    /**
     * Retreive the complete list of AnnotationInfo ordered by type and shortlabel alphabeticaly.
     *
     * @return a non null list.
     * @since 2.0.1
     */
    public Collection<AnnotationInfo> getAllAnnotationInfoSortedByTypeAndLabel() {
        return getAllAnnotationInfoSorted( new Comparator<AnnotationInfo>() {
            public int compare( AnnotationInfo ai1, AnnotationInfo ai2 ) {

                if ( ai1.getType().equals( ai2.getType() ) ) {
                    return ai1.getShortLabel().compareTo( ai2.getShortLabel() );
                } else {
                    return ai1.getType().compareTo( ai2.getType() );
                }
            }
        } );
    }

}