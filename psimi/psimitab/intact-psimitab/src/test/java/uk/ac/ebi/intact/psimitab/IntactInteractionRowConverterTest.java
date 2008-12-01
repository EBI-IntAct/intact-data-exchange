/*
 * Copyright (c) 2008 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
 
package uk.ac.ebi.intact.psimitab;

import org.junit.Test;
import junit.framework.Assert;
import psidev.psi.mi.tab.mock.PsimiTabMockBuilder;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.Column;
import psidev.psi.mi.tab.model.builder.Row;
import uk.ac.ebi.intact.psimitab.mock.IntactPsimiTabMockBuilder;

import java.util.List;

/**
 * IntactInteractionRowConverter Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class IntactInteractionRowConverterTest {

    private static final String EMPTY_COLUMN = String.valueOf( Column.EMPTY_COLUMN );

    @Test
    public void createRow_expansionMethod_empty() throws Exception {
        IntactInteractionRowConverter converter = new IntactInteractionRowConverter();
        IntactPsimiTabMockBuilder mockBuilder = new IntactPsimiTabMockBuilder();
        final IntactBinaryInteraction interaction = (IntactBinaryInteraction) mockBuilder.createInteractionRandom();

        final List<String> methods = interaction.getExpansionMethods();
        methods.clear();

        methods.add( EMPTY_COLUMN );

        final Row row = converter.createRow( interaction );
        final Column column = row.getColumnByIndex( IntactDocumentDefinition.EXPANSION_METHOD );
        Assert.assertNotNull( column );
        Assert.assertEquals( 1, column.getFields().size() );
        Assert.assertEquals( EMPTY_COLUMN, column.getFields().iterator().next().getValue() );
    }

    @Test
    public void createRow_expansionMethod_mixedSpoke() throws Exception {
        IntactInteractionRowConverter converter = new IntactInteractionRowConverter();
        IntactPsimiTabMockBuilder mockBuilder = new IntactPsimiTabMockBuilder();
        final IntactBinaryInteraction interaction = (IntactBinaryInteraction) mockBuilder.createInteractionRandom();

        final List<String> methods = interaction.getExpansionMethods();
        methods.clear();

        methods.add( EMPTY_COLUMN );
        methods.add( "Spoke" );

        final Row row = converter.createRow( interaction );
        final Column column = row.getColumnByIndex( IntactDocumentDefinition.EXPANSION_METHOD );
        Assert.assertNotNull( column );
        Assert.assertEquals( 1, column.getFields().size() );
        Assert.assertEquals( EMPTY_COLUMN, column.getFields().iterator().next().getValue() );
    }

    @Test
    public void createRow_expansionMethod_spokeOnly() throws Exception {
        IntactInteractionRowConverter converter = new IntactInteractionRowConverter();
        IntactPsimiTabMockBuilder mockBuilder = new IntactPsimiTabMockBuilder();
        final IntactBinaryInteraction interaction = (IntactBinaryInteraction) mockBuilder.createInteractionRandom();

        final List<String> methods = interaction.getExpansionMethods();
        methods.clear();

        final String spoke = "Spoke";
        methods.add( spoke );
        methods.add( spoke );

        final Row row = converter.createRow( interaction );
        final Column column = row.getColumnByIndex( IntactDocumentDefinition.EXPANSION_METHOD );
        Assert.assertNotNull( column );
        Assert.assertEquals( 1, column.getFields().size() );
        Assert.assertEquals( spoke, column.getFields().iterator().next().getValue() );
    }

    @Test (expected = IllegalStateException.class)
    public void createRow_expansionMethod_moreThan2Methods() throws Exception {
        IntactInteractionRowConverter converter = new IntactInteractionRowConverter();
        IntactPsimiTabMockBuilder mockBuilder = new IntactPsimiTabMockBuilder();
        final IntactBinaryInteraction interaction = (IntactBinaryInteraction) mockBuilder.createInteractionRandom();

        final List<String> methods = interaction.getExpansionMethods();
        methods.clear();

        methods.add( "Spoke" );
        methods.add( "Matrix" );
        methods.add( EMPTY_COLUMN );

        converter.createRow( interaction );
    }

    @Test (expected = IllegalStateException.class)
    public void createRow_expansionMethod_moreThan2MethodsIncludingEmptyColumn() throws Exception {
        IntactInteractionRowConverter converter = new IntactInteractionRowConverter();
        IntactPsimiTabMockBuilder mockBuilder = new IntactPsimiTabMockBuilder();
        final IntactBinaryInteraction interaction = (IntactBinaryInteraction) mockBuilder.createInteractionRandom();

        final List<String> methods = interaction.getExpansionMethods();
        methods.clear();

        methods.add( "Spoke" );
        methods.add( "Matrix" );

        converter.createRow( interaction );
    }
}
