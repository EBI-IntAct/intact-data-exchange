package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.Attribute;
import psidev.psi.mi.xml.model.Source;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.InstitutionXref;

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

        IntactConverterUtils.populateNames(psiObject.getNames(), institution);

        if (psiObject.getXref() != null) {
            IntactConverterUtils.populateXref(psiObject.getXref(), institution, new XrefConverter<InstitutionXref>(institution, InstitutionXref.class));
        }

        if (psiObject.getBibref() != null) {
            IntactConverterUtils.populateXref(psiObject.getBibref().getXref(), institution, new XrefConverter<InstitutionXref>(getInstitution(), InstitutionXref.class));
        }
        IntactConverterUtils.populateAnnotations(psiObject, institution, institution);
  
        for (Attribute attribute : psiObject.getAttributes()) {
            String attributeName = attribute.getName();

            if (attributeName.equals("postalAddress")) {
                institution.setPostalAddress(attribute.getValue());
            } else if (attributeName.equals("url")) {
                institution.setUrl(attribute.getValue());
            }
        }

        setInstitution(institution);

        return institution;
    }

    public Source intactToPsi(Institution intactObject)
    {
        Source source = super.intactToPsi( intactObject );

        if ( !isNewPsiObjectCreated() ) {
            return source;
        }

        source = new Source();
        PsiConverterUtils.populate(intactObject, source, this);

        source.setReleaseDate(new Date());

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
