/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyCollection;

import java.util.*;

/**
 * That class reflects what is needed to create an IntAct <code>Component</code>.
 * <p/>
 * <pre>
 *       &lt;proteinParticipant&gt;
 *           &lt;proteinInteractorRef ref="EBI-333"/&gt;
 *           &lt;role&gt;prey&lt;/role&gt;
 *       &lt;/proteinParticipant&gt;
 * <p/>
 *          - OR -
 * <p/>
 *       &lt;proteinParticipant&gt;
 *            &lt;proteinInteractor id="EBI-333"&gt;
 *                    &lt;names&gt;
 *                        &lt;shortLabel&gt;yev6_yeast&lt;/shortLabel&gt;
 *                        &lt;fullName&gt;Hypothetical 29.7 kDa protein in RSP5-LCP5
 *                            intergenic region&lt;/fullName&gt;
 *                    &lt;/names&gt;
 *                    &lt;xref&gt;
 *                        &lt;primaryRef db="uniprot" id="P40078" secondary="yev6_yeast" version=""/&gt;
 *                        &lt;secondaryRef db="interpro" id="IPR001047" secondary="Ribosomal_S8E" version=""/&gt;
 *                    &lt;/xref&gt;
 *                    &lt;organism ncbiTaxId="4932"&gt;
 *                        &lt;names&gt;
 *                            &lt;shortLabel&gt;s cerevisiae&lt;/shortLabel&gt;
 *                            &lt;fullName&gt;Saccharomyces cerevisiae&lt;/fullName&gt;
 *                        &lt;/names&gt;
 *                    &lt;/organism&gt;
 *                    &lt;sequence&gt;MPQNDY (...) VNAVLLV&lt;/sequence&gt;
 *              &lt;/proteinInteractor&gt;
 *              &lt;role&gt;prey&lt;/role&gt;
 *      &lt;/proteinParticipant&gt;
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.model.Component
 */
public final class ProteinParticipantTag {

    private final ProteinInteractorTag proteinInteractor;
    private final String role;
    private final ExpressedInTag expressedIn;
    private final Collection features;
    private final Boolean isTaggedProtein;
    private final Boolean isOverExpressedProtein;


    ///////////////////////////
    // Constructors

    public ProteinParticipantTag( final ProteinInteractorTag proteinInteractor,
                                  final String role,
                                  final ExpressedInTag expressedIn,
                                  final Collection features,
                                  final Boolean isTaggedProtein,
                                  final Boolean isOverExpressedProtein ) {

        if ( proteinInteractor == null ) {
            throw new IllegalArgumentException( "You must give a non null proteinInteractor for a proteinParticipant" );
        }

        if ( role == null || "".equals( role.trim() ) ) {
            throw new IllegalArgumentException( "You must give a non null/empty role for a proteinParticipant" );
        }

        if ( features == null ) {
            this.features = Collections.EMPTY_LIST;
        } else {
            // check the collection content
            for ( Iterator iterator = features.iterator(); iterator.hasNext(); ) {
                Object o = (Object) iterator.next();
                if ( !( o instanceof FeatureTag ) ) {

                    String type = null;
                    if ( o == null ) {
                        type = "null";
                    } else {
                        type = o.getClass().getName();
                    }

                    throw new IllegalArgumentException( "The feature collection added to the protein participant " +
                                                        "doesn't contains only FeatureTag (ie. " +
                                                        type + ")." );
                }
            }
            this.features = new ReadOnlyCollection( features );
        }

        this.proteinInteractor = proteinInteractor;
        this.role = role;
        this.expressedIn = expressedIn;
        this.isTaggedProtein = isTaggedProtein;
        this.isOverExpressedProtein = isOverExpressedProtein;
    }


    //////////////////////////
    // Getters

    public ProteinInteractorTag getProteinInteractor() {
        return proteinInteractor;
    }

    public String getRole() {
        return role;
    }

    public ExpressedInTag getExpressedIn() {
        return expressedIn;
    }

    public Collection getFeatures() {
        return features;
    }

    public Boolean getTaggedProtein() {
        return isTaggedProtein;
    }

    public Boolean getOverExpressedProtein() {
        return isOverExpressedProtein;
    }

    /**
     * Get from a feature object the primaryId of an Xref having the given database.
     *
     * @param feature
     * @param database
     *
     * @return the primaryId of null if we can't find it.
     */
    private String getFeatureId( FeatureTag feature, String database ) {

        for ( Iterator iterator = feature.getXrefs().iterator(); iterator.hasNext(); ) {
            XrefTag xref = (XrefTag) iterator.next();
            if ( xref.getDb().equalsIgnoreCase( database ) ) {
                return xref.getId();
            }
        }

        return null;
    }

    /**
     * Checks if a feature has an Xref with the given primaryId and database.
     *
     * @param feature
     * @param database
     * @param id
     *
     * @return true if it has it.
     */
//    private boolean hasFeatureXref( FeatureTag feature, String database, String id ) {
//
//        String _id = getFeatureId( feature, database );
//
//        if ( id.equals( _id ) ) {
//            return true;
//        }
//
//        return false;
//    }

    public boolean hasFeature() {
        return features != null && !features.isEmpty();
    }

    private Map mapCached = null;

    /**
     * Produce a map that represents a clustering of the features. That method is there to help with PSI version 1 that
     * has only one location per feature, then we cluster the features in order to reflect that relationship 1 feature
     * to 0..n locations/ranges
     *
     * @return a map <cluster ID> ----> <Collection of FeatureTag>
     */
    public Map getClusteredFeatures() {

        if ( mapCached != null ) {
            return mapCached;
        }

        // TODO is a feature doesn't have an id, it would cluster them on value: 'null'
        // we need to split those in cluster with random id

        Map map = new HashMap(); // feature id -> Features

        // read the current features and generate a set of new Features having multiple Locations.
        for ( Iterator iterator = features.iterator(); iterator.hasNext(); ) {
            FeatureTag feature = (FeatureTag) iterator.next();
            String id = getFeatureId( feature, FeatureTag.FEATURE_CLUSTER_ID_XREF );


            Collection features = null;
            features = (Collection) map.get( id );
            if ( null == features ) {
                features = new ArrayList();
                map.put( id, features );
            }

            if ( id != null ) {

                // If the feature has a cluster ID given explicitly in the PSI file, we
                // check that all other features have the same type and detection method
                // TODO write a test for that !!
                for ( Iterator iterator1 = features.iterator(); iterator1.hasNext(); ) {
                    FeatureTag featureTag = (FeatureTag) iterator1.next();

                    if ( featureTag.getFeatureType().equals( feature.getFeatureType() ) ) {

                        if ( featureTag.hasFeatureDetection() && feature.hasFeatureDetection() ) {

                            if ( !featureTag.getFeatureDetection().equals( feature.getFeatureDetection() ) ) {

                                throw new IllegalArgumentException( "FeatureTag having same clusterID(" + id +
                                                                    ") but different feature detection" );
                            }
                        } else {
                            if ( featureTag.hasFeatureDetection() ) {

                                throw new IllegalArgumentException( "FeatureTag having same clusterID(" + id +
                                                                    ") but different feature detection" );

                            } else if ( feature.hasFeatureDetection() ) {

                                throw new IllegalArgumentException( "FeatureTag having same clusterID(" + id +
                                                                    ") but different feature detection" );
                            }
                        }
                    } else {

                        // if feature have different feature type, they should belong to different cluster.

                        throw new IllegalArgumentException( "FeatureTag having same clusterID(" + id +
                                                            ") but different feature type" );
                    }
                }
            }

            features.add( feature );
        }

        // reminder, the cluster ID: null means that no ID have been given in the PSI file.

        Collection features = (Collection) map.get( null );
        if ( null != features ) {
            // we need to split that category into many having distinct (random) CLUSTER IDs
            int count = 1;
            for ( Iterator iterator = features.iterator(); iterator.hasNext(); ) {
                FeatureTag featureTag = (FeatureTag) iterator.next();

                while ( map.keySet().contains( FeatureTag.FEATURE_CLUSTER_ID_PREFIX + count ) ) {
                    count++;
                }

                // we've found a key that doesn't exists yet !
                Collection newFeatures = new ArrayList( 1 );
                newFeatures.add( featureTag );
                map.put( ( FeatureTag.FEATURE_CLUSTER_ID_PREFIX + count ), newFeatures );
                iterator.remove();
                count++;
            }

            map.remove( null );
        }

        return map;
    }

    
    ////////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof ProteinParticipantTag ) ) {
            return false;
        }

        final ProteinParticipantTag proteinParticipantTag = (ProteinParticipantTag) o;

        if ( expressedIn != null ? !expressedIn.equals( proteinParticipantTag.expressedIn ) : proteinParticipantTag.expressedIn != null ) {
            return false;
        }
        if ( features != null ? !features.equals( proteinParticipantTag.features ) : proteinParticipantTag.features != null ) {
            return false;
        }
        if ( isOverExpressedProtein != null ? !isOverExpressedProtein.equals( proteinParticipantTag.isOverExpressedProtein ) : proteinParticipantTag.isOverExpressedProtein != null ) {
            return false;
        }
        if ( isTaggedProtein != null ? !isTaggedProtein.equals( proteinParticipantTag.isTaggedProtein ) : proteinParticipantTag.isTaggedProtein != null ) {
            return false;
        }
        if ( !proteinInteractor.equals( proteinParticipantTag.proteinInteractor ) ) {
            return false;
        }
        if ( !role.equals( proteinParticipantTag.role ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = proteinInteractor.hashCode();
        result = 29 * result + role.hashCode();
        result = 29 * result + ( expressedIn != null ? expressedIn.hashCode() : 0 );
        result = 29 * result + ( features != null ? features.hashCode() : 0 );
        result = 29 * result + ( isTaggedProtein != null ? isTaggedProtein.hashCode() : 0 );
        result = 29 * result + ( isOverExpressedProtein != null ? isOverExpressedProtein.hashCode() : 0 );
        return result;
    }


    ////////////////////////
    // Display

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append( "ProteinParticipantTag{" );
        sb.append( "expressedIn=" ).append( expressedIn );
        sb.append( ", proteinInteractor=" ).append( proteinInteractor );
        sb.append( ", isOverExpressedProtein=" ).append( ( isOverExpressedProtein == null ? "not specified" : isOverExpressedProtein.toString() ) );
        sb.append( ", isTaggedProtein=" ).append( ( isTaggedProtein == null ? "not specified" : isTaggedProtein.toString() ) );
        sb.append( ", role='" ).append( role ).append( '\'' );
        sb.append( ", features=" );
        for ( Iterator iterator = features.iterator(); iterator.hasNext(); ) {
            FeatureTag featureTag = (FeatureTag) iterator.next();
            sb.append( featureTag ).append( ',' ).append( ' ' );
        }
        sb.append( "}" );

        return sb.toString();
    }
}
