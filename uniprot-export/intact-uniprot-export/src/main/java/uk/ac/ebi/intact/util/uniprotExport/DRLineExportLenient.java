/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.cli.*;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * That class performs the export of the IntAct DR lines to UniProt. <br> We export all proteins that have been seen in
 * at least one high confidence interaction. <br> Only protein imported directly from UniProt are taken into account
 * <br> note: the proteins created via the editor should have an Annotation( CvTopic( no-uniprot-update ) )  attached to
 * it.
 * <p/>
 * <br> <br> Now I'll explain the details of how we decide whether or not an interaction is of high confidence.
 * <pre>
 * <p/>
 *   Here is a sketch of the algorithm for the DR lines ( I hope I will be clear enough ;) ):
 *      - let P be a Protein (and we are trying to determinate if it should be exported) imported from UniProtKB.
 *        note: in the case of splice variant (which are individual Protein in IntAct), if it is found to
 *              be eligible, it's parent is exported in the DR file instead. (eg. if P12345-1 were to be eligible,
 *              we would put P12345 as eligible without further checking).
 *      - let I be all non negative, binary interactions in which P interacts
 *        (negative interaction being defined as: it was demonstrated in this paper that these interactions
 *         DO NOT occur under the experimental conditions described.)
 *      - IntAct's experiment contain information about the method that was used to detect interactions, these
 *        methods are controlled vocabularies (namely CvInteraction). Each CvInteraction is annotated with
 *        uniprot-dr-export and a value in: yes, no, integer (that defines how many distinct evidence are required
 *        to make a record eligible for export).
 *      - for each interaction i in I:
 *           - get i's related experiment and check if it has an explicit export flag (can be yes, no or an arbitrary
 *             word). This flag is stored in an Annotation( CvTopic( uniprot-dr-export ) ).
 *              - if it is flagged 'yes', the related interaction becomes eligible for export too.
 *              - if it is flagged 'no', the related interaction is declared not eligible.
 *              - if it is an arbitrary word (eg. 'high'), then we check if the interaction has it too,
 *                if so, the interaction becomes eligible for export
 *              - if no flag is found, we check the experiment's method (CvInteraction, eg. y2h, ...) of the
 *                experiment.
 *                Here again we have several cases,
 *                  - if the flag is 'yes', then the experiment (and its interaction) becomes eligible for export,
 *                  - if the flag is 'no', then the experiment (and its interaction) becomes NOT eligible for export,
 *                  - if the flag is a numerical value that describes how many distinct experiment using that method
 *                    should be found in order to get the interaction to be eligible.
 *                    eg. CvInteraction( two hybrid ) has a threshold of 2, let's say we have 2 experiments E1 and E2
 *                        having interaction list E1{I1, I2} and E2{I3}. We also know that P is interacting in I1 and
 *                        I3.
 *                        Now let's say we first look at I1, unfold the algorithm and end up checking on the
 *                        interaction
 *                        detection method in I1's respective experiments, Y2H having a threshold of 2, we need to
 *                        check
 *                        all interaction of P1 (ie. I1 and I3). If an other experiment's detection method has either
 *                        an explicit yes flag or if the count of the experiments having that method allow to reach
 *                        the defined threshold, then we declare the interaction I1 eligible.
 *                        In our example, the threshold is 2 and we have E1 and E2 having Y2H, so P becomes eligible.
 * <p/>
 *         So in a nustshell, if at least one of the interactions of P is declared eligible for export, the protein
 *         becomes itself eligible for DR export.
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: DRLineExport.java 12066 2008-09-08 15:37:28Z skerrien $
 */
public class DRLineExportLenient extends LineExport {

    public DRLineExportLenient() {
        super();
    }

    public DRLineExportLenient(LineExportConfig config) {
        super(config);
    }

    public DRLineExportLenient(LineExportConfig config, PrintStream out) {
        super(config, out);
    }

    /**
     * Define if a Protein object is link to at least one binary, non negative that involves solely uniprot proteins.
     *
     * @param protein the protein we are interrested in.
     *
     * @return true is the protein is plays a role in at least one binary, non negative that involves solely uniprot
     * proteins, otherwise false.
     */
    private boolean isInvolvedWithBinaryInteractions(final Protein protein) {

        boolean involvedWithBinaryInteractions = false;

        String uniprotID = getUniprotID(protein);
        getOut().println("\n\nChecking on Protein: " + uniprotID + "(" + protein.getAc() + ", " + protein.getShortLabel() + ") ...");

        // getting all interactions in which that protein plays a role.
        List interactions = getInteractions(protein);
        getOut().println(interactions.size() + " interactions found.");

        for (int i = 0; i < interactions.size() && involvedWithBinaryInteractions == false; i++) {

            Interaction interaction = (Interaction) interactions.get(i);

            getOut().println("  (" + (i + 1) + ") Interaction: Shortlabel:" + interaction.getShortLabel() + "  AC: " + interaction.getAc());

            if (isNegative(interaction)) {

                getOut().println("\t That interaction or at least one of its experiments is negative, skip it.");
                continue; // skip that interaction
            } else if( ! isBinary( interaction ) ) {
                getOut().println("\t That interaction is not binary, skip it.");
                continue;
            } else {
                // interaction was binary and non negative.
                // now check that the other interactor are proteins and from uniprot ...
                final Collection<Component> components = interaction.getComponents();

                boolean allOK = true;
                for ( Component component : components ) {
                    final Interactor interactor = component.getInteractor();
                    if( interactor instanceof Protein ) {
                        if( ! needsUniprotUpdate( (Protein) interactor ) ) {
                            allOK = false;
                        }
                    } else {
                        allOK = false;
                    }
                }

                if( allOK ) {
                    involvedWithBinaryInteractions = true;
                }
            }

        } // interactions

        return involvedWithBinaryInteractions;
    }

    /**
     * Get a distinct set of Uniprot ID of the protein eligible to export in Swiss-Prot,
     * using a paginated query to avoid excessive memory usage. The pagination is done before
     * the selection of the distinct IDs, so the number of results obtained by this query
     * will be different than the number of maxResults if several rows for the same AC are
     * returned.
     *
     * @param firstResult First result of the page
     * @param maxResults  Maximum number of results.
     *
     * @return a distinct set of Uniprot ID of the protein eligible to export in Swiss-Prot.
     */
    public final Set<String> getEligibleProteins(int firstResult, int maxResults)
            throws SQLException,
                   IntactException,
                   DatabaseContentException {

        List<ProteinImpl> proteins = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getProteinDao().getUniprotProteinsInvolvedInInteractions(firstResult, maxResults);

        getOut().println( proteins.size() + " UniProt protein(s) found in the database" );

        return getEligibleProteins(proteins);
    }

    /**
     * Get a distinct set of Uniprot ID of the protein eligible to export in Swiss-Prot.
     *
     * @return a distinct set of Uniprot ID of the protein eligible to export in Swiss-Prot.
     *
     * @throws java.sql.SQLException error when handling the JDBC connection or query.
     * @throws uk.ac.ebi.intact.business.IntactException
     *
     * @throws uk.ac.ebi.intact.util.uniprotExport.CCLineExport.DatabaseContentException
     *                               if the initialisation process failed (CV not found)
     */
    public final Set<String> getEligibleProteins(Collection<ProteinImpl> proteins)
            throws SQLException,
                   IntactException,
                   DatabaseContentException {

        if (proteins.isEmpty()) {
            return new HashSet<String>();
        }

        Set<String> proteinEligible = new HashSet<String>();

        int proteinCount = 0;

        // Process the proteins one by one.
        for (ProteinImpl protein : proteins) {

            proteinCount++;

            if ((proteinCount % 100) == 0) {
                System.out.print("..." + proteinCount);

                if ((proteinCount % 1000) == 0) {
                    getOut().println("");
                } else {
                    System.out.flush();
                }
            }

            if (isProteinEligible(protein)) {

                // only used in case the current protein is a splice variant
                Protein master = null;
                String uniprotId = null;

                // Note: a protein is low-confidence if it has at least one low-confidence interaction.

                // if this is a splice variant, we try to get its master protein
                if (protein.getShortLabel().indexOf('-') != -1) {

                    String masterAc = getMasterAc(protein);

                    if (masterAc == null) {
                        getOut().println("ERROR: The splice variant having the AC(" + protein.getAc() + ") doesn't have it's master AC.");
                    } else {
                        master = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().getByAc(masterAc);

                        if (master == null) {
                            getOut().println("ERROR: Could not find the master protein of splice variant (" +
                                             protein.getAc() + ") having the AC(" + masterAc + ")");
                        } else {
                            // check that the master hasn't been processed already
                            uniprotId = getUniprotID(master);
                        }
                    }
                } else {
                    uniprotId = getUniprotID(protein);
                }

                if ( uniprotId != null ) {
                    getOut().println("Exporting UniProt( " + uniprotId + " )");
                    proteinEligible.add(uniprotId);
                }

                int count = proteinEligible.size();
                float percentage = ((float) count / (float) proteinCount) * 100;
                getOut().println(count + " protein" + (count > 1 ? "s" : "") +
                                 " eligible for export out of " + proteinCount +
                                 " processed (" + percentage + "%).");
            }
        }// all proteins

        return proteinEligible;
    }

    public boolean isProteinEligible(ProteinImpl protein) {
        // Skip proteins annotated no-uniprot-update
        if (false == needsUniprotUpdate(protein)) {
            getOut().println(protein.getAc() + " " + protein.getShortLabel() + " is not from UniProt, skip it.");
            return false;
        }

        return isInvolvedWithBinaryInteractions(protein);
    }

    public static String formatProtein(String uniprotID) {
        StringBuffer sb = new StringBuffer();

        sb.append(uniprotID).append('\t');
        sb.append("IntAct").append('\t');
        sb.append(uniprotID).append('\t');
        sb.append('-');

        return sb.toString();
    }

    public static void display(Set proteins, PrintStream out) {
        for (Iterator iterator = proteins.iterator(); iterator.hasNext();) {
            String uniprotID = (String) iterator.next();
            out.println(formatProtein(uniprotID));
        }
    }

    private static void writeToFile(Set proteins, Writer out) throws IOException {
        for (Iterator iterator = proteins.iterator(); iterator.hasNext();) {
            String uniprotID = (String) iterator.next();
            out.write(formatProtein(uniprotID));
            out.write(NEW_LINE);
        }
    }

    private static void displayUsage(Options options) {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DRLineExport [-file <filename>] [-debug] [-debugFile]", options);
    }

    public static void main(String[] args) throws Exception {

        // create Option objects
        Option helpOpt = new Option("help", "print this message");

        Option drExportOpt = OptionBuilder.withArgName("drExportFilename").hasArg().withDescription("DR export output filename.").create("drExport");

        Option debugOpt = OptionBuilder.withDescription("Shows verbose output.").create("debug");
        debugOpt.setRequired(false);

        Option debugFileOpt = OptionBuilder.withDescription("Store verbose output in the specified file.").create("debugFile");
        debugFileOpt.setRequired(false);

        Options options = new Options();

        options.addOption(drExportOpt);
        options.addOption(helpOpt);
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

        DRLineExportLenient exporter = new DRLineExportLenient();

        boolean debugEnabled = line.hasOption("debug");
        boolean debugFileEnabled = line.hasOption("debugFile");
        exporter.setDebugEnabled(debugEnabled);
        exporter.setDebugFileEnabled(debugFileEnabled);

        boolean filenameGiven = line.hasOption("drExport");
        String filename = null;
        if (filenameGiven == true) {
            filename = line.getOptionValue("drExport");
        }

        // Prepare CC output file.
        File file = null;
        if (filename != null) {
            try {
                file = new File(filename);
                if (file.exists()) {
                    System.out.println("ERROR: Please give a new file name for the DR output file: " + file.getAbsoluteFile());
                    System.out.println("ERROR: We will use the default filename instead (instead of overwritting the existing file).");
                    filename = null;
                    file = null;
                }
            } catch (Exception e) {
                // nothing, the default filename will be given
            }
        }

        if (filename == null || file == null) {
            filename = "DRLineExport_" + TIME + ".txt";
            System.out.println("Using default filename for the DR export: " + filename);
            file = new File(filename);
        }

        System.out.println("DR export will be saved in: " + filename);

        System.out.println("Database instance: " + IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbName());
        System.out.println("User: " + IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbUserName());

        // get the set of Uniprot ID to be exported to Swiss-Prot, using a paginated query
        // to avoid OutOfMemory errors
        Set<String> proteinEligible;
        int firstResult = 0;
        int maxResults = 100000;

        do {
            proteinEligible = exporter.getEligibleProteins(firstResult, maxResults);

            System.out.println(proteinEligible.size() + " protein(s) selected for export using a paginated query. First result: " + firstResult);

            // save it to a file.

            BufferedWriter out = null;
            FileWriter fw = null;
            try {
                fw = new FileWriter(file);
                out = new BufferedWriter(fw);
                writeToFile(proteinEligible, out);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("ERROR: Could not save the result to :" + filename);
                System.out.println("ERROR: Displays the result on STDOUT:\n\n\n");

                display(proteinEligible, System.out);
                System.exit(1);

            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        System.exit(1);
                    }
                }

                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        System.exit(1);
                    }
                }
            }

            firstResult = firstResult + maxResults;

        } while (!proteinEligible.isEmpty());

        System.exit(0);
    }
}