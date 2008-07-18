/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.psimitab;

import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * IntAct extension of a BinaryInteractionImpl.
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntactBinaryInteraction extends BinaryInteractionImpl {

    private static final long serialVersionUID = -8872335762187393793L;

    private List<CrossReference> experimentalRoleA;

    private List<CrossReference> experimentalRoleB;
    private List<CrossReference> biologicalRoleA;

    private List<CrossReference> biologicalRoleB;
    private List<CrossReference> propertiesA;

    private List<CrossReference> propertiesB;
    private List<CrossReference> interactorTypeA;

    private List<CrossReference> interactorTypeB;

    private List<CrossReference> hostOrganism;

    private List<String> expansionMethods;

    private List<String> dataset;
    private Interactor interactorA;

    private Interactor interactorB;
    private static int expectedColumnCount = 24;

    //////////////////
    // Constructors

    public IntactBinaryInteraction( Interactor interactorA, Interactor interactorB ) {
        super( interactorA, interactorB );
        this.interactorA = interactorA;
        this.interactorB = interactorB;
    }

    ///////////////////////////
    // Getters and Setters

    public List<CrossReference> getExperimentalRolesInteractorA() {
        if ( experimentalRoleA == null ) {
            experimentalRoleA = new ArrayList<CrossReference>( 2 );
        }
        return experimentalRoleA;
    }

    public void setExperimentalRolesInteractorA( List<CrossReference> experimentalRoles ) {
        this.experimentalRoleA = experimentalRoles;
    }

    public List<CrossReference> getExperimentalRolesInteractorB() {
        if ( experimentalRoleB == null ) {
            experimentalRoleB = new ArrayList<CrossReference>( 2 );
        }
        return experimentalRoleB;
    }

    public void setExperimentalRolesInteractorB( List<CrossReference> experimentalRoles ) {
        this.experimentalRoleB = experimentalRoles;
    }

    public List<CrossReference> getBiologicalRolesInteractorA() {
        if ( biologicalRoleA == null ) {
            biologicalRoleA = new ArrayList<CrossReference>( 2 );
        }
        return biologicalRoleA;
    }

    public void setBiologicalRolesInteractorA( List<CrossReference> biologicalRoles ) {
        this.biologicalRoleA = biologicalRoles;
    }

    public List<CrossReference> getBiologicalRolesInteractorB() {
        if ( biologicalRoleB == null ) {
            biologicalRoleB = new ArrayList<CrossReference>( 2 );
        }
        return biologicalRoleB;
    }

    public void setBiologicalRolesInteractorB( List<CrossReference> biologicalRoles ) {
        this.biologicalRoleB = biologicalRoles;
    }

    public List<CrossReference> getPropertiesA() {
        if ( propertiesA == null ) {
            propertiesA = new ArrayList<CrossReference>( 2 );
        }
        return propertiesA;
    }

    public void setPropertiesA( List<CrossReference> propertiesA ) {
        this.propertiesA = propertiesA;
    }

    public List<CrossReference> getPropertiesB() {
        if ( propertiesB == null ) {
            propertiesB = new ArrayList<CrossReference>( 2 );
        }
        return propertiesB;
    }

    public void setPropertiesB( List<CrossReference> propertiesB ) {
        this.propertiesB = propertiesB;
    }

    public List<CrossReference> getInteractorTypeA() {
        if ( interactorTypeA == null ) {
            interactorTypeA = new ArrayList<CrossReference>( 2 );
        }
        return interactorTypeA;
    }

    public void setInteractorTypeA( List<CrossReference> interactorType ) {
        this.interactorTypeA = interactorType;
    }

    public List<CrossReference> getInteractorTypeB() {
        if ( interactorTypeB == null ) {
            interactorTypeB = new ArrayList<CrossReference>( 2 );
        }
        return interactorTypeB;
    }

    public void setInteractorTypeB( List<CrossReference> interactorType ) {
        this.interactorTypeB = interactorType;
    }

    public List<CrossReference> getHostOrganism() {
        if ( hostOrganism == null ) {
            hostOrganism = new ArrayList<CrossReference>( 2 );
        }
        return hostOrganism;
    }

    public void setHostOrganism( List<CrossReference> hostOrganism ) {
        this.hostOrganism = hostOrganism;
    }

    /**
     * @deprecated use getExpansionMethods instead.
     */
    @Deprecated
    public String getExpansionMethod() {
        if ( hasExpansionMethods() ) {
            return expansionMethods.iterator().next();
        } else {
            return null;
        }
    }

    public List<String> getExpansionMethods() {
        if ( expansionMethods == null ) {
            expansionMethods = new ArrayList<String>( 2 );
        }
        return expansionMethods;
    }

    /**
     * @deprecated use setExpansionMethods instead.
     */
    @Deprecated
    public void setExpansionMethod( String expansionMethod ) {
        this.expansionMethods = Arrays.asList( expansionMethod );
    }

    public void setExpansionMethods( List<String> expansionMethods ) {
        this.expansionMethods = expansionMethods;
    }

    public List<String> getDataset() {
        if ( dataset == null ) {
            dataset = new ArrayList<String>( 2 );
        }
        return dataset;
    }

    public void setDataset( List<String> dataset ) {
        this.dataset = dataset;
    }

    public boolean hasExperimentalRolesInteractorA() {
        return !( experimentalRoleA == null || experimentalRoleA.isEmpty() );
    }

    public boolean hasExperimentalRolesInteractorB() {
        return !( experimentalRoleB == null || experimentalRoleB.isEmpty() );
    }

    public boolean hasPropertiesA() {
        return !( propertiesA == null || propertiesA.isEmpty() );
    }

    public boolean hasPropertiesB() {
        return !( propertiesB == null || propertiesB.isEmpty() );
    }

    public boolean hasInteractorTypeA() {
        return !( interactorTypeA == null || interactorTypeA.isEmpty() );
    }

    public boolean hasInteractorTypeB() {
        return !( interactorTypeB == null || interactorTypeB.isEmpty() );
    }

    public boolean hasHostOrganism() {
        return !( hostOrganism == null || hostOrganism.isEmpty() );
    }

    /**
     * @deprecated use hasExpansionMethods() instead.
     */
    @Deprecated
    public boolean hasExpansionMethod() {
        return hasExpansionMethods();
    }

    public boolean hasExpansionMethods() {
        return !( expansionMethods == null || expansionMethods.isEmpty() );
    }

    public boolean hasDatasetName() {
        return !( dataset == null || dataset.isEmpty() );
    }

    public boolean hasBiologicalRolesInteractorA() {
        return !( biologicalRoleA == null || biologicalRoleA.isEmpty() );
    }

    public boolean hasBiologicalRolesInteractorB() {
        return !( biologicalRoleB == null || biologicalRoleB.isEmpty() );
    }

    /**
     * @return number of expected columns
     */
    public int getExpectedColumnCount() {
        return expectedColumnCount;
    }

    /**
     * Setter of the number of expected columns
     *
     * @param expectedColumnCount of expected columns
     */
    public void setExpectedColumnCount( int expectedColumnCount ) {
        this.expectedColumnCount = expectedColumnCount;
    }

    ////////////////////////
    // Object's override

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "\n" );
        sb.append( "BinaryInteractionImpl" );
        sb.append( "{interactorA=" ).append( interactorA );
        sb.append( ", interactorB=" ).append( interactorB );
        sb.append( ", ExperimentalRoleInteractorA=" ).append( experimentalRoleA );
        sb.append( ", ExperimentalRoleInteractorB=" ).append( experimentalRoleB );
        sb.append( ", BiologicalRoleInteractorA=" ).append( biologicalRoleA );
        sb.append( ", BiologicalRoleInteractorB=" ).append( biologicalRoleB );
        sb.append( ", PropertiesA=" ).append( propertiesA );
        sb.append( ", PropertiesB=" ).append( propertiesB );
        sb.append( ", InteractorType of A=" ).append( interactorTypeA );
        sb.append( ", InteractorType of B=" ).append( interactorTypeB );
        sb.append( ", HostOrganismn" ).append( hostOrganism );
        sb.append( ", ExpansionMethod" ).append( expansionMethods );
        sb.append( ", dataset" ).append( dataset );
        sb.append( '}' );
        return sb.toString();
    }
}