package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.model.Xref;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;

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
        Institution institution = getInstitution();

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
        Source source = new Source();
        PsiConverterUtils.populate(intactObject, source);

        source.setReleaseDate(new Date());
       /*
        String label = intactObject.getShortLabel();

        Names names = new Names();
        source.setNames(names);

        names.setShortLabel(label);
        names.setFullName(intactObject.getFullName());

        Xref xref = null;
        Bibref bibref = null;

        if (label.equalsIgnoreCase("ebi")) {
            xref = createXref(CvDatabase.INTACT_MI_REF);
            bibref = createBibref();
        } else if (label.equalsIgnoreCase("MINT")) {
            xref = createXref(CvDatabase.MINT_MI_REF);
            bibref = createBibref();
        } else if (label.equalsIgnoreCase("DIP")) {
            xref = createXref(CvDatabase.DIP_MI_REF);
        }

        if (xref != null) {
            source.setXref(xref);
        }
        if (bibref != null) {
            source.setBibref(bibref);
        }
           */
        return source;
    }

    protected String psiElementKey(Source psiObject) {
        return String.valueOf("source:"+psiObject.getNames().getShortLabel());
    }

    private Xref createXref(String dbMiRef) {
        DbReference dbReference = new DbReference(dbMiRef, CvDatabase.PSI_MI);
        dbReference.setDbAc(CvDatabase.PSI_MI_MI_REF);
        dbReference.setRefType(CvXrefQualifier.PRIMARY_REFERENCE);
        dbReference.setRefTypeAc(CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);

        return new Xref(dbReference);
    }

    private Bibref createBibref() {
        DbReference dbReference = new DbReference("14681455", CvDatabase.PUBMED);
        dbReference.setDbAc(CvDatabase.PUBMED_MI_REF);
        dbReference.setRefType(CvXrefQualifier.PRIMARY_REFERENCE);
        dbReference.setRefTypeAc(CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);

        Xref xref = new Xref(dbReference);

        return new Bibref(xref);
     }
}
