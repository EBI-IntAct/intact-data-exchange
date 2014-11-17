package uk.ac.ebi.intact.dataexchange.dbimporter.reader;

import psidev.psi.mi.jami.commons.MIDataSourceOptionFactory;
import psidev.psi.mi.jami.factory.options.MIFileDataSourceOptions;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.InteractionCategory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Spring reader for files having biological complexes
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public class ComplexFileReader extends AbstractMIFileReader<Complex>{

    @Override
    protected Map<String, Object> createDatasourceOptions(InputStream inputStreamToAnalyse, MIDataSourceOptionFactory optionFactory) throws IOException {
        Map<String, Object> options = super.createDatasourceOptions(inputStreamToAnalyse, optionFactory);
        options.put(MIFileDataSourceOptions.INTERACTION_CATEGORY_OPTION_KEY, InteractionCategory.complex);
        return options;
    }
}
