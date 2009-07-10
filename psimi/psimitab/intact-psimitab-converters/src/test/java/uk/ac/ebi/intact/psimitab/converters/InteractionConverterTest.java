package uk.ac.ebi.intact.psimitab.converters;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.Assert;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.psimitab.model.Parameter;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * InteractionConverter Tester.
 *
 * @author Nadin Neuhauser
 * @version 1.0
 * @since <pre>11/12/2007</pre>
 */
public class InteractionConverterTest extends IntactBasicTestCase {

    @Test
    public void convertToMitab() throws Exception {
        InteractionConverter interactionConverter = new InteractionConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        interaction.setAc( "EBI-zzzzzzz" );
        Iterator<Component> i = interaction.getComponents().iterator();
        i.next().getInteractor().setAc( "EBI-xxxxxxx" );
        i.next().getInteractor().setAc( "EBI-yyyyyyy" );

        BinaryInteraction bi = interactionConverter.toBinaryInteraction( interaction );

        assertNotNull( bi );
    }

    @Test
    public void convertToIntactMitab() throws Exception {
        InteractionConverter interactionConverter = new InteractionConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        Iterator<Component> i = interaction.getComponents().iterator();
        i.next().getInteractor().setAc( "EBI-xxxxxxx" );
        i.next().getInteractor().setAc( "EBI-yyyyyyy" );

        BinaryInteraction bi = interactionConverter.toBinaryInteraction( interaction );

        assertNotNull( bi );

    }

    @Test
    public void parameterTest() throws Exception {
        InteractionConverter interactionConverter = new InteractionConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        Iterator<Component> i = interaction.getComponents().iterator();
        final uk.ac.ebi.intact.model.Interactor interactorA = i.next().getInteractor();
        final uk.ac.ebi.intact.model.Interactor interactorB = i.next().getInteractor();
        interactorA.setAc( "EBI-xxxxxxx" );
        interactorB.setAc( "EBI-yyyyyyy" );

        final ComponentParameter paramB = interactorB.getActiveInstances().iterator().next().getParameters().iterator().next();
        paramB.setFactor(30.2);
        paramB.setExponent(1);

        final InteractionParameter interactionParameter =
                getMockBuilder().createInteractionParameter(
                        getMockBuilder().createCvObject(CvParameterType.class, "MI:9898", "kD"),
                        getMockBuilder().createCvObject(CvParameterUnit.class, "MI:9999", "kilodalton"),
                        4d);
        interaction.getParameters().add(interactionParameter);

        IntactBinaryInteraction bi = interactionConverter.toBinaryInteraction( interaction );

        assertNotNull( bi );
        final Collection<Component> components = interaction.getComponents();
        if ( components.size() != 2 ) {
            throw new IllegalArgumentException( "We only convert binary interaction (2 components or a single with stoichiometry 2)" );
        }

        //check Parameter A
        List<Parameter> parametersA = bi.getInteractorA().getParameters();

        final Parameter parameterA = parametersA.iterator().next();
        Assert.assertEquals( "temperature", parameterA.getType() );
        Assert.assertEquals( "302.0", parameterA.getValue() );
        Assert.assertEquals( "kelvin", parameterA.getUnit() );

        //check Parameter B
        List<Parameter> parametersB = bi.getInteractorB().getParameters();

        final Parameter parameterB = parametersB.iterator().next();
        Assert.assertEquals( "temperature", parameterB.getType() );
        Assert.assertEquals( "30.2 x 10^1", parameterB.getValue() );
        Assert.assertEquals( "kelvin", parameterB.getUnit() );

        //check ParametersInteraction
        List<Parameter> parametersInteraction = bi.getParameters();
        Assert.assertEquals(1, parametersInteraction.size());

        final Parameter ip = bi.getParameters().iterator().next();

        Assert.assertEquals("kD", ip.getType());
        Assert.assertEquals(4d, ip.getFactor());
        Assert.assertEquals("kilodalton", ip.getUnit());





    }//end method


    @Test
    public void convertToIntactMitabWithDatabaseSourceFilter() throws Exception {
        InteractionConverter interactionConverter = new InteractionConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        Iterator<Component> i = interaction.getComponents().iterator();
        i.next().getInteractor().setAc("EBI-xxxxxxx");
        i.next().getInteractor().setAc("EBI-yyyyyyy");

        //Create institution
        Institution institution = getMockBuilder().createInstitution("MI:0471", "mint");
        interaction.setOwner(institution);

        //Create institution Xrefs
        Collection<InstitutionXref> xrefs = new ArrayList<InstitutionXref>();

        //create identity CvXrefQualifier
        CvXrefQualifier identityQualifier = new CvXrefQualifier(institution, CvXrefQualifier.IDENTITY);
        identityQualifier.setIdentifier(CvXrefQualifier.IDENTITY_MI_REF);

        CvDatabase pubmedDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.PUBMED_MI_REF, CvDatabase.PUBMED);
        InstitutionXref pubmedXref1 = getMockBuilder().createXref(institution, "60001", identityQualifier, pubmedDb);
        xrefs.add(pubmedXref1);

        InstitutionXref pubmedXref2 = getMockBuilder().createXref(institution, "70001", identityQualifier, pubmedDb);
        xrefs.add(pubmedXref2);

        CvDatabase mintDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.PSI_MI_MI_REF, CvDatabase.PSI_MI);
        InstitutionXref mintXref = getMockBuilder().createXref(institution, "MI:0471", identityQualifier, mintDb);
        xrefs.add(mintXref);

        institution.setXrefs(xrefs);

        //3 institution xrefs 2 pubmeds, 1 psi-mi
        Assert.assertEquals(3, interaction.getOwner().getXrefs().size());

        IntactBinaryInteraction bi = interactionConverter.toBinaryInteraction(interaction);
        assertNotNull(bi);

        //after filtering pubmeds only 1 psi-mi xref should be there
        Assert.assertEquals(1, bi.getSourceDatabases().size());

/*
        final IntactDocumentDefinition docDef = new IntactDocumentDefinition();
        final String line = docDef.interactionToString(bi);
        System.out.println(line);
*/

    }

}