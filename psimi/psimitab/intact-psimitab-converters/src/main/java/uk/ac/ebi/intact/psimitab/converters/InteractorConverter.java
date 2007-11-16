/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.converters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.*;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvAliasType;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.InteractorAlias;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Interactor Converter.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class InteractorConverter {

    private static final Log logger = LogFactory.getLog( InteractorConverter.class );

    private CrossReferenceConverter xRefConverter = new CrossReferenceConverter();

    private static final List<String> uniprotKeys = new ArrayList( Arrays.asList( new String[]
            {"gene name", "gene name synonym, isoform synonym, ordered locus name, open reading frame name"} ) );


    public Interactor toMitab( uk.ac.ebi.intact.model.Interactor intactInteractor ) {
        if (intactInteractor == null){
            throw new IllegalArgumentException( "Interactor must not be null" );
        }

        Interactor tabInteractor = new Interactor();

        // set identifiers of interactor
        Collection<CrossReference> identifiers = xRefConverter.toMitab( intactInteractor.getXrefs(), true );
        identifiers.add( CrossReferenceFactory.getInstance().build( CvDatabase.INTACT, intactInteractor.getAc() ) );
        tabInteractor.setIdentifiers( identifiers );

        // set alternative identifiers of interactor  & set aliases of interactor
        if ( intactInteractor.getAliases() != null ) {
            Collection<CrossReference> altIds = new ArrayList<CrossReference>();
            Collection<Alias> tabAliases = new ArrayList<Alias>();
            for ( InteractorAlias alias : intactInteractor.getAliases() ) {
                String text = alias.getCvAliasType().getShortLabel();
                String id = alias.getName();
                String db;
                if ( uniprotKeys.contains( text ) ) {
                    db = CvDatabase.UNIPROT;
                } else {
                    db = CvDatabase.INTACT;
                }

                if ( id != null && db != null ) {
                    if ( text.equals( CvAliasType.GENE_NAME ) ) {
                        CrossReference altId = CrossReferenceFactory.getInstance().build( db, id, text );
                        altIds.add( altId );
                    } else {
                        Alias tabAlias = new AliasImpl( db, id );
                        tabAliases.add( tabAlias );
                    }
                }
            }
            tabInteractor.setAlternativeIdentifiers( altIds );
            tabInteractor.setAliases( tabAliases );
        }

        // set taxid of interactor
        if ( intactInteractor.getBioSource() != null ) {
            int taxid = Integer.parseInt( intactInteractor.getBioSource().getTaxId() );
            String name = intactInteractor.getBioSource().getShortLabel();
            Organism oragnism = new OrganismImpl( taxid, name );
            tabInteractor.setOrganism( oragnism );
        }

        return tabInteractor;
    }


    public Component fromMitab( Interactor interactor ) {
        return null;
    }
}
