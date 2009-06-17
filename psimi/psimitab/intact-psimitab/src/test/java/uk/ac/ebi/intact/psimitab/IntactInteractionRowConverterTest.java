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
import psidev.psi.mi.tab.model.builder.Field;
import uk.ac.ebi.intact.psimitab.mock.IntactPsimiTabMockBuilder;
import uk.ac.ebi.intact.psimitab.model.Annotation;

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

    @Test
    public void removeLineCharacterFromAnnotationTest() throws Exception {
        IntactInteractionRowConverter converter = new IntactInteractionRowConverter();
        Annotation annotation = new Annotation( "comment", "test\ntext\n test\r\ntext" );

        Assert.assertTrue( annotation.getText().contains( "\n" ) );
        Assert.assertTrue( annotation.getText().contains( "\r" ) );

        Field field = converter.createFieldFromAnnotation( annotation );

        Assert.assertFalse( field.getValue().contains( "\n" ) );
        Assert.assertFalse( field.getValue().contains( "\r" ) );

    }


    @Test
    public void removeLineCharacterFromAnnotationFromBigMitabFile() throws Exception {

        String testString = "comment:\"Sequence:ACACAGAGAGAAAGGCTAAAGTTCTCTGGAGGATGTGGCTGCAGAGCCTGCTGCTCTTGG\n" +
                            " GCACTGTGGCCTGCAGCATCTCTGCACCCGCCCGCTCGCCCAGCCCCAGCACGCAGCCCT\n" +
                            " GGGAGCATGTGAATGCCATCCAGGAGGCCCGGCGTCTCCTGAACCTGAGTAGAGACACTG\n" +
                            " CTGCTGAGATGAATGAAACAG\r\nTAGAAGTCATCTCAGAAATGTTTGACCTCCAGGAGCCGA\n" +
                            " CCTGCCTACAGACCCGCCTGGAGCTGTACAAGCAGGGCCTGCGGGGCAGCCTCACCAAGC\n" +
                            " TCAAGGGCCCCTTGACCATGATGGCCAGCCACTACAAGCAGCACTGCCCTCCAACCCCGG\n" +
                            " AAACTTCCTGTGCAACCCAGATTATCACCTTTGAAAGTTTCAAAGAGAACCTGAAGGACT\r\n" +
                            " TTCTGCTTGTCATCCCCTTTGACTGCTGGGAGCCAGTCCAGGAGTGAGACCGGCCAGATG\n" +
                            " AGGCTGGCCAAGCCGGGGAGCTGCTCTCTCATGAAACAAGAGCTAGAAACTCAGGATGGT\n\n\n\n" +
                            " CATCTTGGAGGGACCAAGGGGTGGGCCACAGCCATGGTGGGAGTGGCCTGGACCTGCCCT\n" +
                            " GGGCCACACTGACCCTGATACAGGCATGGCAGAAGAATGGGAATATTTTATACTGACAGA\n" +
                            " AATCAGTAATATTTATATATTTATATTTTTAAAATATTTATTTATTTATTTATTTAAGTT\n" +
                            " CATATTCCATATTTATTCAAGATGTTTTACCGTAATAATTATTATTAAAAATATGCTTCT\n" +
                            " A\"";

        
        Annotation annotation = new Annotation( "comment", testString );
        Assert.assertTrue( annotation.getText().contains( "\n" ) );

        IntactInteractionRowConverter converter = new IntactInteractionRowConverter();
        Field field = converter.createFieldFromAnnotation( annotation );

        Assert.assertFalse( field.getValue().contains( "\n" ) );
        Assert.assertFalse( field.getValue().contains( "\r" ) );


    }


}
