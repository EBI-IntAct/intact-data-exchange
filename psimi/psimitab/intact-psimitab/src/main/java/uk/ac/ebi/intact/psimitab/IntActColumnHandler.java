/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.psimitab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import psidev.psi.mi.tab.PsimitabHeader;
import psidev.psi.mi.tab.converter.tab2xml.XmlConvertionException;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineParserUtils;
import psidev.psi.mi.tab.converter.xml2tab.ColumnHandler;
import psidev.psi.mi.tab.converter.xml2tab.CrossReferenceConverter;
import psidev.psi.mi.tab.formatter.TabulatedLineFormatter;
import psidev.psi.mi.tab.model.Author;
import psidev.psi.mi.tab.model.AuthorImpl;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.column.Column;
import psidev.psi.mi.xml.model.DbReference;
import psidev.psi.mi.xml.model.ExperimentDescription;
import psidev.psi.mi.xml.model.ExperimentalRole;
import psidev.psi.mi.xml.model.Interaction;
import psidev.psi.mi.xml.model.Interactor;
import psidev.psi.mi.xml.model.InteractorType;
import psidev.psi.mi.xml.model.Names;
import psidev.psi.mi.xml.model.Organism;
import psidev.psi.mi.xml.model.Participant;
import psidev.psi.mi.xml.model.Xref;

/**
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26-Jan-2007</pre>
 */
public class IntActColumnHandler implements ColumnHandler {

	   /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( IntActColumnHandler.class );
 
	/**
	 * CrossReference Converter
	 */
	private CrossReferenceConverter xConverter = new CrossReferenceConverter();
    
	
	
    public void process( BinaryInteractionImpl bi, Interaction interaction ) {
        
    	IntActBinaryInteraction dbi = ( IntActBinaryInteraction ) bi;
        
        if ( interaction.getParticipants().size() != 2 ) {
            log.warn( "interaction (id:" + interaction.getId() + ") could not be converted to MITAB25 as it does not have exactly 2 participants." );
        }
        
        Iterator<Participant> pi = interaction.getParticipants().iterator();
        Participant pA = pi.next();
        Participant pB = pi.next();
        
        if (pA.getExperimentalRoles().size() != 1){
        	log.warn( "interaction (id:" + interaction.getId() + ") could not be converted to MITAB25 as it does not have exactly 1 experimentalRole." );
        } else {
	        ExperimentalRole roleA = pA.getExperimentalRoles().iterator().next();
	        String id = roleA.getXref().getPrimaryRef().getId().split(":")[1];
	        String db = roleA.getXref().getPrimaryRef().getId().split(":")[0];
	        String text = roleA.getNames().getShortLabel();
	        CrossReferenceImpl experimentalRoleA = new CrossReferenceImpl( db, id, text );
	        
	       	if (dbi.hasExperimentalRolesInteractorA()){
	       		//System.out.println(interaction.getXref().getPrimaryRef().getId() + "has more than one experimentalRole");
        		dbi.getExperimentalRolesInteractorA().add( experimentalRoleA );
        	} else {
        		List<CrossReference> xrefs = new ArrayList<CrossReference>();
        		xrefs.add(experimentalRoleA);
        		dbi.setExperimentalRolesInteractorA( xrefs );
        	}
        }
        
        if (pB.getExperimentalRoles().size() != 1){
        	log.warn( "interaction (id:" + interaction.getId() + ") could not be converted to MITAB25 as it does not have exactly 1 experimentalRole." );
        } else {
	        ExperimentalRole roleB = pB.getExperimentalRoles().iterator().next();
	        String id = roleB.getXref().getPrimaryRef().getId().split(":")[1];
	        String db = roleB.getXref().getPrimaryRef().getId().split(":")[0];
	        String text = roleB.getNames().getShortLabel();
	        CrossReferenceImpl experimentalRoleB = new CrossReferenceImpl( db, id, text );
	        
	       	if (dbi.hasExperimentalRolesInteractorB()){
        		dbi.getExperimentalRolesInteractorB().add( experimentalRoleB );
        	} else {
        		List<CrossReference> xrefs = new ArrayList<CrossReference>();
        		xrefs.add(experimentalRoleB);
        		dbi.setExperimentalRolesInteractorB( xrefs );
        	}
        }
        
        if (pA.getInteractor().getInteractorType() == null){
        	log.warn( "interaction (id:" + interaction.getId() + ") could not be converted to MITAB25 as it does not have exactly 1 interactorType." );
        } else {
        	String id = pA.getInteractor().getInteractorType().getXref().getPrimaryRef().getId().split(":")[1];
        	String db = pA.getInteractor().getInteractorType().getXref().getPrimaryRef().getId().split(":")[0];
        	String text = pA.getInteractor().getInteractorType().getNames().getShortLabel();
        	CrossReferenceImpl interactorType = new CrossReferenceImpl( db, id ,text );
        	
	       	if (dbi.hasInteractorTypeA()){
        		dbi.getInteractorTypeA().add( interactorType );
        	} else {
        		List<CrossReference> xrefs = new ArrayList<CrossReference>();
        		xrefs.add( interactorType );
        		dbi.setInteractorTypeA( xrefs );
        	}
        }

        if (pB.getInteractor().getInteractorType() == null){
        	log.warn( "interaction (id:" + interaction.getId() + ") could not be converted to MITAB25 as it does not have exactly 1 interactorType." );
        } else {
        	String id = pB.getInteractor().getInteractorType().getXref().getPrimaryRef().getId().split(":")[1];
        	String db = pB.getInteractor().getInteractorType().getXref().getPrimaryRef().getId().split(":")[0];
        	String text = pB.getInteractor().getInteractorType().getNames().getShortLabel();
        	CrossReferenceImpl interactorType = new CrossReferenceImpl( db, id ,text );
        	
	       	if (dbi.hasInteractorTypeB()){
        		dbi.getInteractorTypeB().add( interactorType );
        	} else {
        		List<CrossReference> xrefs = new ArrayList<CrossReference>();
        		xrefs.add( interactorType );
        		dbi.setInteractorTypeB( xrefs );
        	}
        }
  
        if (pA.getInteractor().getXref().getSecondaryRef() != null && !pA.getInteractor().getXref().getSecondaryRef().isEmpty()){ 
	        for ( DbReference dbrefA : pA.getInteractor().getXref().getSecondaryRef() ){
	        	
	        	String id, db;
		        	
	        	if (dbrefA.getId().contains(":")){
	        		id = dbrefA.getId().split(":")[1].toLowerCase();
	        		db = dbrefA.getId().split(":")[0].toLowerCase();
		        } else {
		        	id = dbrefA.getId();
		        	db = dbrefA.getDb();
		        }
	        	CrossReferenceImpl xrefA = new CrossReferenceImpl( db, id );
	        	//String text = dbrefA.getSecondary();
	        	//xrefA.setText(text);        	
	        	
	        	if (dbi.hasPropertiesA()){
	        		dbi.getPropertiesA().add(xrefA);
			    } else {
			    	List<CrossReference> xrefs = new ArrayList<CrossReference>();
			    	xrefs.add(xrefA);
			    	dbi.setPropertiesA(xrefs);
		        } 
	        }
        } 
        
        
        if (pB.getInteractor().getXref().getSecondaryRef() != null && !pB.getInteractor().getXref().getSecondaryRef().isEmpty()){
	        for ( DbReference dbrefB : pB.getInteractor().getXref().getSecondaryRef() ){
	        	
	        	String id, db;
	        	if (dbrefB.getId().contains(":")){
	        		id = dbrefB.getId().split(":")[1].toLowerCase();
	        		db = dbrefB.getId().split(":")[0].toLowerCase();
		        } else {
		        	id = dbrefB.getId();
		        	db = dbrefB.getDb();
		        }
	        	CrossReferenceImpl xrefB = new CrossReferenceImpl( db, id );
	        	//String text = dbrefB.getSecondary();
	        	//xrefB.setText(text); 	
			        	
	        	if ( dbi.hasPropertiesB() ){
	        		dbi.getPropertiesB().add(xrefB);
			    } else {
			    	List<CrossReference> xrefs = new ArrayList<CrossReference>();
			    	xrefs.add(xrefB);
			    	dbi.setPropertiesB(xrefs);
		        }
	        }
        } 
        
        if ( !interaction.getExperiments().isEmpty()){
			for (ExperimentDescription description : interaction.getExperiments()){
				
				if (description.hasHostOrganisms()){
				
					Organism hostOrganism = description.getHostOrganisms().iterator().next();
					
					String id = new Integer(hostOrganism.getNcbiTaxId()).toString();
					String db = hostOrganism.getNames().getShortLabel();
					CrossReference organismRef = new CrossReferenceImpl(db, id );
					//String text = hostOrganism.getNames().getFullName();
					//CrossReference organismRef = new CrossReferenceImpl(db, id ,text);
	
					if( dbi.hasHostOrganism() ){
						dbi.getHostOrganism().add(organismRef);
					} else {
						List<CrossReference> hos = new ArrayList<CrossReference>();
						hos.add(organismRef);
						dbi.setHostOrganism(hos);
					}
	
				}
			}
		}        
        
        List<Author> authors = new ArrayList<Author>();
        for (ExperimentDescription experiment : interaction.getExperiments()){
        	final StringBuilder sb = new StringBuilder();
        	sb.append(experiment.getNames().getShortLabel().split("-")[0]);
        	sb.append(" et al ");
        	sb.append("(");
        	sb.append(experiment.getNames().getShortLabel().split("-")[1]);
        	sb.append(")");
        	String authorname = sb.substring(0,1).toUpperCase().toString().concat(sb.substring(1));
        	authors.add(new AuthorImpl(authorname));
        	
        	dbi.setAuthors(authors);
        }
    }

    /**
     * Gets the additional information of the BinaryInteraction.
     */
    public void updateHeader( PsimitabHeader header ){
    	
    	header.appendColumnName("experimentalRole interactor A");
    	header.appendColumnName("experimentalRole interactor B");
    	
    	header.appendColumnName("properties interactor A");
    	header.appendColumnName("properties interactor B");
    	
    	header.appendColumnName("interactorType of A");
    	header.appendColumnName("interactorType of B");
    	
    	header.appendColumnName("hostOrganism");
    	
    }
    
    /**
     * Sets the additional colums for the BinaryInteraction.
     */
    public void  formatAdditionalColumns(BinaryInteractionImpl bi, StringBuffer sb){
    	
    	IntActBinaryInteraction dbi = (IntActBinaryInteraction)bi;
    	
        // field 16 - experimentalRole of interactorA
		sb.append( TabulatedLineFormatter.formatCv( dbi.getExperimentalRolesInteractorA() ) );
        sb.append( '\t' );
    	if (!dbi.hasExperimentalRolesInteractorA()){
    		log.warn("No experimentalRole for Interactor A found for " + dbi.getInteractionAcs());
    	}
        
        // field 17 - experimentalRole of interactorB
    	sb.append( TabulatedLineFormatter.formatCv( dbi.getExperimentalRolesInteractorB() ) );
        sb.append( '\t' );  
        if (!dbi.hasExperimentalRolesInteractorB()){
        	log.warn("No experimentalRole for Interactor B found for " + dbi.getInteractionAcs());
        }
    	
        // field 18 - properties of interactorA
    	sb.append( TabulatedLineFormatter.formatCv( dbi.getPropertiesA() ) );
    	sb.append( '\t' );
        if (!dbi.hasPropertiesA()){
        	log.warn("No properties for Interactor A found for " + dbi.getInteractionAcs());
        }
        
        // field 19 - properties of interactorB
    	sb.append( TabulatedLineFormatter.formatCv( dbi.getPropertiesB() ) );
        sb.append( '\t' );
        if (!dbi.hasPropertiesB()){
        	log.warn("No properties for Interactor B found for " + dbi.getInteractionAcs());
        }
               
        // field 20 - interactorType of A
        sb.append( TabulatedLineFormatter.formatCv( dbi.getInteractorTypeA() ) );
        sb.append( '\t' );
        if (!dbi.hasInteractorTypeA()){
        	log.warn("No interactorType for A found for " + dbi.getInteractionAcs());
        }
        
        // field 21 - interactorType of B
        sb.append( TabulatedLineFormatter.formatCv( dbi.getInteractorTypeB() ) );
        sb.append( '\t' );
        if (!dbi.hasInteractorTypeB()){
        	log.warn("No interactorType for B found for " + dbi.getInteractionAcs());
        }
        
        // field 22 - interactorType of B
        sb.append( TabulatedLineFormatter.formatCv( dbi.getHostOrganism() ) );
        sb.append( '\t' );
        if (!dbi.hasHostOrganism()){
        	log.warn("No hostOrganism found for " + dbi.getInteractionAcs());
        }

    }
    /**
     * Gets the additional information of the BinaryInteraction.
     */
    public void parseColumn(BinaryInteractionImpl bi, Iterator columnIterator) {
		
		IntActBinaryInteraction dbi = ( IntActBinaryInteraction ) bi;
		Iterator<Column> iterator = columnIterator;
		
		try {
			if (iterator.hasNext()) {
			// Experimental role of Interactor A
			String field16 = iterator.next().getData();
			dbi.setExperimentalRolesInteractorA( MitabLineParserUtils.parseCrossReference( field16 ) );
			}
			
			if (iterator.hasNext()){
			// Experimental role of Interactor B
			String field17 = iterator.next().getData();
			dbi.setExperimentalRolesInteractorB( MitabLineParserUtils.parseCrossReference( field17 ) );
			}
			
			if (iterator.hasNext()){
			// Properties of Interactor A
			String field18 = iterator.next().getData();
			dbi.setPropertiesA( MitabLineParserUtils.parseCrossReference( field18 ) );
			}
			
			if (iterator.hasNext()){
			// Properties of Interactor B
			String field19 = iterator.next().getData();
			dbi.setPropertiesB( MitabLineParserUtils.parseCrossReference( field19 ) );
			}
			
			if (iterator.hasNext()){
			// InteractorType of A
			String field20 = iterator.next().getData();
			dbi.setInteractorTypeA( MitabLineParserUtils.parseCrossReference( field20 ) );
			}
			
			if (iterator.hasNext()){
			// InteractorType of B
			String field21 = iterator.next().getData();
			dbi.setInteractorTypeB( MitabLineParserUtils.parseCrossReference( field21 ) );
			}
			
			if (iterator.hasNext()){
				// HostOrganism
				String field22 = iterator.next().getData();
				dbi.setHostOrganism( MitabLineParserUtils.parseCrossReference(field22 ) );
			}

		} catch (MitabLineException e) {
			e.printStackTrace();
		}
	}
    
    /**
     * This method parse the information from BinaryInteraction 
     */
    public void parseColumn(BinaryInteractionImpl bi, StringTokenizer st) {
		IntActBinaryInteraction dbi = ( IntActBinaryInteraction ) bi;
				
		try {
			// Experimental role of Interactor A
	        if ( !st.hasMoreTokens() ) throw new MitabLineException( "Column " + 16 + " must not be empty." );
	        String field16 = st.nextToken();
	        dbi.setExperimentalRolesInteractorA( MitabLineParserUtils.parseCrossReference( field16 ) );
	
			// Experimental role of Interactor B
	        if ( !st.hasMoreTokens() ) throw new MitabLineException( "Column " + 17 + " must not be empty." );
	        String field17 = st.nextToken();
	        dbi.setExperimentalRolesInteractorB( MitabLineParserUtils.parseCrossReference( field17 ) );
			
			// Properties of Interactor A
	        if ( !st.hasMoreTokens() ) throw new MitabLineException( "Column " + 18 + " must not be empty." );
	        String field18 = st.nextToken();
	        dbi.setPropertiesA( MitabLineParserUtils.parseCrossReference( field18 ) );
			
			// Properties of Interactor B
	        if ( !st.hasMoreTokens() ) throw new MitabLineException( "Column " + 19 + " must not be empty." );
	        String field19 = st.nextToken();
	        dbi.setPropertiesB( MitabLineParserUtils.parseCrossReference( field19 ) );
	
			// InteractorType of A
	        if ( !st.hasMoreTokens() ) throw new MitabLineException( "Column " + 20 + " must not be empty." );
	        String field20 = st.nextToken();
	        dbi.setInteractorTypeA( MitabLineParserUtils.parseCrossReference( field20 ) );
	
			// InteractorType of B
	        if ( !st.hasMoreTokens() ) throw new MitabLineException( "Column " + 21 + " must not be empty." );
	        String field21 = st.nextToken();
	        dbi.setInteractorTypeB( MitabLineParserUtils.parseCrossReference( field21 ) );
	        
			// InteractorType of B
	        if ( !st.hasMoreTokens() ) throw new MitabLineException( "Column " + 22 + " must not be empty." );
	        String field22 = st.nextToken();
	        dbi.setHostOrganism( MitabLineParserUtils.parseCrossReference( field22 ) );
	
		} catch (MitabLineException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * This methode creates valid Participants and Interactors for the xml.
	 */
	public void updateParticipants(BinaryInteractionImpl binaryInteraction, Participant pA, Participant pB, int index) {
		
		Interactor iA = pA.getInteractor();
		Interactor iB = pB.getInteractor();
		
		IntActBinaryInteraction dbi = (IntActBinaryInteraction) binaryInteraction;
		
		
		if (dbi.hasExperimentalRolesInteractorA()){
			// delete default ExperimentalRoles
			pA.getExperimentalRoles().clear();
			
			// now create the new ExperimentalRoles
			String roleA = dbi.getExperimentalRolesInteractorA().get(index).getText();
			String dbA = dbi.getExperimentalRolesInteractorA().get(index).getDatabase().concat(":".concat(dbi.getExperimentalRolesInteractorA().get(0).getIdentifier()));
			
			Names names = new Names();	
			if (roleA == null){
				names.setShortLabel("unspecified role");
				names.setFullName("unspecified role");
			} else {
				names.setShortLabel(roleA);
				names.setFullName(roleA);
			}
			
			DbReference dbRef = new DbReference();		
			dbRef.setDb("psi-mi");
			if (dbA == null){
				dbRef.setId("MI:0499");
			} else {
				dbRef.setId(dbA);
			}
			dbRef.setDbAc("MI:0488");
			dbRef.setRefType("identity");
			dbRef.setRefTypeAc("MI:0356");	
			
			Xref experimentalXref = new Xref(dbRef);
			ExperimentalRole experimentalRole = new ExperimentalRole(names, experimentalXref);
			
			// add new ExperimentalRoles
			if (!pA.getExperimentalRoles().add(experimentalRole)){
				log.warn("ExperimentalRole couldn't add to the participant");
			}
		}
		
		if (dbi.hasExperimentalRolesInteractorB()){
			// delete default ExperimentalRoles
			pB.getExperimentalRoles().clear();
			
			// now create the new ExperimentalRoles
			String roleB = dbi.getExperimentalRolesInteractorB().get(index).getText();
			String dbB = dbi.getExperimentalRolesInteractorB().get(index).getDatabase().concat(":".concat(dbi.getExperimentalRolesInteractorB().get(0).getIdentifier()));
			
			Names names = new Names();	
			if (roleB == null){
				names.setShortLabel("unspecified role");
				names.setFullName("unspecified role");
			} else {
				names.setShortLabel(roleB);
				names.setFullName(roleB);
			}
			
			DbReference dbRef = new DbReference();		
			dbRef.setDb("psi-mi");
			if (dbB == null){
				dbRef.setId("MI:0499");
			} else {
				dbRef.setId(dbB);
			}
			dbRef.setDbAc("MI:0488");
			dbRef.setRefType("identity");
			dbRef.setRefTypeAc("MI:0356");	
			
			Xref experimentalXref = new Xref(dbRef);
			ExperimentalRole experimentalRole = new ExperimentalRole(names, experimentalXref);
			
			// add new ExperimentalRoles
			if (!pA.getExperimentalRoles().add(experimentalRole)){
				log.warn("ExperimentalRole couldn't add to the participant");
			}

		}
	
		try {
			if (dbi.hasInteractorTypeA()){
				InteractorType typeA = (InteractorType)xConverter.fromMitab(dbi.getInteractorTypeA().get(0), InteractorType.class);
				iA.setInteractorType(typeA);
			}
			
			if (dbi.hasInteractorTypeB()){
				InteractorType typeB = (InteractorType)xConverter.fromMitab(dbi.getInteractorTypeB().get(0), InteractorType.class);
				iB.setInteractorType(typeB);
			}
	
			if (dbi.hasPropertiesA()){
				for (CrossReference property : dbi.getPropertiesA()){
					DbReference secDbRef = new DbReference();
					secDbRef.setDb(property.getDatabase());
					if (property.getDatabase().equalsIgnoreCase("GO")){
						secDbRef.setId(property.getDatabase().concat(":".concat(property.getIdentifier())));
						secDbRef.setDbAc("MI:0448");
					} else {
						secDbRef.setId(property.getIdentifier());
						if (property.getDatabase().equals("interpro")){
							secDbRef.setDbAc("MI:0449");
						} 
						if (property.getDatabase().equals("intact")){
							secDbRef.setDbAc("MI:0469");
						}
						if (property.getDatabase().equals("uniprotkb")){
							secDbRef.setDbAc("MI:0486");
						}
					}
					
					if (property.hasText()){
						secDbRef.setSecondary(property.getText());
					}
					
					iA.getXref().getSecondaryRef().add(secDbRef);
				}
			}
			
			if (dbi.hasPropertiesB()){
				for (CrossReference property : dbi.getPropertiesB()){
					DbReference secDbRef = new DbReference();
					secDbRef.setDb(property.getDatabase());
					if (property.getDatabase().equalsIgnoreCase("GO")){
						secDbRef.setId(property.getDatabase().concat(":".concat(property.getIdentifier())));
						secDbRef.setDbAc("MI:0448");
					} else {
						secDbRef.setId(property.getIdentifier());
						if (property.getDatabase().equals("interpro")){
							secDbRef.setDbAc("MI:0449");
						} 
						if (property.getDatabase().equals("intact")){
							secDbRef.setDbAc("MI:0469");
						}
						if (property.getDatabase().equals("uniprotkb")){
							secDbRef.setDbAc("MI:0486");
						}
						
					}
					
					if (property.hasText()){
						secDbRef.setSecondary(property.getText());
					}
					
					iB.getXref().getSecondaryRef().add(secDbRef);
				}
			}
	
		} catch (XmlConvertionException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * This method updates the default hostOrganism for the xml.
	 */
	public void updateHostOrganism(BinaryInteractionImpl binaryInteraction, Organism hostOrganism, int index) {
		IntActBinaryInteraction dbi = (IntActBinaryInteraction)binaryInteraction;
		
		if ( dbi.hasHostOrganism() ){
			CrossReference o  = dbi.getHostOrganism().get(index);
			
			int taxid = Integer.parseInt(o.getIdentifier());
			hostOrganism.setNcbiTaxId(taxid);
			
			Names organismNames = new Names();
			organismNames.setShortLabel(o.getDatabase());
			if (o.hasText()) organismNames.setFullName(o.getText());
			hostOrganism.setNames(organismNames);			
		} 
	}


	public void mergeCollection(BinaryInteractionImpl interaction, BinaryInteractionImpl targets) {
		IntActBinaryInteraction s = (IntActBinaryInteraction)interaction;
        IntActBinaryInteraction t = (IntActBinaryInteraction)targets;
        
        Collection<CrossReference> hostorganismsource = s.getHostOrganism();
        Collection<CrossReference> hostorganismtarget = t.getHostOrganism();

        if ( hostorganismsource == null ) {
            throw new IllegalArgumentException( "Source collection must not be null." );
        }

        if ( hostorganismtarget == null ) {
            throw new IllegalArgumentException( "Target collection must not be null." );
        }

        if ( hostorganismsource == hostorganismtarget ) throw new IllegalStateException();

        for ( CrossReference o : hostorganismsource ) {
            // Repeat of objects are a necessity (eg. for detection method across multiple publications)
        	hostorganismtarget.add( o );
        }
        
        Collection<CrossReference> roleAsource = s.getExperimentalRolesInteractorA();
        Collection<CrossReference> roleAtarget = t.getExperimentalRolesInteractorA();

        if ( roleAsource == null ) {
            throw new IllegalArgumentException( "Source collection must not be null." );
        }

        if ( roleAtarget == null ) {
            throw new IllegalArgumentException( "Target collection must not be null." );
        }

        if ( roleAsource == roleAtarget ) throw new IllegalStateException();

        for ( CrossReference o : roleAsource ) {
            // Repeat of objects are a necessity (eg. for detection method across multiple publications)
        	roleAtarget.add( o );
        }
        
        Collection<CrossReference> roleBsource = s.getExperimentalRolesInteractorB();
        Collection<CrossReference> roleBtarget = t.getExperimentalRolesInteractorB();

        if ( roleBsource == null ) {
            throw new IllegalArgumentException( "Source collection must not be null." );
        }

        if ( roleBtarget == null ) {
            throw new IllegalArgumentException( "Target collection must not be null." );
        }

        if ( roleBsource == roleBtarget ) throw new IllegalStateException();

        for ( CrossReference o : roleBsource ) {
            // Repeat of objects are a necessity (eg. for detection method across multiple publications)
        	roleBtarget.add( o );
        }
		
	}





	
	
	


}