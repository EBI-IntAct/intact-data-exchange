/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.psimitab;

import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.AbstractBinaryInteraction;
import uk.ac.ebi.intact.psimitab.model.Annotation;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.psimitab.model.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

/**
 * IntAct extension of a BinaryInteractionImpl.
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntactBinaryInteraction extends AbstractBinaryInteraction<ExtendedInteractor> {

    private static final long serialVersionUID = -8872335762187393793L;

    private List<CrossReference> hostOrganism;

    private List<String> expansionMethods;

    private List<String> dataset;

    private ExtendedInteractor interactorA;
    private ExtendedInteractor interactorB;

    private List<Parameter> parameters;

    private static int expectedColumnCount = 24;

    //////////////////
    // Constructors

    public IntactBinaryInteraction( ExtendedInteractor interactorA, ExtendedInteractor interactorB ) {
        super( interactorA, interactorB );
        this.interactorA = interactorA;
        this.interactorB = interactorB;
    }

    ///////////////////////////
    // Getters and Setters
    @Deprecated
    public List<CrossReference> getExperimentalRolesInteractorA() {
        return interactorA.getExperimentalRoles();
    }

    @Deprecated
    public void setExperimentalRolesInteractorA( List<CrossReference> experimentalRoles ) {
        interactorA.setExperimentalRoles(experimentalRoles);
    }

    @Deprecated
    public List<CrossReference> getExperimentalRolesInteractorB() {
        return interactorB.getExperimentalRoles();
    }

    @Deprecated
    public void setExperimentalRolesInteractorB( List<CrossReference> experimentalRoles ) {
        interactorB.setExperimentalRoles(experimentalRoles);
    }

    @Deprecated
    public List<CrossReference> getBiologicalRolesInteractorA() {
        return interactorA.getBiologicalRoles();
    }

    @Deprecated
    public void setBiologicalRolesInteractorA( List<CrossReference> biologicalRoles ) {
        interactorA.setBiologicalRoles(biologicalRoles);
    }

    @Deprecated
    public List<CrossReference> getBiologicalRolesInteractorB() {
        return interactorB.getBiologicalRoles();
    }

    @Deprecated
    public void setBiologicalRolesInteractorB( List<CrossReference> biologicalRoles ) {
        interactorB.setBiologicalRoles(biologicalRoles);
    }

    @Deprecated
    public List<CrossReference> getPropertiesA() {
        return interactorA.getProperties();
    }

    @Deprecated
    public void setPropertiesA( List<CrossReference> propertiesA ) {
        interactorA.setProperties(propertiesA);
    }

    @Deprecated
    public List<CrossReference> getPropertiesB() {
        return interactorB.getProperties();
    }

    @Deprecated
    public void setPropertiesB( List<CrossReference> propertiesB ) {
        interactorB.setProperties(propertiesB);
    }

    @Deprecated
    public List<CrossReference> getInteractorTypeA() {
        return Collections.singletonList(interactorA.getInteractorType());
    }

    @Deprecated
    public void setInteractorTypeA( List<CrossReference> interactorType ) {
        interactorA.setInteractorType(interactorType.iterator().next());
    }

    @Deprecated
    public List<CrossReference> getInteractorTypeB() {
        return Collections.singletonList(interactorB.getInteractorType());
    }

    @Deprecated
    public void setInteractorTypeB( List<CrossReference> interactorType ) {
        interactorB.setInteractorType(interactorType.iterator().next());
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

    public List<Parameter> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<Parameter>();
        }
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Deprecated
    public boolean hasExperimentalRolesInteractorA() {
        return interactorA.hasExperimentalRoles();
    }

    @Deprecated
    public boolean hasExperimentalRolesInteractorB() {
        return interactorB.hasExperimentalRoles();
    }

    @Deprecated
    public boolean hasPropertiesA() {
        return interactorA.hasProperties();
    }

    @Deprecated
    public boolean hasPropertiesB() {
        return interactorB.hasProperties();
    }

    @Deprecated
    public boolean hasInteractorTypeA() {
        return interactorA.hasInteractorType();
    }

    @Deprecated
    public boolean hasInteractorTypeB() {
        return interactorB.hasInteractorType();
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

    @Deprecated
    public boolean hasBiologicalRolesInteractorA() {
        return interactorA.hasBiologicalRoles();
    }

    public boolean hasBiologicalRolesInteractorB() {
        return interactorB.hasBiologicalRoles();
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
        sb.append( ", HostOrganismn" ).append( hostOrganism );
        sb.append( ", ExpansionMethod" ).append( expansionMethods );
        sb.append( ", dataset" ).append( dataset );
        sb.append( '}' );
        return sb.toString();
    }
}