package uk.ac.ebi.intact.psimitab.converters;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.XrefUtils;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * InteractorConverter Tester.
 *
 * @author Nadin Neuhauser
 * @version 1.0
 * @since <pre>11/12/2007</pre>
 */
public class InteractorConverterTest extends IntactBasicTestCase {


    @Test
    @DirtiesContext
    public void toMitabTest() {
        InteractorConverter converter = new InteractorConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        final Component c = interaction.getComponents().iterator().next();
        c.getInteractor().setAc( "EBI-xxxxxx" );

        Interactor interactor = converter.toMitab( c, interaction );
        assertNotNull( interactor );

    }

    @Test
    @DirtiesContext
    public void toMitabWithAllPropertiesTest(){

        Interaction binaryInteraction = getMockBuilder().createInteractionRandomBinary();
        Iterator<Component> i = binaryInteraction.getComponents().iterator();
        final uk.ac.ebi.intact.model.Interactor interactorA = i.next().getInteractor();
        final uk.ac.ebi.intact.model.Interactor interactorB = i.next().getInteractor();

        interactorA.setAc( "EBI-xxxxxA" );
        interactorB.setAc( "EBI-xxxxxB" );

        CvDatabase goDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
        InteractorXref goXrefA = getMockBuilder().createXref(interactorA, "GO:0007028", null, goDb);
        goXrefA.setSecondaryId("some textA");
        interactorA.addXref(goXrefA);

        InteractorXref goXrefB = getMockBuilder().createXref(interactorA, "GO:0008032", null, goDb);
        goXrefB.setSecondaryId("some textB");
        interactorB.addXref(goXrefB);

        CvDatabase interproDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.INTERPRO_MI_REF, CvDatabase.INTERPRO);
        CvXrefQualifier seeAlso = new CvXrefQualifier(getMockBuilder().createInstitution("MI:1234","ebi"),CvXrefQualifier.SEE_ALSO);
        interactorA.addXref(getMockBuilder().createXref(interactorA, "IPR000867", seeAlso, interproDb));

        CvDatabase uniprotDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.UNIPROT_MI_REF, CvDatabase.UNIPROT);
        CvXrefQualifier secAc = new CvXrefQualifier(getMockBuilder().createInstitution("MI:1234","ebi"),CvXrefQualifier.SECONDARY_AC);
        interactorA.addXref(getMockBuilder().createXref(interactorA,"P26439", secAc,uniprotDb));

        CvDatabase intactDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.INTACT_MI_REF, CvDatabase.INTACT);
        CvXrefQualifier intactIdentity = new CvXrefQualifier(getMockBuilder().createInstitution("MI:1234","ebi"),CvXrefQualifier.IDENTITY);
        interactorA.addXref(getMockBuilder().createXref(interactorA,"EBI-12345", intactIdentity,intactDb));

        Assert.assertEquals(5,interactorA.getXrefs().size());
        Assert.assertEquals(2,interactorB.getXrefs().size());

        Assert.assertEquals(1,XrefUtils.getIdentityXrefs(interactorA).size());
        Assert.assertEquals(1,XrefUtils.getIdentityXrefs(interactorB).size());

        InteractorAlias geneNameSynonymAlias = getMockBuilder().createAlias(interactorA, "lala-gene-name-synonym", CvAliasType.GENE_NAME_SYNONYM_MI_REF, CvAliasType.GENE_NAME_SYNONYM);

        interactorA.addAlias(geneNameSynonymAlias);


        //Database to Mitab
        InteractionConverter interactionConverter = new InteractionConverter();
        IntactBinaryInteraction intactBi = interactionConverter.toBinaryInteraction( binaryInteraction );
        Assert.assertTrue("No identifier in PropertyA",checkIfPropertiesHasIdentity(intactBi.getInteractorA().getProperties()));
        Assert.assertTrue("No identifier in PropertyB",checkIfPropertiesHasIdentity(intactBi.getInteractorB().getProperties()));

        Assert.assertEquals(2,checkNumberOfIdentifiersInProperties(intactBi.getInteractorA().getProperties()));
        Assert.assertEquals(1,checkNumberOfIdentifiersInProperties(intactBi.getInteractorB().getProperties()));

        List<CrossReference> propertiesA = intactBi.getInteractorA().getProperties();
        Assert.assertEquals(5,propertiesA.size());

        List<CrossReference> propertiesB = intactBi.getInteractorB().getProperties();
        Assert.assertEquals(2,propertiesB.size());

        final IntactDocumentDefinition docDef = new IntactDocumentDefinition();
        final String line = docDef.interactionToString(intactBi);
        System.out.println(line);
    }

    private boolean checkIfPropertiesHasIdentity(Collection<CrossReference> crossReferences) {
        boolean hasIdentifierInProperty = false;
        for (CrossReference crossReference : crossReferences) {
            if (crossReference.getText().equals("identity")) {
                hasIdentifierInProperty = true;
            }
        }
        return hasIdentifierInProperty;
    }

    private int  checkNumberOfIdentifiersInProperties(Collection<CrossReference> crossReferences) {
        int hasIdentifierInPropertyCounter = 0;
        for (CrossReference crossReference : crossReferences) {
            if (crossReference.getText().equals("identity")) {
                hasIdentifierInPropertyCounter++;
            }
        }
        return hasIdentifierInPropertyCounter;
    }

    @Test
    @DirtiesContext
    public void intactIdentity_1() throws Exception {
        final Interaction bi = getMockBuilder().createInteractionRandomBinary();
        final Protein protein = getMockBuilder().createProtein( "P12345", "ABC_HUMAN" );
        protein.setAc( "EBI-12345" );
        Assert.assertEquals( 1, protein.getXrefs().size() );
        Assert.assertEquals( 1, protein.getAliases().size() );

        bi.getComponents().iterator().next().setInteractor( protein );
        
        final InteractorConverter converter = new InteractorConverter();
        final ExtendedInteractor ei = converter.toMitab(  bi.getComponents().iterator().next(), bi );

        Assert.assertNotNull( ei );

        Assert.assertNotNull( ei.getIdentifiers() );
        Assert.assertEquals( 2, ei.getIdentifiers().size() );

        Assert.assertNotNull( ei.getAlternativeIdentifiers() );
        Assert.assertEquals( 1, ei.getAlternativeIdentifiers().size() );

    }

    @Test
    @DirtiesContext
    public void intactIdentity_2() throws Exception {
        final Interaction bi = getMockBuilder().createInteractionRandomBinary();
        final Protein protein = getMockBuilder().createProtein( "P12345", "ABC_HUMAN" );
        protein.setAc( "EBI-12345" );
        CvDatabase intact = getMockBuilder().createCvObject( CvDatabase.class, CvDatabase.INTACT_MI_REF, CvDatabase.INTACT );
        protein.getXrefs().add( getMockBuilder().createIdentityXref( protein, "EBI-99999", intact ) );

        Assert.assertEquals( 2, protein.getXrefs().size() );
        Assert.assertEquals( 1, protein.getAliases().size() );

        bi.getComponents().iterator().next().setInteractor( protein );

        final InteractorConverter converter = new InteractorConverter();
        final ExtendedInteractor ei = converter.toMitab(  bi.getComponents().iterator().next(), bi );

        Assert.assertNotNull( ei );

        Assert.assertNotNull( ei.getIdentifiers() );
        Assert.assertEquals( 2, ei.getIdentifiers().size() );

        Assert.assertNotNull( ei.getAlternativeIdentifiers() );
        Assert.assertEquals( 2, ei.getAlternativeIdentifiers().size() );

    }

    @Test
    @DirtiesContext
    public void addShortLabelToAlternativeId() {

        final uk.ac.ebi.intact.model.Interactor interactorA_dna = getMockBuilder().createNucleicAcidRandom();
        final Protein interactorB_protein = getMockBuilder().createProteinRandom();

        Interaction binaryInteraction = getMockBuilder().createInteraction( interactorA_dna, interactorB_protein );

        interactorA_dna.setAc( "EBI-xxxxxA" );
        interactorB_protein.setAc( "EBI-xxxxxB" );

        interactorA_dna.setShortLabel( "iamadna" );
        interactorB_protein.setShortLabel( "iamaprotein" );

        InteractionConverter interactionConverter = new InteractionConverter();
        IntactBinaryInteraction intactBi = interactionConverter.toBinaryInteraction( binaryInteraction );

        Assert.assertEquals(1,intactBi.getInteractorA().getAlternativeIdentifiers().size());
        Assert.assertEquals("iamadna",intactBi.getInteractorA().getAlternativeIdentifiers().iterator().next().getIdentifier());
        Assert.assertEquals("intact",intactBi.getInteractorA().getAlternativeIdentifiers().iterator().next().getDatabase());
        Assert.assertEquals("shortlabel",intactBi.getInteractorA().getAlternativeIdentifiers().iterator().next().getText());

        Assert.assertEquals("iamaprotein",intactBi.getInteractorB().getAlternativeIdentifiers().iterator().next().getIdentifier());
        Assert.assertEquals("uniprotkb",intactBi.getInteractorB().getAlternativeIdentifiers().iterator().next().getDatabase());
        Assert.assertEquals("shortlabel",intactBi.getInteractorB().getAlternativeIdentifiers().iterator().next().getText());

    }
}
