package uk.ac.ebi.intact.dataexchange.cvutils;

import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Set of methods to deal with OBO files 
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OboUtils {

    private static final String PSI_MI_OBO_LOCATION = "http://intact.svn.sourceforge.net/viewvc/*checkout*/intact/repo/utils/data/controlledVocab/psi-mi25-4intact.obo";

    private OboUtils() {}

    public static IntactOntology createOntologyFromOboLatestPsiMi() throws IOException, PsiLoaderException {
        URL url = new URL(PSI_MI_OBO_LOCATION);
        return createOntologyFromObo(url);
    }

    public static IntactOntology createOntologyFromOboDefault(int revision) throws IOException, PsiLoaderException {
        URL url = new URL(PSI_MI_OBO_LOCATION+"?revision="+revision);
        return createOntologyFromObo(url);
    }

    public static IntactOntology createOntologyFromObo(URL url) throws IOException, PsiLoaderException {
        PSILoader psi = new PSILoader();
        IntactOntology ontology = psi.parseOboFile(url);

        return ontology;
    }

     public static IntactOntology createOntologyFromObo(File oboFile) throws IOException, PsiLoaderException {
        PSILoader psi = new PSILoader();
        IntactOntology ontology = psi.parseOboFile(oboFile);

        return ontology;
    }

}
