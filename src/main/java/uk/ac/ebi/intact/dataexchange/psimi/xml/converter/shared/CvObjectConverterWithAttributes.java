package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.Attribute;
import psidev.psi.mi.xml.model.OpenCvType;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.AnnotationConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.Institution;

import java.util.Collection;

/**
 * Extension of CvObjectConverter for cvobjects having attributes
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/10/11</pre>
 */

public class CvObjectConverterWithAttributes<C extends CvObject, T extends OpenCvType> extends CvObjectConverter< C, T> {

    protected AnnotationConverter annotationConverter;

    public CvObjectConverterWithAttributes(Institution institution, Class<C> intactCvClass, Class<T> psiCvClass) {
        super(institution, intactCvClass, psiCvClass);
        this.annotationConverter = new AnnotationConverter(institution);
    }

    public C psiToIntact(T psiObject) {
        psiStartConversion(psiObject);

        C cv = super.psiToIntact(psiObject);

        if (!psiObject.getAttributes().isEmpty()) {
            for (Attribute attribute : psiObject.getAttributes()) {
                Annotation annotation = annotationConverter.psiToIntact(attribute);
                annotation.setOwner(getInstitution());

                if (!cv.getAnnotations().contains(annotation)) {
                    cv.getAnnotations().add(annotation);
                }
            }
        }

        psiEndConversion(psiObject);

        return cv;
    }

    public T intactToPsi(C intactObject) {
        intactStartConversation(intactObject);

        T cvType = (T) ConversionCache.getElement(elementKey(intactObject));

        if (cvType != null) {
            return cvType;
        }

        cvType = newCvInstance(psiCvClass);

        // Set id, annotations, xrefs and aliases
        PsiConverterUtils.populateNames(intactObject, cvType, aliasConverter);
        PsiConverterUtils.populateXref(intactObject, cvType, xrefConverter);

        AnnotationConverterConfig configAnnotation = ConverterContext.getInstance().getAnnotationConfig();

        Collection<Annotation> annotations;
        if (annotationConverter.isCheckInitializedCollections()){
            annotations = IntactCore.ensureInitializedAnnotations(intactObject);
        }
        else {
            annotations = intactObject.getAnnotations();
        }
        for ( Annotation annotation : annotations ) {
            if (!configAnnotation.isExcluded(annotation.getCvTopic())) {
                Attribute attribute = annotationConverter.intactToPsi( annotation );
                if (!cvType.getAttributes().contains( attribute )) {
                    cvType.getAttributes().add( attribute );
                }
            }
        }

        ConversionCache.putElement(elementKey(intactObject), cvType);

        intactEndConversion(intactObject);

        return cvType;
    }

    @Override
    public void setInstitution(Institution institution){
        super.setInstitution(institution);
        this.annotationConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setInstitution(Institution institution, String institId){
        super.setInstitution(institution, institId);
        this.annotationConverter.setInstitution(institution, institId);

    }

    @Override
    public void setCheckInitializedCollections(boolean check){
        super.setCheckInitializedCollections(check);
        this.annotationConverter.setCheckInitializedCollections(check);
    }

}
