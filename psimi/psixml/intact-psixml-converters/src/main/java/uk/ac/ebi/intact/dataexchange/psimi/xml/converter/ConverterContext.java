package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.InteractionConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.InteractorConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.AnnotationConverterConfig;

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
    
    private AnnotationConverterConfig configAnnotation;

    private InteractionConverterConfig configInteraction;

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
        this.configAnnotation = new AnnotationConverterConfig();
        this.configInteraction = new InteractionConverterConfig();
    }

    public InteractorConverterConfig getInteractorConfig() {
        return configInteractor;
    }


    public AnnotationConverterConfig getAnnotationConfig() {
        return configAnnotation;
    }

    public void setInteractorConfig( InteractorConverterConfig configInteractor ) {
        this.configInteractor = configInteractor;
    }
    
    public void setAnnotationConfig( AnnotationConverterConfig configAnnotation ) {
        this.configAnnotation = configAnnotation;
    }

    public InteractionConverterConfig getInteractionConfig() {
        return configInteraction;
    }

    @Deprecated
    public void setConfig( InteractorConverterConfig configInteractor ) {
        this.configInteractor = configInteractor;
    }
}
