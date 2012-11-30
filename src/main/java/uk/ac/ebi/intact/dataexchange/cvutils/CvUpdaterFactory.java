package uk.ac.ebi.intact.dataexchange.cvutils;

import org.springframework.stereotype.Component;
import uk.ac.ebi.intact.core.context.IntactContext;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
public class CvUpdaterFactory {

    private IntactContext intactContext;

    private CvUpdaterFactory() {
    }

    public CvUpdater createInstance(IntactContext intactContext) {
        final CvUpdater updater = new CvUpdater(intactContext);
        //intactContext.getSpringContext().getBeanFactory().configureBean(
        return updater;
    }
}
