package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

/**
 * Defines how PSI-MI XML data s hould be exported in specific context.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public interface ExportProfile {

    public void configure( ConverterContext context );
    
}
