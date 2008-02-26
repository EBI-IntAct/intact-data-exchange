package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util;

import org.junit.*;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Institution;
import psidev.psi.mi.xml.model.Names;

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
}
