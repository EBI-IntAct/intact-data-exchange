package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Institution;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InstitutionConverter extends AbstractAnnotatedObjectConverter<Institution, Source>
{
    private static final Log log = LogFactory.getLog(InstitutionConverter.class);

    public InstitutionConverter()
    {
        super(new Institution("Unknown"), Institution.class, Source.class);
    }

    public Institution psiToIntact(Source psiObject)
    {
        Institution institution = super.psiToIntact( psiObject );
        setInstitution(institution);

        if ( !isNewIntactObjectCreated() ) {
            return institution;
        }

        psiStartConversion(psiObject);

        IntactConverterUtils.populateNames(psiObject.getNames(), institution, aliasConverter);
        IntactConverterUtils.populateXref(psiObject.getXref(), institution, xrefConverter);

        if (psiObject.getBibref() != null) {
            IntactConverterUtils.populateXref(psiObject.getBibref().getXref(), institution, xrefConverter);
        }
        IntactConverterUtils.populateAnnotations(psiObject, institution, institution, annotationConverter);

        setInstitution(institution);

        psiEndConversion(psiObject);

        return institution;
    }

    public Source intactToPsi(Institution intactObject)
    {
        Source source = super.intactToPsi( intactObject );

        if ( !isNewPsiObjectCreated() ) {
            return source;
        }

        intactStartConversation(intactObject);

        // Set id, annotations, xrefs and aliases
        PsiConverterUtils.populateNames(intactObject, source, aliasConverter);
        PsiConverterUtils.populateXref(intactObject, source, xrefConverter);
        PsiConverterUtils.populateAttributes(intactObject, source, annotationConverter);

        Bibref bibref = new Bibref();
        // we extract the primary ref if possible
        if (!intactObject.getXrefs().isEmpty()){
            Collection<uk.ac.ebi.intact.model.Xref> primaryRefs = searchXrefs(intactObject, CvDatabase.PUBMED_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, isCheckInitializedCollections());

            if (primaryRefs.isEmpty()){
                primaryRefs = searchXrefs(intactObject, CvDatabase.DOI_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, isCheckInitializedCollections());
            }

            if (!primaryRefs.isEmpty()){
                Iterator<uk.ac.ebi.intact.model.Xref> iterator = primaryRefs.iterator();
                uk.ac.ebi.intact.model.Xref primaryRef = iterator.next();

                Xref xref = new Xref();
                xref.setPrimaryRef(new DbReference(primaryRef.getCvDatabase().getShortLabel(), primaryRef.getCvDatabase().getIdentifier(), primaryRef.getPrimaryId(), primaryRef.getCvXrefQualifier().getShortLabel(), primaryRef.getCvXrefQualifier().getIdentifier()));
                bibref.setXref(xref);

                while (iterator.hasNext()){
                    primaryRef = iterator.next();
                    xref.getSecondaryRef().add(new DbReference(primaryRef.getCvDatabase().getShortLabel(), primaryRef.getCvDatabase().getIdentifier(), primaryRef.getPrimaryId(), primaryRef.getCvXrefQualifier().getShortLabel(), primaryRef.getCvXrefQualifier().getIdentifier()));
                }
            }
        }

        if (bibref.getXref() != null && bibref.getXref().getPrimaryRef() != null){
           source.setBibref(bibref);
        }
        else {
            log.error("Institution without primary ref " + intactObject.getShortLabel());
        }

        source.setReleaseDate(new Date());

        intactEndConversion(intactObject);
        return source;
    }

    protected Object psiElementKey(Source psiObject) {
        String key;

        if (psiObject.getNames() != null) {
            key = "source:"+psiObject.getNames().getShortLabel();
        } else if (psiObject.getXref() != null) {
            key = "source:xref:"+psiObject.getXref().getPrimaryRef().getId();
        } else if (psiObject.getBibref() != null) {
            key = "source:bibref:"+psiObject.getBibref().getXref().getPrimaryRef().getId();
        } else {
            throw new PsiConversionException("Could not create key to cache the source: "+psiObject);
        }

        return key;

    }
}
