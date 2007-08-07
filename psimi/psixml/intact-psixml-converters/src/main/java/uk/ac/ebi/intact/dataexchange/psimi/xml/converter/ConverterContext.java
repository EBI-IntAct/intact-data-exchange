package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Makes the configuration available to the current thread (through ThreadLocal).
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 2.0.0
 */
public class ConverterContext {

    public static final Log log = LogFactory.getLog(ConverterContext.class);

    private InteractorConverterConfig configInteractor;

    private static ThreadLocal<ConverterContext> instance = new ThreadLocal<ConverterContext>() {
        @Override
        protected ConverterContext initialValue() {
            return new ConverterContext();
        }
    };

    public static ConverterContext getInstance() {
        return instance.get();
    }

    private ConverterContext() {
        this.configInteractor = new InteractorConverterConfig();
    }

    public InteractorConverterConfig getInteractorConfig() {
        return configInteractor;
    }

    public void setConfig( InteractorConverterConfig configInteractor ) {
        this.configInteractor = configInteractor;
    }
}
