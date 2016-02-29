// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.util.uniprotExport;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class CcLine implements Comparable {

    private final String ccLine;
    private final String geneName;
    private final String uniprotID;

    public CcLine( String ccLine, String geneName, String uniprotID ) {

        this.ccLine = ccLine;
        this.geneName = geneName;
        this.uniprotID = uniprotID;
    }

    public String getCcLine() {
        return ccLine;
    }

    public String getGeneName() {
        return geneName;
    }

    public String getUniprotID() {
        return uniprotID;
    }

    /**
     * Compare two CC lines and more particularly the gene names.
     * The ordering is based after the following rules:
     * <pre>
     *    - If a protein interacts with itself, the gene name is 'Self' which appears always first.
     *    - In other cases, we do first an non case sensitive comparison of the gene name,
     *                      if there are differences, keep that order.
     *                      if there are NO differences, perform a case sensitive comparison.
     *                      2005-10-20: if gene names are equals, order bu UniProt ID
     *
     * </pre>
     *
     * @param o
     * @return
     */
    public int compareTo( Object o ) {
        CcLine cc2 = null;
        cc2 = (CcLine) o;

        final String gene1 = getGeneName();
        final String gene2 = cc2.getGeneName();

        // the current string comes first if it's before in the alphabetical order

        if( gene1 == null ) {
            System.out.println( this );
        }

        if( gene1.equals( "Self" ) ) {

            // we put first the Self interaction
            return -1;

        } else if( gene2.equals( "Self" ) ) {

            return 1;

        } else {

            String lovercaseGene1 = gene1.toLowerCase();
            String lovercaseGene2 = gene2.toLowerCase();

            // TODO ask Elizabeth if we still need to do the upper AND lowercase check for gene-name

            int score = lovercaseGene1.compareTo( lovercaseGene2 );

            if( score == 0 ) {
                score = gene1.compareTo( gene2 );

                if( score == 0 ) {
                    // gene names are the same, then compare the uniprotID
                    String uniprotID1 = getUniprotID();
                    String uniprotID2 = cc2.getUniprotID();

                    if( uniprotID1 != null && uniprotID2 != null ) {
                        score = uniprotID1.compareTo( uniprotID2 );
                    }
                }
            }

            return score;
        }
    }


    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        CcLine ccLine1 = (CcLine) o;

        if (ccLine != null ? !ccLine.equals(ccLine1.ccLine) : ccLine1.ccLine != null)
        {
            return false;
        }
        if (geneName != null ? !geneName.equals(ccLine1.geneName) : ccLine1.geneName != null)
        {
            return false;
        }
        if (uniprotID != null ? !uniprotID.equals(ccLine1.uniprotID) : ccLine1.uniprotID != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (ccLine != null ? ccLine.hashCode() : 0);
        result = 31 * result + (geneName != null ? geneName.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "CcLine{" +
               "ccLine='" + ccLine + "'" +
               ", geneName='" + geneName + "'" +
               "}";
    }
}