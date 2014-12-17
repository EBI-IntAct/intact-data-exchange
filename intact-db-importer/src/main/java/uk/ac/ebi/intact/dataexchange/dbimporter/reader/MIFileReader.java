package uk.ac.ebi.intact.dataexchange.dbimporter.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import psidev.psi.mi.jami.model.Interaction;

/**
 * Interface for MIFile readers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/12/14</pre>
 */

public interface MIFileReader<I extends Interaction> extends ItemReader<I>, ItemStream {
}
