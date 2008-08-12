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
package uk.ac.ebi.intact.psimitab;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.Column;
import psidev.psi.mi.tab.model.builder.Field;
import psidev.psi.mi.tab.model.builder.MitabInteractionRowConverter;
import psidev.psi.mi.tab.model.builder.Row;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactInteractionRowConverter extends MitabInteractionRowConverter {

    public BinaryInteraction createBinaryInteraction(Row row) {
        if (row.getColumnCount() < 25) {
            throw new IllegalArgumentException("At least 25 columns were expected in row: "+row);
        }

        BinaryInteraction binaryInteraction = new IntactBinaryInteraction(createInteractorA(row), createInteractorB(row));
        populateBinaryInteraction(binaryInteraction, row);

        return binaryInteraction;
    }

    public Row createRow( BinaryInteraction binaryInteraction ) {
        IntactBinaryInteraction interaction = (IntactBinaryInteraction) binaryInteraction;

        Row row = super.createRow(binaryInteraction);

        row.appendColumn( createColumnFromCrossReferences( interaction.getExperimentalRolesInteractorA() ) );
        row.appendColumn( createColumnFromCrossReferences( interaction.getExperimentalRolesInteractorB() ) );
        row.appendColumn( createColumnFromCrossReferences( interaction.getBiologicalRolesInteractorA() ) );
        row.appendColumn( createColumnFromCrossReferences( interaction.getBiologicalRolesInteractorB() ) );
        row.appendColumn( createColumnFromCrossReferences( interaction.getPropertiesA() ) );
        row.appendColumn( createColumnFromCrossReferences( interaction.getPropertiesB() ) );
        row.appendColumn( createColumnFromCrossReferences( interaction.getInteractorTypeA() ) );
        row.appendColumn( createColumnFromCrossReferences( interaction.getInteractorTypeB() ) );
        row.appendColumn( createColumnFromCrossReferences( interaction.getHostOrganism() ) );
        row.appendColumn( createColumnFromStrings( interaction.getExpansionMethods() ) );
        row.appendColumn( createColumnFromStrings( interaction.getDataset() ) );
        row.appendColumn( createColumnFromConfidences( interaction.getConfidenceValues() ) );

        return row;
    }

    @Override
    protected void populateBinaryInteraction( BinaryInteraction binaryInteraction, Row row ) {
        super.populateBinaryInteraction( binaryInteraction, row );

        IntactBinaryInteraction ibi = (IntactBinaryInteraction ) binaryInteraction;

        Column expRoleA = row.getColumnByIndex(15);
        Column expRoleB = row.getColumnByIndex(16);
        Column bioRoleA = row.getColumnByIndex(17);
        Column bioRoleB = row.getColumnByIndex(18);
        Column propA = row.getColumnByIndex(19);
        Column propB = row.getColumnByIndex(20);
        Column typeA = row.getColumnByIndex(21);
        Column typeB = row.getColumnByIndex(22);
        Column hostOrganism = row.getColumnByIndex(23);
        Column expansion = row.getColumnByIndex(24);
        Column dataset = row.getColumnByIndex(25);

        ibi.setExperimentalRolesInteractorA(createCrossReferences(expRoleA) );
        ibi.setExperimentalRolesInteractorB(createCrossReferences(expRoleB) );
        ibi.setBiologicalRolesInteractorA(createCrossReferences(bioRoleA) );
        ibi.setBiologicalRolesInteractorB(createCrossReferences(bioRoleB) );
        ibi.setPropertiesA(createCrossReferences(propA) );
        ibi.setPropertiesB(createCrossReferences(propB) );
        ibi.setInteractorTypeA( createCrossReferences( typeA ) );
        ibi.setInteractorTypeB( createCrossReferences( typeB ) );
        ibi.setHostOrganism( createCrossReferences( hostOrganism ) );
        ibi.setExpansionMethods( createStringsFromColumn( expansion ) );
        ibi.setDataset( createStringsFromColumn( dataset ) );
    }

    protected List<String> createStringsFromColumn( Column column ) {
        List<String> strings = new ArrayList<String>( );
        for ( Field field : column.getFields() ) {
            strings.add( field.getValue() );
        }
        return strings;
    }

    protected Column createColumnFromStrings( List<String> strings ) {
        final LinkedList<Field> fields = new LinkedList<Field>();
        for ( String str : strings ) {
            fields.add( new Field(str) );
        }
        return new Column( fields );
    }
}