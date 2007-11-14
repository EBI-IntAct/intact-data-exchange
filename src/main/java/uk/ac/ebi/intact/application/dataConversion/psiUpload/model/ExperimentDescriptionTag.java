/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyCollection;
import uk.ac.ebi.intact.model.CvDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * That class reflects what is needed to create an IntAct <code>Experiment</code> (non including interactions).
 * <p/>
 * <pre>
 *     &lt;experimentDescription id="EBI-12"&gt;
 *         &lt;names&gt;
 *             &lt;shortLabel&gt;gavin-2002&lt;/shortLabel&gt;
 *             &lt;fullName&gt;Functional organization of the yeast
 *            proteome by systematic analysis of protein complexes.&lt;/fullName&gt;
 *         &lt;/names&gt;
 *         &lt;bibref&gt;
 *             &lt;xref&gt;
 *            &lt;primaryRef db="pubmed" id="11805826" secondary="" version=""/&gt;
 *             &lt;/xref&gt;
 *         &lt;/bibref&gt;
 *         &lt;hostOrganism ncbiTaxId="4932"&gt;
 *             &lt;names&gt;
 *            &lt;shortLabel&gt;s cerevisiae&lt;/shortLabel&gt;
 *            &lt;fullName&gt;Saccharomyces cerevisiae&lt;/fullName&gt;
 *             &lt;/names&gt;
 *         &lt;/hostOrganism&gt;
 *         &lt;interactionDetection&gt;
 *             &lt;names&gt;
 *            &lt;shortLabel&gt;tandem affinity puri&lt;/shortLabel&gt;
 *            &lt;fullName&gt;tandem affinity purification&lt;/fullName&gt;
 *             &lt;/names&gt;
 *             &lt;xref&gt;
 *            &lt;primaryRef db="pubmed" id="10504710" secondary="" version=""/&gt;
 *            &lt;secondaryRef db="psi-mi" id="MI:0109" secondary="" version=""/&gt;
 *             &lt;/xref&gt;
 *         &lt;/interactionDetection&gt;
 *         &lt;participantDetection&gt;
 *             &lt;names&gt;
 *            &lt;shortLabel&gt;peptide massfingerpr&lt;/shortLabel&gt;
 *            &lt;fullName&gt;peptide massfingerprinting&lt;/fullName&gt;
 *             &lt;/names&gt;
 *             &lt;xref&gt;
 *            &lt;primaryRef db="pubmed" id="11752590" secondary="" version=""/&gt;
 *            &lt;secondaryRef db="psi-mi" id="MI:0082" secondary="" version=""/&gt;
 *            &lt;secondaryRef db="pubmed" id="10967324" secondary="" version=""/&gt;
 *             &lt;/xref&gt;
 *         &lt;/participantDetection&gt;
 *     &lt;/experimentDescription&gt;
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.model.Experiment
 */
public final class ExperimentDescriptionTag {

    private final String shortlabel;
    private final String fullname;

    private final XrefTag bibRef;
    private final Collection additionalBibRef;
    private final HostOrganismTag hostOrganism;
    private final InteractionDetectionTag interactionDetection;
    private final ParticipantDetectionTag participantDetection;

    /**
     * Reflects <xref> Collection of XrefTag
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag
     */
    private final Collection xrefs;

    /**
     * Reflects <attributeList> Collection of AnnotationTag
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.AnnotationTag
     */
    private final Collection annotations;

    ///////////////////////
    // Constructors

    public ExperimentDescriptionTag( final String shortlabel,
                                     final String fullname,
                                     final XrefTag bibRef,
                                     final Collection additionalBibRef,
                                     final Collection xrefs,
                                     final Collection annotations,
                                     final HostOrganismTag hostOrganism,
                                     final InteractionDetectionTag interactionDetection,
                                     final ParticipantDetectionTag participantDetection ) {

        if ( shortlabel == null || shortlabel.trim().equals( "" ) ) {
            throw new IllegalArgumentException( "You must give a non null/empty shortlabel for an experimentDescription" );
        }

//        if ( fullname == null || fullname.trim().equals( "" ) ) {
//            throw new IllegalArgumentException( "You must give a non null/empty fullname for an experimentDescription" );
//        }


        // TODO make a switch for the mandatory-ness of that value !!!
        // TODO Because of people submiting their data without having a pubmed ID 
        if ( bibRef == null ) {
            throw new IllegalArgumentException( "You must give a non null bibRef for an experimentDescription" );
        }

        if ( !CvDatabase.PUBMED.equals( bibRef.getDb() ) ) {
            throw new IllegalArgumentException( "You must give a pubmed Xref, not " + bibRef.getDb() +
                                                " for an experimentDescription" );
        }

        if ( hostOrganism == null ) {
            throw new IllegalArgumentException( "You must give a non null hostOrganism for an experimentDescription" );
        }

        if ( interactionDetection == null ) {
            throw new IllegalArgumentException( "You must give a non null interactionDetection for an experimentDescription" );
        }

        if ( participantDetection == null ) {
            throw new IllegalArgumentException( "You must give a non null participantDetection for an experimentDescription" );
        }

        this.shortlabel = shortlabel.toLowerCase().trim();
        this.fullname = fullname;
        this.bibRef = bibRef;

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

        if ( additionalBibRef == null ) {
            this.additionalBibRef = new ReadOnlyCollection( new ArrayList( 0 ) );
        } else {
            // check the collection content
            for ( Iterator iterator = additionalBibRef.iterator(); iterator.hasNext(); ) {
                Object o = (Object) iterator.next();
                if ( !( o instanceof XrefTag ) ) {
                    throw new IllegalArgumentException( "The additionalBibRef collection added to the experiment doesn't " +
                                                        "contains only XrefTag." );
                }
            }
            this.additionalBibRef = new ReadOnlyCollection( additionalBibRef );
        }

        if ( annotations == null ) {
            this.annotations = new ReadOnlyCollection( new ArrayList( 0 ) );
        } else {
            // check the collection content
            for ( Iterator iterator = annotations.iterator(); iterator.hasNext(); ) {
                Object o = (Object) iterator.next();
                if ( !( o instanceof AnnotationTag ) ) {
                    throw new IllegalArgumentException( "The annotation collection added to the experiment doesn't " +
                                                        "contains only AnnotationTag." );
                }
            }
            this.annotations = new ReadOnlyCollection( annotations );
        }

        this.hostOrganism = hostOrganism;
        this.interactionDetection = interactionDetection;
        this.participantDetection = participantDetection;
    }


    ////////////////////////////
    // Getters

    public String getShortlabel() {
        return shortlabel;
    }

    public String getFullname() {
        return fullname;
    }

    public XrefTag getBibRef() {
        return bibRef;
    }

    public Collection getAdditionalBibRef() {
        return additionalBibRef;
    }

    public Collection getXrefs() {
        return xrefs;
    }

    public Collection getAnnotations() {
        return annotations;
    }

    public HostOrganismTag getHostOrganism() {
        return hostOrganism;
    }

    public InteractionDetectionTag getInteractionDetection() {
        return interactionDetection;
    }

    public ParticipantDetectionTag getParticipantDetection() {
        return participantDetection;
    }


    ////////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof ExperimentDescriptionTag ) ) {
            return false;
        }

        final ExperimentDescriptionTag experimentDescriptionTag = (ExperimentDescriptionTag) o;

        if ( annotations != null ? !annotations.equals( experimentDescriptionTag.annotations ) : experimentDescriptionTag.annotations != null ) {
            return false;
        }
        if ( additionalBibRef != null ? !additionalBibRef.equals( experimentDescriptionTag.additionalBibRef ) : experimentDescriptionTag.additionalBibRef != null ) {
            return false;
        }
        if ( !bibRef.equals( experimentDescriptionTag.bibRef ) ) {
            return false;
        }
        if ( !fullname.equals( experimentDescriptionTag.fullname ) ) {
            return false;
        }
        if ( !hostOrganism.equals( experimentDescriptionTag.hostOrganism ) ) {
            return false;
        }
        if ( !interactionDetection.equals( experimentDescriptionTag.interactionDetection ) ) {
            return false;
        }
        if ( !participantDetection.equals( experimentDescriptionTag.participantDetection ) ) {
            return false;
        }
        if ( !shortlabel.equals( experimentDescriptionTag.shortlabel ) ) {
            return false;
        }
        if ( xrefs != null ? !xrefs.equals( experimentDescriptionTag.xrefs ) : experimentDescriptionTag.xrefs != null ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = shortlabel.hashCode();
        result = 29 * result + fullname.hashCode();
        result = 29 * result + bibRef.hashCode();
        result = 29 * result + hostOrganism.hashCode();
        result = 29 * result + interactionDetection.hashCode();
        result = 29 * result + participantDetection.hashCode();
        result = 29 * result + ( xrefs != null ? xrefs.hashCode() : 0 );
        result = 29 * result + ( annotations != null ? annotations.hashCode() : 0 );
        result = 29 * result + ( additionalBibRef != null ? additionalBibRef.hashCode() : 0 );
        return result;
    }


    public String toString() {
        final StringBuffer buf = new StringBuffer();
        buf.append( "ExperimentDescriptionTag" );
        buf.append( "{shortlabel=" ).append( shortlabel );
        buf.append( ",fullname=" ).append( fullname );
        buf.append( ",bibRef=" ).append( bibRef );
        buf.append( ",additional bibref=" );
        if ( additionalBibRef.size() == 0 ) {
            buf.append( "none" );
        }
        for ( Iterator iterator = additionalBibRef.iterator(); iterator.hasNext(); ) {
            final XrefTag bibref = (XrefTag) iterator.next();
            buf.append( bibref );
        }

        buf.append( ",annotations=" );
        if ( annotations.size() == 0 ) {
            buf.append( "none" );
        }
        for ( Iterator iterator = annotations.iterator(); iterator.hasNext(); ) {
            final AnnotationTag annotation = (AnnotationTag) iterator.next();
            buf.append( annotation );
        }

        buf.append( ",xrefs=" );
        if ( xrefs.size() == 0 ) {
            buf.append( "none" );
        }
        for ( Iterator iterator = xrefs.iterator(); iterator.hasNext(); ) {
            final XrefTag xref = (XrefTag) iterator.next();
            buf.append( xref );
        }

        buf.append( ",hostOrganism=" ).append( hostOrganism );
        buf.append( ",interactionDetection=" ).append( interactionDetection );
        buf.append( ",participantDetection=" ).append( participantDetection );
        buf.append( '}' );
        return buf.toString();
    }
}
