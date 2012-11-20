package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.HasId;
import psidev.psi.mi.xml.model.Interaction;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.location.InteractionLocationItem;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.location.LocationItem;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.Collection;

/**
 * Abstract Intact Psi Converter.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractIntactPsiConverter<I, P> implements IntactPsiConverter<I, P> {

    private static final Log log = LogFactory.getLog( AbstractIntactPsiConverter.class );

    private Institution institution;
    private String institutionPrimaryId;

    private boolean checkInitializedCollections = true;

    public AbstractIntactPsiConverter(Institution institution) {
        this.institution = institution;

        if (institution != null){
            try {
                this.institutionPrimaryId = calculateInstitutionPrimaryId(institution);
            } catch (Throwable e) {
                log.error("Problem calculating primaryId for institution: "+institution.getShortLabel());
            }
        }
    }

    public AbstractIntactPsiConverter(Institution institution, boolean checkInitializedCollections) {
        this.institution = institution;
        this.checkInitializedCollections = checkInitializedCollections;

        if (institution != null){
            try {
                this.institutionPrimaryId = calculateInstitutionPrimaryId(institution);
            } catch (Throwable e) {
                log.error("Problem calculating primaryId for institution: "+institution.getShortLabel());
            }
        }
    }

    public String calculateInstitutionPrimaryId(Institution institution) {
        String institutionPrimaryId = null;
        if (institution != null) {
            InstitutionXref xref = XrefUtils.getPsiMiIdentityXref(institution);

            if (xref != null) {
                institutionPrimaryId = xref.getPrimaryId();
            }
        }
        return institutionPrimaryId;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution)
    {
        this.institution = institution;
        if (institution != null){
            try {
                this.institutionPrimaryId = calculateInstitutionPrimaryId(institution);
            } catch (Throwable e) {
                log.error("Problem calculating primaryId for institution: "+institution.getShortLabel());
            }
        }
    }

    protected void failIfInconsistentConversion(I intactEntry, Entry P) {
        throw new UnsupportedOperationException();
    }

    protected void failIfInconsistentCollectionSize(String type, Collection intactCol, Collection psiCol) {
        if (intactCol.size() > 0 && psiCol.size() > 0 && intactCol.size() != psiCol.size()) {
            throw new InconsistentConversionException(type, intactCol.size(), psiCol.size());
        }
    }

    protected void psiStartConversion(Object psiObject) {
        String id = null;
        if (psiObject instanceof HasId) {
            id = String.valueOf(((HasId)psiObject).getId());
        }

        if (psiObject instanceof Interaction) {
            final Interaction interaction = (Interaction) psiObject;
            if (interaction.getImexId() != null) {
                ConverterContext.getInstance().getLocation().newChild(new InteractionLocationItem(id, psiObject.getClass(), interaction.getImexId()));
            } else {
                ConverterContext.getInstance().getLocation().newChild(new LocationItem(id, psiObject.getClass()));
            }
        } else if (id != null) {
            ConverterContext.getInstance().getLocation().newChild(new LocationItem(id, psiObject.getClass()));
        }
    }

    protected void psiEndConversion(Object psiObject) {
        ConverterContext.getInstance().getLocation().resetPosition();
    }

    protected void intactStartConversation(Object intactObject) {
        String id = null;
        if (intactObject instanceof IntactObject) {
            final String ac = ((IntactObject) intactObject).getAc();
            if (ac != null) {
                id = ac;
            }
        }

        ConverterContext.getInstance().getLocation().newChild(new LocationItem(id, intactObject.getClass()));
    }

    protected void intactEndConversion(Object intactObject) {
        ConverterContext.getInstance().getLocation().resetPosition();
    }

    protected void addMessageToContext(MessageLevel level, String message, boolean autoFixed) {
        final ConverterMessage converterMessage = new ConverterMessage(level, message, ConverterContext.getInstance().getLocation().getCurrentLocation());
        converterMessage.setAutoFixed(autoFixed);
        ConverterContext.getInstance().getReport().getMessages().add(converterMessage);
    }

    public String getInstitutionPrimaryId() {
        return institutionPrimaryId;
    }

    public void setInstitutionPrimaryId(String institutionPrimaryId) {
        this.institutionPrimaryId = institutionPrimaryId;
    }

    public void setInstitution(Institution institution, String institutionPrimaryId) {
        this.institution = institution;
        this.institutionPrimaryId = institutionPrimaryId;
    }

    public boolean isCheckInitializedCollections() {
        return checkInitializedCollections;
    }

    public void setCheckInitializedCollections(boolean checkInitializedCollections) {
        this.checkInitializedCollections = checkInitializedCollections;
    }
}
