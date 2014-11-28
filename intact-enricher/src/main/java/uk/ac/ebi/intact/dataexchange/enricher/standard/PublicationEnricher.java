/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.full.FullPublicationEnricher;
import psidev.psi.mi.jami.enricher.listener.PublicationEnricherListener;
import psidev.psi.mi.jami.enricher.listener.impl.log.PublicationEnricherLogger;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Publication;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.PublicationFetcher;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Intact extension of publication enricher
 *
 */
@Component(value = "intactPublicationEnricher")
@Lazy
public class PublicationEnricher extends FullPublicationEnricher {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(PublicationEnricher.class);

    @Autowired
    private EnricherContext enricherContext;

    @Resource(name = "miCvObjectEnricher")
    private MiCvObjectEnricher miCvObjectEnricher;

    @Resource(name = "intactInstitutionEnricher")
    private InstitutionEnricher institutionEnricher;

    @Autowired
    public PublicationEnricher(@Qualifier("intactPublicationFetcher") PublicationFetcher intactPublicationFetcher) {
        super(intactPublicationFetcher);
    }

    @Override
    protected void processCurationDepth(Publication publicationToEnrich, Publication fetched) throws EnricherException {
        if (fetched != null){
            super.processCurationDepth(publicationToEnrich, fetched);
        }

        // process source
        if (publicationToEnrich.getSource() != null
                && enricherContext.getConfig().isUpdateCvTerms()
                && institutionEnricher != null){
            institutionEnricher.enrich(publicationToEnrich.getSource());
        }
    }

    @Override
    protected void processXrefs(Publication objectToEnrich, Publication objectSource) throws EnricherException {
        if (objectSource != null){
            super.processXrefs(objectToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && miCvObjectEnricher != null){
            for (Object obj : objectToEnrich.getXrefs()) {
                Xref xref = (Xref)obj;
                if (xref.getQualifier()!= null) {
                    miCvObjectEnricher.enrich(xref.getQualifier());
                }
                miCvObjectEnricher.enrich(xref.getDatabase());
            }
        }
    }

    @Override
    protected void processAnnotations(Publication objectToEnrich, Publication objectSource) throws EnricherException {
        if (objectSource != null){
            super.processAnnotations(objectToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && miCvObjectEnricher != null){
            for (Object obj : objectToEnrich.getAnnotations()) {
                Annotation annotation = (Annotation)obj;
                miCvObjectEnricher.enrich(annotation.getTopic());
            }
        }
    }

    @Override
    protected void processJournal(Publication publicationToEnrich, Publication fetched) throws EnricherException {
        if((fetched.getJournal() != null && !fetched.getJournal().equals(publicationToEnrich.getJournal()))
                || (fetched.getJournal() == null && publicationToEnrich.getJournal() != null)) {
            String oldJournal = publicationToEnrich.getJournal();
            publicationToEnrich.setJournal(fetched.getJournal());
            if(getPublicationEnricherListener() != null)
                getPublicationEnricherListener().onJournalUpdated(publicationToEnrich, oldJournal);
        }
    }

    @Override
    protected void processPublicationTitle(Publication publicationToEnrich, Publication fetched) throws EnricherException {
        if((fetched.getTitle() != null && !fetched.getTitle().equals(publicationToEnrich.getTitle()))
                || (fetched.getTitle() == null && publicationToEnrich.getTitle() != null)) {
            String oldTitle = publicationToEnrich.getTitle();
            publicationToEnrich.setTitle(fetched.getTitle());
            if(getPublicationEnricherListener() != null)
                getPublicationEnricherListener().onTitleUpdated(publicationToEnrich , oldTitle);
        }
    }

    @Override
    protected void processPublicationDate(Publication publicationToEnrich, Publication fetched) throws EnricherException {
        // == PUBLICATION DATE =================================================================================
        if((fetched.getPublicationDate() != null && !fetched.getPublicationDate().equals( publicationToEnrich.getPublicationDate()) )
                || (fetched.getPublicationDate() == null && publicationToEnrich.getPublicationDate() != null)) {
            Date oldValue = publicationToEnrich.getPublicationDate();
            publicationToEnrich.setPublicationDate(fetched.getPublicationDate());
            if(getPublicationEnricherListener() != null)
                getPublicationEnricherListener().onPublicationDateUpdated(publicationToEnrich , oldValue);
        }
    }

    @Override
    protected void processAuthors(Publication publicationToEnrich, Publication fetched) throws EnricherException {
        // == AUTHORS ===========================================================================================
        if(!CollectionUtils.isEqualCollection(publicationToEnrich.getAuthors(), fetched.getAuthors())){
            Iterator<String> authorIterator = publicationToEnrich.getAuthors().iterator();
            while(authorIterator.hasNext()){
                String auth = authorIterator.next();
                authorIterator.remove();
                if(getPublicationEnricherListener() != null)
                    getPublicationEnricherListener().onAuthorRemoved(publicationToEnrich, auth);
            }
            for(String author : fetched.getAuthors()){
                publicationToEnrich.getAuthors().add(author);
                if(getPublicationEnricherListener() != null)
                    getPublicationEnricherListener().onAuthorAdded(publicationToEnrich , author);
            }
        }
    }

    @Override
    protected void processIdentifiers(Publication objectToEnrich, Publication fetched) throws EnricherException {
        if (objectToEnrich != null){
            super.processIdentifiers(objectToEnrich, fetched);
        }

        fixPubmedXrefIfNecessary(objectToEnrich);

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && miCvObjectEnricher != null){
            for (Object obj : objectToEnrich.getIdentifiers()) {
                Xref xref = (Xref)obj;
                if (xref.getQualifier()!= null) {
                    miCvObjectEnricher.enrich(xref.getQualifier());
                }
                miCvObjectEnricher.enrich(xref.getDatabase());
            }
        }
    }

    @Override
    protected void onEnrichedVersionNotFound(Publication publicationToEnrich) throws EnricherException {
        processAnnotations(publicationToEnrich, null);
        processIdentifiers(publicationToEnrich, null);
        processXrefs(publicationToEnrich, null);
        processCurationDepth(publicationToEnrich, null);
        super.onEnrichedVersionNotFound(publicationToEnrich);
    }

    protected void fixPubmedXrefIfNecessary(Publication publication) {
        if (publication.getPubmedId() != null){
            Xref pubmed = XrefUtils.collectFirstIdentifierWithDatabaseAndId(publication.getIdentifiers(), Xref.PUBMED_MI, Xref.PUBMED, publication.getPubmedId());

            if (pubmed != null){
                log.warn( "Fixing pubmed xref with identity qualifier: "+pubmed.getId() );

                publication.getIdentifiers().remove(pubmed);
                publication.getIdentifiers().
                        add(XrefUtils.createXrefWithQualifier(Xref.PUBMED, Xref.PUBMED_MI, pubmed.getId(), Xref.PRIMARY, Xref.PRIMARY_MI));
            }
        }
        else{
            Collection<Xref> pubmeds = XrefUtils.collectAllXrefsHavingDatabase(publication.getXrefs(), Xref.PUBMED_MI, Xref.PUBMED);
            if (pubmeds.size() == 1){
                Xref pubmed = pubmeds.iterator().next();
                log.warn( "Fixing pubmed xref with no qualifier: "+pubmed.getId() );

                publication.getIdentifiers().remove(pubmed);
                publication.getIdentifiers().
                        add(XrefUtils.createXrefWithQualifier(Xref.PUBMED, Xref.PUBMED_MI, pubmed.getId(), Xref.PRIMARY, Xref.PRIMARY_MI));
            }
        }

        if (publication.getDoi() != null){
            Xref doi = XrefUtils.collectFirstIdentifierWithDatabaseAndId(publication.getIdentifiers(), Xref.DOI_MI, Xref.DOI, publication.getDoi());
            if (doi != null){
                log.warn( "Fixing doi xref with identity qualifier: "+doi.getId() );

                publication.getIdentifiers().remove(doi);
                publication.getIdentifiers().
                        add(XrefUtils.createXrefWithQualifier(Xref.DOI, Xref.DOI_MI, doi.getId(), Xref.PRIMARY, Xref.PRIMARY_MI));
            }
        }
        else{
            Collection<Xref> dois = XrefUtils.collectAllXrefsHavingDatabase(publication.getXrefs(), Xref.DOI_MI, Xref.DOI);
            if (dois.size() == 1){
                Xref doi = dois.iterator().next();
                log.warn( "Fixing doi xref with identity qualifier: "+doi.getId() );

                publication.getIdentifiers().remove(doi);
                publication.getIdentifiers().
                        add(XrefUtils.createXrefWithQualifier(Xref.DOI, Xref.DOI_MI, doi.getId(), Xref.PRIMARY, Xref.PRIMARY_MI));
            }
        }
    }

    @Override
    public PublicationEnricherListener getPublicationEnricherListener() {
        if (super.getPublicationEnricherListener() == null){
            super.setPublicationEnricherListener(new PublicationEnricherLogger());
        }
        return super.getPublicationEnricherListener();
    }
}
