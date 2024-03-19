package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.AnnotationConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.InteractionConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.InteractorConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.location.DisabledLocationTree;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.location.LocationTree;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.InstitutionXref;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Makes the configuration available to the current thread (through ThreadLocal).
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 2.0.0
 */
public class ConverterContext {

    public static final Log log = LogFactory.getLog(ConverterContext.class);

    private boolean generateExpandedXml;

    private InteractorConverterConfig configInteractor;

    private AnnotationConverterConfig configAnnotation;

    private InteractionConverterConfig configInteraction;

    private ConverterReport report;

    private LocationTree location;

    private Institution defaultInstitutionForAcs;
    private String defaultInstitutionPrimaryIdForAcs;

    private boolean locationInfoDisabled;

    private boolean autoFixInteractionSourceReference;

    private Set<String> dnaTypeMis;
    private Set<String> rnaTypeMis;

    private Set<String> dnaTypeLabels;
    private Set<String> rnaTypeLabels;

    private boolean isCheckingExperimentForPrimaryRefs = false;

    private static ThreadLocal<ConverterContext> instance = new ThreadLocal<ConverterContext>() {
        @Override
        protected ConverterContext initialValue() {
            return new ConverterContext();
        }
    };

    public static ConverterContext getInstance() {
        return instance.get();
    }

    public static void removeInstance() {
        instance.remove();
    }

    private ConverterContext() {
        this.generateExpandedXml = true;
        this.configInteractor = new InteractorConverterConfig();
        this.configAnnotation = new AnnotationConverterConfig();
        this.configInteraction = new InteractionConverterConfig();
        this.report = new ConverterReport();
        this.autoFixInteractionSourceReference = true;

        resetLocation();

        // preload the CvInteractorTypes
//        CvInteractorType dna = daoFactory.getCvObjectDao( CvInteractorType.class ).getByPsiMiRef( CvInteractorType.DNA_MI_REF );
//        dnaTypeMis = CvObjectUtils.getChildrenMIs( dna );
//        CvInteractorType rna = daoFactory.getCvObjectDao( CvInteractorType.class ).getByPsiMiRef( CvInteractorType.RNA_MI_REF );
//        rnaTypeMis = CvObjectUtils.getChildrenMIs( rna );

        // in order to avoid connection to the database, we list here all the MIs of DNA and RNA related terms
        rnaTypeMis = new HashSet( Arrays.asList( "MI:0320", "MI:0321", "MI:0322", "MI:0323", "MI:0324",
                "MI:0325", "MI:0607", "MI:0608", "MI:0609", "MI:0610",
                "MI:0611", "MI:0679", "MI:0679", "MI:2204" ) );

        rnaTypeLabels = new HashSet( Arrays.asList( "ribonucleic acid", "catalytic rna", "guide rna", "heterogeneous nuclear rna", "messenger rna", "poly adenine", "transfer rna",
                "small nuclear rna", "ribosomal rna", "small nucleolar rna", "small interfering rna", "signal recognition particle rna",
                "poly adenine", "micro rna") );

        dnaTypeMis = new HashSet( Arrays.asList( "MI:0319", "MI:0680", "MI:0681", "MI:0250" ) );

        dnaTypeLabels = new HashSet( Arrays.asList( "deoxyribonucleic acid", "single stranded deoxyribonucleic acid", "double stranded deoxyribonucleic acid", "gene" ) );

    }

    public void configure( ExportProfile profile ) {
        profile.configure( this );
    }

    public boolean isGenerateExpandedXml() {
        return generateExpandedXml;
    }

    public boolean isGenerateCompactXml() {
        return ! generateExpandedXml;
    }

    public void setGenerateExpandedXml( boolean generateExpandedXml ) {
        this.generateExpandedXml = generateExpandedXml;
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
            if (defaultInstitutionForAcs != null) {
                InstitutionXref xref = XrefUtils.getPsiMiIdentityXref(defaultInstitutionForAcs);

                if (xref != null) {
                    defaultInstitutionPrimaryIdForAcs = xref.getPrimaryId();
                }
            }
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

    public Set<String> getDnaTypeLabels() {
        return dnaTypeLabels;
    }

    public Set<String> getRnaTypeLabels() {
        return rnaTypeLabels;
    }


    public Set<String> getDnaTypeMis() {
        return dnaTypeMis;
    }

    public Set<String> getRnaTypeMis() {
        return rnaTypeMis;
    }

    public boolean isAutoFixInteractionSourceReference() {
        return autoFixInteractionSourceReference;
    }

    public void setAutoFixInteractionSourceReference(boolean autoFixInteractionSourceReference) {
        this.autoFixInteractionSourceReference = autoFixInteractionSourceReference;
    }

    public String getDefaultInstitutionPrimaryIdForAcs() {
        return defaultInstitutionPrimaryIdForAcs;
    }

    public void setDefaultInstitutionPrimaryIdForAcs(String defaultInstitutionPrimaryIdForAcs) {
        this.defaultInstitutionPrimaryIdForAcs = defaultInstitutionPrimaryIdForAcs;
    }

    public boolean isCheckingExperimentForPrimaryRefs() {
        return isCheckingExperimentForPrimaryRefs;
    }

    public void setCheckingExperimentForPrimaryRefs(boolean checkingExperimentForPrimaryRefs) {
        isCheckingExperimentForPrimaryRefs = checkingExperimentForPrimaryRefs;
    }
}
