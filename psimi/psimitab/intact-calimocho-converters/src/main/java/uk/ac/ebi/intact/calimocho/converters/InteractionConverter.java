package uk.ac.ebi.intact.calimocho.converters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.DefaultRow;
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Checksum;
import psidev.psi.mi.jami.model.Confidence;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Parameter;
import psidev.psi.mi.jami.model.ParticipantEvidence;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.ChecksumUtils;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactAnnotation;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactConfidence;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactParameter;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactExperiment;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractionEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;
import uk.ac.ebi.intact.jami.model.extension.InteractionXref;
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
    private CrossReferenceConverter<InteractionXref> xrefConverter;
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
        this.xrefConverter = new CrossReferenceConverter<>();
        this.annotConverter = new AnnotationConverter();
        this.parameterConverter = new ParameterConverter();

        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        yearFormat = new SimpleDateFormat("yyyy");
        monthFormat = new SimpleDateFormat("MM");
        dayFormat = new SimpleDateFormat("dd");

        this.expansionName = expansionName;
        this.expansionMI = expansionMI;
    }

    public List<Row> intactToCalimocho(IntactInteractionEvidence interaction) throws NotExpandableInteractionException {

        if ( interaction == null ) {
            throw new IllegalArgumentException( "Interaction must not be null" );
        }

        List<Row> rows = new ArrayList<Row>();

        // case of intra molecular interactions and self interactions
        if (interaction.getParticipants().size() == 1) {
            ParticipantEvidence c = interaction.getParticipants().iterator().next();

            if (c.getStoichiometry() != null && c.getStoichiometry().getMinValue() < 2){
                Row row = processBinaryInteraction(interaction, false);

                interactorConverter.intactToCalimocho((IntactParticipantEvidence) c, row, true);
                rows.add(row);

                return rows;
            }
        }

        if (!expansionStrategy.isExpandable(interaction)) {
            if (log.isWarnEnabled()) log.warn("Filtered interaction: "+interaction.getAc()+" (not expandable)");
            return Collections.EMPTY_LIST;
        }

        Collection<BinaryInteraction> interactions = expansionStrategy.expand(interaction);

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

        for (BinaryInteraction binary : interactions) {
            IntactInteractionEvidence binaryInteraction = (IntactInteractionEvidence) binary;

            Row row = processBinaryInteraction(binaryInteraction, expanded);

            if (row != null){
                final Collection<ParticipantEvidence> components = binaryInteraction.getParticipants();
                Iterator<ParticipantEvidence> iterator = components.iterator();
                final ParticipantEvidence componentA = iterator.next();
                final ParticipantEvidence componentB = iterator.next();

                // convert interactors
                interactorConverter.intactToCalimocho((IntactParticipantEvidence) componentA, row, true);
                interactorConverter.intactToCalimocho((IntactParticipantEvidence) componentB, row, false);
                rows.add(row);
            }
        }

        return rows;
    }

    private Row processBinaryInteraction(IntactInteractionEvidence binary, boolean isExpanded) {
        Row row = new DefaultRow();

        // process interaction type
        if (binary.getInteractionType() != null){
            Field type = cvObjectConverter.intactToCalimocho((IntactCvTerm) binary.getInteractionType());

            if (type != null){
                row.addField(InteractionKeys.KEY_INTERACTION_TYPE, type);
            }
        }

        // convert confidences
        if (!binary.getConfidences().isEmpty()){
            Collection<Field> confs = new ArrayList<Field>(binary.getConfidences().size());

            for (Confidence conf : binary.getConfidences()){
                Field confField = confidenceConverter.intactToCalimocho((AbstractIntactConfidence) conf);

                if (confField != null){
                    confs.add(confField);
                }
            }
            row.addFields(InteractionKeys.KEY_CONFIDENCE, confs);
        }

        // process AC
        if (binary.getAc() != null){
            Field id = new DefaultField();

            id.set(CalimochoKeys.KEY, "intact");
            id.set(CalimochoKeys.DB, "intact");
            id.set(CalimochoKeys.VALUE, binary.getAc());

            row.addField(InteractionKeys.KEY_INTERACTION_ID, id);
        }

        // process complex expansion
        if (isExpanded && expansionMI != null){
            Field expansion = new DefaultField();

            String db = CvTerm.PSI_MI;

            expansion.set(CalimochoKeys.KEY, db);
            expansion.set(CalimochoKeys.DB, db);
            expansion.set(CalimochoKeys.VALUE, expansionMI);

            if (expansionName != null){
                expansion.set(CalimochoKeys.TEXT, expansionName);
            }

            row.addField(InteractionKeys.KEY_EXPANSION, expansion);
        }
        else if (isExpanded && expansionMI == null){
            throw new IllegalArgumentException("Interaction is expanded but no expansion strategy has been set.");
        }

        // process experiments
        if (binary.getExperiment() != null){
            experimentConverter.intactToCalimocho((IntactExperiment) binary.getExperiment(), row);
        }

        //process xrefs
        Collection<Xref> interactionRefs = binary.getXrefs();

        if (!interactionRefs.isEmpty()){
            Collection<Field> otherXrefs = new ArrayList<Field>(interactionRefs.size());

            // convert xrefs
            for (Xref ref : interactionRefs){

                Field refField = xrefConverter.intactToCalimocho((InteractionXref) ref, true);
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
                Field annotField = annotConverter.intactToCalimocho((AbstractIntactAnnotation) annots);

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
                Field paramField = parameterConverter.intactToCalimocho((AbstractIntactParameter) param);

                if (paramField != null){
                    paramFields.add(paramField);
                }
            }

            if (!paramFields.isEmpty()){
                row.addFields(InteractionKeys.KEY_PARAMETERS_I, paramFields);
            }
        }

        //process checksum
        Checksum crc64 = ChecksumUtils.collectFirstChecksumWithMethod(binary.getChecksums(), null, "crc64");
        if (crc64 != null) {
            Field crcField = new DefaultField();
            crcField.set(CalimochoKeys.KEY, CRC);
            crcField.set(CalimochoKeys.DB, CRC);
            crcField.set(CalimochoKeys.VALUE, crc64.getValue());

            row.addField(InteractionKeys.KEY_CHECKSUM_I, crcField);
        }

        //process negative
        if (binary.isNegative()){
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
