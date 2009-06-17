package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

/**
 * Provides default configuration for exporting data to IMEx partners.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class ImexExportProfile implements ExportProfile {

    // Example of data filters:
    //  - CVs should only include shortlabel & xref(psi-mi / identity)
    //  - Proteins Xrefs should only be for CvDatabase: uniprot / genbank or a protein sequence database
    //  - no shortlabels/fullname/alias should be exported unless specificaly requested (eg. author-assigned-name on participant)
    //  - generate expanded XML

    public void configure( ConverterContext context ) {

        context.setGenerateExpandedXml( true );



    }
}
