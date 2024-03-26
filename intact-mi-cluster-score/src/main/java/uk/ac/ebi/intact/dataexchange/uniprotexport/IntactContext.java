package uk.ac.ebi.intact.dataexchange.uniprotexport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class IntactContext {

    private static final Log log = LogFactory.getLog(IntactContext.class);

    /**
     * Initializes a standalone context.
     */
    public static void initContext(String[] configurationResourcePaths) {
        initContext(configurationResourcePaths, null);
    }

    /**
     * Initializes a standalone context.
     */
    public static void initContext(String[] configurationResourcePaths, ApplicationContext parent) {
        // check for overflow initialization
        for (int i = 5; i < Thread.currentThread().getStackTrace().length; i++) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[i];

            if (stackTraceElement.getClassName().equals(IntactContext.class.getName())
                    && stackTraceElement.getMethodName().equals("initContext")) {
                throw new IntactInitializationError("Infinite recursive invocation to IntactContext.initContext(). This" +
                        " may be due to an illegal invocation of IntactContext.getCurrentInstance() during bean instantiation.");
            }
        }

        // the order of the resources matters when overriding beans, so we add the intact first,
        // so the user can override the default beans.
        List<String> resourcesList = new LinkedList<String>();
        resourcesList.add("classpath*:/META-INF/intact.spring.xml");
        resourcesList.addAll(Arrays.asList(configurationResourcePaths));

        configurationResourcePaths = resourcesList.toArray(new String[resourcesList.size()]);

        if (log.isDebugEnabled()) {
            log.debug("Loading Spring XML config:");
            for (String configurationResourcePath : configurationResourcePaths) {
                log.debug(" - " + configurationResourcePath);
            }
        }

        // init Spring
        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext(configurationResourcePaths, parent);
        springContext.registerShutdownHook();
    }
}
