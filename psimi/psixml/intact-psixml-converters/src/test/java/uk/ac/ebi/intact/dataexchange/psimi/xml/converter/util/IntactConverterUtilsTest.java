package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util;

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.xml.model.Feature;
import psidev.psi.mi.xml.model.Names;
import psidev.psi.mi.xml.model.Participant;
import psidev.psi.mi.xml.model.Range;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.PsiMockFactory;
import uk.ac.ebi.intact.model.*;

/**
 * IntactConverterUtils Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @since 2.0.0
 * @version $Id$
 */
public class IntactConverterUtilsTest extends IntactBasicTestCase {

    @Test
    public void populateNames() throws Exception {

        final CvTopic topic = getMockBuilder().createCvObject( CvTopic.class, "MI:xxxx", "foo" );

        Names names = new Names();
        names.setShortLabel( "SHORTLABEL" );
        names.setFullName( "FULLNAME" );

        IntactConverterUtils.populateNames( names, topic );

        Assert.assertEquals( "shortlabel", topic.getShortLabel() );
        Assert.assertEquals( "FULLNAME", topic.getFullName() );

    }

    @Test
    public void populateNames_institution() throws Exception {

        final Institution institution = getMockBuilder().createInstitution( "MI:zzzz", "institution" );

        Names names = new Names();
        names.setShortLabel( "EBI" );
        names.setFullName( "European Bioinformatics Institute" );

        IntactConverterUtils.populateNames( names, institution );

        Assert.assertEquals( "EBI", institution.getShortLabel() );
        Assert.assertEquals( "European Bioinformatics Institute", institution.getFullName() );
    }

    @Test
    public void convert_featureRanges_sequence(){
        Institution institution = getMockBuilder().createInstitution( "MI:zzzz", "institution" );
        Interaction interaction = getMockBuilder().createDeterministicInteraction();
        Participant p = PsiMockFactory.createMockParticipant(new psidev.psi.mi.xml.model.Interaction());
        Feature f = PsiMockFactory.createFeature();
        Range r = PsiMockFactory.createRange(); // position 1-5

        String s = "KTTPPSVYPLAPGCGDTTGSSVTLGCLVKGYFPESVTVTWNSGSLSSSVHTFPALLQSGL" +
                "YTMSSSVTVPSSTWPSQTVTCSVAHPASSTTVDKKLEPSGPISTINPCPPCKECHKCPAP" +
                "NLEGGPSVFIFPPNIKDVLMISLTPKVTCVVVDVSEDDPDVQISWFVNNVEVHTAQTQTH" +
                "REDYNSTIRVVSTLPIQHQDWMSGKEFKCKVNNKDLPSPIERTISKIKGLVRAPQVYILP" +
                "PPAEQLSRKDVSLTCLVVGFNPGDISVEWTSNGHTEENYKDTAPVLDSDGSYFIYSKLNM" +
                "KTSKWEKTDSFSCNVRHEGLKNYYLKKTISRSPGLDLDDICAEAKDGELDGLWTTITIFI" +
                "SLFLLSVCYSASVTLFKVKWIFSSVVELKQKISPDYRNMIGQGA";

        f.getRanges().add(r);
        p.getFeatures().add(f);
        p.getInteractor().setSequence(s);

        Component component = IntactConverterUtils.newComponent(institution, p, interaction);

        Assert.assertEquals(s, ((Polymer)component.getInteractor()).getSequence());

        for (uk.ac.ebi.intact.model.Feature feature : component.getBindingDomains()){
            for (uk.ac.ebi.intact.model.Range ra : feature.getRanges()){
                Assert.assertNotNull(ra.getSequence());
                Assert.assertNotNull(ra.getFullSequence()); 
            }
        }
        Assert.assertNotNull(component.getBindingDomains());
    }
}
