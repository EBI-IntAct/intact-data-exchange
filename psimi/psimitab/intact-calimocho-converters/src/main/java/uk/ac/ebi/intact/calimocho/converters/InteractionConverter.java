package uk.ac.ebi.intact.calimocho.converters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.DefaultRow;
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import psidev.psi.mi.tab.utils.MitabEscapeUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.psimitab.converters.expansion.NotExpandableInteractionException;
import uk.ac.ebi.intact.psimitab.converters.expansion.SpokeWithoutBaitExpansion;

import java.util.*;

/**
 * This converter will convert an Interaction in/from a list of Rows for calimocho
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/05/12</pre>
 */

public class InteractionConverter {

    private ExpansionStrategy expansionStrategy;
    private static final Log log = LogFactory.getLog(InteractionConverter.class);
    private InteractorConverter interactorConverter;
    private PublicationConverter publicationConverter;
    private CvObjectConverter cvObjectConverter;
    private ConfidenceConverter confidenceConverter;

    public InteractionConverter(ExpansionStrategy strategy){
        this.expansionStrategy = strategy;
        if (this.expansionStrategy == null){
            this.expansionStrategy = new SpokeWithoutBaitExpansion();
        }
        this.interactorConverter = new InteractorConverter();
        this.cvObjectConverter = new CvObjectConverter();
        this.confidenceConverter = new ConfidenceConverter();
    }

    public InteractionConverter(){
        this.expansionStrategy = new SpokeWithoutBaitExpansion();
        this.interactorConverter = new InteractorConverter();
        this.publicationConverter = new PublicationConverter();
    }

    public List<Row> toCalimocho(Interaction interaction) throws NotExpandableInteractionException {

        if ( interaction == null ) {
            throw new IllegalArgumentException( "Interaction must not be null" );
        }

        List<Row> rows = new ArrayList<Row>();

        // case of intra molecular interactions
        if (interaction.getComponents().size() == 1) {
            Row row = new DefaultRow();

            Component c = interaction.getComponents().iterator().next();

            if (c.getStoichiometry() < 2){
                interactorConverter.toCalimocho(c, row, true);
            }

            rows.add(row);
        }
        else {
            if (!expansionStrategy.isExpandable(interaction)) {
                if (log.isWarnEnabled()) log.warn("Filtered interaction: "+interaction.getAc()+" (not expandable)");
                return Collections.EMPTY_LIST;
            }

            Collection<Interaction> interactions = expansionStrategy.expand(interaction);

            boolean expanded = false;

            // if we have more than one interaction, it means that we have spoke expanded interactions
            if (interactions != null && interactions.size() > 1) {

                expanded = true;
            }
            else if (interactions == null){
                return Collections.EMPTY_LIST;
            }

            if (interactions.isEmpty()) {
                if (log.isErrorEnabled()) {
                    log.error("Expansion did not generate any interaction for: "+interaction);
                    return Collections.EMPTY_LIST;
                }
            }

            for (Interaction binary : interactions){

                Row row = processBinaryInteraction(binary);
                if (row != null){
                    rows.add(row);
                }
            }

            // add expansion
            if (expanded){

            }

        }

        return rows;
    }

    private Row processBinaryInteraction(Interaction binary, boolean isExpanded) {
        Row row = new DefaultRow();
        final Collection<Component> components = binary.getComponents();
        Iterator<Component> iterator = components.iterator();
        final Component componentA = iterator.next();
        final Component componentB = iterator.next();

        // convert interactors
        interactorConverter.toCalimocho(componentA, row, true);
        interactorConverter.toCalimocho(componentB, row, false);

        // process interaction type
        if (binary.getCvInteractionType() != null){
            Field type = cvObjectConverter.toCalimocho(binary.getCvInteractionType());

            if (type != null){
                row.addField(InteractionKeys.KEY_INTERACTION_TYPE, type);
            }
        }

        // convert confidences
        if (!binary.getConfidences().isEmpty()){
           Collection<Field> confs = new ArrayList<Field>(binary.getConfidences().size());

            for (Confidence conf : binary.getConfidences()){
                Field confField = confidenceConverter.toCalimocho(conf);

                if (confField != null){
                    row.addField(InteractionKeys.KEY_CONFIDENCE, confField);
                }
            }
        }

        // process AC
        if (binary.getAc() != null){
            Field id = new DefaultField();

            id.set(CalimochoKeys.KEY, CvDatabase.INTACT);
            id.set(CalimochoKeys.DB, CvDatabase.INTACT);
            id.set(CalimochoKeys.VALUE, binary.getAc());

            row.addField(InteractionKeys.KEY_INTERACTION_ID, id);
        }

        // process complex expansion
        if (isExpanded && expansionStrategy != null){
            Field field = new DefaultField();

            String db = CrossReferenceConverter.DATABASE_UNKNOWN;
            if (expansionStrategy.){
                db= MitabEscapeUtils.escapeFieldElement(ref.getCvDatabase().getShortLabel());
            }

            field.set( CalimochoKeys.KEY, db);
            field.set( CalimochoKeys.DB, db);
            field.set( CalimochoKeys.VALUE, MitabEscapeUtils.escapeFieldElement(ref.getPrimaryId()));

        }
        else if (isExpanded && expansionStrategy == null){
           throw new IllegalArgumentException("Interaction is expanded but no expansion strategy has been set.");
        }

        // process experiments
        for (Experiment exp : binary.getExperiments()){
            // process publication
            Publication pub = exp.getPublication();
            if (pub != null){
                publicationConverter.toCalimocho(pub, row);
            }

            // convert interaction detection method
            if (exp.getCvInteraction() != null){
                Field detMethod = cvObjectConverter.toCalimocho(exp.getCvInteraction());

                if (detMethod != null){
                    row.addField(InteractionKeys.KEY_DETMETHOD, detMethod);
                }
            }
        }

        return row;
    }

}
