package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.Attribute;
import psidev.psi.mi.xml.model.Source;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.model.Institution;

import java.util.Date;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InstitutionConverter extends AbstractAnnotatedObjectConverter<Institution, Source>
{
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

        if (psiObject.getXref() != null) {
            IntactConverterUtils.populateXref(psiObject.getXref(), institution, xrefConverter);
        }

        if (psiObject.getBibref() != null) {
            IntactConverterUtils.populateXref(psiObject.getBibref().getXref(), institution, xrefConverter);
        }
        IntactConverterUtils.populateAnnotations(psiObject, institution, institution, annotationConverter);
  
        for (Attribute attribute : psiObject.getAttributes()) {
            String attributeName = attribute.getName();

            if (attributeName.equals("postalAddress")) {
                institution.setPostalAddress(attribute.getValue());
            } else if (attributeName.equals("url")) {
                institution.setUrl(attribute.getValue());
            }
        }

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
