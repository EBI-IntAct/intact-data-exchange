/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.CvContext;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.InteractionUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class LineExport {

    protected static String TIME;

    static {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd@HH.mm");
        TIME = formatter.format(new Date());
        formatter = null;
    }

    private Map<String, Boolean> binaryInteractions = new HashMap<String, Boolean>(4096);

    private Map<String, String> protAcToUniprotIdCache = new LRUMap(4096);

    private CvObject uniprotCcExport = getCvContext().getByLabel(CvTopic.class, CvTopic.UNIPROT_CC_EXPORT);
    //////////////////////////
    // Constants

    protected static final String METHOD_EXPORT_KEYWORK_EXPORT = "yes";
    protected static final String METHOD_EXPORT_KEYWORK_DO_NOT_EXPORT = "no";

    protected static final String EXPERIMENT_EXPORT_KEYWORK_EXPORT = "yes";
    protected static final String EXPERIMENT_EXPORT_KEYWORK_DO_NOT_EXPORT = "no";

    protected static final String NEW_LINE = System.getProperty("line.separator");
    protected static final char TABULATION = '\t';

    ////////////////////////////
    // Inner Class

    public class ExperimentStatus {

        // Experiment status
        public static final int EXPORT = 0;
        public static final int DO_NOT_EXPORT = 1;
        public static final int NOT_SPECIFIED = 2;
        public static final int LARGE_SCALE = 3;

        private int status;
        private Collection<String> keywords;

        public ExperimentStatus(int status) {
            this.status = status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public Collection<String> getKeywords() {
            return keywords;
        }

        public boolean doExport() {
            return status == EXPORT;
        }

        public boolean doNotExport() {
            return status == DO_NOT_EXPORT;
        }

        public boolean isNotSpecified() {
            return status == NOT_SPECIFIED;
        }

        public boolean isLargeScale() {
            return status == LARGE_SCALE;
        }

        public void addKeywords(Collection<String> keywords) {
            if (keywords == null) {
                throw new IllegalArgumentException("Keywords must not be null");
            }
            this.keywords = keywords;
        }

        public String toString() {

            StringBuffer sb = new StringBuffer(128);

            sb.append("ExperimentStatus{ keywords= ");
            if (keywords != null) {
                for (String kw : keywords) {
                    sb.append(kw).append(' ');
                }
            }

            sb.append(" status=");
            switch (status) {
                case EXPORT:
                    sb.append("EXPORT");
                    break;
                case DO_NOT_EXPORT:
                    sb.append("DO_NOT_EXPORT");
                    break;
                case NOT_SPECIFIED:
                    sb.append("NOT_SPECIFIED");
                    break;
                case LARGE_SCALE:
                    sb.append("LARGE_SCALE");
                    break;
                default:
                    sb.append("UNKNOWN VALUE !!!!!!!!!!!!!!!!!");
            }
            sb.append(" }");

            return sb.toString();
        }
    }


    public class CvInteractionStatus {

        // Method status
        public static final int EXPORT = 0;
        public static final int DO_NOT_EXPORT = 1;
        public static final int NOT_SPECIFIED = 2;
        public static final int CONDITIONAL_EXPORT = 3;

        private int status;
        private int minimumOccurence = 1;

        public CvInteractionStatus(int status) {
            this.status = status;
        }

        public CvInteractionStatus(int status, int minimumOccurence) {
            this.minimumOccurence = minimumOccurence;
            this.status = status;
        }

        public int getMinimumOccurence() {
            return minimumOccurence;
        }

        public boolean doExport() {
            return status == EXPORT;
        }

        public boolean doNotExport() {
            return status == DO_NOT_EXPORT;
        }

        public boolean isNotSpecified() {
            return status == NOT_SPECIFIED;
        }

        public boolean isConditionalExport() {
            return status == CONDITIONAL_EXPORT;
        }

        public String toString() {

            StringBuffer sb = new StringBuffer(128);

            sb.append("CvInteractionStatus{ minimumOccurence=").append(minimumOccurence);

            sb.append(" status=");
            switch (status) {
                case EXPORT:
                    sb.append("EXPORT");
                    break;
                case DO_NOT_EXPORT:
                    sb.append("DO_NOT_EXPORT");
                    break;
                case NOT_SPECIFIED:
                    sb.append("NOT_SPECIFIED");
                    break;
                case CONDITIONAL_EXPORT:
                    sb.append("CONDITIONAL_EXPORT");
                    break;
                default:
                    sb.append("UNKNOWN VALUE !!!!!!!!!!!!!!!!!");
            }
            sb.append(" }");

            return sb.toString();
        }
    }


    protected class DatabaseContentException extends RuntimeException {

        public DatabaseContentException(String message) {
            super(message);
        }
    }


    /**
     * Service termination hook (gets called when the JVM terminates from a signal).
     */
    protected static class CloseFileOnShutdownHook extends Thread {

        private static final Log log = LogFactory.getLog(CloseFileOnShutdownHook.class);

        private FileWriter outputFileWriter;

        public CloseFileOnShutdownHook(BufferedWriter outputBufferedWriter, FileWriter outputFileWriter) {
            super();
            this.outputFileWriter = outputFileWriter;

            log.info("Output File close on Shutdown Hook installed.");
        }

        public void run() {
            if (outputFileWriter != null) {
                try {
                    outputFileWriter.close();
                } catch (IOException e) {
                    log.info("An error occured when trying to close the output file");
                    return;
                }
            }

            if (outputFileWriter != null) {
                try {
                    outputFileWriter.close();
                } catch (IOException e) {
                    log.error("ERROR: " + "An error occured when trying to close the output file");
                    return;
                }
            }
            log.info("Output file is now closed.");
        }
    }

    //////////////////////////////
    // Attributes

    /**
     * Cache the CvInteraction property for the export. CvInteraction.ac -> Boolean.TRUE or Boolean.FALSE
     */
    protected HashMap<String, CvInteractionStatus> cvInteractionExportStatusCache = new HashMap<String, CvInteractionStatus>();

    /**
     * Cache the Experiment property for the export. Experiment.ac -> Integer (EXPORT, DO_NOT_EXPORT, NOT_SPECIFIED)
     */
    protected HashMap<String, ExperimentStatus> experimentExportStatusCache = new HashMap<String, ExperimentStatus>();

    protected boolean debugEnabled = false; // protected to allow the testcase to modify it.

    protected boolean debugFileEnabled = false;

    protected BufferedWriter outputBufferedWriter;
    protected FileWriter outputFileWriter;

    private LineExportConfig config;
    private PrintStream out;

    public LineExport() {
        this.config = new LineExportConfig();
        this.out = System.out;
    }

    public LineExport(LineExportConfig config) {
        this.config = config;
        this.out = System.out;
    }

    public LineExport(LineExportConfig config, PrintStream out) {
        this.config = config;
        this.out = out;
    }

    /////////////////////////////
    // Methods

    /**
     * Get a CvObject based on its class name and its shortlabel.
     *
     * @param clazz      the Class we are looking for
     * @param shortlabel the shortlabel of the object we are looking for
     *
     * @return the CvObject of type <code>clazz</code> and having the shortlabel <code>shorltabel<code>.
     *
     * @throws IntactException          if the search failed
     * @throws DatabaseContentException if the object is not found.
     */
    private CvObject getCvObject(Class<CvTopic> clazz, String shortlabel)
            throws IntactException,
                   DatabaseContentException {

        return getCvObject(clazz, null, shortlabel);
    }

    /**
     * Get a CvObject based on its class name and its shortlabel. <br>
     * If specified, the MI reference will be used first, then only the shortlabel.
     *
     * @param clazz      the Class we are looking for
     * @param mi         the mi reference of the CvObject (if any, otherwise: null)
     * @param shortlabel the shortlabel of the object we are looking for
     *
     * @return the CvObject of type <code>clazz</code> and having the shortlabel <code>shorltabel<code>.
     *
     * @throws IntactException          if the search failed
     * @throws DatabaseContentException if the object is not found.
     */
    private CvObject getCvObject(Class clazz, String mi, String shortlabel)
            throws IntactException,
                   DatabaseContentException {

        if (mi == null && shortlabel == null) {
            throw new IllegalArgumentException("You must give at least a MI reference or a shortlabel of the CV you are looking for.");
        }

        CvObject cv = null;

        if (mi != null) {
            cv = IntactContext.getCurrentInstance().getCvContext().getByMiRef(clazz, mi);
            if (cv == null) {
                out.println("ERROR: The MI reference you gave doesn't exists. Using the shortlabel instead.");
            }
        }

        if (cv == null) {
            cv = IntactContext.getCurrentInstance().getCvContext().getByLabel(clazz, shortlabel);
        }

        if (cv == null) {
            StringBuffer sb = new StringBuffer(128);
            sb.append("Could not find ");
            sb.append(clazz.getName());
            sb.append(' ');
            sb.append(shortlabel);
            if (mi != null) {
                sb.append(' ');
                sb.append('(').append(mi).append(')');
            }
            sb.append(" in your IntAct node");

            throw new DatabaseContentException(sb.toString());
        }

        return cv;
    }

    /**
     * @param flag
     */
    protected void setDebugEnabled(boolean flag) {
        debugEnabled = flag;
    }

    /**
     * @param enabled
     */
    protected void setDebugFileEnabled(boolean enabled) {
        debugFileEnabled = enabled;

        if (enabled) {
            // create the output file if the user requested it.
            String filename = "export2uniprot_verboseOutput_" + TIME + ".txt";
            File file = new File(filename);
            out.println("Save verbose output to: " + file.getAbsolutePath());
            outputBufferedWriter = null;
            outputFileWriter = null;
            try {
                outputFileWriter = new FileWriter(file);
                outputBufferedWriter = new BufferedWriter(outputFileWriter);

                Runtime.getRuntime().addShutdownHook(new CloseFileOnShutdownHook(outputBufferedWriter, outputFileWriter));

            } catch (IOException e) {
                e.printStackTrace();
                debugFileEnabled = false;
            }
        }
    }

    /**
     * Get the uniprot primary ID from Protein and Splice variant.
     *
     * @param protein the Protein for which we want the uniprot ID.
     *
     * @return the uniprot ID as a String or null if none is found (should not occur)
     */
    public String getUniprotID(final Protein protein) {

        if (protAcToUniprotIdCache.containsKey(protein.getAc())) {
            return protAcToUniprotIdCache.get(protein.getAc());
        }

        String uniprotId = null;

        Collection<InteractorXref> xrefs = protein.getXrefs();

        CvDatabase uniprotCv = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvDatabase.class, CvDatabase.UNIPROT_MI_REF);
        CvXrefQualifier identityCv = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF);

        for (InteractorXref xref : xrefs) {
            if (uniprotCv.equals(xref.getCvDatabase()) &&
                identityCv.equals(xref.getCvXrefQualifier())) {
                uniprotId = xref.getPrimaryId();
                break;
            }
        }

        //ProteinDao proteinDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao() ;
        //String uniprotId = proteinDao.getUniprotAcByProteinAc(protein.getAc());

        protAcToUniprotIdCache.put(protein.getAc(), uniprotId);

        return uniprotId;
    }

    /**
     * Get the intact master AC from the given Splice variant.
     *
     * @param protein the splice variant (Protein) for which we its intact master AC.
     *
     * @return the intact AC
     */
    public final String getMasterAc(final Protein protein) {

        String ac = null;

        Collection<InteractorXref> xrefs = protein.getXrefs();
        boolean found = false;
        for (Iterator<InteractorXref> iterator = xrefs.iterator(); iterator.hasNext() && !found;) {
            Xref xref = iterator.next();

            if (getCvContext().getByMiRef(CvDatabase.class, CvDatabase.INTACT_MI_REF).equals(xref.getCvDatabase()) &&
                getCvContext().getByMiRef(CvXrefQualifier.class, CvXrefQualifier.ISOFORM_PARENT_MI_REF).equals(xref.getCvXrefQualifier())) {
                ac = xref.getPrimaryId();
                found = true;
            }
        }

        return ac;
    }

    /**
     * Fetches the master protein of a splice variant. <br> Nothing is returned if the protein is not a splice variant
     * or if the splice variant doesn't have a valid Xref to the IntAct database.
     *
     * @param protein the protein for which we want a splice variant
     *
     * @return a Protein or null is nothing is found or an error occurs.
     */
    protected Protein getMasterProtein(Protein protein) {

        Protein master = null;

        String ac = getMasterAc(protein);

        if (ac != null) {
            // search for that Protein
            Collection<ProteinImpl> proteins = null;
            try {
                proteins = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().getByAcLike(ac, false);
            } catch (IntactException e) {
                e.printStackTrace();
            }

            if (proteins != null) {
                if (!proteins.isEmpty()) {
                    master = proteins.iterator().next();
                } else {
                    out.println("ERROR: Could not find the master protein (AC: " + ac +
                                " ) of the splice variant AC: " + protein.getAc());
                }
            } else {
                out.println("ERROR: An error occured when searching the IntAct database for Protein having the AC: " + ac);
            }
        } else {
            out.println("ERROR: Could not find a master protein AC in the Xrefs of the protein: " +
                        protein.getAc() + ", " + protein.getShortLabel());
        }

        return master;
    }

    /**
     * Get all interaction related to the given Protein.
     *
     * @param protein the protein of which we want the interactions.
     *
     * @return a Collection if Interaction.
     */
    protected final List<Interaction> getInteractions(final Protein protein) {
        Collection<Component> components = protein.getActiveInstances();
        int componentsSize = components.size();
        List<Interaction> interactions = new ArrayList<Interaction>(componentsSize);

        out.println("Found " + componentsSize + " components for protein " + protein.getShortLabel()
                    + ". Starting to get the interactions from those components.");

        for (Component component : components) {
            Interaction interaction = component.getInteraction();

            if (!interactions.contains(interaction)) {
                interactions.add(interaction);
            }
        }

        return interactions;

//        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractionDao().getInteractionsByInteractorAc(protein.getAc());

    }

    /**
     * Tells us if an Interaction is binary or not.<br>
     * <pre>
     *      Rules:
     *         - the sum of the stoichiometry of the components must be 2 (2*1 or 1*2)
     *         - the interacting partner must be UniProt proteins
     * </pre>
     *
     * @param interaction the interaction we are interrested in.
     *
     * @return true if the interaction is binary, otherwise false.
     */
    public boolean isBinary(Interaction interaction) {

        Boolean isBinaryInteraction = binaryInteractions.get(interaction.getAc());

        if (isBinaryInteraction != null) {
            return isBinaryInteraction;
        }

        Collection<Component> components = interaction.getComponents();

        isBinaryInteraction = InteractionUtils.isBinaryInteraction(interaction);

        if (!isBinaryInteraction) {
            out.println("\t\tInteraction NOT binary");
        }

        if (isBinaryInteraction) {
            // then test if all interactors are UniProt Proteins
            for (Iterator iterator = interaction.getComponents().iterator(); iterator.hasNext()
                                                                             && isBinaryInteraction;) {
                Component component = (Component) iterator.next();

                Interactor interactor = component.getInteractor();
                if (interactor instanceof Protein) {

                    Protein protein = (Protein) interactor;

                    // check that the protein is a UniProt protein
                    String uniprotID = getUniprotID(protein);
                    if (uniprotID == null) {
                        isBinaryInteraction = false; // stop the loop, involve a protein without uniprot ID

                        out.println("\t\t\t Interaction is binary but doesn't involve only UniProt proteins (eg. " +
                                    protein.getAc() + " / " + protein.getShortLabel() + "). ");
                    } else {

                        // check that the protein doesn't have a no-uniprot-update annotation
                        if (!needsUniprotUpdate(protein)) {
                            isBinaryInteraction = false; // stop the loop, Protein having no-uniprot-update involved

                            out.println("\t\t\t Interaction is binary but at least one UniProt protein is flagged '" +
                                        CvTopic.NON_UNIPROT + "' (eg. " + protein.getAc() + " / " + protein.getShortLabel() + "). ");
                        }
                    }

                } else {
                    isBinaryInteraction = false; // stop the loop, that component doesn't involve a Protein

                    out.println("\t\t\t Interaction is binary but at least one partner is not a Protein (ie. " +
                                interactor.getAc() + " / " + interactor.getShortLabel() + " / " + component.getClass() + "). ");
                }
            } // components
        }

        binaryInteractions.put(interaction.getAc(), isBinaryInteraction);

        return isBinaryInteraction;
    }


    /**
     * Answers the question: does that technology is to be exported to SwissProt ? <br> Do give that answer, we check
     * that the CvInteration has an annotation having <br> <b>CvTopic</b>: uniprot-dr-export <br> <b>text</b>: yes [OR]
     * no [OR] &lt;integer value&gt;
     * <p/>
     * if <b>yes</b>: the method is meant to be exported <br> if <b>no</b>: the method is NOT meant to be exported <br>
     * if <b>&lt;integer value&gt;</b>: the protein linked to that method are ment to be exported only if they are seen
     * in a minimum number interactions belonging to distinct experiment. eg. let's set that value to 2 for Y2H. To be
     * eligible for export a protein must have been seen in at least 2 Y2H distinct experiment. </p>
     *
     * @param cvInteraction the method to check
     * @param logPrefix     indentatin of the log
     *
     * @return the status of that method (EXPORT, DO_NOT_EXPORT, NOT_SPECIFIED, CONDITIONAL_EXPORT) with an optional
     *         count.
     */
    public final CvInteractionStatus getMethodExportStatus(final CvInteraction cvInteraction, String logPrefix) {

        CvInteractionStatus status = null;

        // cache the CvInteraction status
        if (null != cvInteraction) {

            CvInteractionStatus cache = cvInteractionExportStatusCache.get(cvInteraction.getAc());
            if (null != cache) {

//                out.println( logPrefix + "\t\t\t\t CvInteraction: Status already processed, retreived from cache." );
                status = cache;

            } else {

                if (config.isIgnoreUniprotDrExportAnnotation()) {
                    status = new CvInteractionStatus(CvInteractionStatus.EXPORT);
                } else {

                    boolean found = false;
                    boolean multipleAnnotationFound = false;
                    Collection<Annotation> annotations = null;

                    annotations = cvInteraction.getAnnotations();

                    out.println(logPrefix + "\t\t\t " + annotations.size() + " annotations found.");

                    if (config.isIgnoreUniprotDrExportAnnotation()) {
                        out.println(logPrefix + "\t\t\tIGNORING uniprot-dr-export check");
                        status = new CvInteractionStatus(CvInteractionStatus.EXPORT);
                    } else {

                        Annotation annotation = null;
                        for (Iterator<Annotation> iterator = annotations.iterator(); iterator.hasNext() && !multipleAnnotationFound;)
                        {
                            Annotation _annotation = iterator.next();

                            out.println("\t\t\t\tAnnotation CvTopic: " + _annotation.getCvTopic());

                            if (getCvContext().getByLabel(CvTopic.class, CvTopic.UNIPROT_DR_EXPORT).equals(_annotation.getCvTopic())) {

                                out.println(logPrefix + "\t\t\t\t Found uniprot-dr-export annotation: " + _annotation);

                                if (found) {
                                    multipleAnnotationFound = true;
                                    out.println("ERROR: There are multiple annotation having Topic:" + CvTopic.UNIPROT_DR_EXPORT +
                                                " in CvInteraction: " + cvInteraction.getShortLabel() +
                                                ". \nWe do not export.");
                                } else {
                                    found = true;
                                    annotation = _annotation;
                                }
                            }
                        }


                        if (multipleAnnotationFound) {

                            status = new CvInteractionStatus(CvInteractionStatus.DO_NOT_EXPORT);
                            out.println(logPrefix + "\t\t\t multiple annotation found: do not export ");

                        } else {

                            if (found) {

                                String text = annotation.getAnnotationText();
                                if (null != text) {
                                    text = text.toLowerCase().trim();
                                }

                                if (METHOD_EXPORT_KEYWORK_EXPORT.equals(annotation.getAnnotationText())) {

                                    status = new CvInteractionStatus(CvInteractionStatus.EXPORT);
                                    out.println(logPrefix + "\t\t\t " + METHOD_EXPORT_KEYWORK_EXPORT + " found: export ");

                                } else if (METHOD_EXPORT_KEYWORK_DO_NOT_EXPORT.equals(annotation.getAnnotationText())) {

                                    status = new CvInteractionStatus(CvInteractionStatus.DO_NOT_EXPORT);
                                    out.println(logPrefix + "\t\t\t " + METHOD_EXPORT_KEYWORK_DO_NOT_EXPORT + " found: do not export ");

                                } else {

                                    out.println(logPrefix + "\t\t\t neither YES or NO found: should be an integer value... ");

                                    // it must be an integer value, let's check it.
                                    try {
                                        Integer value = new Integer(text);
                                        int i = value;

                                        if (i >= 2) {

                                            // value is >= 2
                                            status = new CvInteractionStatus(CvInteractionStatus.CONDITIONAL_EXPORT, i);
                                            out.println(logPrefix + "\t\t\t " + i + " found: conditional export ");

                                        } else if (i == 1) {

                                            String err = cvInteraction.getShortLabel() + " having annotation (" + CvTopic.UNIPROT_DR_EXPORT +
                                                         ") has an annotationText like <integer value>. Value was: " + i +
                                                         ", We consider it as to be exported.";
                                            out.println(err);

                                            status = new CvInteractionStatus(CvInteractionStatus.EXPORT);
                                            out.println(logPrefix + "\t\t\t integer == " + i + " found: export ");

                                        } else {
                                            // i < 1

                                            String err = cvInteraction.getShortLabel() + " having annotation (" + CvTopic.UNIPROT_DR_EXPORT +
                                                         ") has an annotationText like <integer value>. Value was: " + i +
                                                         " However, having a value < 1 is not valid, We consider it as to be NOT exported.";
                                            out.println("ERROR: " + err);

                                            status = new CvInteractionStatus(CvInteractionStatus.DO_NOT_EXPORT);
                                            out.println(logPrefix + "\t\t\t integer < 1 (" + i + ") found: do not export ");
                                        }

                                    } catch (NumberFormatException e) {
                                        // not an integer !
                                        out.println("ERROR: " + cvInteraction.getShortLabel() + " having annotation (" + CvTopic.UNIPROT_DR_EXPORT +
                                                    ") has an annotationText different from yes/no/<integer value> !!!" +
                                                    " value was: '" + text + "'.");
                                        out.println(logPrefix + "\t\t\t not an integer:(" + text + ") found: do not export ");

                                        status = new CvInteractionStatus(CvInteractionStatus.DO_NOT_EXPORT);
                                    }
                                }
                            } else {
                                // no annotation implies NO EXPORT !
                                out.println("ERROR: " + cvInteraction.getShortLabel() +
                                            " doesn't have an annotation: " + CvTopic.UNIPROT_DR_EXPORT);
                                out.println(logPrefix + "\t\t\t not annotation found: do not export ");

                                status = new CvInteractionStatus(CvInteractionStatus.DO_NOT_EXPORT);
                            }
                        }
                    }

                    // cache it !
                    cvInteractionExportStatusCache.put(cvInteraction.getAc(), status);
                }
            }
        }

        out.println("\t\t CvInteractionExport status: " + status);

        return status;
    }

    /**
     * Checks if there is a uniprot-cc-exportt annotation defined at the experiment level. if set to yes, export. if set
     * to no, do not export. if no set or the keyword is not yes, no, relies on the standart method.
     *
     * @param experiment the experiment for which we check if we have to export it.
     * @param logPrefix  for all logging messages
     *
     * @return an Integer that has 4 possible value based on constant value: EXPORT, DO_NOT_EXPORT, NOT_SPECIFIED,
     *         LARGE_SCALE. and a list of keywords that is set in case of large scale experiment.
     */
    public final ExperimentStatus getCCLineExperimentExportStatus(final Experiment experiment, String logPrefix) {

        ExperimentStatus status = null;

        // cache the cvInteraction
        ExperimentStatus cache = experimentExportStatusCache.get(experiment.getAc());
        if (null != cache) {

            status = cache;
            return status;

        } else {
            Collection<Annotation> annotations = experiment.getAnnotations();
            boolean yesFound = false;
            boolean noFound = false;


            for (Annotation _annotation : annotations) {
                if (uniprotCcExport.equals(_annotation.getCvTopic())) {

                    out.println(logPrefix + _annotation);

                    String text = _annotation.getAnnotationText();
                    if (text != null) {
                        text = text.trim().toLowerCase();
                    }

                    if (EXPERIMENT_EXPORT_KEYWORK_EXPORT.equals(text)) {
                        yesFound = true;
                        out.println(logPrefix + "\t\t\t\t '" + EXPERIMENT_EXPORT_KEYWORK_EXPORT + "' found");

                    } else {
                        if (EXPERIMENT_EXPORT_KEYWORK_DO_NOT_EXPORT.equals(text)) {
                            noFound = true;
                            out.println(logPrefix + "\t\t\t\t '" + EXPERIMENT_EXPORT_KEYWORK_DO_NOT_EXPORT + "' found");

                        } else {

                            out.println(logPrefix + "\t\t\t\t '" + text + "' found, that keyword wasn't recognised.");
                        }
                    }
                }
            } // annotations

            if (yesFound) {
                status = new ExperimentStatus(ExperimentStatus.EXPORT);
            } else if (noFound) {
                status = new ExperimentStatus(ExperimentStatus.DO_NOT_EXPORT);
            }
        }

        if (status != null) {

            // cache it.
            experimentExportStatusCache.put(experiment.getAc(), status);

            return status;
        }

        return getExperimentExportStatus(experiment, logPrefix);
    }


    /**
     * Answers the question: does that experiment is to be exported to SwissProt ? <br> Do give that answer, we check
     * that the Experiment has an annotation having <br> <b>CvTopic</b>: uniprot-dr-export <br> <b>text</b>: yes [OR] no
     * [OR] &lt;keyword list&gt;
     * <p/>
     * if <b>yes</b>: the experiment is meant to be exported <br> if <b>no</b>: the experiment is NOT meant to be
     * exported <br> if <b>&lt;keyword list&gt;</b>: the experiment is meant to be exported but only interactions that
     * have an annotation with, as text, one of the keyword specified in the list. This is considered as a Large Scale
     * experiment. </p>
     *
     * @param experiment the experiment for which we check if we have to export it.
     * @param logPrefix  for all logging messages
     *
     * @return an Integer that has 4 possible value based on constant value: EXPORT, DO_NOT_EXPORT, NOT_SPECIFIED,
     *         LARGE_SCALE. and a list of keywords that is set in case of large scale experiment.
     */
    public final ExperimentStatus getExperimentExportStatus(final Experiment experiment, String logPrefix) {

        ExperimentStatus status = null;

        // cache the cvInteraction
        ExperimentStatus cache = experimentExportStatusCache.get(experiment.getAc());
        if (null != cache) {

            status = cache;

        } else {

            boolean yesFound = false;
            boolean noFound = false;
            boolean keywordFound = false;

            // most experiment won't need that, so we jsut allocate the collection when needed
            Collection<String> keywords = null;

            Collection<Annotation> annotations = experiment.getAnnotations();
            out.println(logPrefix + annotations.size() + " annotation(s) found");

            for (Annotation annotation : annotations) {
                if (getCvContext().getByLabel(CvTopic.class, CvTopic.UNIPROT_DR_EXPORT).equals(annotation.getCvTopic())) {

                    out.println(logPrefix + annotation);

                    String text = annotation.getAnnotationText();
                    if (text != null) {
                        text = text.trim().toLowerCase();
                    }

                    if (EXPERIMENT_EXPORT_KEYWORK_EXPORT.equals(text)) {
                        yesFound = true;
                        out.println(logPrefix + "'" + EXPERIMENT_EXPORT_KEYWORK_EXPORT + "' found");

                    } else {
                        if (EXPERIMENT_EXPORT_KEYWORK_DO_NOT_EXPORT.equals(text)) {
                            noFound = true;
                            out.println(logPrefix + "'" + EXPERIMENT_EXPORT_KEYWORK_DO_NOT_EXPORT + "' found");

                        } else {
                            if (keywords == null) {
                                keywords = new ArrayList<String>(2);
                            }
                            keywordFound = true;

                            out.println(logPrefix + "'" + text + "' keyword found");
                            keywords.add(text);
                        }
                    }
                }
            }


            if (yesFound && !keywordFound) { // if at least one keyword found, set to large scale experiment.
                status = new ExperimentStatus(ExperimentStatus.EXPORT);
            } else if (noFound) {
                status = new ExperimentStatus(ExperimentStatus.DO_NOT_EXPORT);
            } else if (keywordFound) {
                status = new ExperimentStatus(ExperimentStatus.LARGE_SCALE);
                status.addKeywords(keywords);
            } else {
                status = new ExperimentStatus(ExperimentStatus.NOT_SPECIFIED);
            }

            // cache it.
            experimentExportStatusCache.put(experiment.getAc(), status);
        }

        out.println(logPrefix + "Experiment status: " + status);
        return status;
    }

    /**
     * Answers the question: is that AnnotatedObject (Interaction, Experiment) annotated as negative ?
     *
     * @param annotatedObject the object we want to introspect
     *
     * @return true if the object is annotated with the 'negative' CvTopic, otherwise false.
     */
    public boolean isNegative(AnnotatedObject annotatedObject) {

        boolean isNegative = false;

        Collection<Annotation> annotations = annotatedObject.getAnnotations();
        for (Iterator<Annotation> iterator = annotations.iterator(); iterator.hasNext() && false == isNegative;) {
            Annotation annotation = iterator.next();

            if (getCvContext().getByLabel(CvTopic.class, CvTopic.NEGATIVE).equals(annotation.getCvTopic())) {
                isNegative = true;
            }
        }

        return isNegative;
    }

    /**
     * @param interaction
     *
     * @return
     */
    protected Collection<String> getCCnote(Interaction interaction) {
        Collection<String> notes = null;

        for (Annotation annotation : interaction.getAnnotations()) {
            if (getCvContext().getByLabel(CvTopic.class, CvTopic.CC_NOTE).equals(annotation.getCvTopic())) {
                if (notes == null) {
                    notes = new ArrayList<String>(2); // should rarely have more than 2
                }

                notes.add(annotation.getAnnotationText());
            }
        }

        return notes;
    }


    /**
     * Assess if a protein is a aplice variant on the basis of its shortlabel as we use the following format SPID-# and
     * if it has a isoform-parent cross reference. <br> Thought it doesn't mean we will find a master protein for it.
     *
     * @param protein the protein we are interrested in knowing if it is a splice variant.
     *
     * @return true if the name complies to the splice variant format.
     */
    protected boolean isSpliceVariant(Protein protein) {

        // TODO check here is it has a master or not.

        if (protein.getShortLabel().indexOf("-") != -1) {
            // eg. P12345-2

            if (getMasterAc(protein) != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Retreive the gene name of a protein, if this is a splice variant, get it from its master protein.
     *
     * @param protein the protein from which we want to get a gene name.
     *
     * @return a gene name or null if non could be found.
     */
    public String getGeneName(Protein protein) {

        String geneName = null;

        // Take into account that a Protein object can be either a protein or a splice variant,
        // in the case of a splice variant, we should pick the gene name from the master protein.
        Protein queryProtein = null;

        if (isSpliceVariant(protein)) {

            // get the master protein.
            queryProtein = getMasterProtein(protein);
            if (queryProtein == null) {

                queryProtein = protein;
            }

        } else {

            queryProtein = protein;
        }

        // look first for gene-name
        List<Alias> geneNames = selectAliasByCvTopic(queryProtein.getAliases(), getCvContext().getByMiRef(CvAliasType.class, CvAliasType.GENE_NAME_MI_REF));

        if (geneNames.isEmpty()) {

            // then look for locus
            geneNames = selectAliasByCvTopic(queryProtein.getAliases(), getCvContext().getByMiRef(CvAliasType.class, CvAliasType.LOCUS_NAME_MI_REF));

            if (geneNames.isEmpty()) {

                // then look for orf
                geneNames = selectAliasByCvTopic(queryProtein.getAliases(), getCvContext().getByMiRef(CvAliasType.class, CvAliasType.ORF_NAME_MI_REF));

                if (geneNames.isEmpty()) {

                    // no gene-name, locus or orf for that protein, will display a dash ( '-' ) instead.

                } else {
                    geneName = (geneNames.get(0)).getName();
                }

            } else {
                geneName = (geneNames.get(0)).getName();
            }

        } else {
            geneName = (geneNames.get(0)).getName();
        }

//        // search for a gene name in the aliases of that protein - stop when we find one.
//        for( Iterator iterator = queryProtein.getAliases().iterator(); iterator.hasNext() && null == geneName; ) {
//            Alias alias = (Alias) iterator.next();
//
//            if( geneNameAliasType.equals( alias.getCvAliasType() ) ) {
//                geneName = alias.getName();
//            }
//        }

        return geneName;
    }

    public List<Alias> selectAliasByCvTopic(Collection<? extends Alias> aliases, CvAliasType aliasType) {

        List<Alias> result = null;

        for (Alias alias : aliases) {
            if (aliasType.equals(alias.getCvAliasType())) {

                if (result == null) {
                    result = new ArrayList<Alias>(4);
                }

                result.add(alias);
            }
        }

        if (result == null) {

            result = Collections.EMPTY_LIST;

        } else {

            Comparator<Alias> c = new Comparator<Alias>() {
                public int compare(Alias alias1, Alias alias2) {
                    String s1 = alias1.getName();
                    String s2 = alias2.getName();

                    if( s1 == null && s2 == null ) return 0;

                    if( s1 == null ) return Integer.MIN_VALUE;
                    if( s2 == null ) return Integer.MAX_VALUE;

                    return s1.compareTo( s2 );
                }
            };

            if (result.size() > 1) {
                Collections.sort(result, c);
            }
        }

        return result;
    }

    /**
     * Checks if the protein has been annotated with the no-uniprot-update CvTopic, if so, return false, otherwise true.
     * That flag is added to a protein when created via the editor. As some protein may have a UniProt ID as identity we
     * don't want those to be overwitten.
     *
     * @param protein the protein to check
     *
     * @return false if no Annotation having CvTopic( no-uniprot-update ), otherwise true.
     */
    protected boolean needsUniprotUpdate(final Protein protein) {

        boolean needsUpdate = true;

        CvTopic noUniprotUpdate = getCvContext().getByLabel(CvTopic.class, CvTopic.NON_UNIPROT);

        if (null == noUniprotUpdate) {
            // in case the term hasn't been created, assume there are no proteins created via editor.
            return true;
        }

        for (Iterator<Annotation> iterator = protein.getAnnotations().iterator(); iterator.hasNext() && true == needsUpdate;)
        {
            Annotation annotation = iterator.next();

            if (noUniprotUpdate.equals(annotation.getCvTopic())) {
                needsUpdate = false;
            }
        }

        return needsUpdate;
    }


    public CvContext getCvContext() {
        return IntactContext.getCurrentInstance().getCvContext();
    }

    protected PrintStream getOut() {
        return out;
    }
}
