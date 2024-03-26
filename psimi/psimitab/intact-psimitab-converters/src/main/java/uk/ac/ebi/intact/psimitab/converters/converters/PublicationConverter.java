package uk.ac.ebi.intact.psimitab.converters.converters;

import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.tab.model.Author;
import psidev.psi.mi.tab.model.AuthorImpl;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;
import uk.ac.ebi.intact.jami.model.extension.IntactSource;
import uk.ac.ebi.intact.jami.model.extension.SourceXref;
import uk.ac.ebi.intact.psimitab.converters.util.PsimitabTools;

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
    private CrossReferenceConverter<SourceXref> xrefConverter;
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
        this.xrefConverter = new CrossReferenceConverter<>();
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

    public void intactToMitab(IntactPublication pub, BinaryInteraction binary){

        if (pub != null && binary != null){

            Collection<Xref> pubRefs = pub.getXrefs();
            Collection<Annotation> pubAnnotations = PsimitabTools.getPublicAnnotations(pub.getAnnotations());

            for (Xref pubRef : pubRefs){
                if (pubRef.getQualifier() != null && pubRef.getDatabase().getShortName() != null) {
                    // publications
                    if (Xref.PRIMARY_MI.equals(pubRef.getQualifier().getMIIdentifier())) {
                        binary.getPublications().add(new CrossReferenceImpl(pubRef.getDatabase().getShortName(), pubRef.getId()));
                    }
                    // imexId
                    else if (Xref.IMEX_PRIMARY_MI.equals(pubRef.getQualifier().getMIIdentifier())) {
                        binary.getPublications().add(new CrossReferenceImpl(pubRef.getDatabase().getShortName(), pubRef.getId()));
                    }
                }
            }

            String authorDateValue = null;
            String author = null;
            String date = null;

            for (Annotation annot : pubAnnotations){
                if (annot.getTopic() != null){
                    // tag
                    if (tagsToExport.contains(annot.getTopic().getMIIdentifier())){
                        psidev.psi.mi.tab.model.Annotation tag = annotationConverter.intactToMitab(annot);
                        if (tag != null){
                            binary.getAnnotations().add(tag);
                        }
                    }
                    // author
                    else if ( "MI:0636".equals(annot.getTopic().getMIIdentifier())){
                        author = annot.getValue();
                    }
                    // date
                    else if ( "MI:0886".equals(annot.getTopic().getMIIdentifier())){
                        date = annot.getValue();
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
            IntactSource institution = (IntactSource) pub.getSource();

            if (institution != null){
                Collection<Xref> ownerRefs = institution.getXrefs();

                CrossReference identityRef = null;
                for (Xref ref : ownerRefs){
                    if (Xref.IDENTITY_MI.equals(ref.getQualifier().getMIIdentifier())) {
                        identityRef = xrefConverter.createCrossReference((SourceXref) ref, false);
                        break;
                    }
                }

                if (identityRef == null && institution.getShortName() != null){
                    identityRef = new CrossReferenceImpl();

                    String db = CrossReferenceConverter.DATABASE_UNKNOWN;

                    identityRef.setDatabase(db);
                    identityRef.setIdentifier(institution.getShortName());
                    identityRef.setText(institution.getShortName());
                }
                else if (institution.getShortName() != null){
                    identityRef.setText(institution.getShortName());
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
