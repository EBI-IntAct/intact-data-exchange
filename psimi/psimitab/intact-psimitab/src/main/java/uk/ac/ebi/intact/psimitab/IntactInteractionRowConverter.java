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
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.builder.*;
import uk.ac.ebi.intact.psimitab.model.Annotation;
import uk.ac.ebi.intact.psimitab.model.Parameter;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactInteractionRowConverter extends AbstractInteractionRowConverter<IntactBinaryInteraction> {

    protected IntactBinaryInteraction newBinaryInteraction(Interactor interactorA, Interactor interactorB) {
        return new IntactBinaryInteraction((ExtendedInteractor)interactorA, (ExtendedInteractor)interactorB);
    }

    protected Interactor newInteractor() {
        return new ExtendedInteractor();
    }

    public IntactBinaryInteraction createBinaryInteraction(Row row) {
        if (row.getColumnCount() < 25) {
            throw new IllegalArgumentException("At least 25 columns were expected in row: "+row);
        }

        IntactBinaryInteraction binaryInteraction = super.createBinaryInteraction(row);
        populateBinaryInteraction(binaryInteraction, row);

        return binaryInteraction;
    }

    public Row createRow( IntactBinaryInteraction interaction ) {

        Row row = super.createRow(interaction);

        row.appendColumn( ParseUtils.createColumnFromCrossReferences( interaction.getInteractorA().getExperimentalRoles() ) );
        row.appendColumn( ParseUtils.createColumnFromCrossReferences( interaction.getInteractorB().getExperimentalRoles() ) );
        row.appendColumn( ParseUtils.createColumnFromCrossReferences( interaction.getInteractorA().getBiologicalRoles() ) );
        row.appendColumn( ParseUtils.createColumnFromCrossReferences( interaction.getInteractorB().getBiologicalRoles() ) );
        row.appendColumn( ParseUtils.createColumnFromCrossReferences( interaction.getInteractorA().getProperties() ) );
        row.appendColumn( ParseUtils.createColumnFromCrossReferences( interaction.getInteractorB().getProperties() ) );
        row.appendColumn( ParseUtils.createColumnFromCrossReferences( interaction.getInteractorA().getInteractorType() ) );
        row.appendColumn( ParseUtils.createColumnFromCrossReferences( interaction.getInteractorB().getInteractorType() ) );
        row.appendColumn( ParseUtils.createColumnFromCrossReferences( interaction.getHostOrganism() ) );
        row.appendColumn( createColumnFromStrings( interaction.getExpansionMethods() ) );
        row.appendColumn( createColumnFromStrings( interaction.getDataset() ) );
        row.appendColumn( createColumnFromAnnotations( interaction.getInteractorA().getAnnotations() ) );
        row.appendColumn( createColumnFromAnnotations( interaction.getInteractorB().getAnnotations() ) );
        row.appendColumn( createColumnFromParameters( interaction.getInteractorA().getParameters() ) );
        row.appendColumn( createColumnFromParameters( interaction.getInteractorB().getParameters() ) );
        row.appendColumn( createColumnFromParameters( interaction.getParameters() ) );

        return row;
    }

    @Override
    protected void populateBinaryInteraction( BinaryInteraction binaryInteraction, Row row ) {
        super.populateBinaryInteraction( binaryInteraction, row );

        IntactBinaryInteraction ibi = (IntactBinaryInteraction ) binaryInteraction;

        if (row.getColumnCount() <= IntactDocumentDefinition.EXPERIMENTAL_ROLE_A) {
            return;
        }

        Column expRoleA = row.getColumnByIndex(IntactDocumentDefinition.EXPERIMENTAL_ROLE_A);
        Column expRoleB = row.getColumnByIndex(IntactDocumentDefinition.EXPERIMENTAL_ROLE_B);
        Column bioRoleA = row.getColumnByIndex(IntactDocumentDefinition.BIOLOGICAL_ROLE_A);
        Column bioRoleB = row.getColumnByIndex(IntactDocumentDefinition.BIOLOGICAL_ROLE_B);
        Column propA = row.getColumnByIndex(IntactDocumentDefinition.PROPERTIES_A);
        Column propB = row.getColumnByIndex(IntactDocumentDefinition.PROPERTIES_B);
        Column typeA = row.getColumnByIndex(IntactDocumentDefinition.INTERACTOR_TYPE_A);
        Column typeB = row.getColumnByIndex(IntactDocumentDefinition.INTERACTOR_TYPE_B);
        Column hostOrganism = row.getColumnByIndex(IntactDocumentDefinition.HOST_ORGANISM);
        Column expansion = row.getColumnByIndex(IntactDocumentDefinition.EXPANSION_METHOD);
        Column dataset = row.getColumnByIndex(IntactDocumentDefinition.DATASET);


        ibi.getInteractorA().setExperimentalRoles(ParseUtils.createCrossReferences(expRoleA) );
        ibi.getInteractorB().setExperimentalRoles(ParseUtils.createCrossReferences(expRoleB) );
        ibi.getInteractorA().setBiologicalRoles(ParseUtils.createCrossReferences(bioRoleA) );
        ibi.getInteractorB().setBiologicalRoles(ParseUtils.createCrossReferences(bioRoleB) );
        ibi.getInteractorA().setProperties(ParseUtils.createCrossReferences(propA) );
        ibi.getInteractorB().setProperties(ParseUtils.createCrossReferences(propB) );
        ibi.getInteractorA().setInteractorType( ParseUtils.createCrossReferences( typeA ).iterator().next() );
        ibi.getInteractorB().setInteractorType( ParseUtils.createCrossReferences( typeB ).iterator().next() );
        ibi.setHostOrganism( ParseUtils.createCrossReferences( hostOrganism ) );
        ibi.setExpansionMethods( createStringsFromColumn( expansion ) );
        ibi.setDataset( createStringsFromColumn( dataset ) );


        // second extension (annotations

        if (row.getColumnCount() > IntactDocumentDefinition.ANNOTATIONS_B) {
            Column annotationsA = row.getColumnByIndex(IntactDocumentDefinition.ANNOTATIONS_A);
            ibi.getInteractorA().setAnnotations(createAnnotations(annotationsA));

            Column annotationsB = row.getColumnByIndex(IntactDocumentDefinition.ANNOTATIONS_B);
            ibi.getInteractorB().setAnnotations(createAnnotations(annotationsB));
        }

        // third extension (parameters)

        if(row.getColumnCount() > IntactDocumentDefinition.PARAMETERS_INTERACTION ){
            Column parametersA = row.getColumnByIndex( IntactDocumentDefinition.PARAMETERS_A );
            ibi.getInteractorA().setParameters(createParameters(parametersA) );

            Column parametersB = row.getColumnByIndex( IntactDocumentDefinition.PARAMETERS_B );
            ibi.getInteractorB().setParameters(createParameters(parametersB) );

            Column parametersInteraction = row.getColumnByIndex( IntactDocumentDefinition.PARAMETERS_INTERACTION );
            ibi.setParameters(createParameters(parametersInteraction) );
        }
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

    protected Column createColumnFromAnnotations( Collection<Annotation> annotations ) {
        final LinkedList<Field> fields = new LinkedList<Field>();
        for ( Annotation annotation : annotations ) {
            ParseUtils.addFieldToList( fields, createFieldFromAnnotation( annotation ));
        }
        return new Column( fields );
    }

     protected Column createColumnFromParameters( Collection<Parameter> parameters ) {
        final LinkedList<Field> fields = new LinkedList<Field>();
        for ( Parameter parameter : parameters ) {
            ParseUtils.addFieldToList(fields, createFieldFromParameter(parameter));
        }
        return new Column( fields );
    }

    protected Field createFieldFromAnnotation( Annotation annotation ) {
        return new Field( annotation.getType(), annotation.getText() );
    }

    protected Field createFieldFromParameter( Parameter parameter ) {
        return new Field( parameter.getType(), parameter.getValue(), parameter.getUnit() );
    }

    protected List<Annotation> createAnnotations(Column column) {
        List<Annotation> annotations = new ArrayList<Annotation>();

        for (Field field : column.getFields()) {
            annotations.add(createAnnotation(field));
        }

        return annotations;
    }

    protected Annotation createAnnotation(Field field) {
        return new Annotation(field.getType(), field.getValue());
    }

    protected List<Parameter> createParameters(Column column) {
        List<Parameter> parameters = new ArrayList<Parameter>();

        for (Field field : column.getFields()) {
            parameters.add(createParameter(field));
        }

        return parameters;
    }

    protected Parameter createParameter(Field field) {
        return new Parameter(field.getType(), field.getValue(),field.getDescription());
    }
}