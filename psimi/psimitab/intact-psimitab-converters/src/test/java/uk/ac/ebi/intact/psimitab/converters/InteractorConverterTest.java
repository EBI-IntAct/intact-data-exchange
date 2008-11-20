package uk.ac.ebi.intact.psimitab.converters;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.Assert;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.XrefUtils;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

import java.util.List;
import java.util.Collection;
import java.util.Iterator;

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

        Interactor interactor = converter.toMitab( c.getInteractor(), interaction );
        assertNotNull( interactor );

    }

    @Test
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

        Assert.assertEquals(4,interactorA.getXrefs().size());
        Assert.assertEquals(2,interactorB.getXrefs().size());

        Assert.assertEquals(1,XrefUtils.getIdentityXrefs(interactorA).size());
        Assert.assertEquals(1,XrefUtils.getIdentityXrefs(interactorB).size());

        //Database to Mitab
        InteractionConverter interactionConverter = new InteractionConverter();
        IntactBinaryInteraction intactBi = interactionConverter.toBinaryInteraction( binaryInteraction );

        List<CrossReference> propertiesA = intactBi.getInteractorA().getProperties();
        Assert.assertEquals(4,propertiesA.size());

        List<CrossReference> propertiesB = intactBi.getInteractorB().getProperties();
        Assert.assertEquals(2,propertiesB.size());
        Assert.assertTrue("No identifier in Property",checkIfPropertiesHasIdentity(intactBi));

        /*final IntactDocumentDefinition docDef = new IntactDocumentDefinition();
        final String line = docDef.interactionToString(intactBi);
        System.out.println(line);*/
    }

    private boolean checkIfPropertiesHasIdentity(IntactBinaryInteraction dbi) {
        boolean hasIdentifierInProperty = false;
        for (CrossReference crossReference : dbi.getInteractorA().getProperties()) {
            if (crossReference.getText().equals("identity")) {
                hasIdentifierInProperty = true;
            }
        }
        return hasIdentifierInProperty;
    }

}
