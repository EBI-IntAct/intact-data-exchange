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
import psidev.psi.mi.tab.model.builder.MitabDocumentDefinition;

import java.util.*;
import java.io.*;

import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * This Class calculates the RelevanceScore for a given binaryinteraction
 * based on the properties(interactortype, experimentrole, biologicalrole, and name)
 * RSC should be of the format B1B2E1E2T1T2N1N2
 *
 * BETN, with
 * B=Biological Role (eg: enzyme, inhibitor)
 * E=Experimental Role(eg: bait, prey)
 * T=Molecule Type (eg: protein, smallmolecule)
 * N=Name. N will be abbreviated to a fixed length.
 *
 * For the given particular role or type, the property file is referred and the corresponding score is fetched.
 * For new roles or unspecified roles a default score N is assigned
 * Then generated overall score as
 * B1B2E1E2T1T2N1N2.
 *
 *   
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.2-SNAPSHOT
 */
public class RelevanceScoreCalculatorImpl implements RelevanceScoreCalculator {

    private static final String DEFAULT_SCORE = "N";

    private static final Log log = LogFactory.getLog( RelevanceScoreCalculatorImpl.class );

    Properties rscProperties;

    public RelevanceScoreCalculatorImpl() {
        rscProperties = new Properties();
    }

    public RelevanceScoreCalculatorImpl(Properties properties) {
        rscProperties = properties;
    }

    
    public String calculateScore( Row row ) {

        /*****************
         * Biological Role
         ****************/
        final Column biologicalRoleA = row.getColumnByIndex( IntactDocumentDefinition.BIOLOGICAL_ROLE_A );
        Set<String> biorolesA = getDescriptionFromColumn( biologicalRoleA );
        String B1 =  getScoreForGivenRolesAndTypes( biorolesA );

        final Column biologicalRoleB = row.getColumnByIndex( IntactDocumentDefinition.BIOLOGICAL_ROLE_B );
        Set<String> biorolesB = getDescriptionFromColumn( biologicalRoleB );
        String B2 =  getScoreForGivenRolesAndTypes( biorolesB );

        /*****************
         * Experimental Role
         ****************/
        final Column experimentRoleA = row.getColumnByIndex( IntactDocumentDefinition.EXPERIMENTAL_ROLE_A );
        Set<String> exprolesA = getDescriptionFromColumn( experimentRoleA );
        String E1 =  getScoreForGivenRolesAndTypes( exprolesA );

        final Column experimentRoleB = row.getColumnByIndex( IntactDocumentDefinition.EXPERIMENTAL_ROLE_B );
        Set<String> exprolesB = getDescriptionFromColumn( experimentRoleB );
        String E2 =  getScoreForGivenRolesAndTypes( exprolesB );


        /*****************
         * Interactor Types
         ****************/
        final Column typeA = row.getColumnByIndex( IntactDocumentDefinition.INTERACTOR_TYPE_A );
        final Column typeB = row.getColumnByIndex( IntactDocumentDefinition.INTERACTOR_TYPE_B );

        Set<String> typesA = getDescriptionFromColumn( typeA );
        Set<String> typesB = getDescriptionFromColumn( typeB );

        String T1 = getScoreForGivenRolesAndTypes(typesA);
        String T2 = getScoreForGivenRolesAndTypes(typesB);

        String B1B2E1E2T1T2 = appendAll( B1, B2, E1, E2, T1, T2 );
        if ( log.isDebugEnabled() ) {
            log.debug( "B1B2E1E2T1T2 " + B1B2E1E2T1T2);
        }

        return B1B2E1E2T1T2;

    }


    public String calculateScore( Row row, String nameA, String nameB ) {

        String B1B2E1E2T1T2 = calculateScore( row );
        nameA = limitToFixedLength( nameA );
        nameB = limitToFixedLength( nameB );

        String  B1B2E1E2T1T2N1N2 = appendAll( B1B2E1E2T1T2, nameA, nameB );
        return B1B2E1E2T1T2N1N2;

    }

    private String limitToFixedLength(String name){
        if ( name != null ) {
            if ( name.length() > 20 ) {
                name = name.substring( 0, 20 ).toLowerCase();
            }
            return name.toLowerCase();
        }
     return null;
    }

    private String appendAll(String... str){
        StringBuilder builder = new StringBuilder();
        for ( String s : str ) {
           builder.append(s );
        }

        return builder.toString();
    }
    private String getScoreForGivenRolesAndTypes( Set<String> roles ) {
        String score = DEFAULT_SCORE;
        if ( roles != null && roles.size() > 0 ) {
            if ( roles.size() > 1 ) {
                score = getMaxScoreForGivenRoles( roles );
            } else if ( roles.size() == 1 ) {
                score = getScoreForGivenRole( roles.iterator().next() );
            }
        }

        return score;
    }

    protected String getMaxScoreForGivenRoles( Set<String> roles ) {
        //read property file and load the role with maximum score
        String maxScore = DEFAULT_SCORE;
        for ( String role : roles ) {
            String tempScore = getScoreForGivenRole( role );
            if ( tempScore.compareTo( maxScore ) < 0 ) {
                maxScore = tempScore;
            }
        }

        return maxScore;
    }

    protected String getScoreForGivenRole(String role){
        role = role.replaceAll( "\\s+", "" );
        if(rscProperties.containsKey( role )){
            return (String) rscProperties.get( role );
        }else{
            return DEFAULT_SCORE;
        }

    }

    

 
    public boolean writePropertiesFile( OutputStream propertiesFileStream ) throws IOException {
        final Properties properties = getWeights();
        if ( properties != null ) {
            try {
                properties.store(  propertiesFileStream , null );
            } catch ( IOException e ) {
                throw new IOException( "IOException thrown when writing Rsc Properties File" );
            }
            return true;
        }

        return false;
    }


    public Properties readPropertiesFile( InputStream propertiesFileStream ) throws IOException {
        Properties properties = new Properties();
        try {
            properties.load(  propertiesFileStream  );
        } catch ( IOException e ) {
            e.printStackTrace();
            throw new IOException( "IOException thrown when reading Rsc Properties File " );
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

    public void setWeights( Properties rscProperties ) {
        this.rscProperties = rscProperties;
    }



}
