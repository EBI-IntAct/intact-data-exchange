package uk.ac.ebi.intact.psimitab.converters;

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        interaction.setOwner((Institution) IntactContext.getCurrentInstance().getSpringContext().getBean("institutionIntact"));

        interaction.setAc( "EBI-zzzzzzz" );
        Iterator<Component> i = interaction.getComponents().iterator();
        i.next().getInteractor().setAc( "EBI-xxxxxxx" );
        i.next().getInteractor().setAc( "EBI-yyyyyyy" );

        Experiment exp = interaction.getExperiments().iterator().next();
        CvDatabase imex = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        CvXrefQualifier imexPrimary = getMockBuilder().createCvObject(CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        exp.addXref(getMockBuilder().createXref(exp, "IM-1234", imexPrimary, imex));

        interaction.addXref(getMockBuilder().createXref(interaction,  "IM-1234-1", imexPrimary, imex));

        BinaryInteraction bi = interactionConverter.toBinaryInteraction( interaction );

        assertNotNull( bi );

        Assert.assertEquals(2, bi.getPublications().size());  // imex and pubmed
        Assert.assertEquals(2, bi.getInteractionAcs().size());

        CrossReference sourceXref = (CrossReference) bi.getSourceDatabases().iterator().next();
        System.out.println(sourceXref);
        Assert.assertEquals("MI:0469", sourceXref.getIdentifier());
        Assert.assertEquals("intact", sourceXref.getText());
    }

    @Test
    public void convertToMitab_imexInPub() throws Exception {
        InteractionConverter interactionConverter = new InteractionConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        interaction.setAc( "EBI-zzzzzzz" );
        Iterator<Component> i = interaction.getComponents().iterator();
        i.next().getInteractor().setAc( "EBI-xxxxxxx" );
        i.next().getInteractor().setAc( "EBI-yyyyyyy" );

        Publication pub = interaction.getExperiments().iterator().next().getPublication();
        CvDatabase imex = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        CvXrefQualifier imexPrimary = getMockBuilder().createCvObject(CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        pub.addXref(getMockBuilder().createXref(pub, "IM-1234", imexPrimary, imex));

        BinaryInteraction bi = interactionConverter.toBinaryInteraction( interaction );

        assertNotNull( bi );

        Assert.assertEquals(2, bi.getPublications().size());  // imex and pubmed
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
    public void convertToIntactMitabConfidences() throws Exception {
        InteractionConverter interactionConverter = new InteractionConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        interaction.getConfidences().add(getMockBuilder().createConfidence(CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvConfidenceType.class, null, "intact-score"), "0.8"));
        Iterator<Component> i = interaction.getComponents().iterator();
        i.next().getInteractor().setAc( "EBI-xxxxxxx" );
        i.next().getInteractor().setAc( "EBI-yyyyyyy" );

        Assert.assertEquals(1, interaction.getConfidences().size());

        BinaryInteraction bi = interactionConverter.toBinaryInteraction( interaction );

        assertNotNull( bi );
        assertEquals(1, bi.getConfidenceValues().size());
        psidev.psi.mi.tab.model.Confidence conf = (Confidence) bi.getConfidenceValues().iterator().next();
        assertEquals("intact-score", conf.getType());
        assertEquals("0.8", conf.getValue());
    }

    @Test
    public void convertToIntactMitab_imex() throws Exception {
        InteractionConverter interactionConverter = new InteractionConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();

        CvDatabase imexDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        CvXrefQualifier imexPrimary = getMockBuilder().createCvObject(CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        interaction.addXref(getMockBuilder().createXref(interaction, "IM-1234-1", imexPrimary, imexDb));

        BinaryInteraction bi = interactionConverter.toBinaryInteraction( interaction );

        CrossReference xref = (CrossReference) bi.getInteractionAcs().iterator().next();

        Assert.assertEquals("imex", xref.getDatabase());
        Assert.assertEquals("IM-1234-1", xref.getIdentifier());

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

        final InteractionParameter interactionParameter =
                getMockBuilder().createInteractionParameter(
                        getMockBuilder().createCvObject(CvParameterType.class, "MI:9898", "kD"),
                        getMockBuilder().createCvObject(CvParameterUnit.class, "MI:9999", "kilodalton"),
                        4d);
        interaction.getParameters().add(interactionParameter);

        BinaryInteraction<psidev.psi.mi.tab.model.Interactor> bi = interactionConverter.toBinaryInteraction( interaction );

        assertNotNull( bi );
        final Collection<Component> components = interaction.getComponents();
        if ( components.size() != 2 ) {
            throw new IllegalArgumentException( "We only convert binary interaction (2 components or a single with stoichiometry 2)" );
        }

        //check ParametersInteraction
        List<psidev.psi.mi.tab.model.Parameter> parametersInteraction = bi.getInteractionParameters();
        Assert.assertEquals(1, parametersInteraction.size());

        final psidev.psi.mi.tab.model.Parameter ip = bi.getInteractionParameters().iterator().next();

        Assert.assertEquals("kD", ip.getType());
        Assert.assertEquals(4d, ip.getFactor(), 0d);
        Assert.assertEquals("kilodalton", ip.getUnit());

        CrossReference sourceXred = bi.getSourceDatabases().iterator().next();
        Assert.assertEquals("unknown", sourceXred.getIdentifier());
        Assert.assertEquals("unknown", sourceXred.getText());



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

        BinaryInteraction bi = interactionConverter.toBinaryInteraction(interaction);
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