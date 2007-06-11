/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.trembl;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.model.ProteinImpl;
import uk.ac.ebi.intact.util.Crc64;
import uk.ac.ebi.intact.util.SearchReplace;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Batch convertion of IntAct protein into TrEMBL flat file.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05-Oct-2005</pre>
 */
public class ExportToTrEMBL {

    public static final String TREMBL_RELEASE_DATE = "${TREMBL_RELEASE_DATE}";
    public static final String TREMBL_RELEASE_NUMBER = "${TREMBL_RELEASE_NUMBER}";

    public static final String R_LINE_REFERENCE_POSITION = "${REFERENCE_POSITION}";
    public static final String R_LINE_PUBMED = "${PUBMED_ID}";
    public static final String R_LINE_TISSUE = "${TISSUE}";
    public static final String R_LINE_REFERENCE_AUTHOR = "${REFERENCE_AUTHORS}";
    public static final String R_LINE_REFERENCE_TITLE = "${REFERENCE_TITLE}";
    public static final String R_LINE_REFERENCE_LINE = "${REFERENCE_LINE}";

    public static final String NCBI_TAXID = "${NCBI_TAXID}";
    public static final String ORGANISM_NAME = "${ORGANISM NAME}";

    public static final String PROTEIN_AC = "${PROTEIN_AC}";
    public static final String PROTEIN_ID = "${PROTEIN_ID}";
    public static final String PROTEIN_FULLNAME = "${PROTEIN_FULLNAME}";
    public static final String PROTEIN_CREATION_DATE = "${PROTEIN_CREATION_DATE}";
    public static final String PROTEIN_SEQUENCE = "${SEQUENCE}";
    public static final String PROTEIN_SEQUENCE_LENGTH = "${SEQUENCE_LENGTH}";
    public static final String PROTEIN_CRC64 = "${CRC64}";

    private static final String NEW_LINE = System.getProperty( "line.separator" );
    public static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat( "dd-MMM-yyyy" );

    public static final String
            TREMBL_TEMPLATE = "ID   "+PROTEIN_ID+"_"+ ORGANISM_NAME +"     PRELIMINARY;   PRT;  " + PROTEIN_SEQUENCE_LENGTH + " AA." + NEW_LINE +
                              "AC   "+PROTEIN_ID+";" + NEW_LINE +
                              "DT   " + TREMBL_RELEASE_DATE + " (TrEMBLrel. " + TREMBL_RELEASE_NUMBER + ", Created)" + NEW_LINE +
                              "DT   " + TREMBL_RELEASE_DATE + " (TrEMBLrel. " + TREMBL_RELEASE_NUMBER + ", Last sequence update)" + NEW_LINE +
                              "DT   " + TREMBL_RELEASE_DATE + " (TrEMBLrel. " + TREMBL_RELEASE_NUMBER + ", Last annotation update)" + NEW_LINE +
                              "DE   " + PROTEIN_FULLNAME + "{EI1}." + NEW_LINE +
                              "OS   Mus musculus (Mouse)." + NEW_LINE +
                              "OC   Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi;" + NEW_LINE +
                              "OC   Mammalia; Eutheria; Euarchontoglires; Glires; Rodentia; Sciurognathi;" + NEW_LINE +
                              "OC   Muroidea; Muridae; Murinae; Mus."  + NEW_LINE +
                              "OX   NCBI_TaxID=" + NCBI_TAXID + ";" + NEW_LINE +
                              "RN   [1]{EI1}" + NEW_LINE +
                              "RP   " + R_LINE_REFERENCE_POSITION + "." + NEW_LINE +
                              "RC   TISSUE=" + R_LINE_TISSUE + "{EI1};" + NEW_LINE +
                              "RX   PubMed="+ R_LINE_PUBMED +";" + NEW_LINE +
                              "RA   " + R_LINE_REFERENCE_AUTHOR + ";" + NEW_LINE +
                              "RT   \"" + R_LINE_REFERENCE_TITLE + ".\";" + NEW_LINE +
                              "RL   " + R_LINE_REFERENCE_LINE + "." + NEW_LINE +
                              "**" + NEW_LINE +
                              "**   #################    INTERNAL SECTION    ##################" + NEW_LINE +
                              "**EV EI1; IntAct; -; " + PROTEIN_AC + "; " + PROTEIN_CREATION_DATE + "." + NEW_LINE +
                              "SQ   SEQUENCE   " + PROTEIN_SEQUENCE_LENGTH + " AA;  1 MW;  " + PROTEIN_CRC64 + " CRC64;" + NEW_LINE +
                              "     " + PROTEIN_SEQUENCE + NEW_LINE +
                              "//";

    public static final int COLUMN_LENGTH = 10;
    public static final int COLUMN_COUNT = 6;

    private ExportToTrEMBL()
    {
        // so this class is never instantiated
    }

    //////////////////////////
    // public methods

    /**
     * Tramsform an IntAct protein into a TrEMBL file.
     * <br>
     * We apply Search replace to the TrEMBL template.
     *
     * @param protein             the protein to transform.
     * @param tremblReleaseDate
     * @param tremblReleaseNumber
     * @param referencePosition
     * @param referenceTissue
     * @param referenceAuthor
     * @param referenceTitle
     * @param referenceLine
     *
     * @return the TrEMBL representation of the given IntAct protein.
     */
    public static String formatTremblEntry( Protein protein,
                                            String organismName,
                                            String tremblReleaseDate,
                                            String tremblReleaseNumber,
                                            String referencePosition,
                                            String pubmedId,
                                            String referenceTissue,
                                            String referenceAuthor,
                                            String referenceTitle,
                                            String referenceLine,
                                            String proteinID) {

        String tremblEntry = SearchReplace.replace( TREMBL_TEMPLATE, PROTEIN_SEQUENCE_LENGTH, "" + protein.getSequence().length() );

        tremblEntry = SearchReplace.replace( tremblEntry, PROTEIN_ID, proteinID );
        tremblEntry = SearchReplace.replace( tremblEntry, ORGANISM_NAME, organismName );
        tremblEntry = SearchReplace.replace( tremblEntry, TREMBL_RELEASE_DATE, tremblReleaseDate );
        tremblEntry = SearchReplace.replace( tremblEntry, TREMBL_RELEASE_NUMBER, tremblReleaseNumber );

        // todo remove last '.' if it exists
        tremblEntry = SearchReplace.replace( tremblEntry, PROTEIN_FULLNAME, protein.getFullName() );

        tremblEntry = SearchReplace.replace( tremblEntry, NCBI_TAXID, protein.getBioSource().getTaxId() );

        tremblEntry = SearchReplace.replace( tremblEntry, R_LINE_REFERENCE_POSITION, referencePosition );
        tremblEntry = SearchReplace.replace( tremblEntry, R_LINE_PUBMED, pubmedId );
        tremblEntry = SearchReplace.replace( tremblEntry, R_LINE_TISSUE, referenceTissue );
        tremblEntry = SearchReplace.replace( tremblEntry, R_LINE_REFERENCE_AUTHOR, referenceAuthor );
        tremblEntry = SearchReplace.replace( tremblEntry, R_LINE_REFERENCE_TITLE, referenceTitle );
        tremblEntry = SearchReplace.replace( tremblEntry, R_LINE_REFERENCE_LINE, referenceLine );

        tremblEntry = SearchReplace.replace( tremblEntry, PROTEIN_AC, protein.getAc() );

        // generate the creation date in the right format
        String time = DATE_FORMATER.format( new Date( protein.getCreated().getTime() ) );
        tremblEntry = SearchReplace.replace( tremblEntry, PROTEIN_CREATION_DATE, time.toUpperCase() );

        tremblEntry = SearchReplace.replace( tremblEntry, PROTEIN_CRC64, Crc64.getCrc64( protein.getSequence() ) );
        String formatedSequence = formatSequence( protein.getSequence() );
        tremblEntry = SearchReplace.replace( tremblEntry, PROTEIN_SEQUENCE, formatedSequence );

        return tremblEntry;
    }


    /**
     * Format the sequence into TrEMBL style.
     * <br>
     * chunk it in 6 columns of 10 AA each.
     *
     * @param sequence
     *
     * @return the formated sequence.
     */
    private static String formatSequence( String sequence ) {
        StringBuffer sb = new StringBuffer( 1024 );

        for ( int i = 0; i < sequence.length(); i++ ) {
            char aa = sequence.charAt( i );
            if ( i> 0 && ( i % COLUMN_LENGTH ) == 0 ) {

                if ( ( i % ( COLUMN_LENGTH * COLUMN_COUNT ) ) == 0 ) {
                    sb.append( NEW_LINE ).append( "     " );
                } else {
                    sb.append( ' ' );
                }
            }

            sb.append( aa );
        }

        // if the last char is NEW_LINE, remove it.
        if ( sb.charAt( sb.length() - 1 ) == NEW_LINE.toCharArray()[ 0 ] ) {
            System.out.println( "INFO: delete last NEW_LINE." );
            sb.deleteCharAt( sb.length() );
        }

        return sb.toString();
    }


    /**
     * M A I N
     */
    public static void main( String[] args ) throws IntactException {

            Collection<ProteinImpl> proteins = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().getByShortLabelLike("%afcs%");

            System.out.println( proteins.size() + " protein(s) found." );


            // TODO rt rc rx are not mandatory ... if the user input is empty, then remove those lines.

            String organismName = "MOUSE";
            String tremblReleaseDate = "24-JAN-2006";
            String tremblReleaseNumber = "32";
            String rp = "NUCLEOTIDE SEQUENCE [MRNA]";
            String pubmedId = "15102471";
            String rx = "B-cell";
            String ra = "Papin J., Subramaniam S.";
            String rt = "Bioinformatics and cellular signaling";
            String rl = "Curr. Opin. Biotechnol. 15:78-81(2004)";

            // TODO flat file loader
            List<String> proteinIDs = new ArrayList<String>( );
            proteinIDs.add( "P8R4B7");
            proteinIDs.add( "P8R4B6");
            proteinIDs.add( "P8R4B5");
            proteinIDs.add( "P8R4B4");
            proteinIDs.add( "P8R4B3");
            proteinIDs.add( "P8R4B2");
            proteinIDs.add( "P8R4B1");
            proteinIDs.add( "P8R4B0");
            proteinIDs.add( "P8R4A9");
            proteinIDs.add( "P8R4A8");
            proteinIDs.add( "P8R4A7");
            proteinIDs.add( "P8R4A6");
            proteinIDs.add( "P8R4A5");
            proteinIDs.add( "P8R4A4");
            proteinIDs.add( "P8R4A3");
            proteinIDs.add( "P8R4A2");
            proteinIDs.add( "P8R4A1");
            proteinIDs.add( "P8R4A0");
            proteinIDs.add( "P8R499");
            proteinIDs.add( "P8R498");
            proteinIDs.add( "P8R497");
            proteinIDs.add( "P8R496");
            proteinIDs.add( "P8R495");
            proteinIDs.add( "P8R494");
            proteinIDs.add( "P8R493");

            for (Protein protein : proteins)
            {
                String proteinID = "";
                if (! proteinIDs.isEmpty())
                {
                    Iterator it = proteinIDs.iterator();
                    proteinID = (String) it.next();
                    it.remove();
                }

                System.out.println(formatTremblEntry(protein,
                                                     organismName,
                                                     tremblReleaseDate,
                                                     tremblReleaseNumber,
                                                     rp,
                                                     pubmedId,
                                                     rx,
                                                     ra,
                                                     rt,
                                                     rl,
                                                     proteinID));
            }
    }
}