/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * That class reflects what is needed to create an IntAct <code>Feature</code>.
 * <p/>
 * <pre>
 *      &lt;feature &gt;
 *          &lt;xref &gt;
 *              &lt;primaryRef db="interpro" id="IPR001977" secondary="Depp_CoAkinase"/&gt;
 *          &lt;/xref&gt;
 * <p/>
 *          &lt;featureDescription &gt;
 *              &lt;names &gt;
 *                  &lt;shortLabel &gt;my feature&lt;/shortLabel&gt;
 *              &lt;/names&gt;
 *              &lt;xref &gt;
 *                  &lt;primaryRef db="psi-mi" id="PSI_MI" secondary="formylation reaction"/&gt;
 *              &lt;/xref&gt;
 *          &lt;/featureDescription&gt;
 * <p/>
 *          &lt;location &gt;
 *              &lt;beginInterval begin="2" end="5"/&gt;
 *              &lt;end position="9"/&gt;
 *          &lt;/location&gt;
 * <p/>
 *          &lt;featureDetection &gt;
 *              &lt;names &gt;
 *                  &lt;shortLabel &gt;western blot&lt;/shortLabel&gt;
 *              &lt;/names&gt;
 *              &lt;xref &gt;
 *                  &lt;primaryRef db="psi-mi" id="MI:0113"/&gt;
 *              &lt;/xref&gt;
 *          &lt;/featureDetection&gt;
 *      &lt;/feature&gt;
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.model.Feature
 * @see uk.ac.ebi.intact.model.Range
 */
public class FeatureTag {

    public static final String FEATURE_CLUSTER_ID_XREF = "location_clusterID";
    public static final String FEATURE_CLUSTER_ID_PREFIX = "_AUTO_ASSIGNED_CLUSTER_ID_";

    ////////////////////////
    // Instance variables

    private String shortlabel;
    private String fullname;
    private final Collection xrefs;

    private final FeatureTypeTag featureType; // map to CvFeatureIdentification
    private final FeatureDetectionTag featureDetection; // map to CvFeatureIdentification

    private final LocationTag location;

    // TODO put a Collection of LocationTag or add a method that generated it afterward
    // if Collection then
    //    I have to read the features and then to build an other one 



    ///////////////////////////
    // Constructor

    public FeatureTag( String shortlabel, String fullname,
                       FeatureTypeTag featureType,
                       LocationTag location,
                       FeatureDetectionTag featureDetection,
                       Collection xrefs ) {

        if ( featureType == null ) {
            throw new IllegalArgumentException( "You must give a non null featureType for a feature" );
        }

        // TODO can't I create feature without location ?
        if ( location == null ) {
            throw new IllegalArgumentException( "You must give a non null location for a feature" );
        }

        if ( xrefs == null ) {
            this.xrefs = new ReadOnlyCollection( new ArrayList( 0 ) );
        } else {
            // check the collection content
            for ( Iterator iterator = xrefs.iterator(); iterator.hasNext(); ) {
                Object o = (Object) iterator.next();
                if ( !( o instanceof XrefTag ) ) {
                    throw new IllegalArgumentException( "The annotation collection added to the experiment doesn't " +
                                                        "contains only XrefTag." );
                }
            }
            this.xrefs = new ReadOnlyCollection( xrefs );
        }

        if ( shortlabel != null ) {
            this.shortlabel = shortlabel.toLowerCase();
        }
        this.fullname = fullname;
        this.location = location;
        this.featureDetection = featureDetection;
        this.featureType = featureType;
    }


    ////////////////////////
    // Getters

    public Collection getXrefs() {
        return xrefs;
    }

    public boolean hasFeatureDetection() {
        return featureDetection != null;
    }

    public FeatureDetectionTag getFeatureDetection() {
        return featureDetection;
    }

    public FeatureTypeTag getFeatureType() {
        return featureType;
    }

    public String getFullname() {
        return fullname;
    }

    public LocationTag getLocation() {
        return location;
    }

    public String getShortlabel() {
        return shortlabel;
    }


    ////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof FeatureTag ) ) {
            return false;
        }

        final FeatureTag featureTag = (FeatureTag) o;

        if ( xrefs != null ? !xrefs.equals( featureTag.xrefs ) : featureTag.xrefs != null ) {
            return false;
        }
        if ( featureDetection != null ? !featureDetection.equals( featureTag.featureDetection ) : featureTag.featureDetection != null ) {
            return false;
        }
        if ( !featureType.equals( featureTag.featureType ) ) {
            return false;
        }
        if ( fullname != null ? !fullname.equals( featureTag.fullname ) : featureTag.fullname != null ) {
            return false;
        }
        if ( !location.equals( featureTag.location ) ) {
            return false;
        }
        if ( shortlabel != null ? !shortlabel.equals( featureTag.shortlabel ) : featureTag.shortlabel != null ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = 29 + ( shortlabel != null ? shortlabel.hashCode() : 0 );
        result = 29 * result + ( fullname != null ? fullname.hashCode() : 0 );
        result = 29 * result + ( xrefs != null ? xrefs.hashCode() : 0 );
        result = 29 * result + featureType.hashCode();
        result = 29 + ( featureDetection != null ? featureDetection.hashCode() : 0 );
        result = 29 * result + location.hashCode();
        return result;
    }


    /////////////////////
    // Display

    public String toString() {

        StringBuffer sb = new StringBuffer( 128 );
        sb.append( "FeatureTag{" );
        sb.append( "descriptionXrefs=" );
        if ( xrefs == null || xrefs.isEmpty() ) {
            sb.append( "none" );
        } else {
            sb.append( '{' );
            for ( Iterator iterator = xrefs.iterator(); iterator.hasNext(); ) {
                XrefTag xref = (XrefTag) iterator.next();
                sb.append( xref );
                if ( iterator.hasNext() ) {
                    sb.append( ',' );
                }
            }
            sb.append( '}' );
        }
        sb.append( ", shortlabel='" + shortlabel + "'" );
        sb.append( ", fullname='" + fullname + "'" );
        sb.append( ", featureType=" + featureType );
        sb.append( ", featureDetection=" + featureDetection );
        sb.append( ", location=" + location );
        sb.append( '}' );

        return sb.toString();
    }
}