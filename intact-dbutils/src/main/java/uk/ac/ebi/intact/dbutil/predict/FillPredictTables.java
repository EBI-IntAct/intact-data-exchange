/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.predict;

import org.hibernate.Session;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.InteractionImpl;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.ProteinImpl;
import uk.ac.ebi.intact.persistence.dao.BaseDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.InteractionDao;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Runs the PAYG algorithm.
 *
 * @author konrad.paszkiewicz (konrad.paszkiewicz@ic.ac.uk)
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class FillPredictTables {

    /**
     * Data access.
     */
    private DaoFactory daoFactory;

    /**
     * Database connection
     */
    private Connection con;

    /**
     * To generate a random number.
     */
    private Random myRandom = new Random();

    /**
     * Output
     */
    private PrintStream output;

    /**
     * Default constructor.
     */
    public FillPredictTables(PrintStream output) {
        daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        this.output = output;
    }

    private Connection getConnection() {

        BaseDao dao = daoFactory.getBaseDao();
        con = ((Session) dao.getSession()).connection();
        return con;
    }

    private void closeConnection() throws SQLException {
        con.close();
    }

    private List getNodes(ResultSet rs) throws SQLException {
        List list = new ArrayList();
        while (rs.next()) {
            list.add(new Node(rs.getString(1), rs.getString(2)));
        }
        return list;
    }

    private List getEdges(ResultSet rs) throws SQLException {
        List list = new ArrayList();
        while (rs.next()) {
            list.add(new Edge(rs.getString(1), rs.getString(2)));
        }
        return list;
    }

    private void PrepareTables() throws SQLException, IntactTransactionException {

        Statement stmt = null;
        try {
            //Create current_edge table
            fillCurrentEdgesTable(); // Get interactions from intact database

            // NOTE: the statement is closed in fillCurrentEdgesTable()
            IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            stmt = getConnection().createStatement();
            stmt.executeUpdate("UPDATE ia_payg_current_edge SET seen=0, conf=0");

            //Setup the Pay-As-You-Go table
            stmt.executeUpdate("INSERT INTO ia_payg_temp_node " +
                               "SELECT distinct nidA, species " +
                               "FROM ia_payg_current_edge");

            stmt.executeUpdate("INSERT INTO ia_payg_temp_node " +
                               "SELECT distinct nidB, species " +
                               "FROM ia_payg_current_edge");

            ResultSet rs = stmt.executeQuery("SELECT distinct nid, species " +
                                             "FROM ia_payg_temp_node " +
                                             "WHERE nid IS NOT NULL");

            for (Iterator iter = getNodes(rs).iterator(); iter.hasNext();) {
                Node tn = (Node) iter.next();
                String nid = tn.getNid();
                String species = tn.getSpecies();
//                System.out.println("Inserting ia_payg value " + nid);
                ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) " +
                                                  "FROM ia_payg_current_edge " +
                                                  "WHERE nidA=\'" + nid + "\'");
                if (rs2.next()) {
                    if (rs2.getInt(1) == 0) {
                        stmt.executeUpdate("INSERT INTO ia_payg (nid, bait, prey, indegree, outdegree, qdegree, eseen, econf, really_used_as_bait, species) " +
                                           "VALUES (\'" + nid + "\',0,0,0,0,0.0,0,0,'N',\'" + species + "\')");
                    } else {
                        stmt.executeUpdate("INSERT INTO ia_payg (nid, bait, prey,indegree, outdegree, qdegree, eseen, econf,really_used_as_bait, species) " +
                                           "VALUES (\'" + nid + "\',0,0,0,0,0.0,0,0,'Y',\'" + species + "\')");
                    }
                }

                rs2.close();

            } // for

            rs.close();

            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        }
        catch (SQLException sqle) {
            sqle.printStackTrace();
            throw sqle;
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException e) {
                }
            }
        }
    }

    public static final int CHUNK_SIZE = 300;

    private void fillCurrentEdgesTable() throws SQLException, IntactTransactionException {

        String bait = "";

        // Get the interactions count

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        InteractionDao idao = daoFactory.getInteractionDao();
        int interactionCount = idao.countAll();
        idao = null;
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();

        // Process all interaction by chunk
        System.out.println(interactionCount + " interaction to process...");
        int i = 0;
        List<InteractionImpl> interactions = null;

        Statement stmt = null;

        try {

            while (i < interactionCount) {

                IntactContext.getCurrentInstance().getDataContext().beginTransaction();
                idao = daoFactory.getInteractionDao();

                stmt = getConnection().createStatement();

                System.out.println("Processing interactions " + i + ".." + (i + CHUNK_SIZE));
                interactions = idao.getAll(i, CHUNK_SIZE);

                for (InteractionImpl interaction : interactions) {
                    i++;

                    Collection components = interaction.getComponents();

                    // For each interaction get the components
                    for (Iterator iterator2 = components.iterator(); iterator2.hasNext();) {
                        Component component = (Component) iterator2.next();

                        Interactor interactor = component.getInteractor();
                        if (interactor != null) {//  interactor.getClass().isAssignableFrom( Protein.class )) {

                            if (interactor instanceof ProteinImpl) {

                                String role = component.getCvExperimentalRole().getShortLabel();
                                String species = interactor.getBioSource().getTaxId();

                                if (role.equals("bait")) {
                                    bait = interactor.getShortLabel();
//                                  System.out.println("Bait: " + bait);
                                } else if (role.equals("prey")) {
                                    String prey = interactor.getShortLabel();
                                    stmt.executeUpdate("INSERT INTO ia_payg_current_edge " +
                                                       "VALUES(\'" + bait + "\',\'" + prey + "\',0,0,\'" + species + "\')");
//                                  System.out.println("Prey: " + prey);
                                }
                            } else {
                                output.println("[WARN] Skipping non Protein interactor: " + interactor.getClass().getSimpleName() + ". " +
                                               "AC: " + interactor.getAc() + " Shortlabel:" + interactor.getShortLabel());
                            }
                        }
                    }
                } // for interactions

                interactions.clear();
                interactions = null;

                // release the memory for that chunk of data.
                IntactContext.getCurrentInstance().getDataContext().commitTransaction();

            } // while

        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException e) {
                }
            }
        }
    }

    private ArrayList getSpeciesTypes() throws SQLException, IntactTransactionException {
        ArrayList species = new ArrayList();
        Statement stmt = null;

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT distinct species " +
                                             "FROM ia_payg");
            while (rs.next()) {
                species.add(rs.getString(1));
            }
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException e) {
                }
            }
        }

        IntactContext.getCurrentInstance().getDataContext().commitTransaction();

        return species;
    }

    private void doPayAsYouGo(String species) throws SQLException, IntactTransactionException {

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        String nextbait = getNextNode(species);
        // while we have uncovered node
        for (int counter = 1; !nextbait.equals(""); counter++) {
            virtualPullOut(nextbait, counter, species);
            nextbait = getNextNode(species);
        }

        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }

    private String getNextNode(String species) throws SQLException {

        // selecting the next bait but only from one species
        Statement stmt = null;
        int in = 0;
        double avg = 0.0;
        String nid = "";
        try {
            System.out.print('.');

            // determine avg Q in of node sampled so far
            stmt = getConnection().createStatement();
            String query = "SELECT avg( indegree) " +
                           "FROM ia_payg " +
                           "WHERE bait=0 AND species =\'" + species + "\'";

            // System.out.println("Q>"+Query);
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                avg = rs.getDouble(1);
            }
            rs.close();
//            System.out.println("Current avg=" + avg);

            // get the node with max indegree
            rs = stmt.executeQuery("SELECT nID, indegree, qdegree, outdegree " +
                                   "FROM ia_payg " +
                                   "WHERE ROWNUM=1 " +
                                   "      AND bait=0 " +
                                   "      AND species =\'" + species + "\' " +
                                   "ORDER BY indegree DESC, qdegree DESC");

            if (rs.next()) {
                nid = rs.getString(1);
                in = rs.getInt(2);
//                double q = rs.getDouble(3);
//                out = rs.getInt(4);
            }
            rs.close();
//            System.out.println("nextNode:" + nid + " out=" + out + "\tin=" + in + "\tq=" + q);

            // if we are below average : random sampling
            if (in <= avg) {
                // we have nothing above average or q is empty
                // since we have nothing yet, so random jumpstart
//                System.out.println(">>>> random!");
//                System.out.println("Species is: " + species);
                rs = stmt.executeQuery("SELECT nID " +
                                       "FROM ia_payg " +
                                       "WHERE bait=0 AND species =\'" + species + "\'");
                String rnid = getRandomNid(rs);
                // Only set it if it isn't null.
                if (rnid != null) {
                    nid = rnid;
                }
                rs.close();
            } // end if nid = 0
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException e) {
                }
            }
        }
        // System.out.println("getNextNode:"+nid);
        return nid;
    }

    private void virtualPullOut(String id, int step, String species) throws SQLException {
        Statement stmt = null;
        try {
            // determine k & deltaK
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) " +
                                             "FROM ia_payg_current_edge " +
                                             "WHERE species =\'" + species + "\' " +
                                             "      AND (nidA=\'" + id + "\' OR nidB=\'" + id + "\') " +
                                             "      AND (nidA!=nidB) " +
                                             "      AND nidA IS NOT NULL " +
                                             "      AND nidB IS NOT NULL");
            int k = 0;
            if (rs.next()) {
                k = rs.getInt(1);
            }
            rs.close();
//            System.out.println("K = " + k);
            double delta = 1 / (double) k;
//            System.out.println("delta = " + delta);

            // mark the bait & set k
            stmt.executeUpdate("UPDATE ia_payg " +
                               "SET bait=" + step + ", outdegree=" + k + " " +
                               "WHERE nID=\'" + id + "\' " +
                               "      AND species =\'" + species + "\'");

            // then mark all the adjacent edge as covered
            stmt.executeUpdate("UPDATE ia_payg_current_edge " +
                               "SET conf=" + step + " " +
                               "WHERE (nidA=\'" + id + "\' OR nidB=\'" + id + "\')  " +
                               "      AND species = \'" + species + "\' " +
                               "      AND nidA != nidB " +
                               "      AND seen > 0 " +
                               "      AND conf = 0 " +
                               "      AND nidA IS NOT NULL " +
                               "      AND nidB IS NOT NULL");

            stmt.executeUpdate("UPDATE ia_payg_current_edge " +
                               "SET seen=" + step + " " +
                               "WHERE (nidA = \'" + id + "\' OR nidB = \'" + id + "\') " +
                               "      AND nidA != nidB  " +
                               "      AND species = \'" + species + "\' " +
                               "      AND seen = 0");

            // conduct the virtualPullOut
            // 1. mark all the neighbours as prey
            rs = stmt.executeQuery("SELECT nidA, nidB " +
                                   "FROM ia_payg_current_edge " +
                                   "WHERE species =\'" + species + "\' " +
                                   "      AND (nidA=\'" + id + "\' OR nidB=\'" + id + "\') " +
                                   "      AND (nidA!=nidB) " +
                                   "      AND nidA IS NOT NULL " +
                                   "      AND nidB IS NOT NULL");

            for (Iterator iter = getEdges(rs).iterator(); iter.hasNext();) {
                Edge edge = (Edge) iter.next();
                String aId = edge.getIda();
                String bId = edge.getIdb();
                String preyId = "";
                if (aId.equals(id)) {
                    preyId = bId;
                } else {
                    preyId = aId;
                }
//                System.out.println(">> " + id + " =-> " + preyId);
                stmt.executeUpdate("UPDATE ia_payg " +
                                   "SET prey=" + step + " " +
                                   "WHERE nID=\'" + preyId + "\' " +
                                   "      AND prey = 0 " +
                                   "      AND species = \'" + species + "\'");

                // update the indegree and delta for all adjacent node
                stmt.executeUpdate("UPDATE ia_payg " +
                                   "SET indegree=indegree+1, qdegree=qdegree+" + delta + " " +
                                   "WHERE nID=\'" + preyId + "\' " +
                                   "      AND species =\'" + species + "\'");
            } // next interaction of this id
            rs.close();
            // 2. compute the Nr. of edge seen & confirmed so far
            int nSeen = 0;
            rs = stmt.executeQuery("SELECT COUNT(*) " +
                                   "FROM ia_payg_current_edge " +
                                   "WHERE seen>0 " +
                                   "      AND (nidA!=nidB) " +
                                   "      AND nidA IS NOT NULL " +
                                   "      AND nidB IS NOT NULL " +
                                   "      AND species =\'" + species + "\'");
            if (rs.next()) {
                nSeen = rs.getInt(1);
            }
//            System.out.println("seen=" + nSeen);
            rs.close();

            int nConfirm = 0;
            rs = stmt.executeQuery("SELECT COUNT(*) " +
                                   "FROM ia_payg_current_edge " +
                                   "WHERE conf>0 " +
                                   "      AND (nidA!=nidB) " +
                                   "      AND nidA IS NOT NULL " +
                                   "      AND nidB IS NOT NULL " +
                                   "      AND species =\'" + species + "\'");
            if (rs.next()) {
                nConfirm = rs.getInt(1);
            }
//            System.out.println("conf=" + nConfirm);

            // Results saved into ia_payg
            stmt.executeUpdate("UPDATE ia_payg " +
                               "SET eseen =" + nSeen + ", econf =" + nConfirm + " " +
                               "WHERE nID=\'" + id + "\' AND species =\'" + species + "\'");
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException e) {
                }
            }
        }
    }

    private String getRandomNid(ResultSet rs) throws SQLException {
        // The temp list to collect values from the result set.
        List list = new ArrayList();
        // Add to the list.
        while (rs.next()) {
            list.add(rs.getString(1));
        }
        if (list.isEmpty()) {
            return null;
        }
        // Get a random value between 0 and the list size - 1.
        return (String) list.get(myRandom.nextInt(list.size()));
    }

    private void cleanTables() throws SQLException {
        Statement stmt = getConnection().createStatement();
        stmt.executeUpdate("delete FROM ia_payg");
        stmt.executeUpdate("delete FROM ia_payg_temp_node");
        stmt.executeUpdate("delete FROM ia_payg_current_edge");
    }

    public static void runTask(PrintStream output) throws Exception {
        FillPredictTables pred = null;
        try {

            pred = new FillPredictTables(output);

            // cleaning tables
            output.println("Clearing pay-as-you-go tables...");
            pred.cleanTables();

            output.println("Preparing tables...");
            pred.PrepareTables();

            ArrayList species_list = pred.getSpeciesTypes();

            for (ListIterator iterator = species_list.listIterator(); iterator.hasNext();) {
                String species = (String) iterator.next();

                //Perform Pay-As-You-Go algorithm on the interaction network for each species
                output.println("\tPerforming Pay-As-You-Go Strategy for Taxonomic ID: " + species);
                pred.doPayAsYouGo(species);
            }
        }
        catch (SQLException sqle) {
            while (sqle != null) {
                output.println("**** SQLException ****");
                output.println("** SQLState: " + sqle.getSQLState());
                output.println("** Message: " + sqle.getMessage());
                output.println("** Error Code: " + sqle.getErrorCode());
                output.println("***********");

                sqle = sqle.getNextException();
            }
        }
        catch (IntactException ie) {
            ie.printStackTrace();
        }
        finally {
            // Close the connection regardless.
            if (pred != null) {
                pred.closeConnection();
            }
        }
    }

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        long stop = start;


        runTask(System.out);

        stop = System.currentTimeMillis();
        System.out.println("Time elapsed: " + ((stop - start) / (1000 * 60)) + " minute(s)."); // in minutes
    }
}