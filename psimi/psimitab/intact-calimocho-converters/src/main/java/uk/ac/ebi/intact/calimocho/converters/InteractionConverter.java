package uk.ac.ebi.intact.calimocho.converters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.DefaultRow;
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.InstitutionUtils;
import uk.ac.ebi.intact.model.util.InteractionUtils;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.psimitab.converters.expansion.NotExpandableInteractionException;
import uk.ac.ebi.intact.psimitab.converters.expansion.SpokeWithoutBaitExpansion;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    private CvObjectConverter cvObjectConverter;
    private ConfidenceConverter confidenceConverter;
    private ExperimentConverter experimentConverter;
    private CrossReferenceConverter xrefConverter;
    private AnnotationConverter annotConverter;
    private ParameterConverter parameterConverter;
    private DateFormat dateFormat;
    private DateFormat yearFormat;
    private DateFormat monthFormat;
    private DateFormat dayFormat;

    public static String CRC = "intact-crc";

    private String expansionName;
    private String expansionMI;

    public InteractionConverter(ExpansionStrategy strategy, String expansionName, String expansionMI){
        this.expansionStrategy = strategy;
        if (this.expansionStrategy == null){
            this.expansionStrategy = new SpokeWithoutBaitExpansion();
        }
        this.interactorConverter = new InteractorConverter();
        this.cvObjectConverter = new CvObjectConverter();
        this.confidenceConverter = new ConfidenceConverter();
        this.experimentConverter = new ExperimentConverter();
        this.xrefConverter = new CrossReferenceConverter();
        this.annotConverter = new AnnotationConverter();
        this.parameterConverter = new ParameterConverter();

        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        yearFormat = new SimpleDateFormat("yyyy");
        monthFormat = new SimpleDateFormat("MM");
        dayFormat = new SimpleDateFormat("dd");

        this.expansionName = expansionName;
        this.expansionMI = expansionMI;
    }

    public List<Row> intactToCalimocho(Interaction interaction) throws NotExpandableInteractionException {

        if ( interaction == null ) {
            throw new IllegalArgumentException( "Interaction must not be null" );
        }

        List<Row> rows = new ArrayList<Row>();

        // case of intra molecular interactions and self interactions
        if (interaction.getComponents().size() == 1) {
            Component c = interaction.getComponents().iterator().next();

            if (c.getStoichiometry() < 2){
                Row row = processBinaryInteraction(interaction, false);

                interactorConverter.intactToCalimocho(c, row, true);
                rows.add(row);

                return rows;
            }
        }

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

            Row row = processBinaryInteraction(binary, expanded);

            if (row != null){
                final Collection<Component> components = binary.getComponents();
                Iterator<Component> iterator = components.iterator();
                final Component componentA = iterator.next();
                final Component componentB = iterator.next();

                // convert interactors
                interactorConverter.intactToCalimocho(componentA, row, true);
                interactorConverter.intactToCalimocho(componentB, row, false);
                rows.add(row);
            }
        }

        return rows;
    }

    private Row processBinaryInteraction(Interaction binary, boolean isExpanded) {
        Row row = new DefaultRow();

        // process interaction type
        if (binary.getCvInteractionType() != null){
            Field type = cvObjectConverter.intactToCalimocho(binary.getCvInteractionType());

            if (type != null){
                row.addField(InteractionKeys.KEY_INTERACTION_TYPE, type);
            }
        }

        // convert confidences
        if (!binary.getConfidences().isEmpty()){
            Collection<Field> confs = new ArrayList<Field>(binary.getConfidences().size());

            for (Confidence conf : binary.getConfidences()){
                Field confField = confidenceConverter.intactToCalimocho(conf);

                if (confField != null){
                    confs.add(confField);
                }
            }
            row.addFields(InteractionKeys.KEY_CONFIDENCE, confs);
        }

        // process AC
        if (binary.getAc() != null){
            Field id = new DefaultField();

            id.set(CalimochoKeys.KEY, CvDatabase.INTACT);
            id.set(CalimochoKeys.DB, CvDatabase.INTACT);
            id.set(CalimochoKeys.VALUE, binary.getAc());

            if (binary.getOwner() != null){
                Institution institution = binary.getOwner();

                CvDatabase database = InstitutionUtils.retrieveCvDatabase(IntactContext.getCurrentInstance(), institution);

                if (database != null && database.getShortLabel() != null){
                    id.set(CalimochoKeys.KEY, database.getShortLabel());
                    id.set(CalimochoKeys.DB, database.getShortLabel());
                }
            }

            row.addField(InteractionKeys.KEY_INTERACTION_ID, id);
        }

        // process complex expansion
        if (isExpanded && expansionMI != null){
            Field expansion = new DefaultField();

            String db = CvDatabase.PSI_MI;

            expansion.set(CalimochoKeys.KEY, db);
            expansion.set(CalimochoKeys.DB, db);
            expansion.set(CalimochoKeys.VALUE, expansionMI);

            if (expansionName != null){
                expansion.set(CalimochoKeys.TEXT, expansionName);
            }

            row.addField(InteractionKeys.KEY_CONFIDENCE, expansion);
        }
        else if (isExpanded && expansionMI == null){
            throw new IllegalArgumentException("Interaction is expanded but no expansion strategy has been set.");
        }

        // process experiments
        for (Experiment exp : binary.getExperiments()){
            experimentConverter.intactToCalimocho(exp, row);
        }

        //process xrefs
        Collection<InteractorXref> interactionRefs = binary.getXrefs();

        if (!interactionRefs.isEmpty()){
            Collection<Field> otherXrefs = new ArrayList<Field>(interactionRefs.size());

            // convert xrefs
            for (InteractorXref ref : interactionRefs){

                Field refField = xrefConverter.intactToCalimocho(ref, true);
                if (refField != null){
                    otherXrefs.add(refField);
                }
            }
            if (!otherXrefs.isEmpty()){
                row.addFields(InteractionKeys.KEY_XREFS_I, otherXrefs);
            }
        }

        //process annotations (could have been processed with experiments)
        Collection<Annotation>  annotations = binary.getAnnotations();
        if (!annotations.isEmpty()){
            Collection<Field> annotationFields = row.getFields(InteractionKeys.KEY_ANNOTATIONS_I);

            if (annotationFields == null){
                annotationFields = new ArrayList<Field>(annotations.size());
            }

            for (Annotation annots : annotations){
                Field annotField = annotConverter.intactToCalimocho(annots);

                if (annotField != null){
                    annotationFields.add(annotField);
                }
            }
            if (!annotationFields.isEmpty()){
                row.addFields(InteractionKeys.KEY_ANNOTATIONS_I, annotationFields);
            }
        }

        //process parameters
        if (!binary.getParameters().isEmpty()){
            Collection<Field> paramFields = new ArrayList<Field>(binary.getParameters().size());

            for (Parameter param : binary.getParameters()){
                Field paramField = parameterConverter.intactToCalimocho(param);

                if (paramField != null){
                    paramFields.add(paramField);
                }
            }

            if (!paramFields.isEmpty()){
                row.addFields(InteractionKeys.KEY_PARAMETERS_I, paramFields);
            }
        }

        //process checksum
        if (binary.getCrc() != null){
            Field crc = new DefaultField();
            crc.set(CalimochoKeys.KEY, CRC);
            crc.set(CalimochoKeys.DB, CRC);
            crc.set(CalimochoKeys.VALUE, binary.getCrc());

            row.addField(InteractionKeys.KEY_CHECKSUM_I, crc);
        }

        //process negative
        if (InteractionUtils.isNegative(binary)){
            Field neg = new DefaultField();
            neg.set(CalimochoKeys.VALUE, "true");

            row.addField(InteractionKeys.KEY_NEGATIVE, neg);
        }

        // process update date
        if (binary.getUpdated() != null){
            Field updated = new DefaultField();
            updated.set(CalimochoKeys.VALUE, dateFormat.format(binary.getUpdated()));
            updated.set(CalimochoKeys.DAY, dayFormat.format(binary.getUpdated()));
            updated.set(CalimochoKeys.MONTH, monthFormat.format(binary.getUpdated()));
            updated.set(CalimochoKeys.YEAR, yearFormat.format(binary.getUpdated()));

            row.addField(InteractionKeys.KEY_UPDATE_DATE, updated);
        }

        return row;
    }

}
