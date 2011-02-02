package uk.ac.ebi.intact.util.uniprotExport;

import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/02/11</pre>
 */

public class UniprotExportBase extends IntactBasicTestCase {

    public List<GOParameters> createGOParameters(){

        List<GOParameters> parameters = new ArrayList<GOParameters>(3);

        String uniprotAc1 = "Q9NET8";
        String uniprotAc2 = "Q22534";
        String uniprotAc4 = "Q17862";
        String uniprotAc5 = "P33327";

        String pmid1 = "14704431";
        String pmid2 = "18467557";

        Set<String> publications1 = new HashSet<String>(1);
        publications1.add(pmid1);

        Set<String> publications2 = new HashSet<String>(1);
        publications2.add(pmid2);

        GOParameters parameter1 = new GOParametersImpl(uniprotAc1, uniprotAc2, publications1);
        GOParameters parameter2 = new GOParametersImpl(uniprotAc1, uniprotAc4, publications1);
        GOParameters parameter3 = new GOParametersImpl(uniprotAc5, uniprotAc5, publications2);

        parameters.add(parameter1);
        parameters.add(parameter2);
        parameters.add(parameter3);

        return parameters;
    }

    public List<CCParameters> createCCParameters(){

        List<CCParameters> parameters = new ArrayList<CCParameters>(3);

        String uniprotAc1 = "Q9NET8";
        String uniprotAc2 = "Q22534";
        String uniprotAc4 = "Q17862";
        String uniprotAc5 = "P33327";

        String pmid1 = "14704431";
        String pmid2 = "18467557";

        Set<String> publications1 = new HashSet<String>(1);
        publications1.add(pmid1);

        Set<String> publications2 = new HashSet<String>(1);
        publications2.add(pmid2);

        //CCParameters parameter1 = new GOParametersImpl(uniprotAc1, uniprotAc2, publications1);
        //GOParameters parameter2 = new GOParametersImpl(uniprotAc1, uniprotAc4, publications1);
        //GOParameters parameter3 = new GOParametersImpl(uniprotAc5, uniprotAc5, publications2);

       // parameters.add(parameter1);
        //parameters.add(parameter2);
        //parameters.add(parameter3);

        return parameters;
    }

    public List<DRParameters> createDRParameters(){

        List<DRParameters> parameters = new ArrayList<DRParameters>(3);

        String uniprotAc1 = "Q9NET8";
        String uniprotAc2 = "Q22534";
        String uniprotAc4 = "Q17862";
        String uniprotAc5 = "P33327";

        DRParameters parameter1 = new DRParametersImpl(uniprotAc1, 2);
        DRParameters parameter2 = new DRParametersImpl(uniprotAc2, 1);
        DRParameters parameter3 = new DRParametersImpl(uniprotAc4, 1);
        DRParameters parameter4 = new DRParametersImpl(uniprotAc5, 1);

        parameters.add(parameter1);
        parameters.add(parameter2);
        parameters.add(parameter3);
        parameters.add(parameter4);

        return parameters;
    }

    public boolean areFilesEqual (File file1, File file2) throws IOException {

        BufferedReader reader1 = new BufferedReader(new FileReader(file1));
        BufferedReader reader2 = new BufferedReader(new FileReader(file2));

        String line1 = reader1.readLine();
        String line2 = reader2.readLine();

        boolean isEqual = true;

        while (line1 != null && line2 != null && isEqual){
            isEqual = line1.equals(line2);
            line1 = reader1.readLine();
            line2 = reader2.readLine();
        }

        return isEqual;
    }
}
