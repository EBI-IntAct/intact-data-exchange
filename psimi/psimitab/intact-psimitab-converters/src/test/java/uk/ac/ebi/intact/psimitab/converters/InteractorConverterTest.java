package uk.ac.ebi.intact.psimitab.converters;

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.XrefUtils;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractionConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractorConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.MitabInteractor;

import java.io.IOException;
import java.io.StringWriter;
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
    public void toMitabTest() {
        InteractorConverter converter = new InteractorConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        final Component c = interaction.getComponents().iterator().next();
        c.getInteractor().setAc( "EBI-xxxxxx" );

        MitabInteractor interactor = converter.intactToMitab(c);
        assertNotNull( interactor );
        assertNotNull( interactor.getInteractor() );

    }

    @Test
    public void toMitabWithAllPropertiesTest() throws IOException {

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
        BinaryInteraction intactBi = interactionConverter.toBinaryInteraction( binaryInteraction );
        Assert.assertTrue("No identifier in PropertyA",checkIfPropertiesHasIdentity(intactBi.getInteractorA().getXrefs()));
        // two xrefs but no identity
        Assert.assertFalse("No identifier in PropertyB", checkIfPropertiesHasIdentity(intactBi.getInteractorB().getXrefs()));

        Assert.assertEquals(1,checkNumberOfIdentifiersInProperties(intactBi.getInteractorA().getXrefs()));
        Assert.assertEquals(0,checkNumberOfIdentifiersInProperties(intactBi.getInteractorB().getXrefs()));

        List<CrossReference> propertiesA = intactBi.getInteractorA().getXrefs();
        Assert.assertEquals(4,propertiesA.size());

        List<CrossReference> propertiesB = intactBi.getInteractorB().getXrefs();
        Assert.assertEquals(1,propertiesB.size());

        PsimiTabWriter writer = new PsimiTabWriter(PsimiTabVersion.v2_7);
        StringWriter stringWriter = new StringWriter();
        writer.write(intactBi, stringWriter);

        System.out.println(stringWriter.toString());
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
    public void intactIdentity_1() throws Exception {
        final Interaction bi = getMockBuilder().createInteractionRandomBinary();
        final Protein protein = getMockBuilder().createProtein( "P12345", "ABC_HUMAN" );
        protein.setAc( "EBI-12345" );
        Assert.assertEquals( 1, protein.getXrefs().size() );
        Assert.assertEquals( 1, protein.getAliases().size() );

        bi.getComponents().iterator().next().setInteractor( protein );
        
        final InteractorConverter converter = new InteractorConverter();
        final MitabInteractor interactor = converter.intactToMitab(bi.getComponents().iterator().next());
        assertNotNull( interactor );
        assertNotNull( interactor.getInteractor() );

        Assert.assertNotNull( interactor.getInteractor().getIdentifiers() );
        Assert.assertEquals( 1, interactor.getInteractor().getIdentifiers().size() );
        Assert.assertEquals( 1, interactor.getInteractor().getAlternativeIdentifiers().size() );

        Assert.assertNotNull( interactor.getInteractor().getAlternativeIdentifiers() );
        Assert.assertEquals( 1, interactor.getInteractor().getAlternativeIdentifiers().size() );

    }

    @Test
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
        final MitabInteractor interactor = converter.intactToMitab(bi.getComponents().iterator().next());
        assertNotNull( interactor );
        assertNotNull( interactor.getInteractor() );

        Assert.assertNotNull( interactor.getInteractor().getIdentifiers() );
        Assert.assertEquals( 1, interactor.getInteractor().getIdentifiers().size() );

        Assert.assertNotNull( interactor.getInteractor().getAlternativeIdentifiers() );
        Assert.assertEquals( 2, interactor.getInteractor().getAlternativeIdentifiers().size() );
        // two aliases plus display_short and display_long
        Assert.assertEquals( 3, interactor.getInteractor().getAliases().size() );

    }

    @Test
    public void addShortLabelToAliases() {

        final uk.ac.ebi.intact.model.Interactor interactorA_dna = getMockBuilder().createNucleicAcidRandom();
        final Protein interactorB_protein = getMockBuilder().createProteinRandom();

        Interaction binaryInteraction = getMockBuilder().createInteraction( interactorA_dna, interactorB_protein );

        interactorA_dna.setAc( "EBI-xxxxxA" );
        interactorB_protein.setAc( "EBI-xxxxxB" );

        interactorA_dna.setShortLabel( "iamadna" );
        interactorB_protein.setShortLabel( "iamaprotein" );

        InteractionConverter interactionConverter = new InteractionConverter();
        BinaryInteraction intactBi = interactionConverter.toBinaryInteraction( binaryInteraction );

        // display short plus display long, shortlabel, gene name
        Assert.assertEquals(3,intactBi.getInteractorA().getAliases().size());
        boolean hasFoundAliasLabel = false;
        for (psidev.psi.mi.tab.model.Alias al : intactBi.getInteractorA().getAliases()){
           if (al.getName().equalsIgnoreCase("iamadna")){
               if ("psi-mi".equals(al.getDbSource()) && "display_short".equals(al.getAliasType())){
                   hasFoundAliasLabel = true;
               }
           }
        }
        Assert.assertTrue(hasFoundAliasLabel);

        boolean hasFoundAliasLabel2 = false;
        for (psidev.psi.mi.tab.model.Alias al : intactBi.getInteractorB().getAliases()){
            if (al.getName().equalsIgnoreCase("iamaprotein") && al.getDbSource().equalsIgnoreCase("psi-mi")){
                Assert.assertEquals("display_long",al.getAliasType());
                hasFoundAliasLabel2=true;
            }
        }
        Assert.assertTrue(hasFoundAliasLabel2);

    }
}
