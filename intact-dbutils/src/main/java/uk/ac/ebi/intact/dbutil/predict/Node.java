/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.predict;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14-Nov-2006</pre>
 */
public class Node {

    private String myNid;
    private String mySpecies;

    public Node( String nid, String species ) {
        myNid = nid;
        mySpecies = species;
    }

    public String getNid() {
        return myNid;
    }

    public String getSpecies() {
        return mySpecies;
    }
}