/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.psimitab;

import java.util.List;

import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;

/**
 * IntAct extension of a BinaryInteractionImpl.
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26-Jan-2007</pre>
 */
public class IntActBinaryInteraction extends BinaryInteractionImpl {

    private List<CrossReference> experimentalRoleA;
    private List<CrossReference> experimentalRoleB;
    
    private List<CrossReference> propertiesA;
    private List<CrossReference> propertiesB;
    
    private List<CrossReference> interactorTypeA;
    private List<CrossReference> interactorTypeB;
    
    private List<CrossReference> hostOrganism;
    
	private Interactor interactorA;
	private Interactor interactorB;

    private int expectedColumnCount = 22;
    

    public IntActBinaryInteraction( Interactor interactorA, Interactor interactorB ) {
        super( interactorA, interactorB );
    	this.interactorA = interactorA;
    	this.interactorB = interactorB;
    }

    public IntActBinaryInteraction( ) {
    	super( );
    }
    
    public boolean hasExperimentalRolesInteractorA(){
    	return experimentalRoleA != null;
    }
    
    public boolean hasExperimentalRolesInteractorB(){
    	return experimentalRoleB != null;
    }
    
    public List<CrossReference> getExperimentalRolesInteractorA() {
        return experimentalRoleA;
    }

    public List<CrossReference> getExperimentalRolesInteractorB() {
        return experimentalRoleB;
    }
    
    public void setExperimentalRolesInteractorA( List<CrossReference> experimentalRoles ) {
        this.experimentalRoleA = experimentalRoles;
    }
    
    public void setExperimentalRolesInteractorB( List<CrossReference> experimentalRoles ) {
        this.experimentalRoleB = experimentalRoles;
    }
    
    public boolean hasPropertiesA(){
    	return propertiesA != null;
    }
    
    public boolean hasPropertiesB(){
    	return propertiesB != null;
    }
	public void setPropertiesA( List <CrossReference> propertiesA ) {
		this.propertiesA = propertiesA;
	}

	public void setPropertiesB( List<CrossReference> propertiesB ) {
		this.propertiesB = propertiesB;
	}

	public List<CrossReference> getPropertiesA() {
		return propertiesA;
	}

	public List<CrossReference> getPropertiesB() {
		return propertiesB;
	}

	public boolean hasInteractorTypeA(){
		return interactorTypeA != null;
	}

	public boolean hasInteractorTypeB(){
		return interactorTypeB != null;
	}

	public void setInteractorTypeA( List<CrossReference> interactorType ){
		this.interactorTypeA = interactorType;
	}

	public void setInteractorTypeB( List<CrossReference> interactorType ){
		this.interactorTypeB = interactorType;
	}

	public List<CrossReference> getInteractorTypeA() {
		return interactorTypeA;
	}
	
	public List<CrossReference> getInteractorTypeB() {
		return interactorTypeB;
	}
	
	public boolean hasHostOrganism(){
		return hostOrganism != null;
	}
	
	public List<CrossReference> getHostOrganism() {
		return hostOrganism;
	}

	public void setHostOrganism(List<CrossReference> hostOrganism) {
		this.hostOrganism = hostOrganism;
	}
	
	/**
     * Getter of the number of expected columns
     * 
     * @return
     */
	public int getExpectedColumnCount() {
		return expectedColumnCount;
	}

	/**
	 * Setter of the number of expected columns
	 * 
	 * @param number of expected columns
	 */
	public void setExpectedColumnCount(int expectedColumnCount) {
		this.expectedColumnCount = expectedColumnCount;
	}
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append( "BinaryInteractionImpl" );
        sb.append( "{interactorA=" ).append( interactorA );
        sb.append( ", interactorB=" ).append( interactorB );
        sb.append( ", ExperimentalRoleInteractorA=" ).append( experimentalRoleA);
        sb.append( ", ExperimentalRoleInteractorB=" ).append( experimentalRoleB);
        sb.append( ", PropertiesA=" ).append( propertiesA );
        sb.append( ", PropertiesB=" ).append( propertiesB );
        sb.append( ", InteractorType of A=" ).append( interactorTypeA );
        sb.append( ", InteractorType of B=" ).append( interactorTypeB );
        sb.append( ", HostOrganismn" ).append( hostOrganism );
        sb.append( '}' );
        return sb.toString();
    }



}