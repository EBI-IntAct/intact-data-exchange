package uk.ac.ebi.intact.psimitab.converters.converters;

import psidev.psi.mi.tab.model.*;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Converts an Intact publication in MITAB
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/07/12</pre>
 */

public class PublicationConverter {

    private AnnotationConverter annotationConverter;
    private CrossReferenceConverter<InstitutionXref> xrefConverter;
    private List<String> tagsToExport;

    private final static String FULL_COVERAGE_MI = "MI:0957";
    private final static String PARTIAL_COVERAGE_MI = "MI:0958";
    private final static String CURATION_DEPTH_MI = "MI:0955";
    private final static String EXPERIMENTALLY_OBSERVED_MI = "MI:1054";
    private final static String IMPORTED_MI = "MI:1058";
    private final static String INTERNALLY_CURATED_MI = "MI:1055";
    private final static String PREDICTED_MI = "MI:1057";
    private final static String TEXT_MINING_MI = "MI:1056";
    private final static String DATASET_MI = "MI:0875";

    public PublicationConverter(){
        this.annotationConverter = new AnnotationConverter();
        this.xrefConverter = new CrossReferenceConverter<InstitutionXref>();
        tagsToExport = new ArrayList<String>();
        initializeTagsToExport();
    }

    // tags at the publication level that will be exported as interaction annotations
    private void initializeTagsToExport(){
        // full coverage
        tagsToExport.add(FULL_COVERAGE_MI);
        // partial coverage
        tagsToExport.add(PARTIAL_COVERAGE_MI);
        // curation depth
        tagsToExport.add(CURATION_DEPTH_MI);
        // experimentally observed
        tagsToExport.add(EXPERIMENTALLY_OBSERVED_MI);
        // imported
        tagsToExport.add(IMPORTED_MI);
        // internally curated
        tagsToExport.add(INTERNALLY_CURATED_MI);
        // predicted
        tagsToExport.add(PREDICTED_MI);
        // textMining
        tagsToExport.add(TEXT_MINING_MI);
        // dataset
        tagsToExport.add(DATASET_MI);
    }

    public void intactToMitab(Publication pub, BinaryInteraction binary){

        if (pub != null && binary != null){

            Collection<PublicationXref> pubRefs = pub.getXrefs();
            Collection<Annotation> pubAnnotations = AnnotatedObjectUtils.getPublicAnnotations(pub);

            for (PublicationXref pubRef : pubRefs){
                if (pubRef.getCvXrefQualifier() != null && pubRef.getCvDatabase().getShortLabel() != null) {
                    // publications
                    if (CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(pubRef.getCvXrefQualifier().getIdentifier())) {
                        binary.getPublications().add(new CrossReferenceImpl(pubRef.getCvDatabase().getShortLabel(), pubRef.getPrimaryId()));
                    }
                    // imexId
                    else if (CvXrefQualifier.IMEX_PRIMARY_MI_REF.equals(pubRef.getCvXrefQualifier().getIdentifier())) {
                        binary.getPublications().add(new CrossReferenceImpl(pubRef.getCvDatabase().getShortLabel(), pubRef.getPrimaryId()));
                    }
                }
            }

            String authorDateValue = null;
            String author = null;
            String date = null;

            for (Annotation annot : pubAnnotations){
                if (annot.getCvTopic() != null){
                    // tag
                    if (tagsToExport.contains(annot.getCvTopic().getIdentifier())){
                        psidev.psi.mi.tab.model.Annotation tag = annotationConverter.intactToMitab(annot);
                        if (tag != null){
                            binary.getInteractionAnnotations().add(tag);
                        }
                    }
                    // author
                    else if ( CvTopic.AUTHOR_LIST_MI_REF.equals(annot.getCvTopic().getIdentifier())){
                        author = annot.getAnnotationText();
                    }
                    // date
                    else if ( CvTopic.PUBLICATION_YEAR_MI_REF.equals(annot.getCvTopic().getIdentifier())){
                        date = annot.getAnnotationText();
                    }
                }
            }

            // create author/year value
            if (author != null && date != null){

                if (author.contains(" ")){
                    authorDateValue = author.split(" ")[0] + " et al. ("+date+")";
                }
                else {
                    authorDateValue = author + " et al. ("+date+")";
                }
            }
            else if (author != null){
                if (author.contains(" ")){
                    authorDateValue = author.split(" ")[0] + " et al.";
                }
                else {
                    authorDateValue = author + " et al.";
                }
            }
            else if (date != null){
                authorDateValue = "- ("+date+")";
            }
            if (authorDateValue != null){
                Author mitabAuthor = new AuthorImpl(authorDateValue);
                binary.getAuthors().add(mitabAuthor);
            }

            // create source database
            Institution institution = pub.getOwner();

            if (institution != null){
                Collection<InstitutionXref> ownerRefs = institution.getXrefs();

                CrossReference identityRef = null;
                for (InstitutionXref ref : ownerRefs){
                    if (CvXrefQualifier.IDENTITY_MI_REF.equals(ref.getCvXrefQualifier().getIdentifier())) {
                        identityRef = xrefConverter.createCrossReference(ref, false);
                        break;
                    }
                }

                if (identityRef == null && institution.getShortLabel() != null){
                    identityRef = new CrossReferenceImpl();

                    String db = CrossReferenceConverter.DATABASE_UNKNOWN;

                    identityRef.setDatabase(db);
                    identityRef.setIdentifier("-");
                    identityRef.setText(institution.getShortLabel());
                }
                else if (institution.getShortLabel() != null){
                    identityRef.setText(institution.getShortLabel());
                }

                if (identityRef != null){
                    binary.getSourceDatabases().add(identityRef);
                }
            }

            // creation date of publication
            if (pub.getCreated() != null){
                binary.getCreationDate().add(pub.getCreated());
            }
        }
    }
}
