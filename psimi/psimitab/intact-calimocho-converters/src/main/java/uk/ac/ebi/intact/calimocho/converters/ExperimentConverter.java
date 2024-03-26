package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import psidev.psi.mi.jami.model.Publication;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactExperiment;
import uk.ac.ebi.intact.jami.model.extension.IntactOrganism;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;

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

    public void intactToCalimocho(IntactExperiment exp, Row row){

        if (exp != null){
            // process publication
            Publication pub = exp.getPublication();
            if (pub != null){
                publicationConverter.intactToCalimocho((IntactPublication) pub, row);
            }

            // convert interaction detection method
            if (exp.getInteractionDetectionMethod() != null){
                Field detMethod = cvObjectConverter.intactToCalimocho((IntactCvTerm) exp.getInteractionDetectionMethod());

                if (detMethod != null){
                    row.addField(InteractionKeys.KEY_DETMETHOD, detMethod);
                }
            }

            // process organism
            if (exp.getHostOrganism() != null){
                Collection<Field> bioSourceField = biosourceConverter.intactToCalimocho((IntactOrganism) exp.getHostOrganism());

                if (!bioSourceField.isEmpty()){
                    row.addFields(InteractionKeys.KEY_HOST_ORGANISM, bioSourceField);
                }
            }

            // process participant detection method
            if (exp.getParticipantIdentificationMethod() != null){
                Field detMethod = cvObjectConverter.intactToCalimocho((IntactCvTerm) exp.getParticipantIdentificationMethod());

                if (detMethod != null){
                    row.addField(InteractionKeys.KEY_PART_IDENT_METHOD_A, detMethod);
                    row.addField(InteractionKeys.KEY_PART_IDENT_METHOD_B
                            , detMethod);
                }
            }
        }

    }
}
