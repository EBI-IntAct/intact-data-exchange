package uk.ac.ebi.intact.util.uniprotExport.filters.config;

/**
 * A thread local access to related information about uniprot export filters.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public class FilterContext {

    private FilterConfig config;

    private static ThreadLocal<FilterContext> instance = new ThreadLocal<FilterContext>() {
        @Override
        protected FilterContext initialValue() {
            return new FilterContext();
        }
    };

    public static FilterContext getInstance() {
        return instance.get();
    }

    private FilterContext() {
        // initialize here default configuration
        this.config = new FilterConfig();
    }

    public FilterConfig getConfig() {
        return config;
    }

    public void setConfig( FilterConfig config ) {
        this.config = config;
    }
}
