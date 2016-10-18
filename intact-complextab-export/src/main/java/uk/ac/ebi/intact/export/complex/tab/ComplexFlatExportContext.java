package uk.ac.ebi.intact.export.complex.flat;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ComplexFlatExportContext {
    private static ComplexFlatExportContext context = new ComplexFlatExportContext();
    private ComplexFlatExportConfig config;

    private ComplexFlatExportContext() {
        // initialize here default configuration
        ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/complex-flat-export-config.xml");
        this.config = context.getBean(ComplexFlatExportConfig.class);
    }

    public static ComplexFlatExportContext getInstance() {
        return context;
    }

    public ComplexFlatExportConfig getConfig() {
        return config;
    }

    public void setConfig(ComplexFlatExportConfig config) {
        this.config = config;
    }
}
