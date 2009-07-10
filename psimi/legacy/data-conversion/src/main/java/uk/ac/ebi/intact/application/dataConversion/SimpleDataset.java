/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

/**
 * Simple representation of a dataset.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19-Sep-2006</pre>
 */
public class SimpleDataset {

    /**
     * Name of the dataset.
     */
    private String name;

    /**
     * publication
     */
    private String pmid;

    /**
     * Short description of the dataset.
     */
    private String description;

    public SimpleDataset( String name ) {
        if ( name == null ) {
            throw new IllegalArgumentException( "Name cannot be null." );
        }
        this.name = name;
    }

    /**
     * Returns name of the dataset.
     *
     * @return name of the dataset.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of the dataset.
     *
     * @param name name of the dataset.
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * Returns short description of the dataset.
     *
     * @return short description of the dataset.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets short description of the dataset.
     *
     * @param description short description of the dataset.
     */
    public void setDescription( String description ) {
        this.description = description;
    }

    /**
     * Returns publication
     *
     * @return publication
     */
    public String getPmid() {
        return pmid;
    }

    /**
     * Sets publication
     *
     * @param pmid publication
     */
    public void setPmid( String pmid ) {
        this.pmid = pmid;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        final SimpleDataset that = (SimpleDataset) o;

        if ( !name.equals( that.name ) ) {
            return false;
        }
        if ( pmid != null ? !pmid.equals( that.pmid ) : that.pmid != null ) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = name.hashCode();
        result = 29 * result + ( pmid != null ? pmid.hashCode() : 0 );
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "SimpleDataset" );
        sb.append( "{name='" ).append( name ).append( '\'' );
        sb.append( ", pmid='" ).append( pmid ).append( '\'' );
        sb.append( ", description='" ).append( description ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}