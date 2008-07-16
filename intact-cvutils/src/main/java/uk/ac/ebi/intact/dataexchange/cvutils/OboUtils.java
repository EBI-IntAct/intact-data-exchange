package uk.ac.ebi.intact.dataexchange.cvutils;

import org.obo.dataadapter.DefaultOBOParser;
import org.obo.dataadapter.OBOParseEngine;
import org.obo.dataadapter.OBOParseException;
import org.obo.datamodel.OBOSession;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDatasetFactory;
import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;


/**
 * Set of methods to deal with OBO files 
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OboUtils {

    private static final String PSI_MI_OBO_LOCATION_OLD = "http://intact.svn.sourceforge.net/viewvc/*checkout*/intact/repo/utils/data/controlledVocab/psi-mi25-4intact.obo";
    private static final String PSI_MI_LOCAL_ANNOTATIONS = "http://intact.svn.sourceforge.net/viewvc/*checkout*/intact/repo/utils/data/controlledVocab/CvObject-annotation-update.txt";

    //file location for OBO file pointing directly to psi cvs
    public static final String PSI_MI_OBO_LOCATION = "http://psidev.cvs.sourceforge.net/*checkout*/psidev/psi/mi/rel25/data/psi-mi25.obo";

    private OboUtils() {}

    public static OBOSession createOBOSession(URL ... paths) throws IOException, OBOParseException {
        String[] strPaths = new String[paths.length];

        for (int i=0; i<strPaths.length; i++) {
            strPaths[i] = paths[i].toString();
        }

        return createOBOSession(strPaths);
    }

    public static OBOSession createOBOSession(String ... paths) throws IOException, OBOParseException {
        DefaultOBOParser parser = new DefaultOBOParser();
        OBOParseEngine engine = new OBOParseEngine(parser);
        //OBOParseEngine can parse several files at once
	    //and create one munged-together ontology,
	    //so we need to provide a Collection to the setPaths() method
        engine.setPaths(Arrays.asList(paths));
        engine.parse();
        OBOSession session = parser.getSession();
        return session;
    }


  

    public static OBOSession createOBOSessionFromLatestMi() throws IOException, OBOParseException {
      return createOBOSessionFromDefault("HEAD");

    }

    public static AnnotationInfoDataset createAnnotationInfoDatasetFromLatestResource() throws IOException {
        URL url = new URL(PSI_MI_LOCAL_ANNOTATIONS);
        return createAnnotationInfoDatasetFromResource(url.openStream());
    }

    public static OBOSession createOBOSessionFromDefault(String revision) throws IOException,  OBOParseException {
        URL url = new URL(PSI_MI_OBO_LOCATION +"?revision="+revision);
        return createOBOSession(url);
    }

    public static AnnotationInfoDataset createAnnotationInfoDatasetFromDefault(int revision) throws IOException, PsiLoaderException {
        URL url = new URL(PSI_MI_LOCAL_ANNOTATIONS+"?revision="+revision);
        return createAnnotationInfoDatasetFromResource(url.openStream());
    }


    public static AnnotationInfoDataset createAnnotationInfoDatasetFromResource(InputStream is) throws IOException{
        return AnnotationInfoDatasetFactory.buildFromCsv( is );
    }

     public static AnnotationInfoDataset createAnnotationInfoDatasetFromResource(InputStream is, char separator) throws IOException{
        return AnnotationInfoDatasetFactory.buildFromCsv( is, separator, '\b' );
    }

    public static AnnotationInfoDataset createAnnotationInfoDatasetFromResource(InputStream is, char separator, char delimiter) throws IOException{
        return AnnotationInfoDatasetFactory.buildFromCsv( is, separator, delimiter );
    }


    @Deprecated
    public static IntactOntology createOntologyFromOboLatestPsiMi() throws IOException, PsiLoaderException {
        URL url = new URL(PSI_MI_OBO_LOCATION_OLD);
        return createOntologyFromObo(url);
    }

    @Deprecated
    public static IntactOntology createOntologyFromOboDefault(int revision) throws IOException, PsiLoaderException {
        URL url = new URL(PSI_MI_OBO_LOCATION_OLD +"?revision="+revision);
        return createOntologyFromObo(url);
    }

    @Deprecated
    public static IntactOntology createOntologyFromObo(URL url) throws IOException, PsiLoaderException {
        PSILoader psi = new PSILoader();
        IntactOntology ontology = psi.parseOboFile(url);

        return ontology;
    }

    @Deprecated
     public static IntactOntology createOntologyFromObo(File oboFile) throws IOException, PsiLoaderException {
        PSILoader psi = new PSILoader();
        IntactOntology ontology = psi.parseOboFile(oboFile);

        return ontology;
    }

}
