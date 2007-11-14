/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

import uk.ac.ebi.intact.model.Protein;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class ProteinHolder {

    private Protein protein;
    private Protein spliceVariant;
    private ProteinInteractorTag proteinInteractor;

    ////////////////////////
    // Constructors

    public ProteinHolder( ProteinInteractorTag proteinInteractor ) {
        if( proteinInteractor == null ) {
            throw new IllegalArgumentException( "You must give the definition of the protein." );
        }
        this.proteinInteractor = proteinInteractor;
    }

    public ProteinHolder( Protein protein, Protein spliceVariant, ProteinInteractorTag proteinInteractor ) {
        this(proteinInteractor);
        this.protein = protein;
        this.spliceVariant = spliceVariant;
    }

    public ProteinHolder( Protein protein, ProteinInteractorTag proteinInteractor ) {
        this(proteinInteractor);
        this.protein = protein;
    }

    /////////////////////
    // Getters

    public Protein getProtein() {
        return protein;
    }

    public boolean isSpliceVariantExisting() {
        return spliceVariant != null;
    }

    public Protein getSpliceVariant() {
        return spliceVariant;
    }

    /**
     * a protein is from uniprot only if the attached ProteinInteractorTag answers true to that question.
     * @return
     */
    public boolean isUniprot() {
        if( proteinInteractor != null ) {
            return proteinInteractor.hasUniProtXref();
        }

        return false;
    }

    public ProteinInteractorTag getProteinInteractor() {
        return proteinInteractor;
    }

    //////////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        final ProteinHolder that = (ProteinHolder) o;

        if ( protein != null ? !protein.equals( that.protein ) : that.protein != null ) {
            return false;
        }
        if ( !proteinInteractor.equals( that.proteinInteractor ) ) {
            return false;
        }
        if ( spliceVariant != null ? !spliceVariant.equals( that.spliceVariant ) : that.spliceVariant != null ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = ( protein != null ? protein.hashCode() : 0 );
        result = 29 * result + ( spliceVariant != null ? spliceVariant.hashCode() : 0 );
        result = 29 * result + proteinInteractor.hashCode();
        return result;
    }

    /////////////////////
    // toString

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append( "ProteinHolder" );
        sb.append( "{protein=" ).append( protein );
        sb.append( ", spliceVariant=" ).append( spliceVariant );
        sb.append( ", proteinInteractorTag=" ).append( proteinInteractor );
        sb.append( '}' );

        return sb.toString();
    }

    public void setProtein( Protein protein ) {
        this.protein = protein;
    }
}
