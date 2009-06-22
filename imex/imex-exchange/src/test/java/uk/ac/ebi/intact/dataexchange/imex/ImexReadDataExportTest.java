package uk.ac.ebi.intact.dataexchange.imex;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Experiment;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.PsimiXmlWriter;

/**
 * Sample test that allows a database provider to test its IMEx export output.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
@ContextConfiguration(locations = { "classpath*:/META-INF/intact.spring.xml",
                                    "classpath*:/META-INF/jpa.spring.xml"
                                    }, inheritLocations = false )
public class ImexReadDataExportTest extends IntactBasicTestCase {

    @Autowired
    private ImexExporter exporter;

    @Test
    public void export() throws Exception {

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        final int count = daoFactory.getInteractionDao().countAll();
        System.out.println( count );

        final Experiment experiment = daoFactory.getExperimentDao().getByShortLabel( "ahnesorg-2006-1" );
        if( experiment == null ) {
            throw new IllegalStateException( "Could not find experiment" );
        }

        if( experiment.getPublication() == null ) {
            throw new IllegalStateException( "You experiment doens't have a publication :|" );
        }

        final EntrySet entrySet = exporter.exportPublication( experiment.getPublication() );

        System.out.println( new PsimiXmlWriter().getAsString( entrySet ) );
    }
}
