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
public class Edge {

    private String myIda;
    private String myIdb;

    public Edge( String ida, String idb ) {
        myIda = ida;
        myIdb = idb;
    }

    // Getter methods.

    public String getIda() {
        return myIda;
    }

    public String getIdb() {
        return myIdb;
    }
}