/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

/**
 * That class reflects what is needed to create an IntAct <code>CvCellType</code>.
 * <p/>
 * <pre>
 *      &lt;hostOrganism ncbiTaxId="4932"&gt;
 *          &lt;names&gt;
 *              &lt;shortLabel&gt;s cerevisiae&lt;/shortLabel&gt;
 *              &lt;fullName&gt;Saccharomyces cerevisiae&lt;/fullName&gt;
 *          &lt;/names&gt;
 *          &lt;cellType&gt;
 *              &lt;xref&gt;
 *                  &lt;primaryRef db="psi-mi" id="MI:xxx" secondary="" version=""/&gt;
 *              &lt;/xref&gt;
 *          &lt;/cellType&gt;
 *      &lt;/hostOrganism&gt;
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.model.CvCellType
 */
public class CellTypeTag {

    private String shortlabel;

    private final XrefTag psiDefinition;


    ///////////////////////
    // Constructors

    public CellTypeTag( final XrefTag psiDefinition, final String shortlabel ) {

        if ( shortlabel == null || shortlabel.trim().equals( "" ) ) {
            throw new IllegalArgumentException( "You must give a non null/empty shortlabel for a cellType" );
        }

        // TODO re-activate this when we will have PSI id for cell Type
//        if( psiDefinition == null ) {
//            throw new IllegalArgumentException( "You must give a non null psi definition for a cellType" );
//        }

//        if( !CvDatabase.PSI_MI.equals( psiDefinition.getDb() ) ) {
//            throw new IllegalArgumentException( "You must give a psi-mi Xref, not " + psiDefinition.getDb() +
//                                                " for a cellType" );
//        }

        this.psiDefinition = psiDefinition;
        this.shortlabel = shortlabel;
    }


    /////////////////////////
    // Getter

    public XrefTag getPsiDefinition() {
        return psiDefinition;
    }

    public String getShortlabel() {
        return shortlabel;
    }


    ///////////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof CellTypeTag ) ) {
            return false;
        }

        final CellTypeTag cellTypeTag = (CellTypeTag) o;

        if ( !psiDefinition.equals( cellTypeTag.psiDefinition ) ) {
            return false;
        }
        if ( shortlabel != null ? !shortlabel.equals( cellTypeTag.shortlabel ) : cellTypeTag.shortlabel != null ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = ( shortlabel != null ? shortlabel.hashCode() : 0 );
        result = 29 * result + psiDefinition.hashCode();
        return result;
    }


    public String toString() {
        return "CellTypeTag{" +
               "psiDefinition=" + psiDefinition +
               ", shortlabel='" + shortlabel + "'" +
               "}";
    }
}