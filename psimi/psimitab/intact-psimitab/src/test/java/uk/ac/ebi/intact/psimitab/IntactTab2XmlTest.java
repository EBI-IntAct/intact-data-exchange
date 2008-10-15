package uk.ac.ebi.intact.psimitab;

import org.junit.*;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.PsimiXmlWriterException;

import java.util.Collection;

/**
 * IntactTab2Xml Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @since 2.0.2
 * @version $Id$
 */
public class IntactTab2XmlTest {

    @Test
    public void convert() throws Exception {
        IntactPsimiTabReader reader = new IntactPsimiTabReader( true );
        final Collection<BinaryInteraction> mitabInteractions =
                reader.read( IntactTab2XmlTest.class.getResourceAsStream( "/mitab_samples/sample_dgi.tsv" ) );

        IntactTab2Xml tab2xml = new IntactTab2Xml();
        final EntrySet entrySet = tab2xml.convert( mitabInteractions );

        Assert.assertNotNull( entrySet );
        Assert.assertEquals(1, entrySet.getEntries().size());
        final Entry entry = entrySet.getEntries().iterator().next();

        final Collection<Interaction> interactions = entry.getInteractions();
        Assert.assertNotNull( interactions );
        Assert.assertEquals( 2, interactions.size() );

        for ( Interaction interaction : interactions ) {
            for ( Participant participant : interaction.getParticipants() ) {
                final Interactor interactor = participant.getInteractor();
                Assert.assertFalse( interactor.getId() == 0 );

                if( "CHEBI:45906".equals( interactor.getNames().getShortLabel() ) ) {
                    Assert.assertEquals( 15, interactor.getAttributes().size() );
                }
            }
        }
    }

    @Test
    public void convert_imatinib() throws Exception {
        IntactPsimiTabReader reader = new IntactPsimiTabReader( true );
        final Collection<BinaryInteraction> mitabInteractions =
                reader.read( IntactTab2XmlTest.class.getResourceAsStream( "/mitab_samples/imatinib_full.txt" ) );

        IntactTab2Xml tab2xml = new IntactTab2Xml();
        final EntrySet entrySet = tab2xml.convert( mitabInteractions );

        Assert.assertNotNull( entrySet );
        Assert.assertEquals(1, entrySet.getEntries().size());
        final Entry entry = entrySet.getEntries().iterator().next();
        Assert.assertNotNull( entry );

        // check on the count of interactor's xrefs
        Assert.assertEquals( 11, entry.getInteractions().size() );
        for ( Interaction interaction : entry.getInteractions() ) {
            Assert.assertEquals( 2, interaction.getParticipants().size() );

            Interactor sm = getInteractorByType( interaction, "MI:0328" );
            Assert.assertEquals( 3, sm.getXref().getAllDbReferences().size() );

            // all proteins have a uniprot and an intact AC.
            Interactor p = getInteractorByType( interaction, "MI:0326" );
            Assert.assertEquals( 2, p.getXref().getAllDbReferences().size() );
        }
    }

    private Interactor getInteractorByType( Interaction interaction, String typeMi ) {
        for ( Participant participant : interaction.getParticipants() ) {
            final Interactor interactor = participant.getInteractor();
            if( interactor.getInteractorType().getXref().getPrimaryRef().getId().equals( typeMi ) ) {
                return interactor;
            }
        }
        return null;
    }

    private void printXml( EntrySet entrySet ) throws PsimiXmlWriterException {
        PsimiXmlWriter w = new PsimiXmlWriter();
        final String xml = w.getAsString( entrySet );
        System.out.println( xml );
    }
}
