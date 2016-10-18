package uk.ac.ebi.intact.export.mutation;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class MutationExportContext {
    private static MutationExportContext context = new MutationExportContext();
    private MutationExportConfig config;

    private MutationExportContext() {
        // initialize here default configuration
        ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/feature-export-spring.xml", "META-INF/shortlabel-generator-config.xml");
        this.config = context.getBean(MutationExportConfig.class);
    }

    public static MutationExportContext getInstance() {
        return context;
    }

    public MutationExportConfig getConfig() {
        return config;
    }

    public void setConfig(MutationExportConfig config) {
        this.config = config;
    }
}
