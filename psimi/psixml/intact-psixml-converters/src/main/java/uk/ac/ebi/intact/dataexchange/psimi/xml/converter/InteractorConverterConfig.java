package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the configuration of the converter.
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 2.0.0
 */
public class InteractorConverterConfig {

    /**
     * If true, polymer sequence is ommited in the convertion.
     */
    private boolean excludePolymerSequence = false;

    /**
     * If true, no aliases is converted in the interactor.
     */
    private boolean excludeInteractorAliases = false;

    /**
     * Only export Interactor Xref that have a CvDatabase that is in the list.
     */
    private Set<CvDatabase> includeInteractorXrefCvDatabase = new HashSet<CvDatabase>( );

    /**
     * Do not export Interactor Xref that have a CvDatabase that is in the list.
     */
    private Set<CvDatabase> excludeInteractorXrefCvDatabase = new HashSet<CvDatabase>( );

    /**
     * Only export Interactor Xref that have a CvXrefQualifier that is in the list.
     */
    private Set<CvXrefQualifier> includeInteractorXrefCvXrefQualifier = new HashSet<CvXrefQualifier>( );

    /**
     * Do not export Interactor Xref that have a CvXrefQualifier that is in the list.
     */
    private Set<CvXrefQualifier> excludeInteractorXrefCvXrefQualifier = new HashSet<CvXrefQualifier>( );

    ///////////////////
    // Constructor

    public InteractorConverterConfig() {
    }

    ///////////////////////////////
    // excludePolymerSequence

    public boolean isExcludePolymerSequence() {
        return excludePolymerSequence;
    }

    public void setExcludePolymerSequence( boolean excludePolymerSequence ) {
        this.excludePolymerSequence = excludePolymerSequence;
    }

    ///////////////////////////////
    // excludeInteractorAliases

    public boolean isExcludeInteractorAliases() {
        return excludeInteractorAliases;
    }

    public void setExcludeInteractorAliases( boolean excludeInteractorAliases ) {
        this.excludeInteractorAliases = excludeInteractorAliases;
    }

    ////////////////////////////////////
    // includeInteractorXrefCvDatabase

    public void addIncludeInteractorXrefCvDatabase( CvDatabase database ) {
        if ( database == null ) {
            throw new NullPointerException( "You must give a non null database" );
        }

        includeInteractorXrefCvDatabase.add
                ( database );
    }

    public void removeIncludeInteractorXrefCvDatabase( CvDatabase database ) {
        if ( database == null ) {
            throw new NullPointerException( "You must give a non null database" );
        }

        includeInteractorXrefCvDatabase.remove( database );
    }

    public boolean isIncluded( CvDatabase database ) {
        return includeInteractorXrefCvDatabase.contains( database );
    }

    public boolean hasIncludesCvDatabase() {
        return !includeInteractorXrefCvDatabase.isEmpty();
    }

    ////////////////////////////////////////
    // includeInteractorXrefCvXrefQualifier

    public void addIncludeInteractorXrefCvXrefQualifier( CvXrefQualifier qualifier ) {
        if ( qualifier == null ) {
            throw new NullPointerException( "You must give a non null cvDatabase" );
        }

        includeInteractorXrefCvXrefQualifier.add( qualifier );
    }

    public void removeIncludeInteractorXrefCvXrefQualifier( CvXrefQualifier qualifier ) {
        if ( qualifier == null ) {
            throw new NullPointerException( "You must give a non null qualifier" );
        }

        includeInteractorXrefCvXrefQualifier.remove( qualifier );
    }

    public boolean isIncluded( CvXrefQualifier qualifier ) {
        return includeInteractorXrefCvXrefQualifier.contains( qualifier );
    }

    public boolean hasIncludesCvXrefQualifier() {
        return !includeInteractorXrefCvXrefQualifier.isEmpty();
    }

    ////////////////////////////////////
    // excludeInteractorXrefCvDatabase

    public void addExcludeInteractorXrefCvDatabase( CvDatabase database ) {
        if ( database == null ) {
            throw new NullPointerException( "You must give a non null database" );
        }

        excludeInteractorXrefCvDatabase.add( database );
    }

    public void removeExcludeInteractorXrefCvDatabase( CvDatabase database ) {
        if ( database == null ) {
            throw new NullPointerException( "You must give a non null database" );
        }

        excludeInteractorXrefCvDatabase.remove( database );
    }

    public boolean isExcluded( CvDatabase database ) {
        return excludeInteractorXrefCvDatabase.contains( database );
    }

    public boolean hasExcludesCvDatabase() {
        return !excludeInteractorXrefCvDatabase.isEmpty();
    }

    ////////////////////////////////////////
    // excludeInteractorXrefCvXrefQualifier

    public void addExcludeInteractorXrefCvXrefQualifier( CvXrefQualifier qualifier ) {
        if ( qualifier == null ) {
            throw new NullPointerException( "You must give a non null cvDatabase" );
        }

        excludeInteractorXrefCvXrefQualifier.add( qualifier );
    }

    public void removeExcludeInteractorXrefCvXrefQualifier( CvXrefQualifier qualifier ) {
        if ( qualifier == null ) {
            throw new NullPointerException( "You must give a non null qualifier" );
        }

        excludeInteractorXrefCvXrefQualifier.remove( qualifier );
    }

    public boolean isExcluded( CvXrefQualifier qualifier ) {
        return excludeInteractorXrefCvXrefQualifier.contains( qualifier );
    }

    public boolean hasExcludesCvXrefQualifier() {
        return !excludeInteractorXrefCvXrefQualifier.isEmpty();
    }
}
