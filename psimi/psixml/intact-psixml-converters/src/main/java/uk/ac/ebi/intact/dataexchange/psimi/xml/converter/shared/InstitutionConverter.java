package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.model.Xref;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Interactor;

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
        super(new Institution(), Institution.class, Source.class);
    }

    public Institution psiToIntact(Source psiObject)
    {
        Institution institution = super.psiToIntact( psiObject );

        if ( !isNewIntactObjectCreated() ) {
            return institution;
        }

        institution = getInstitution();

        IntactConverterUtils.populateNames(psiObject.getNames(), institution);

        if (psiObject.getXref() != null) {
            IntactConverterUtils.populateXref(psiObject.getXref(), institution, new XrefConverter<InstitutionXref>(getInstitution(), InstitutionXref.class));
        }

        if (psiObject.getBibref() != null) {
            IntactConverterUtils.populateXref(psiObject.getBibref().getXref(), institution, new XrefConverter<InstitutionXref>(getInstitution(), InstitutionXref.class));
        }
        IntactConverterUtils.populateAnnotations(psiObject, institution, getInstitution());
  
        for (Attribute attribute : psiObject.getAttributes()) {
            String attributeName = attribute.getName();

            if (attributeName.equals("postalAddress")) {
                institution.setPostalAddress(attribute.getValue());
            } else if (attributeName.equals("url")) {
                institution.setUrl(attribute.getValue());
            }
        }

        return institution;
    }

    public Source intactToPsi(Institution intactObject)
    {
        Source source = super.intactToPsi( intactObject );

        if ( !isNewPsiObjectCreated() ) {
            return source;
        }

        source = new Source();
        PsiConverterUtils.populate(intactObject, source);

        source.setReleaseDate(new Date());

        return source;
    }

    protected String psiElementKey(Source psiObject) {
        return String.valueOf("source:"+psiObject.getNames().getShortLabel());
    }
}
