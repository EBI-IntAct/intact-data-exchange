package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Publication;

import java.util.Collection;

/**
 * Experiment converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/05/12</pre>
 */

public class ExperimentConverter {

    private BioSourceConverter biosourceConverter;
    private CvObjectConverter cvObjectConverter;
    private PublicationConverter publicationConverter;

    public ExperimentConverter(){
        this.biosourceConverter = new BioSourceConverter();
        this.cvObjectConverter = new CvObjectConverter();
        this.publicationConverter = new PublicationConverter();
    }

    public void intactToCalimocho(Experiment exp, Row row){

        if (exp != null){
            // process publication
            Publication pub = exp.getPublication();
            if (pub != null){
                publicationConverter.intactToCalimocho(pub, row);
            }

            // convert interaction detection method
            if (exp.getCvInteraction() != null){
                Field detMethod = cvObjectConverter.intactToCalimocho(exp.getCvInteraction());

                if (detMethod != null){
                    row.addField(InteractionKeys.KEY_DETMETHOD, detMethod);
                }
            }

            // process organism
            if (exp.getBioSource() != null){
                Collection<Field> bioSourceField = biosourceConverter.intactToCalimocho(exp.getBioSource());

                if (!bioSourceField.isEmpty()){
                    row.addFields(InteractionKeys.KEY_HOST_ORGANISM, bioSourceField);
                }
            }

            // process participant detection method
            if (exp.getCvIdentification() != null){
                Field detMethod = cvObjectConverter.intactToCalimocho(exp.getCvIdentification());

                if (detMethod != null){
                    row.addField(InteractionKeys.KEY_PART_IDENT_METHOD_A, detMethod);
                    row.addField(InteractionKeys.KEY_PART_IDENT_METHOD_B
                            , detMethod);
                }
            }
        }

    }
}
