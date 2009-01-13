/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.rsc;

import psidev.psi.mi.tab.model.builder.Row;
import psidev.psi.mi.tab.model.builder.Column;
import psidev.psi.mi.tab.model.builder.Field;

import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;

import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import org.apache.commons.lang.StringUtils;

/**
 * This Class calculates the RelevanceScore for a given binaryinteraction
 * based on the properties(interactortype, experimentrole and biologicalrole)
 * RSC should also include Name but calculateScore doesn't handle this
 *
 * InteractorType = The interactorTypes for the given InteractorA(eg: Protein) and InteractorB(eg: Small Molecule)
 * is stored in the Property as Protein-Small Molecule. Similarly all the combinations are stored.
 * All the combinations are ranked and given relevance score (usually by curators) and stored in .properties file
 * Whenever calculateScore encounters allready existing combination it returns the relavance score for the same and if not
 * if gives a default relavance score for that type (eg: SM1, SM2...)
 *
 * For Roles = Take Experimental Role of A and Biological role of A, if one of it is unspecified role and other one is stored in property(mergeRole method does that)
 * similarly Take Experimental Role of B and Biological role of B, if one of it is unspecified role and other one is stored in property(mergeRole method does that)
 *   
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.2-SNAPSHOT
 */
public class RelevanceScoreCalculatorImpl implements RelevanceScoreCalculator {

    private static final String UNSPECIFIED_ROLE = "unspecified role";
    private static final String DEFAULT_TYPE_PREFIX = "SM";
    private static final String DEFAULT_ROLE_PREFIX = "RL";


    Properties rscProperties;

    public RelevanceScoreCalculatorImpl() {
        rscProperties = new Properties();
    }

    public RelevanceScoreCalculatorImpl(Properties properties) {
        rscProperties = properties;
    }

    public String calculateScore( Row row ) {

        /*****************
         * Experimental Role
         ****************/
        final Column experimentRoleA = row.getColumnByIndex( IntactDocumentDefinition.EXPERIMENTAL_ROLE_A );
        Set<String> exprolesA = getDescriptionFromColumn( experimentRoleA );
        if ( exprolesA != null && exprolesA.size() > 1 ) {
            removeUnspecifiedRole( exprolesA );
        }
        String expRoleA = joinRoles( exprolesA );

        final Column experimentRoleB = row.getColumnByIndex( IntactDocumentDefinition.EXPERIMENTAL_ROLE_B );
        Set<String> exprolesB = getDescriptionFromColumn( experimentRoleB );
        if ( exprolesB != null && exprolesB.size() > 1) {
            removeUnspecifiedRole( exprolesB );
        }

        String expRoleB = joinRoles( exprolesB );

        /*****************
         * Biological Role
         ****************/
        final Column biologicalRoleA = row.getColumnByIndex( IntactDocumentDefinition.BIOLOGICAL_ROLE_A );
        Set<String> biorolesA = getDescriptionFromColumn( biologicalRoleA );
        if ( biorolesA != null && biorolesA.size() > 1 ) {
            removeUnspecifiedRole( biorolesA );
        }
        String bioRoleA = joinRoles( biorolesA );

        final Column biologicalRoleB = row.getColumnByIndex( IntactDocumentDefinition.BIOLOGICAL_ROLE_B );
        Set<String> biorolesB = getDescriptionFromColumn( biologicalRoleB );
         if(biorolesB!=null && biorolesB.size() > 1){
            removeUnspecifiedRole( biorolesB );
        }
        String bioRoleB = joinRoles( biorolesB );

      //merge Roles
        String mergedRoleA = mergeRole( expRoleA, bioRoleA );
        //remove white spaces as the properties.store method will add escate character
        if(mergedRoleA!=null){
            mergedRoleA = mergedRoleA.replaceAll( "\\s+","" );
        }
        addToProperties( mergedRoleA, DEFAULT_ROLE_PREFIX );

        String mergedRoleB = mergeRole( expRoleB, bioRoleB );
        if(mergedRoleB!=null){
            mergedRoleB = mergedRoleB.replaceAll( "\\s+","" );
        }
        addToProperties( mergedRoleB, DEFAULT_ROLE_PREFIX );

        String mergedRoleA_B = mergeRole (mergedRoleA,mergedRoleB);
        if(mergedRoleA_B!=null){
            mergedRoleA_B.replaceAll( "\\s+","" );
        }
        String rscScoreRoleA_B = addToProperties( mergedRoleA_B, DEFAULT_ROLE_PREFIX );

        /*****************
         * Interactor Types
         ****************/
        final Column typeA = row.getColumnByIndex( IntactDocumentDefinition.INTERACTOR_TYPE_A );
        final Column typeB = row.getColumnByIndex( IntactDocumentDefinition.INTERACTOR_TYPE_B );

        Set<String> typesA = getDescriptionFromColumn( typeA );
        Set<String> typesB = getDescriptionFromColumn( typeB );

        String mergedType;
        if ( typesA != null && typesA.size() == 1 && typesB != null && typesB.size() == 1 ) {
            String strTypeA = typesA.iterator().next();
            String strTypeB = typesB.iterator().next();
            mergedType = mergeStringsAfterSorting( strTypeA, strTypeB );
        } else {
            throw new IllegalStateException( "Either one of the interactorType is missing for an interactor or it has more than one type " + row.toString() );
        }

        if(mergedType!=null){
            mergedType = mergedType.replaceAll( "\\s+","" );
        }
        String rscScoreType = addToProperties( mergedType, DEFAULT_TYPE_PREFIX );


        return mergeStrings( rscScoreType, rscScoreRoleA_B );
    }


    private void removeUnspecifiedRole( Set<String> roles ) {

        if ( roles.contains( UNSPECIFIED_ROLE ) ) {
            roles.remove( UNSPECIFIED_ROLE );
        }

    }

    protected String addToProperties( String property, String defaultKey ) {

        if ( rscProperties.containsKey( property ) ) {
            return rscProperties.getProperty( property );
        } else {
            //add to the properties  and return value
            final Collection<Object> values = rscProperties.values();

            int incrementor = 1;

            if ( values != null && values.size() > 0 ) {
                incrementor = getNextIncrementor( values, defaultKey );
            }
            rscProperties.put( property, defaultKey + incrementor );
            return rscProperties.getProperty( property );
        }

    }

    private int getNextIncrementor(Collection<Object> values, String defaultKey){

        int index = 1;

        for ( Object value : values ) {
             String valueStr = (String)value;
            if(valueStr.startsWith( defaultKey )){
                index++;
            }
        }
      return index;
    }

    public boolean writePropertiesFile( File propertiesFile ) throws IOException {
        final Properties properties = getWeights();
        if ( properties != null ) {
            try {
                properties.store( new FileOutputStream( propertiesFile ), null );
            } catch ( IOException e ) {
                throw new IOException( "IOException thrown when writing Rsc Properties File" );
            }
            return true;
        }

        return false;
    }


    public Properties readPropertiesFile( File propertiesFile ) throws IOException {
        Properties properties = new Properties();
        try {
            properties.load( new FileInputStream( propertiesFile ) );
        } catch ( IOException e ) {
            throw new IOException( "IOException thrown when reading Rsc Properties File" );
        }
        return properties;
    }

    public Properties getWeights() {
        return rscProperties;
    }

    private Set<String> getDescriptionFromColumn( Column column ) {

        Set<String> descriptions = new HashSet<String>();
        for ( Field field : column.getFields() ) {
            if ( field.getDescription() != null ) {
                descriptions.add( field.getDescription() );
            }
        }
        return descriptions;
    }

    private String joinRoles( Set<String> roles ) {
        String role;
        if ( roles.size() > 1 ) {
            Collections.sort( new ArrayList<String>(roles) );
            role = StringUtils.join( roles.toArray(), "," );
        } else {
            role = roles.iterator().next();
        }
        return role;
    }



    private String mergeRole( String roleA, String roleB ) {
        String mergedRole;

        if(UNSPECIFIED_ROLE.equals( roleA ) && UNSPECIFIED_ROLE.equals( roleB )){
            return "unspecifiedrole";
        }

        if ( UNSPECIFIED_ROLE.equals( roleA ) ) {
            mergedRole = roleB;
        } else if ( UNSPECIFIED_ROLE.equals( roleB ) ) {
            mergedRole = roleA;
        } else {
            mergedRole = mergeStringsAfterSorting(roleA, roleB);
        }
        return mergedRole;
    }

    private String mergeStrings(String typeA, String typeB){
      return typeA + "-"+ typeB;
    }

    private String mergeStringsAfterSorting( String typeA, String typeB ) {
        if ( typeA == null ) {
            throw new NullPointerException( "You must give a non null typeA" );
        }

        if ( typeB == null ) {
            throw new NullPointerException( "You must give a non null typeB" );
        }

         if(typeA.compareToIgnoreCase( typeB )< 0){
            return typeA + "-" + typeB;
        }else if(typeA.compareToIgnoreCase( typeB )>0){
            return typeB + "-" + typeA;
        }else{
             return typeA + "-" + typeB;
        }
    }
}
