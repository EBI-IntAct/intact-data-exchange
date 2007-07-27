/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.cli.*;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.XrefDao;
import uk.ac.ebi.intact.util.MemoryMonitor;
import uk.ac.ebi.intact.util.uniprotExport.event.CcLineCreatedEvent;
import uk.ac.ebi.intact.util.uniprotExport.event.CcLineEventListener;
import uk.ac.ebi.intact.util.uniprotExport.event.DrLineProcessedEvent;
import uk.ac.ebi.intact.util.uniprotExport.event.NonBinaryInteractionFoundEvent;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * That class allow to create a flat file containing the CC line to export to Uniprot.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class CCLineExport extends LineExport {

    ///////////////////////////////
    // Inner class

    public class ExportableInteraction {

        private Interaction interaction;
        private int experimentalSupportCount;

        public ExportableInteraction(Interaction interaction, int experimentalSupportCount) {
            this.interaction = interaction;
            this.experimentalSupportCount = experimentalSupportCount;
        }

        public int getExperimentalSupportCount() {
            return experimentalSupportCount;
        }

        public Interaction getInteraction() {
            return interaction;
        }
    }

    ///////////////////////////////
    // Instance variables

    /**
     * Storage of the CC lines per protein. <br> Structure: Map( ProteinAC, Collection( CCLine ) ) <br> <b>Note</b>:
     * proteinAC must be a protein AC, not a Splice Variant ID.
     */
    private Map ccLines = new HashMap(4096);

    /**
     * Store Interaction's AC of those that have been already processed.
     */
    private HashSet alreadyProcessedInteraction = new HashSet(4096);

    /**
     * Use to out the CC lines in a file.
     */
    private Writer ccWriter;

    /**
     * Use to out the GO lines in a file.
     */
    private Writer goWriter;

    private int drProcessedCount;
    private int ccLineCount;
    private int goaLineCount;


    ///////////////////////////////
    // Constructor
    public CCLineExport(Writer ccWriter, Writer goWriter) throws IntactException,
                                                                 DatabaseContentException {
        this(ccWriter, goWriter, new LineExportConfig(), System.out);
    }

    public CCLineExport(Writer ccWriter, Writer goWriter, LineExportConfig config, PrintStream out) throws IntactException,
                                                                                                           DatabaseContentException {
        super(config, out);

        if (ccWriter == null) {
            throw new NullPointerException("You must give a CC Line writer.");
        }

        if (goWriter == null) {
            throw new NullPointerException("You must give a GO Line writer.");
        }

        this.ccWriter = ccWriter;
        this.goWriter = goWriter;
    }

    ///////////////////////////////
    // Methods

    private boolean isNegative(Interaction interaction) {

        boolean negative = false;

        if (super.isNegative(interaction)) {
            negative = true;
        } else {
            //check its experiments
            for (Iterator iterator = interaction.getExperiments().iterator(); iterator.hasNext() && !negative;) {
                Experiment experiment = (Experiment) iterator.next();

                if (isNegative(experiment)) {
                    negative = true;
                }
            }
        }

        return negative;
    }

    private Collection<ProteinImpl> spliceVariants = new ArrayList<ProteinImpl>(16);

    /**
     * retreives using the provided helper a Protein based on its Xref (uniprot, identity).
     *
     * @param uniprotID the primary id of the cross reference
     *
     * @return a Protein having Xref( uniprotId, uniprot, identity )
     *
     * @throws IntactException if none or more than 2 proteins are found.
     */
    private Collection<ProteinImpl> getProteinFromIntact(String uniprotID) throws IntactException {

        Collection<ProteinImpl> proteins = getProteinByXref(uniprotID,
                                                            (CvDatabase) getCvContext().getByMiRef(CvDatabase.class, CvDatabase.UNIPROT_MI_REF),
                                                            (CvXrefQualifier) getCvContext().getByMiRef(CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF));
        //Collection<ProteinImpl> proteins = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().getByUniprotId(uniprotID);

        if (proteins.size() == 0) {
            throw new IntactException("the ID " + uniprotID + " didn't return the expected number of protein: " +
                                      proteins.size() + ". Abort.");
        }

        spliceVariants.clear();

        // now from that try to get splice variants (if any)
        for (Protein protein : proteins) {
            String ac = protein.getAc();
            Collection<ProteinImpl> sv = getProteinByXref(ac,
                                                          (CvDatabase) getCvContext().getByMiRef(CvDatabase.class, CvDatabase.INTACT_MI_REF),
                                                          (CvXrefQualifier) getCvContext().getByMiRef(CvXrefQualifier.class, CvXrefQualifier.ISOFORM_PARENT_MI_REF));
//      Collection<ProteinImpl> sv =              IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao()
//                    .getByXrefLike((CvDatabase)getCvContext().getByMiRef(CvDatabase.class, CvDatabase.INTACT_MI_REF),
//                            (CvXrefQualifier)getCvContext().getByMiRef(CvXrefQualifier.class, CvXrefQualifier.ISOFORM_PARENT_MI_REF), ac);

            spliceVariants.addAll(sv);
        }

        proteins.addAll(spliceVariants);

        return proteins;
    }

    private Collection<ProteinImpl> getProteinByXref(String primaryId, CvDatabase database, CvXrefQualifier qualifier) {
        XrefDao<InteractorXref> xrefDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getXrefDao(InteractorXref.class);
        Collection<InteractorXref> xrefs = xrefDao.getByPrimaryId(primaryId, false);

        Collection<ProteinImpl> proteins = new ArrayList<ProteinImpl>();
        for (InteractorXref xref : xrefs) {
            if ((null != database && database.equals(xref.getCvDatabase()))
                ||
                (null == database && null == xref.getCvDatabase())) {

                if ((null != qualifier && qualifier.equals(xref.getCvXrefQualifier()))
                    ||
                    (null == qualifier && null == xref.getCvXrefQualifier())) {

                    proteins.add((ProteinImpl) xref.getParent());
                }
            }
        }
        return proteins;
    }

    private void flushCCLine(String id) throws IOException {

        List cc4protein = (List) ccLines.remove(id);

        if (null != cc4protein && !cc4protein.isEmpty()) {
            ccLineCount++;

            // the the CC lines
            Collections.sort(cc4protein);

            StringBuffer sb = new StringBuffer(128 * cc4protein.size());

            sb.append("AC").append("   ").append(id);
            sb.append(NEW_LINE);

            sb.append("CC   -!- INTERACTION:");
            sb.append(NEW_LINE);

            for (Iterator iterator = cc4protein.iterator(); iterator.hasNext();) {
                CcLine ccLine = (CcLine) iterator.next();

                sb.append(ccLine.getCcLine());
            }

            sb.append("//");
            sb.append(NEW_LINE);

            String ccs = sb.toString();

            getOut().println(ccs);

            // write the content in the output file.
            ccWriter.write(ccs);
            ccWriter.flush();

            // fire the event
            fireCcLineCreatedEvent(new CcLineCreatedEvent(this, id, cc4protein));
        }
    }

    /**
     * create the output of a CC line for a set of exportable interactions.
     *
     * @param uniprotID1      the uniprot AC of the protein to which that CC lines will be attached.
     * @param uniprotID2      the uniprot AC of the protein which interacts with the protein to which that CC lines will
     *                        be attached.
     * @param experimentCount count of distinct experimental support for that CC line.
     */
    private void createCCLine(String uniprotID1, Protein protein1,
                              String uniprotID2, Protein protein2,
                              int experimentCount
    ) {

        // BUG:
        //      if we give a UniProt ID which is a splice variant, then we add it with a key and that it never flushed
        //      to fix that bug we need to convert it to a master AC before to put it in ccLines.
        // Fix:
        //      if protein is a splice variant, get its master uniprot ID for use in the ccLines.

        // contains the Uniprot ID of protein 1, if protein 1 is a splice variant, we retreive its master's.
        String master1 = null;
        if (isSpliceVariant(protein1)) {

            Protein proteinMaster1 = getMasterProtein(protein1);

            if (proteinMaster1 == null) {
                getOut().println("ERROR: Could not export a CC line related to the master of " + uniprotID1);
            } else {
                master1 = getUniprotID(proteinMaster1);
            }
        } else {
            master1 = uniprotID1;
        }

        // contains the Uniprot ID of protein 1, if protein 1 is a splice variant, we retreive its master's.
        String master2 = null;
        if (isSpliceVariant(protein2)) {

            Protein proteinMaster2 = getMasterProtein(protein2);

            if (proteinMaster2 == null) {
                getOut().println("ERROR: Could not export a CC line related to the master of " + uniprotID2);
            } else {
                master2 = getUniprotID(proteinMaster2);
            }
        } else {
            master2 = uniprotID2;
        }

        // produce the CC lines for the 1st protein
        CcLine cc1 = formatCCLines(uniprotID1, protein1, uniprotID2, protein2, experimentCount);
        List cc4protein1 = (List) ccLines.get(master1);
        if (null == cc4protein1) {
            cc4protein1 = new ArrayList();
            ccLines.put(master1, cc4protein1);
        }
        cc4protein1.add(cc1);

        // produce the CC lines for the 2nd protein
        if (!uniprotID1.equals(uniprotID2)) {
            CcLine cc2 = formatCCLines(uniprotID2, protein2, uniprotID1, protein1, experimentCount);
            List cc4protein2 = (List) ccLines.get(master2);
            if (null == cc4protein2) {
                cc4protein2 = new ArrayList();
                ccLines.put(master2, cc4protein2);
            }
            cc4protein2.add(cc2);
        }
    }

    /**
     * Generate the CC line content based on the Interaction and its two interactor. <br> protein1 is the entry in which
     * that CC content will appear.
     * <p/>
     * <pre>
     *          <font color=gray>ID   X_HUMAN     STANDARD;      PRT;   208 AA.</font>
     *          <font color=gray>AC   P45594</font>
     *          <font color=gray>DE   blablabla.</font>
     *          <font color=gray>GN   X OR Y.</font>
     *          CC   -!- INTERACTION:
     *          CC       Self; NbExp=1; AC=EBI-307456,EBI-307456;
     *          CC       P01232:rr44; NbExp=3; AC=EBI-307456,EBI-237;
     *          CC       P10981:tsr; NbExp=4; AC=EBI-307456,EBI-234567;
     *          <font color=gray>DR   IntAct; P45594, -.</font>
     * </pre>
     *
     * @param uniprotID1      uniprot ID of the protein in which we will export that CC line
     * @param protein1        IntAct associated protein
     * @param uniprotID2      uniprot ID of the protein with which interacts protein 1
     * @param protein2        IntAct associated protein
     * @param experimentCount count of distinct experimental support
     *
     * @return a CCLine
     */
    private CcLine formatCCLines(String uniprotID1, Protein protein1,
                                 String uniprotID2, Protein protein2,
                                 int experimentCount
    ) {

        StringBuffer buffer = new StringBuffer(128); // average size is 160 char

        buffer.append("CC       ");

        String geneName = null;
        if (uniprotID1.equals(uniprotID2)) {

            geneName = "Self";
            buffer.append(geneName);

        } else {

            // A gene must be there ... it must have been checked before.
            geneName = getGeneName(protein2);
            if (geneName == null) {
                geneName = "-";
            }

            buffer.append(uniprotID2).append(':').append(geneName);
        }

        // generated warning message if the two protein are from different organism
        if (!protein1.getBioSource().equals(protein2.getBioSource())) {
            buffer.append(' ').append("(xeno)");
        }

        buffer.append(';').append(' ').append("NbExp=").append(experimentCount).append(';').append(' ');
        buffer.append("IntAct=").append(protein1.getAc()).append(',').append(' ').append(protein2.getAc()).append(';');

        getOut().println("\t\t\t" + buffer.toString());

        buffer.append(NEW_LINE);

        return new CcLine(buffer.toString(), geneName, uniprotID2);
    }

    public Set getPumedIds(Set experiments) {

        Set pubmeds = new HashSet();

        for (Iterator iterator = experiments.iterator(); iterator.hasNext();) {
            Experiment experiment = (Experiment) iterator.next();
            boolean found = false;

            for (Iterator iterator1 = experiment.getXrefs().iterator(); iterator1.hasNext() && !found;) {
                Xref xref = (Xref) iterator1.next();

                if (getCvContext().getByMiRef(CvDatabase.class, CvDatabase.PUBMED_MI_REF).equals(xref.getCvDatabase()) &&
                    getCvContext().getByMiRef(CvXrefQualifier.class, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF).equals(xref.getCvXrefQualifier())) {
                    found = true;
                    pubmeds.add(xref.getPrimaryId());
                }
            } // xref

            if (found == false) {
                getOut().println("ERROR: " + experiment.getShortLabel() + " " + CvDatabase.PUBMED +
                                 " has no (" + CvXrefQualifier.PRIMARY_REFERENCE + ") assigned.");
            }
        } // experiments

        return pubmeds;
    }

    private void createGoLine(String uniprotID_1, Protein protein1,
                              String uniprotID_2, Protein protein2,
                              Set eligibleExperiments,
                              Set eligibleInteractions
    ) throws IOException {

        // in case a protein is a splice variant, get its master ID
        // we consider an isoform interacting with its parent as self interaction.
        String master1 = null;
        if (isSpliceVariant(protein1)) {

            Protein proteinMaster1 = getMasterProtein(protein1);

            if (proteinMaster1 == null) {
                getOut().println("ERROR: Could not export a CC line related to the master of " + uniprotID_1);
                master1 = uniprotID_1;
            } else {
                master1 = getUniprotID(proteinMaster1);
            }
        } else {
            master1 = uniprotID_1;
        }

        // contains the Uniprot ID of protein 1, if protein 1 is a splice variant, we retreive its master's.
        String master2 = null;
        if (isSpliceVariant(protein2)) {

            Protein proteinMaster2 = getMasterProtein(protein2);

            if (proteinMaster2 == null) {
                getOut().println("ERROR: Could not export a CC line related to the master of " + uniprotID_2);
                master2 = uniprotID_2;
            } else {
                master2 = getUniprotID(proteinMaster2);
            }
        } else {
            master2 = uniprotID_2;
        }

        Set pubmeds = getPumedIds(eligibleExperiments);
        if (pubmeds.isEmpty()) {
            getOut().println("ERROR: No PubMed ID found in that set of experiments. ");
            return;
        }

        // build a pipe separated list of pubmed IDs
        StringBuffer pubmedBuffer = new StringBuffer();
        for (Iterator iterator = pubmeds.iterator(); iterator.hasNext();) {
            String pubmed = (String) iterator.next();
            pubmedBuffer.append("PMID:").append(pubmed);
            if (iterator.hasNext()) {
                pubmedBuffer.append('|');
            }
        }

        // TODO implement that ...
        // if we start using the interaction->component->feature->range->cvFuzzyType
        // then we need to extract a collection of association GO - pudmed and generate muliple lines.

        // generate the line
        StringBuffer line = new StringBuffer();
        line.append("UniProt").append(TABULATION); // DB
        line.append(uniprotID_1).append(TABULATION); // DB_object_ID
        line.append(TABULATION); // DB_Object_symbol
        line.append(TABULATION); // Qualifier

        boolean self = false;
        if (master1.equals(master2)) {
            line.append("GO:0042802").append(TABULATION); // GoId - protein self binding
            self = true;
        } else {
            line.append("GO:0005515").append(TABULATION); // GoId - protein binding
        }

        line.append(pubmedBuffer.toString()).append(TABULATION); // DB:Reference

        line.append("IPI").append(TABULATION); // Evidence
        line.append("UniProt:").append(uniprotID_2).append(TABULATION); // with
        line.append(TABULATION); // Aspect
        line.append(TABULATION); // DB_Object_name
        line.append(TABULATION); // synonym
        line.append(TABULATION); // DB_object_type
        line.append(TABULATION); // Taxon_ID
        line.append(TABULATION); // Date
        line.append("IntAct");   // Assigned By
        line.append(NEW_LINE);

        goaLineCount++;

        if (!self) {
            // write the reverse

            line.append("UniProt").append(TABULATION); // DB
            line.append(uniprotID_2).append(TABULATION); // DB_object_ID
            line.append(TABULATION); // DB_Object_symbol
            line.append(TABULATION); // Qualifier
            line.append("GO:0005515").append(TABULATION); // GoId - protein binding
            line.append(pubmedBuffer.toString()).append(TABULATION); // DB:Reference

            line.append("IPI").append(TABULATION); // Evidence
            line.append("UniProt:").append(uniprotID_1).append(TABULATION); // with
            line.append(TABULATION); // Aspect
            line.append(TABULATION); // DB_Object_name
            line.append(TABULATION); // synonym
            line.append(TABULATION); // DB_object_type
            line.append(TABULATION); // Taxon_ID
            line.append(TABULATION); // Date
            line.append("IntAct");   // Assigned By

            line.append(NEW_LINE);

            goaLineCount++;
        }

        // write into the GO file
        goWriter.write(line.toString());
        goWriter.flush();
    }

    /**
     * process a set of interaction ACs in order to find out which interaction are eligible for CC export. <br> Note 1:
     * those interactions are specific of 2 known proteins. Note 2: an interaction is eligible onl in the context of the
     * given interactions. Note 3: all given interactions are binary, not need to check again. <br> eg. if an
     * interaction has a conditional export (eg. Y2H's experiment) we would look for a number of other interaction
     * having the same experimental method but in different experiment. We would only search in the set of given
     * interaction <br>
     * <p/>
     * <pre>
     * Algorithm sketch
     * ----------------
     *   let INTERACTIONS be the collection of given interactions.
     * <p/>
     *   For each Interaction i in INTERACTIONS do
     *       skip (i) if i is not a binary interaction
     *       For each experiment e attached to Interaction i
     *           skip (e) if status is not exportable
     *           if status is export:
     *               keeps track of it
     *           if no status specified:
     *               Check the CvInteraction cv status
     *               skip (e) if status is no exportable
     *               if status of cv is export: validate the experiment e as exportable
     *               if status is conditional
     *                   let t be the threshold of distinct experiment needed to validate that status
     *                   check in the current interaction if an other experiment can validate the export
     *                   if not
     *                       For each Interaction ii in INTERACTIONS do (each but i)
     *                           if ii.experiment.cvInteraction = cv and ii.experiment not equals to e.
     *                              count it.
     *                           if count >= t, status of cvInteraction gets validated.
     * <p/>
     * </pre>
     *
     * @param interactions         a collection of interaction AC
     * @param eligibleInteractions collection to be filled with all eligible interactions
     *
     * @return Collection(ExportableInteraction) or can be null if no interaction have been found to be eligible for
     *         CC export
     */
    private Set getEligibleExperimentCount(List interactions, Set eligibleInteractions) {

        Set eligibleExperiments = new HashSet();

        final int interactionCount = interactions.size();
        for (int i = 0; i < interactionCount; i++) {

            Interaction interaction = (Interaction) interactions.get(i);

            getOut().println("\t\t Interaction: Shortlabel:" + interaction.getShortLabel() + "  AC: " + interaction.getAc());

            if (isNegative(interaction)) {

                getOut().println("\t\t\t That interaction or at least one of its experiments is negative, skip it.");
                continue; // skip that interaction
            }

            Collection experiments = interaction.getExperiments();

            int expCount = experiments.size();
            getOut().println("\t\t\t interaction related to " + expCount + " experiment" + (expCount > 1 ? "s" : "") + ".");

            for (Iterator iterator2 = experiments.iterator(); iterator2.hasNext();) {
                Experiment experiment = (Experiment) iterator2.next();

                boolean experimentExport = false;
                getOut().println("\t\t\t\t Experiment: Shortlabel:" + experiment.getShortLabel() + "  AC: " + experiment.getAc());

                ExperimentStatus experimentStatus = getCCLineExperimentExportStatus(experiment, "\t\t\t\t\t");
                if (experimentStatus.doNotExport()) {
                    // forbid export for all interactions of that experiment (and their proteins).
                    getOut().println("\t\t\t\t\t No interactions of that experiment will be exported.");

                } else if (experimentStatus.doExport()) {
                    // Authorise export for all interactions of that experiment (and their proteins),
                    // This overwrite the setting of the CvInteraction concerning the export.
                    getOut().println("\t\t\t\t\t All interaction of that experiment will be exported.");

                    experimentExport = true;

                } else if (experimentStatus.isLargeScale()) {

                    // if my interaction has one of those keywords as annotation for DR line export, do export.
                    Collection keywords = experimentStatus.getKeywords();
                    Collection annotations = interaction.getAnnotations();
                    boolean annotationFound = false;

                    CvTopic authorConfidenceTopic = (CvTopic) getCvContext().getByMiRef(CvTopic.class, CvTopic.AUTHOR_CONFIDENCE_MI_REF);

                    // We assume here that an interaction has only one Annotation( uniprot-dr-export ).
                    for (Iterator iterator3 = annotations.iterator(); iterator3.hasNext() && !annotationFound;) {
                        final Annotation annotation = (Annotation) iterator3.next();

                        if (authorConfidenceTopic.equals(annotation.getCvTopic())) {
                            String text = annotation.getAnnotationText();

                            getOut().println("\t\t\t Interaction has " + authorConfidenceTopic.getShortLabel() + ": '" + text + "'");

                            if (text != null) {
                                text = text.trim();
                            }

                            for (Iterator iterator4 = keywords.iterator(); iterator4.hasNext() && !annotationFound;) {
                                String kw = (String) iterator4.next();
                                // NOT case sensitive

                                getOut().println("\t\t\t\t Compare it with '" + kw + "'");

                                if (kw.equalsIgnoreCase(text)) {
                                    annotationFound = true;
                                    getOut().println("\t\t\t\t\t Equals !");
                                }
                            }
                        }
                    }

                    if (annotationFound) {

                        /*
                        * We don't need to check an eventual threshold on the method level because
                        * in the current state, the annotation is on the experiment level that is
                        * lower and hence is dominant on the method's one.
                        */

                        experimentExport = true;
                        getOut().println("\t\t\t that interaction is eligible for export in the context of a large scale experiment");

                    } else {

                        getOut().println("\t\t\t interaction not eligible");
                    }

                } else if (experimentStatus.isNotSpecified()) {

                    getOut().println("\t\t\t\t No experiment status, check the experimental method.");

                    // Then check the experimental method (CvInteraction)
                    // Nothing specified at the experiment level, check for the method (CvInteraction)
                    CvInteraction cvInteraction = experiment.getCvInteraction();

                    if (null == cvInteraction) {
                        // we need to check because cvInteraction is not mandatory in an experiment.
                        continue; // skip it, go to next experiment
                    }

                    CvInteractionStatus methodStatus = getMethodExportStatus(cvInteraction, "\t\t");

                    if (methodStatus.doExport()) {

                        experimentExport = true;

                    } else if (methodStatus.doNotExport()) {

                        // do nothing

                    } else if (methodStatus.isNotSpecified()) {

                        // we should never get in here but just in case...
                        // do nothing

                    } else if (methodStatus.isConditionalExport()) {

                        getOut().println("\t\t\t\t As conditional export, check the count of distinct experiment for that method.");

                        // if the threshold is not reached, iterates over all available interactions to check if
                        // there is (are) one (many) that could allow to reach the threshold.

                        int threshold = methodStatus.getMinimumOccurence();

                        // we create a non redondant set of experiment identifier
                        // TODO couldn't that be a static collection that we empty regularly ?
                        Set experimentAcs = new HashSet(threshold);

                        // check if there are other experiments attached to the current interaction that validate it.
                        boolean enoughExperimentFound = false;
                        for (Iterator iterator = experiments.iterator(); iterator.hasNext();) {
                            Experiment experiment1 = (Experiment) iterator.next();

                            getOut().println("\t\t\t\t Experiment: Shortlabel:" + experiment1.getShortLabel() + "  AC: " + experiment1.getAc());

                            CvInteraction method = experiment1.getCvInteraction();

                            if (cvInteraction.equals(method)) {
                                experimentAcs.add(experiment1.getAc());

                                // we only update if we found one
                                enoughExperimentFound = (experimentAcs.size() >= threshold);
                            }
                        }

                        getOut().println("\t\t\tLooking for other interactions that support that method in other experiments...");

                        for (int j = 0; j < interactionCount && !enoughExperimentFound; j++) {

                            if (i == j) {
                                continue;
                            }

                            //
                            // Have that conditionalMethods at the interaction scope.
                            //
                            // for a interaction
                            //      for each experiment e
                            //          if e.CvInteraction <> cvInteraction -> continue
                            //          else is experiment already processed ? if no, add and check the count >= threashold.
                            //                                                 if reached, stop, esle carry on.
                            //

                            Interaction interaction2 = (Interaction) interactions.get(j);

                            getOut().println("\t\t Interaction: Shortlabel:" + interaction2.getShortLabel() + "  AC: " + interaction2.getAc());

                            Collection experiments2 = interaction2.getExperiments();

                            for (Iterator iterator6 = experiments2.iterator(); iterator6.hasNext() && !enoughExperimentFound;)
                            {
                                Experiment experiment2 = (Experiment) iterator6.next();
                                getOut().println("\t\t\t\t Experiment: Shortlabel:" + experiment2.getShortLabel() + "  AC: " + experiment2.getAc());

                                CvInteraction method = experiment2.getCvInteraction();

                                if (cvInteraction.equals(method)) {
                                    experimentAcs.add(experiment2.getAc());
                                    // we only update if we found one
                                    enoughExperimentFound = (experimentAcs.size() >= threshold);
                                }
                            } // j's experiments

                            getOut().println("\t\t\t\t " + cvInteraction.getShortLabel() + ", threshold: " +
                                             threshold + " #experiment: " +
                                             (experimentAcs == null ? "none" : "" + experimentAcs.size()));
                        } // j

                        if (enoughExperimentFound) {
                            getOut().println("\t\t\t\t Enough experiemnt found");
                            experimentExport = true;
                        } else {
                            getOut().println("\t\t\t\t Not enough experiemnt found");
                        }

                    } // conditional status
                } // experiment status not specified

                if (experimentExport) {
                    eligibleExperiments.add(experiment);
                    eligibleInteractions.add(interaction);
                }
            } // i's experiments
        } // i

        return eligibleExperiments;
    }

    /**
     * Write the CC line export in an output file describe by the given filename.
     * <pre>
     * algo sketch
     * -----------
     * <p/>
     * For each protein eligible for DR export do
     *   - let's call the selected protein P1
     *   - get a set of IntAct proteins from P1's uniprot ID
     *   - get interactions of P1
     *   - while( collection not empty )
     *      - take the first interaction [P1, P2]
     *      - skip it if it is not a binary interaction
     *      - skip it if P2 is not eligible for DR export
     *      - get a set of IntAct proteins from P2's uniprot ID
     *      - get from the interaction of P1, all interaction that involve both P1 and P2. Let call it i
     *      - run algo that select from the set i which protein are to be exported as CC line
     *      - generate the CC line for each Interaction of the resulting set and associate it to P1 and P2
     *   - Write the CC lines of P1 in a file (P2 is not complete yet)
     * <p/>
     * Note: an interaction that as been already processed won't be a second time.
     * </pre>
     *
     * @param uniprotIDs a collection of protein ID (uniprot AC)
     *
     * @throws IntactException
     * @throws SQLException
     */
    public void generateCCLines(Collection<String> uniprotIDs) throws IntactException,
                                                                      SQLException, IOException {

        getOut().println("NOTE: Forced autobegin transaction");
        IntactContext.getCurrentInstance().getConfig().setAutoBeginTransaction(true);

        int count = uniprotIDs.size();
        int idProcessed = 0;
        int percentProteinProcessed;
        List<Interaction> potentiallyEligibleInteraction = new ArrayList<Interaction>(16);

        // iterate over the Uniprot ID of the protein that have been selected for DR export.
        for (String uniprot_ID : uniprotIDs) {
            idProcessed++;

            if ((idProcessed % 50) == 0) {
                getOut().println("..." + idProcessed);

                if ((idProcessed % 500) == 0) {
                    getOut().println("");
                }
            }

            getOut().println("Protein selected: " + uniprot_ID);

            percentProteinProcessed = (int) (((float) idProcessed / (float) count) * 100);
            getOut().println("Protein processed: " + percentProteinProcessed + "% (" + idProcessed + " out of " + count + ")");
            getOut().println("Interaction processed: " + alreadyProcessedInteraction.size());

            // get the protein's and splice variants related to that Uniprot ID
            Collection<ProteinImpl> proteinSet_1 = getProteinFromIntact(uniprot_ID);

            for (Protein protein1 : proteinSet_1) {
                // get the protein1 real uniprot_ID.
                final String uniprotID_1 = getUniprotID(protein1);

                Collection<Interaction> interactionP1 = getInteractions(protein1);

                Protein protein2 = null;
                String uniprotID_2 = null;

                // while( collection not empty )
                //     take the first interaction
                //     get 2 sets of proteins (interaction partner)
                //     get from the interaction set all interaction that have both interactor (remove them)
                //     run algo

                //getOut().println("\t" + uniprotID_1 + "(" + protein1.getBioSource().getShortLabel() + ")" + " has " + interactionP1.size() + " interaction(s).");


                while (!interactionP1.isEmpty()) {
                    Iterator iterator = interactionP1.iterator(); // can't be ouside the loop
                    Interaction interaction = (Interaction) iterator.next();

                    getOut().println("\t Process interaction : " + interaction.getShortLabel() + " (" + interaction.getAc() + ")");

                    // Let's say we process P1 and P2, the subset of the interactions of P1 if then stored in
                    // the cache of already processed interactions. There is no reason why those interaction
                    // would be processed again since they are binary, hence specific to P1 and P2
                    // So when we process P2 and load again those same interactions from the DB, we can skip them.
                    if (alreadyProcessedInteraction.contains(interaction.getAc())) {

                        getOut().println("\t\t That interaction has been processed already ... skip it.");
                        iterator.remove(); // remove it.

                    } else {
                        alreadyProcessedInteraction.add(interaction.getAc());

                        if (false == isBinary(interaction)) {

                            iterator.remove();

                            // fire non-binary interaction event
                            fireNonBinaryInteractionFoundEvent(new NonBinaryInteractionFoundEvent(this, interaction));

                        } else {

                            Component component1 = null;
                            Component component2 = null;

                            // here we know already that the interaction is binary
                            if (interaction.getComponents().size() == 1) {
                                // the protein is interacting with itself (stochio = 2)
                                Iterator iteratorC = interaction.getComponents().iterator();
                                component2 = component1 = (Component) iteratorC.next();
                            } else {
                                // must be 2
                                Iterator iteratorC = interaction.getComponents().iterator();
                                component1 = (Component) iteratorC.next();
                                component2 = (Component) iteratorC.next();
                            }

                            getOut().println("\t\t Interaction has exactly 2 interactors.");

                            // now get the other protein ( other than uniprotID )

                            /**
                             * Uniprot ID -> Collection(Protein) (ie. all species + splice variants)
                             *
                             * in the interaction, for each component, if the protein is part of the collection, take
                             * the protein attached to the other component. If still the same, that means that we have
                             * the same protein interacting as self. could be SV+P or different species though.
                             */

                            // we assume that Component carry only Protein as Interactor.
                            getOut().println("\t\t Check what is the partner of " + uniprotID_1);

                            // TODO what happen is uniprot_ID = P12345 and protein1 is its splice variant: P12345-2 ?

                            Protein p1 = (Protein) component1.getInteractor();
                            getOut().println("\t\t 1st Partner found: " + p1.getShortLabel());

                            Protein p2 = (Protein) component2.getInteractor();
                            getOut().println("\t\t 2nd Partner found: " + p2.getShortLabel());

                            if (protein1.equals(p1)) {
                                protein2 = p2;
                            } else {
                                protein2 = p1;
                            }

                            uniprotID_2 = getUniprotID(protein2);

                            // retreive the UniProt ID of the protein or its master (is splice variant) to check if it
                            // should be exported. Reminder, only protein exported in the DR are exported in the CCs.
                            String uniprotID_2_check = uniprotID_2;
                            if (isSpliceVariant(protein2)) {
                                Protein master = getMasterProtein(protein2);
                                if (master != null) {
                                    uniprotID_2_check = getUniprotID(master);
                                }
                            }

                            // We check that the protein we are currently dealing with was eligible in the DRLines set.
                            // Note: if it is a splice variant, we check on its master protein.
                            if (!uniprotIDs.contains(uniprotID_2_check)) {

                                getOut().println("\t\t " + uniprotID_2 + " was not eligible for DR export, because it was not found in the uniprot ID list");
                                iterator.remove();

                            } else {

                                getOut().println("\n\t\t Look for interactions amongst " + protein1.getShortLabel() + " and " + protein2.getShortLabel() + ".");

                                // extract all interactions have partner in protein1 and proteins2
                                potentiallyEligibleInteraction.clear();

                                // add the current one
                                potentiallyEligibleInteraction.add(interaction);
                                iterator.remove();
                                getOut().println("\t\t\t Add the current one: " + interaction.getShortLabel() + "(" + interaction.getAc() + ")");

                                for (Iterator iterator2 = interactionP1.iterator(); iterator2.hasNext();) {
                                    Interaction interaction1 = (Interaction) iterator2.next();

                                    if (alreadyProcessedInteraction.contains(interaction1.getAc())) {

                                        getOut().println("\t\t\t That interaction " + interaction1.getShortLabel() + "(" +
                                                         interaction1.getAc() + ") has been processed already ... skip it.");
                                        iterator2.remove(); // remove it.

                                    } else {

                                        if (!isBinary(interaction1)) {

                                            alreadyProcessedInteraction.add(interaction1.getAc());
                                            iterator2.remove(); // remove using the local iterator

                                        } else {

                                            Component c1 = null;
                                            Component c2 = null;

                                            if (interaction1.getComponents().size() == 1) {
                                                Iterator iteratorC = interaction1.getComponents().iterator();
                                                c2 = c1 = (Component) iteratorC.next(); // same component, hence interactor
                                            } else {
                                                // must be 2
                                                Iterator iteratorC = interaction1.getComponents().iterator();
                                                c1 = (Component) iteratorC.next();
                                                c2 = (Component) iteratorC.next();
                                            }

                                            Protein p1_ = (Protein) c1.getInteractor();
                                            Protein p2_ = (Protein) c2.getInteractor();

                                            boolean normal = protein1.equals(p1_) && protein2.equals(p2_);
                                            boolean reverse = protein1.equals(p2_) && protein2.equals(p1_);

                                            if (normal || reverse) {

                                                alreadyProcessedInteraction.add(interaction1.getAc());

                                                // select that interaction
                                                potentiallyEligibleInteraction.add(interaction1);
                                                iterator2.remove();

                                                getOut().println("\t\t\t Add: " + interaction1.getShortLabel() + "(" + interaction1.getAc() + ")");
                                            }
                                        } // else
                                    } // else
                                } // selection of the interactions

                                getOut().println("\t\t They have " + potentiallyEligibleInteraction.size() + " interaction(s) in common");

                                // run the algo
                                // we need to output
                                //     the collection of eligible interactions to get the feature information (GO specific)
                                //     the collection of eligible experimentss to get the pubmed ids (GO specific)
                                Set eligibleInteractions = new HashSet();
                                Set eligibleExperiments = getEligibleExperimentCount(potentiallyEligibleInteraction,
                                                                                     eligibleInteractions);

                                if (!eligibleExperiments.isEmpty()) {

                                    getOut().println("\t\t Creating CC Lines");

                                    // CC Lines
                                    createCCLine(uniprotID_1, protein1,
                                                 uniprotID_2, protein2,
                                                 eligibleExperiments.size());

                                    // if we select the interactions that are seen in the collection of experiment,
                                    // then we can inspect the features and extract c-terminal, n-terminal...

                                    // PB: what if protein1 is seen multiple times in the interaction ?
                                    //     -> should not happen as we deal here with binary interactions. though in
                                    //        case of self we could have that. eg. c-terminus and n-terminus at the
                                    //        same time.

                                    getOut().println("\t\t Creating GO Lines");

                                    // GO lines
                                    createGoLine(uniprotID_1, protein1,
                                                 uniprotID_2, protein2,
                                                 eligibleExperiments,
                                                 eligibleInteractions);
                                }

                            } // else (uniprotID_2 is eligible for DR export)
                        } // else (interaction is binary)
                    } // else (interaction hasn't been processed yet)
                } // while there is interactions
            } // proteins associated to UniprotID

            // write the CC content of protein still designated by index 'i' as its processing is finished.
            flushCCLine(uniprot_ID);

            fireDrLineProcessedEvent(new DrLineProcessedEvent(this, uniprot_ID));
            drProcessedCount++;

            try {
                getOut().println("Finished. Committing transaction");
                IntactContext.getCurrentInstance().getDataContext().commitTransaction();
            } catch (IntactTransactionException e) {
                e.printStackTrace();
            }

            getOut().flush();

        } // i (all eligible uniprot IDs)

        // flush and close output file
        if (null != ccWriter) {
            try {
                ccWriter.close();
                getOut().println("Output file closed.");
            } catch (IOException e) {
                getOut().println("ERROR: Could not close the output file.");
            }
        }
    }

    public int getCcLineCount() {
        return ccLineCount;
    }

    public int getGoaLineCount() {
        return goaLineCount;
    }

    /**
     * Convert the DR line export file to an String array of Uniprot IDs.
     *
     * @param file the DR line export file.
     *
     * @return a String array of Uniprot IDs.
     *
     * @throws IOException if the file handling goes wrong.
     */
    public static Set<String> getEligibleProteinsFromFile(String file) throws IOException {

        InputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);

        // Using a HashSet will avoid redundancy
        Set<String> proteins = new HashSet<String>(4096);

        String line = null;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            count++;
            line = line.trim();
            if ("".equalsIgnoreCase(line)) {
                continue;
            }

            // format of that line is: UniprotID \t IntAct \t UniprotId \t -
            // eg. P000001 Intact P00001 -

            Pattern pattern = Pattern.compile("\\S+");
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                String uniprotID = matcher.group();
                proteins.add(uniprotID);
            }
        }

        is.close();

        // the given parameter is just needed to type the returned collection.
        // @see java.util.Collection.toArray(Object a[])
        return proteins;
    }

    protected EventListenerList listenerList =
            new EventListenerList();

    public void addCcLineExportListener(CcLineEventListener eventListener) {
        listenerList.add(CcLineEventListener.class, eventListener);
    }

    // This methods allows classes to unregister for MyEvents
    public void removeCcLineExportListener(CcLineEventListener eventListener) {
        listenerList.remove(CcLineEventListener.class, eventListener);
    }

    void fireCcLineCreatedEvent(CcLineCreatedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == CcLineEventListener.class) {
                ((CcLineEventListener) listeners[i + 1]).ccLineCreated(evt);
            }
        }
    }

    void fireDrLineProcessedEvent(DrLineProcessedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == CcLineEventListener.class) {
                ((CcLineEventListener) listeners[i + 1]).drLineProcessed(evt);
            }
        }
    }

    void fireNonBinaryInteractionFoundEvent(NonBinaryInteractionFoundEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == CcLineEventListener.class) {
                ((CcLineEventListener) listeners[i + 1]).processNonBinaryInteraction(evt);
            }
        }
    }

    /**
     * Show usage for the program.
     *
     * @param options
     */
    private static void displayUsage(Options options) {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("CCLineExport <DR import file> <CC export file> <GO export file> [-debug] [-debugFile]", options);
    }

    public static void main(String[] args) throws IntactException, SQLException, IOException,
                                                  DatabaseContentException {

        MemoryMonitor memoryMonitor = new MemoryMonitor();

        // create Option objects
        Option helpOpt = new Option("help", "print this message");

        Option drExportOpt = OptionBuilder.withArgName("drExportFilename").hasArg()
                .withDescription("DR export input goFile.").create("drExport");
        drExportOpt.setRequired(true);

        Option ccExportOpt = OptionBuilder.withArgName("ccExportFilename").hasArg()
                .withDescription("CC export output goFile.").create("ccExport");

        Option goExportOpt = OptionBuilder.withArgName("goExportFilename").hasArg()
                .withDescription("GO export output goFile.").create("goExport");

        Option debugOpt = OptionBuilder
                .withDescription("Shows verbose output.").create("debug");
        debugOpt.setRequired(false);

        Option debugFileOpt = OptionBuilder
                .withDescription("Store verbose output in the specified goFile.").create("debugFile");
        debugFileOpt.setRequired(false);

        Options options = new Options();

        options.addOption(helpOpt);
        options.addOption(drExportOpt);
        options.addOption(ccExportOpt);
        options.addOption(goExportOpt);
        options.addOption(debugOpt);
        options.addOption(debugFileOpt);

        // create the parser
        CommandLineParser parser = new BasicParser();
        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse(options, args, true);
        } catch (ParseException exp) {
            // Oops, something went wrong

            displayUsage(options);

            System.out.println("ERROR: Parsing failed.  Reason: " + exp.getMessage());
            System.exit(1);
        }

        if (line.hasOption("help")) {
            displayUsage(options);
            System.exit(0);
        }

        boolean debugEnabled = line.hasOption("debug");
        boolean debugFileEnabled = line.hasOption("debugFile");
        String drExportFilename = line.getOptionValue("drExport");
        String ccExportFilename = null;
        String goExportFilename = null;
        if (line.hasOption("ccExport")) {
            goExportFilename = line.getOptionValue("goExport");
        }

        if (line.hasOption("goExport")) {
            ccExportFilename = line.getOptionValue("ccExport");
        }

        System.out.println("Try to open: " + drExportFilename);
        Set<String> uniprotIDs = getEligibleProteinsFromFile(drExportFilename);
        System.out.println(uniprotIDs.size() + " DR protein(s) loaded from drFile: " + drExportFilename);

        // create a database access
        try {
            System.out.println("Database instance: " + IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbName());
            System.out.println("User: " + IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbUserName());
        } catch (SQLException e) {
            System.out.println("ERROR: Could not get database information (instance name and username).");
        }

        // Prepare CC output goFile.
        File ccFile = null;
        if (ccExportFilename != null) {
            try {
                ccFile = new File(ccExportFilename);
                if (ccFile.exists()) {
                    System.out.println("ERROR: Please give a new file name for the CC output file: " + ccFile.getAbsoluteFile());
                    System.out.println("ERROR: We will use the default filename instead (instead of overwritting the existing file).");
                    ccExportFilename = null;
                    ccFile = null;
                }
            } catch (Exception e) {
                // nothing, the default filename will be given
            }
        }

        if (ccExportFilename == null || ccFile == null) {
            String filename = "CCLineExport_" + TIME + ".txt";
            System.out.println("Using default filename for the CC export: " + filename);
            ccFile = new File(filename);
        }

        // Prepare GO output goFile.
        File goFile = null;
        if (goExportFilename != null) {
            try {
                goFile = new File(goExportFilename);
                if (goFile.exists()) {
                    System.out.println("ERROR: Please give a new file name for the GO output file: " + goFile.getAbsoluteFile());
                    System.out.println("ERROR: We will use the default filename instead (instead of overwritting the existing file).");
                    goExportFilename = null;
                    goFile = null;
                }
            } catch (Exception e) {
                // nothing, the default filename will be given
            }
        }

        if (goExportFilename == null || goFile == null) {
            String filename = "GOExport_" + TIME + ".txt";
            System.out.println("Using default filename for the GO export: " + filename);
            goFile = new File(filename);
        }

        FileWriter ccFileWriter = null;
        FileWriter goFileWriter = null;
        try {
            ccFileWriter = new FileWriter(ccFile);
            goFileWriter = new FileWriter(goFile);

            CCLineExport exporter = new CCLineExport((Writer) ccFileWriter, (Writer) goFileWriter);
            exporter.setDebugEnabled(debugEnabled);
            exporter.setDebugFileEnabled(debugFileEnabled);

            // launch the CC export
            exporter.generateCCLines(uniprotIDs);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Could not create the output goFile:" + goFile.getAbsolutePath());
            System.exit(1);

        } finally {
            if (ccFileWriter != null) {
                try {
                    ccFileWriter.close();
                } catch (IOException e) {
                    System.exit(1);
                }
            }

            if (goFileWriter != null) {
                try {
                    goFileWriter.close();
                } catch (IOException e) {
                    System.exit(1);
                }
            }
        }
    }
}