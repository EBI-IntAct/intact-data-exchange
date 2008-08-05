package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.AnnotationConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.InteractionConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.InteractorConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.location.DisabledLocationTree;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.location.LocationTree;
import uk.ac.ebi.intact.model.Institution;

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

    private ConverterReport report;

    private LocationTree location;

    private Institution defaultInstitutionForAcs;

    private boolean locationInfoDisabled;

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
        this.report = new ConverterReport();

        resetLocation();
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

    public void clear() {
        report = new ConverterReport();
        resetLocation();
    }

    public void resetLocation() {
        if (locationInfoDisabled) {
            this.location = new DisabledLocationTree();
        } else {
            this.location = new LocationTree();
        }
    }

    public ConverterReport getReport() {
        return report;
    }

    public LocationTree getLocation() {
        return location;
    }

    public Institution getDefaultInstitutionForAcs() {
        if (defaultInstitutionForAcs == null && IntactContext.currentInstanceExists()) {
            defaultInstitutionForAcs = IntactContext.getCurrentInstance().getInstitution();
        }
        return defaultInstitutionForAcs;
    }

    public void setDefaultInstitutionForAcs(Institution defaultInstitutionForAcs) {
        this.defaultInstitutionForAcs = defaultInstitutionForAcs;
    }

    public boolean isLocationInfoDisabled() {
        return locationInfoDisabled;
    }

    public void setLocationInfoDisabled(boolean locationInfoDisabled) {
        this.locationInfoDisabled = locationInfoDisabled;
        resetLocation();
    }
}
