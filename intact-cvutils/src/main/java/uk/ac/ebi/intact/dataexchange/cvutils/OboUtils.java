package uk.ac.ebi.intact.dataexchange.cvutils;

import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;

import java.io.*;
import java.net.URL;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OboUtils {

    private OboUtils() {}

    public static IntactOntology createOntologyFromOboLatestPsiMi() throws IOException, PsiLoaderException {
        URL url = new URL("http://intact.svn.sourceforge.net/viewvc/*checkout*/intact/repo/utils/data/controlledVocab/psi-mi25-4intact.obo");
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
