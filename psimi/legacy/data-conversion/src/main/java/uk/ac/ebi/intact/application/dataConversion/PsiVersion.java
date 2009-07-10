/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

/**
 * TODO comment
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class PsiVersion {

    ///////////////////////////
    // Constants

    /**
     * Psi version 1.
     */
    public static final PsiVersion VERSION_1 = new PsiVersion( "1" );

    /**
     * Psi version 2.
     */
    public static final PsiVersion VERSION_2 = new PsiVersion( "2" );

    /**
     * Psi version 2.5.
     */
    public static final PsiVersion VERSION_25 = new PsiVersion( "2.5" );


    ////////////////////////////
    // Instance variables

    private String version;


    ////////////////////////////
    // Constructors

    private PsiVersion( String version ) {

        this.version = version;
    }

    public static PsiVersion getVersion1() {
        return VERSION_1;
    }

    public static PsiVersion getVersion2() {
        return VERSION_2;
    }

    public static PsiVersion getVersion25() {
        return VERSION_25;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Gets the correspondent <code>PsiVersion</code> instance using the version String
     * @param version accepted values are '1','2' and '2.5'
     * @return the PsiVersion object for that value
     * @throws IllegalArgumentException if the version passed is not valid
     */
    public static PsiVersion valueOf(String version) throws IllegalArgumentException
    {
        PsiVersion psiVersionFromValue = new PsiVersion(version);

        if (psiVersionFromValue.equals(getVersion1()))
        {
            return getVersion1();
        }
        else if (psiVersionFromValue.equals(getVersion2()))
        {
           return getVersion2();
        }
        else if (psiVersionFromValue.equals(getVersion25()))
        {
            return getVersion25();
        }
        else if (version.equals("1.0"))
        {
            return getVersion1();
        }

        throw new IllegalArgumentException("Invalid PSI version: "+version);

    }

    /////////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof PsiVersion ) ) {
            return false;
        }

        final PsiVersion psiVersion = (PsiVersion) o;

        return version.equals(psiVersion.getVersion());

    }

    public int hashCode() {
        return version.hashCode();
    }


    public String toString() {
        return "PsiVersion{" +
               "version=" + version +
               "}";
    }
}
