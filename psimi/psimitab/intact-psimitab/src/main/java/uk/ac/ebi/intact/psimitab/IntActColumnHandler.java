/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.psimitab;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.PsimitabHeader;
import psidev.psi.mi.tab.converter.tab2xml.XmlConvertionException;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineParserUtils;
import psidev.psi.mi.tab.converter.xml2tab.ColumnHandler;
import psidev.psi.mi.tab.converter.xml2tab.CrossReferenceConverter;
import psidev.psi.mi.tab.converter.xml2tab.IsExpansionStrategyAware;
import psidev.psi.mi.tab.expansion.ExpansionStrategy;
import psidev.psi.mi.tab.formatter.LineFormatter;
import psidev.psi.mi.tab.formatter.TabulatedLineFormatter;
import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.tab.model.column.Column;
import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.model.Interactor;
import psidev.psi.mi.xml.model.Organism;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Handles intact specific columns in the MITAB data import/export.
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntActColumnHandler implements ColumnHandler, IsExpansionStrategyAware {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( IntActColumnHandler.class );

    private static final Pattern EXPERIMENT_LABEL_PATTERN = Pattern.compile( "[a-z-_]+-\\d{4}[a-z]?-\\d+" );

    /**
     * CrossReference Converter
     */
    private final CrossReferenceConverter xConverter = new CrossReferenceConverter();

    /**
     * Information about ExpansionMethod
     */
    private String expansionMethod;


    //////////////////
    // Constructors

    public IntActColumnHandler() {
    }


    /////////////////
    // Getter & Setter

    public void setExpansionMethod( String method ) {
        this.expansionMethod = method;
    }

    /////////////////
    //

    public void process( BinaryInteractionImpl bi, Interaction interaction ) {

        IntActBinaryInteraction dbi = ( IntActBinaryInteraction ) bi;

        if ( interaction.getParticipants().size() != 2 ) {
            if (log.isDebugEnabled()) log.debug( "interaction (id:" + interaction.getId() + ") could not be converted to MITAB25 as it does not have exactly 2 participants." );
        }

        Iterator<Participant> pi = interaction.getParticipants().iterator();
        Participant pA = pi.next();
        Participant pB = pi.next();

        if ( pA.getExperimentalRoles().size() != 1 ) {
            if (log.isDebugEnabled()) log.debug( "interaction (id:" + interaction.getId() + ") could not be converted to MITAB25 as it does not have exactly 1 experimentalRole." );
        } else {
            CrossReference experimentalRoleA = extractExperimentalRole( pA );

            if ( dbi.hasExperimentalRolesInteractorA() ) {
                dbi.getExperimentalRolesInteractorA().add( experimentalRoleA );
            } else {
                List<CrossReference> xrefs = new ArrayList<CrossReference>();
                xrefs.add( experimentalRoleA );
                dbi.setExperimentalRolesInteractorA( xrefs );
            }
        }

        if ( pB.getExperimentalRoles().size() != 1 ) {
            if (log.isDebugEnabled()) log.debug( "interaction (id:" + interaction.getId() + ") could not be converted to MITAB25 as it does not have exactly 1 experimentalRole." );
        } else {
            CrossReference experimentalRoleB = extractExperimentalRole( pB );

            if ( dbi.hasExperimentalRolesInteractorB() ) {
                dbi.getExperimentalRolesInteractorB().add( experimentalRoleB );
            } else {
                List<CrossReference> xrefs = new ArrayList<CrossReference>();
                xrefs.add( experimentalRoleB );
                dbi.setExperimentalRolesInteractorB( xrefs );
            }
        }

        if ( pA.getInteractor().getInteractorType() == null ) {
            if (log.isDebugEnabled()) log.debug( "interaction (id:" + interaction.getId() + ") could not be converted to MITAB25 as it does not have exactly 1 interactorType." );
        } else {
            CrossReference typeA  = extractInteractorType( pA );

            if ( dbi.hasInteractorTypeA() ) {
                dbi.getInteractorTypeA().add( typeA );
            } else {
                List<CrossReference> xrefs = new ArrayList<CrossReference>();
                xrefs.add( typeA );
                dbi.setInteractorTypeA( xrefs );
            }
        }

        if ( pB.getInteractor().getInteractorType() == null ) {
            if (log.isDebugEnabled()) log.debug( "interaction (id:" + interaction.getId() + ") could not be converted to MITAB25 as it does not have exactly 1 interactorType." );
        } else {
            CrossReference typeB  = extractInteractorType( pB );

            if ( dbi.hasInteractorTypeB() ) {
                dbi.getInteractorTypeB().add( typeB );
            } else {
                List<CrossReference> xrefs = new ArrayList<CrossReference>();
                xrefs.add( typeB );
                dbi.setInteractorTypeB( xrefs );
            }
        }

        if ( pA.getInteractor().getXref().getSecondaryRef() != null && !pA.getInteractor().getXref().getSecondaryRef().isEmpty() ) {
            List<CrossReference> propertiesA = extractProperties( pA );
            if ( !dbi.hasPropertiesA() ) dbi.setPropertiesA( new ArrayList<CrossReference>() );
            dbi.getPropertiesA().addAll( propertiesA );
        }


        if ( pB.getInteractor().getXref().getSecondaryRef() != null && !pB.getInteractor().getXref().getSecondaryRef().isEmpty() ) {
            List<CrossReference> propertiesB = extractProperties( pB );
            if ( !dbi.hasPropertiesB() ) dbi.setPropertiesB( new ArrayList<CrossReference>() );
            dbi.getPropertiesB().addAll( propertiesB );

        }

        if ( interaction.getExperiments() != null && !interaction.getExperiments().isEmpty() ) {
            for ( ExperimentDescription description : interaction.getExperiments() ) {
                if ( description.hasHostOrganisms() ) {
                    Organism hostOrganism = description.getHostOrganisms().iterator().next();

                    String id = Integer.toString( hostOrganism.getNcbiTaxId());
                    String db = hostOrganism.getNames().getShortLabel();
                    CrossReference organismRef = new CrossReferenceImpl( db, id );
                    //String text = hostOrganism.getNames().getFullName();
                    //CrossReference organismRef = new CrossReferenceImpl(db, id ,text);

                    if ( dbi.hasHostOrganism() ) {
                        dbi.getHostOrganism().add( organismRef );
                    } else {
                        List<CrossReference> hos = new ArrayList<CrossReference>();
                        hos.add( organismRef );
                        dbi.setHostOrganism( hos );
                    }

                }
            }
        }

        for ( ExperimentDescription experiment : interaction.getExperiments() ){
            for ( Attribute attribute : experiment.getAttributes() ) {
                if( attribute.getName().equals( "dataset" ) ) {
                    String dataset = attribute.getValue();

                    if ( dbi.hasDatasetName() ) {
                        dbi.getDataset().add( dataset );
                    } else {
                        List<String> datasets = new ArrayList<String>();
                        datasets.add( dataset );
                        dbi.setDataset( datasets );
                    }
                }
            }
        }

        List<Author> authors = new ArrayList<Author>();
        for ( ExperimentDescription experiment : interaction.getExperiments() ) {
            final String label = experiment.getNames().getShortLabel();
            if ( isWellFormattedExperimentShortlabel( label ) ) {
                final StringBuilder sb = new StringBuilder();
                final String[] values = label.split( "-" );
                sb.append( StringUtils.capitalize( values[0] ) );
                sb.append( " et al. " );
                sb.append( '(' );
                sb.append( values[1] );
                sb.append( ')' );

                authors.add( new AuthorImpl( sb.toString() ) );
                dbi.setAuthors( authors );
            }
        }
    }

    private boolean isWellFormattedExperimentShortlabel( String label ) {
        if ( label == null ) {
            return false;
        }
        return EXPERIMENT_LABEL_PATTERN.matcher( label ).matches();
    }

    /**
     * Extracts the relevant information for the psimitab experimental role.
     *
     * @param participant
     * @return experimental role
     */
    private CrossReference extractExperimentalRole( Participant participant ) {

        ExperimentalRole role = participant.getExperimentalRoles().iterator().next();
        String id = role.getXref().getPrimaryRef().getId().split( ":" )[1];
        String db = role.getXref().getPrimaryRef().getId().split( ":" )[0];
        String text = role.getNames().getShortLabel();

        return new CrossReferenceImpl( db, id, text );
    }

    /**
     * Extracts the relevant informations for the psimitab interactor type.
     * 
     * @param participant
     * @return interactor type
     */
    private CrossReference extractInteractorType( Participant participant ) {
        String id = participant.getInteractor().getInteractorType().getXref().getPrimaryRef().getId().split( ":" )[1];
        String db = participant.getInteractor().getInteractorType().getXref().getPrimaryRef().getId().split( ":" )[0];
        String text = participant.getInteractor().getInteractorType().getNames().getShortLabel();

        return new CrossReferenceImpl( db, id, text );
    }

    /**
     * Extracts the relevant informations for the psimitab propterties.
     *
     * @param participant
     * @return list of properties
     */
    private List<CrossReference> extractProperties( Participant participant ) {
        List<CrossReference> properties = new ArrayList<CrossReference>();
        for ( DbReference dbref : participant.getInteractor().getXref().getSecondaryRef() ){
            String id, db;

            if ( dbref.getId().contains( ":" ) ) {
                id = dbref.getId().split( ":" )[1].toLowerCase();
                db = dbref.getId().split( ":" )[0].toLowerCase();
            } else {
                id = dbref.getId();
                db = dbref.getDb();
            }
            properties.add( new CrossReferenceImpl( db, id ));
        }
        return properties;
    }

    /**
     * Gets the additional information of the BinaryInteraction.
     */
    public void updateHeader( PsimitabHeader header ) {

        header.appendColumnName( "experimentalRole interactor A" );
        header.appendColumnName( "experimentalRole interactor B" );

        header.appendColumnName( "properties interactor A" );
        header.appendColumnName( "properties interactor B" );

        header.appendColumnName( "interactorType of A" );
        header.appendColumnName( "interactorType of B" );

        header.appendColumnName( "hostOrganism" );

        header.appendColumnName( "expansion method" );

        header.appendColumnName( "dataset" );
    }

    /**
     * Sets the additional colums for the BinaryInteraction.
     */
    public void formatAdditionalColumns( BinaryInteractionImpl bi, StringBuffer sb ) {

        IntActBinaryInteraction dbi = ( IntActBinaryInteraction ) bi;

        // field 16 - experimentalRole of interactorA
        if ( dbi.hasExperimentalRolesInteractorA() ) {
            sb.append( TabulatedLineFormatter.formatCv( dbi.getExperimentalRolesInteractorA() ) );
        } else {
            sb.append( LineFormatter.NONE );
            if (log.isDebugEnabled()) log.debug( "No experimentalRole for Interactor A found for " + dbi.getInteractionAcs() );
        }
        sb.append( '\t' );

        // field 17 - experimentalRole of interactorB
        if ( dbi.hasExperimentalRolesInteractorB() ) {
            sb.append( TabulatedLineFormatter.formatCv( dbi.getExperimentalRolesInteractorB() ) );
        } else {
            sb.append( LineFormatter.NONE );
            if (log.isDebugEnabled()) log.debug( "No experimentalRole for Interactor B found for " + dbi.getInteractionAcs() );
        }
        sb.append( '\t' );

        // field 18 - properties of interactorA
        if ( dbi.hasPropertiesA() ) {
            sb.append( TabulatedLineFormatter.formatCv( dbi.getPropertiesA() ) );
        } else {
            sb.append( LineFormatter.NONE );
            if (log.isDebugEnabled()) log.debug( "No properties for Interactor A found for " + dbi.getInteractionAcs() );
        }
        sb.append( '\t' );

        // field 19 - properties of interactorB
        if ( dbi.hasPropertiesB() ) {
            sb.append( TabulatedLineFormatter.formatCv( dbi.getPropertiesB() ) );
        } else {
            sb.append( LineFormatter.NONE );
            if (log.isDebugEnabled()) log.debug( "No properties for Interactor B found for " + dbi.getInteractionAcs() );
        }
        sb.append( '\t' );

        // field 20 - interactorType of A
        if ( dbi.hasInteractorTypeA() ) {
            sb.append( TabulatedLineFormatter.formatCv( dbi.getInteractorTypeA() ) );
        } else {
            sb.append( LineFormatter.NONE );
            if (log.isDebugEnabled()) log.debug( "No interactorType for A found for " + dbi.getInteractionAcs() );
        }
        sb.append( '\t' );

        // field 21 - interactorType of B
        if ( dbi.hasInteractorTypeB() ){
            sb.append( TabulatedLineFormatter.formatCv( dbi.getInteractorTypeB() ) );
        } else {
            sb.append( LineFormatter.NONE );
            if (log.isDebugEnabled()) log.debug( "No interactorType for B found for " + dbi.getInteractionAcs() );
        }
        sb.append( '\t' );

        // field 22 - interactorType of B
        if ( dbi.hasHostOrganism() ){
            sb.append( TabulatedLineFormatter.formatCv( dbi.getHostOrganism() ) );
        } else {
            sb.append( LineFormatter.NONE );
            if (log.isDebugEnabled()) log.debug( "No hostOrganism found for " + dbi.getInteractionAcs() );
        }
        sb.append( '\t' );

        // field 23 - expansion method
        if ( dbi.hasExpansionMethod() ) {
            sb.append( dbi.getExpansionMethod() );
        } else {
            sb.append( LineFormatter.NONE );
            if (log.isDebugEnabled()) log.debug( "No expansionMethod found for " + dbi.getInteractionAcs() );
        }
        sb.append( '\t' );

        //field 24 - dataset name
        if ( dbi.hasDatasetName() ) {
            sb.append( formatStringList( dbi.getDataset() )  );
        } else {
            sb.append( LineFormatter.NONE );
            if (log.isDebugEnabled()) log.debug( "No dataset name found for " + dbi.getInteractionAcs() );
        }
        sb.append( '\t' );
    }

    private String formatStringList( List<String> field ) {
        StringBuffer sb = new StringBuffer( 64 );
        if ( field != null && !field.isEmpty()) {
            for ( Iterator<String> iterator = field.iterator(); iterator.hasNext(); ) {

                sb.append( iterator.next() );

                if ( iterator.hasNext() ) {
                    sb.append( LineFormatter.PIPE );
                }
            }
        }
        return sb.toString();
    }

    /**
     * Gets the additional information of the BinaryInteraction.
     */
    public void parseColumn( BinaryInteractionImpl bi, Iterator columnIterator ) {

        IntActBinaryInteraction dbi = ( IntActBinaryInteraction ) bi;
        Iterator<Column> iterator = columnIterator;

        try {
            if ( iterator.hasNext() ) {
                // Experimental role of Interactor A
                String field16 = iterator.next().getData();
                dbi.setExperimentalRolesInteractorA( MitabLineParserUtils.parseCrossReference( field16 ) );
            }

            if ( iterator.hasNext() ) {
                // Experimental role of Interactor B
                String field17 = iterator.next().getData();
                dbi.setExperimentalRolesInteractorB( MitabLineParserUtils.parseCrossReference( field17 ) );
            }

            if ( iterator.hasNext() ) {
                // Properties of Interactor A
                String field18 = iterator.next().getData();
                dbi.setPropertiesA( MitabLineParserUtils.parseCrossReference( field18 ) );
            }

            if ( iterator.hasNext() ) {
                // Properties of Interactor B
                String field19 = iterator.next().getData();
                dbi.setPropertiesB( MitabLineParserUtils.parseCrossReference( field19 ) );
            }

            if ( iterator.hasNext() ) {
                // InteractorType of A
                String field20 = iterator.next().getData();
                dbi.setInteractorTypeA( MitabLineParserUtils.parseCrossReference( field20 ) );
            }

            if ( iterator.hasNext() ) {
                // InteractorType of B
                String field21 = iterator.next().getData();
                dbi.setInteractorTypeB( MitabLineParserUtils.parseCrossReference( field21 ) );
            }

            if ( iterator.hasNext() ) {
                // HostOrganism
                String field22 = iterator.next().getData();
                dbi.setHostOrganism( MitabLineParserUtils.parseCrossReference( field22 ) );
            }

            if ( iterator.hasNext() ) {
                // Expansion method
                String field23 = iterator.next().getData();
                dbi.setExpansionMethod( field23 );
            }

            if ( iterator.hasNext() ) {
                // dataset name
                String field24 = iterator.next().getData();
                dbi.setDataset( parseStringList( field24 ) );
            }

        } catch ( MitabLineException e ) {
            e.printStackTrace();
        }
    }

    /**
     * This method parse the information from BinaryInteraction
     */
    public void parseColumn( BinaryInteractionImpl bi, StringTokenizer st ) {
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

            // hostOrganism
            if ( !st.hasMoreTokens() ) throw new MitabLineException( "Column " + 22 + " must not be empty." );
            String field22 = st.nextToken();
            dbi.setHostOrganism( MitabLineParserUtils.parseCrossReference( field22 ) );

            // expansion method
            if ( !st.hasMoreTokens() ) throw new MitabLineException( "Column " + 23 + " must not be empty." );
            String field23 = st.nextToken();
            dbi.setExpansionMethod( field23 );

            // dataset name
            if ( !st.hasMoreTokens() ) throw new MitabLineException( "Column " + 24 + " must not be empty.");
            String field24 = st.nextToken() ;
            dbi.setDataset( parseStringList( field24 ));

        } catch ( MitabLineException e ) {
            e.printStackTrace();
        }
    }

    private List<String> parseStringList( String field ) throws MitabLineException {

        if ( !MitabLineParserUtils.isFieldEmpty( field ) ) {
            String[] strings = field.split( MitabLineParserUtils.PIPE );

            return Arrays.asList( strings );
        }
        return null;
    }

    /**
     * This methode creates valid Participants and Interactors for the xml.
     */
    public void updateParticipants( BinaryInteractionImpl binaryInteraction, Participant pA, Participant pB, int index ) {

        Interactor iA = pA.getInteractor();
        Interactor iB = pB.getInteractor();

        IntActBinaryInteraction dbi = ( IntActBinaryInteraction ) binaryInteraction;


        if ( dbi.hasExperimentalRolesInteractorA() ) {
            // delete default ExperimentalRoles
            pA.getExperimentalRoles().clear();

            // create new ExperimentalRoles
            ExperimentalRole experimentalRole = updateExperimentalRoles( dbi.getExperimentalRolesInteractorA(), index );

            // add new ExperimentalRoles
            if ( !pA.getExperimentalRoles().add( experimentalRole ) ) {
                if (log.isDebugEnabled()) log.debug( "ExperimentalRole couldn't add to the participant" );
            }
        }

        if ( dbi.hasExperimentalRolesInteractorB() ) {
            // delete default ExperimentalRoles
            pB.getExperimentalRoles().clear();

            // create new ExperimentalRoles
            ExperimentalRole experimentalRole = updateExperimentalRoles( dbi.getExperimentalRolesInteractorB(), index );

            // add new ExperimentalRoles
            if ( !pB.getExperimentalRoles().add( experimentalRole ) ) {
                if (log.isDebugEnabled()) log.debug( "ExperimentalRole couldn't add to the participant" );
            }
        }

        try {
            if ( dbi.hasInteractorTypeA() ) {
                InteractorType typeA = ( InteractorType ) xConverter.fromMitab( dbi.getInteractorTypeA().get( 0 ), InteractorType.class );
                iA.setInteractorType( typeA );
            }

            if ( dbi.hasInteractorTypeB() ) {
                InteractorType typeB = ( InteractorType ) xConverter.fromMitab( dbi.getInteractorTypeB().get( 0 ), InteractorType.class );
                iB.setInteractorType( typeB );
            }

            if ( dbi.hasPropertiesA() ) {
                Collection<DbReference> secDbRef = getSecondaryRefs(dbi.getPropertiesA());
                iA.getXref().getSecondaryRef().addAll( secDbRef );
            }

            if ( dbi.hasPropertiesB() ) {
                Collection<DbReference> secDbRef = getSecondaryRefs(dbi.getPropertiesB());
                iB.getXref().getSecondaryRef().addAll( secDbRef );
            }

        } catch ( XmlConvertionException e ) {
            e.printStackTrace();
        }
    }

    private ExperimentalRole updateExperimentalRoles( List<CrossReference> experimentalRoles, int index ) {
        // now create the new ExperimentalRoles
        String roleA = experimentalRoles.get( index ).getText();
        String dbA = experimentalRoles.get( index ).getDatabase().concat( ":".concat( experimentalRoles.get( 0 ).getIdentifier() ) );

        Names names = new Names();
        if ( roleA == null ) {
            names.setShortLabel( "unspecified role" );
            names.setFullName( "unspecified role" );
        } else {
            names.setShortLabel( roleA );
            names.setFullName( roleA );
        }

        DbReference dbRef = new DbReference();
        dbRef.setDb( "psi-mi" );
        if ( dbA == null ) {
            dbRef.setId( "MI:0499" );
        } else {
            dbRef.setId( dbA );
        }

        dbRef.setDbAc( "MI:0488" );
        dbRef.setRefType( "identity" );
        dbRef.setRefTypeAc( "MI:0356" );

        Xref experimentalXref = new Xref( dbRef );

        return new ExperimentalRole( names, experimentalXref );
    }

    private Collection<DbReference> getSecondaryRefs( List<CrossReference> properties ) {
        Collection<DbReference> refs = new ArrayList();
        for ( CrossReference property : properties ) {
            DbReference secDbRef = new DbReference();
            secDbRef.setDb( property.getDatabase() );
            if ( property.getDatabase().equalsIgnoreCase( "GO" ) ) {
                secDbRef.setId( property.getDatabase().concat( ":".concat( property.getIdentifier() ) ) );
                secDbRef.setDbAc( "MI:0448" );
            } else {
                secDbRef.setId( property.getIdentifier() );
                if ( property.getDatabase().equals( "interpro" ) ) {
                    secDbRef.setDbAc( "MI:0449" );
                }
                if ( property.getDatabase().equals( "intact" ) ) {
                    secDbRef.setDbAc( "MI:0469" );
                }
                if ( property.getDatabase().equals( "uniprotkb" ) ) {
                    secDbRef.setDbAc( "MI:0486" );
                }
            }
            if ( property.hasText() ) {
                secDbRef.setSecondary( property.getText() );
            }
            refs.add(secDbRef);
        }
        return refs;
    }

    /**
     * This method updates the default hostOrganism for the xml.
     */
    public void updateHostOrganism( BinaryInteractionImpl binaryInteraction, Organism hostOrganism, int index ) {
        IntActBinaryInteraction dbi = ( IntActBinaryInteraction ) binaryInteraction;

        if ( dbi.hasHostOrganism() ) {
            CrossReference o = dbi.getHostOrganism().get( index );

            int taxid = Integer.parseInt( o.getIdentifier() );
            hostOrganism.setNcbiTaxId( taxid );

            Names organismNames = new Names();
            organismNames.setShortLabel( o.getDatabase() );
            if ( o.hasText() ) organismNames.setFullName( o.getText() );
            hostOrganism.setNames( organismNames );
        }
    }

    /**
     * Merge a Collection
     *
     * @param interaction
     * @param targets
     */
    public void mergeCollection( BinaryInteractionImpl interaction, BinaryInteractionImpl targets ) {
        
        IntActBinaryInteraction source =  (IntActBinaryInteraction) interaction;
        IntActBinaryInteraction target =  (IntActBinaryInteraction) targets;

        if ( source.hasExperimentalRolesInteractorA() )
            mergeCrossReference( source.getExperimentalRolesInteractorA(), target.getExperimentalRolesInteractorA());

        if ( source.hasExperimentalRolesInteractorB() )
            mergeCrossReference( source.getExperimentalRolesInteractorB(), target.getExperimentalRolesInteractorB());

        if ( source.hasHostOrganism() )
            mergeCrossReference( source.getHostOrganism(), target.getHostOrganism());

        if ( source.hasDatasetName() && target.hasDatasetName() ){
            mergeString( source.getDataset(), target.getDataset());
        }

    }

    private void mergeCrossReference(Collection<CrossReference> source, Collection<CrossReference> target ){

        if ( source == null ) throw new IllegalArgumentException( "Source collection must not be null." );

        if ( target == null ) throw new IllegalArgumentException( "Target collection must not be null." );

        if ( source == target ) throw new IllegalStateException( );

        for ( CrossReference s : source) {
            // Repeat of objects are a necessity (eg. for detection method across multiple publications)
            target.add( s );
        }
    }

    private void mergeString(Collection<String> source, Collection<String> target ){

        if ( source == null ) throw new IllegalArgumentException( "Source collection must not be null." );

        if ( target == null ) throw new IllegalArgumentException( "Target collection must not be null." );

        if ( source == target ) throw new IllegalStateException( );

        for ( String s : source) {
            // Repeat of objects are a necessity (eg. for detection method across multiple publications)
            target.add( s );
        }
    }
    ///////////////////////////////
    // IsExpansionStrategyAware

    public void process( BinaryInteractionImpl bi, Interaction interaction, ExpansionStrategy expansionStrategy ) {
        // first, do the usual processing
        process( bi, interaction );

        // deal with expansion strategy now
        if ( bi instanceof IntActBinaryInteraction ) {
            ( ( IntActBinaryInteraction ) bi ).setExpansionMethod( expansionStrategy.getName() );
        }
    }
}