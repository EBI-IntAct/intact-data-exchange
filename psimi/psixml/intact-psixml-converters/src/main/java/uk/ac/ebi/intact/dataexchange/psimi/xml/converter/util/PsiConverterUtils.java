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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.util.XMLChar;
import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.model.Confidence;
import psidev.psi.mi.xml.model.Xref;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.AnnotationConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.InteractorConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.AliasConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.AnnotationConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.CvObjectConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.XrefConverter;
import uk.ac.ebi.intact.model.Alias;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;

/**
 * PSI converter utilities.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk), Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiConverterUtils {

    private static final Log log = LogFactory.getLog(PsiConverterUtils.class);


    private PsiConverterUtils() {
    }

    public static void populate( AnnotatedObject<?, ?> annotatedObject, Object objectToPopulate, AbstractIntactPsiConverter converter ) {
        if ( objectToPopulate instanceof HasId ) {
            populateId( ( HasId ) objectToPopulate );
        }

        if ( objectToPopulate instanceof NamesContainer ) {
            populateNames( annotatedObject, ( NamesContainer ) objectToPopulate );
        }

        if ( objectToPopulate instanceof XrefContainer ) {
            populateXref( annotatedObject, ( XrefContainer ) objectToPopulate, converter );
        }

        if ( objectToPopulate instanceof AttributeContainer ) {
            populateAttributes( annotatedObject, ( AttributeContainer ) objectToPopulate );
        }
    }

    public static void populate( AnnotatedObject<?, ?> annotatedObject, Object objectToPopulate, AliasConverter aliasConverter, AnnotationConverter annotationConverter, XrefConverter xrefConverter, boolean checkInitializedCollections ) {
        if ( objectToPopulate instanceof HasId ) {
            populateId( ( HasId ) objectToPopulate );
        }

        if ( objectToPopulate instanceof NamesContainer ) {
            populateNames( annotatedObject, ( NamesContainer ) objectToPopulate, aliasConverter );
        }

        if ( objectToPopulate instanceof XrefContainer ) {
            populateXref( annotatedObject, ( XrefContainer ) objectToPopulate, xrefConverter );
        }

        if ( objectToPopulate instanceof AttributeContainer ) {
            populateAttributes( annotatedObject, ( AttributeContainer ) objectToPopulate, annotationConverter );
        }
    }

    protected static void populateNames( AnnotatedObject<?, ?> annotatedObject, NamesContainer namesContainer ) {
        Names names = namesContainer.getNames();

        if ( names == null ) {
            names = new Names();
        }

        String shortLabel = annotatedObject.getShortLabel();
        String fullName = annotatedObject.getFullName();

        names.setShortLabel( shortLabel );
        names.setFullName( fullName );

        if ( !ConverterContext.getInstance().getInteractorConfig().isExcludeInteractorAliases() ) {
            AliasConverter aliasConverter = new AliasConverter( annotatedObject.getOwner(),
                    AnnotatedObjectUtils.getAliasClassType( annotatedObject.getClass() ) );

            for ( Alias alias : IntactCore.ensureInitializedAliases(annotatedObject)) {
                names.getAliases().add( aliasConverter.intactToPsi( alias ) );
            }
        }

        namesContainer.setNames( names );
    }

    public static void populateNames( AnnotatedObject<?, ?> annotatedObject, NamesContainer namesContainer, AliasConverter aliasConverter ) {
        Names names = namesContainer.getNames();

        if ( names == null ) {
            names = new Names();
        }

        String shortLabel = annotatedObject.getShortLabel();
        String fullName = annotatedObject.getFullName();

        names.setShortLabel( shortLabel );
        names.setFullName( fullName );

        if ( !ConverterContext.getInstance().getInteractorConfig().isExcludeInteractorAliases() ) {

            Collection<? extends Alias> aliases;
            if (aliasConverter.isCheckInitializedCollections()){
                aliases = IntactCore.ensureInitializedAliases(annotatedObject);
            }
            else {
                aliases = annotatedObject.getAliases();
            }

            for ( Alias alias : aliases) {
                names.getAliases().add( aliasConverter.intactToPsi( alias ) );
            }
        }

        namesContainer.setNames( names );
    }


    private static void populateXref( AnnotatedObject<?, ?> annotatedObject, XrefContainer xrefContainer, AbstractIntactPsiConverter converter ) {

        // ac - create a xref to the institution db
        String ac = annotatedObject.getAc();
        boolean containsAcXref = false;
        DbReference acRef = null;

        if (ac != null)  {
            for ( uk.ac.ebi.intact.model.Xref xref : IntactCore.ensureInitializedXrefs(annotatedObject)) {
                if (annotatedObject.getAc().equals(xref.getPrimaryId())) {
                    containsAcXref = true;
                    break;
                }
            }

            if (!containsAcXref) {
                String dbMi = null;
                String db = null;

                // calculate the owner of the interaction, based on the AC prefix first,
                // then in the defaultInstitutionForACs if passed to the ConverterContext or,
                // finally to the Institution in the source section of the PSI-XML
                if (ac.startsWith("EBI")) {
                    dbMi = Institution.INTACT_REF;
                    db = Institution.INTACT.toLowerCase();
                } else if (ac.startsWith("MINT")) {
                    dbMi = Institution.MINT_REF;
                    db = Institution.MINT.toLowerCase();
                } else if (ConverterContext.getInstance().getDefaultInstitutionForAcs() != null){
                    Institution defaultInstitution = ConverterContext.getInstance().getDefaultInstitutionForAcs();
                    dbMi = converter.calculateInstitutionPrimaryId(defaultInstitution);
                    db = defaultInstitution.getShortLabel().toLowerCase();
                } else {
                    dbMi = converter.getInstitutionPrimaryId();
                    db = converter.getInstitution().getShortLabel().toLowerCase();
                }

                // the ref is identity!!!!
                acRef = new DbReference( db, dbMi, ac,
                        CvXrefQualifier.IDENTITY,
                        CvXrefQualifier.IDENTITY_MI_REF );
            }
        }

        if ( acRef == null && annotatedObject.getXrefs().isEmpty() ) {
            return;
        }

        Xref xref = xrefContainer.getXref();

        if ( xref == null ) {
            xref = new Xref();
        }

        Collection<DbReference> dbRefs = toDbReferences( annotatedObject, IntactCore.ensureInitializedXrefs(annotatedObject));

        // normally the primary reference is the identity reference, but for bibliographic references
        // it is the primary-reference and it does not contain secondary refs
        if ( xrefContainer instanceof Bibref ) {
            final DbReference primaryPubmedRef = getPrimaryReference( dbRefs , CvDatabase.PUBMED_MI_REF );

            if (primaryPubmedRef != null) {
                xref.setPrimaryRef( primaryPubmedRef );
            } else {
                final DbReference primaryDoiRef = getPrimaryReference( dbRefs , CvDatabase.DOI_MI_REF );

                if (primaryDoiRef != null) {
                    xref.setPrimaryRef(primaryDoiRef);

                    if (log.isWarnEnabled()) log.warn("Primary-reference (refTypeAc="+ CvXrefQualifier.PRIMARY_REFERENCE_MI_REF+") " +
                            " found in "+xrefContainer.getClass().getSimpleName()+
                            ": "+xrefContainer+", located at: "+ ConverterContext.getInstance().getLocation().getCurrentLocation().pathFromRootAsString()+
                            " is neither a reference to Pubmed (dbAc=" + CvDatabase.PUBMED_MI_REF + ") nor a DOI (dbAc=" + CvDatabase.DOI_MI_REF + ")");


                } else {
                    final DbReference primaryRef = getPrimaryReference( dbRefs );

                    if (primaryRef != null) {
                        xref.setPrimaryRef(primaryRef);
                    } else  {
                        if (log.isWarnEnabled()) log.warn("No primary-reference (refTypeAc="+ CvXrefQualifier.PRIMARY_REFERENCE_MI_REF+") " +
                                " could be found in "+xrefContainer.getClass().getSimpleName()+
                                ": "+xrefContainer+", located at: "+ ConverterContext.getInstance().getLocation().getCurrentLocation().pathFromRootAsString());
                    }
                }
            }
        } else {

            // remove the primary ref from the collection if it is a experiment
            // so we don't have the same ref in the bibref and the xref sections
            if ( annotatedObject instanceof Experiment ) {
                final DbReference bibref = getPrimaryReference(dbRefs, CvDatabase.PUBMED_MI_REF);

                if (bibref != null) {
                    dbRefs.remove(bibref);
                }
            }

            DbReference primaryRef = getIdentity( dbRefs, acRef );
            xref.setPrimaryRef( primaryRef );

            // remove the primary ref
            // from the collection and add the rest as secondary refs
            dbRefs.remove( primaryRef );

            for ( DbReference secDbRef : dbRefs ) {
                if ( !xref.getSecondaryRef().contains( secDbRef ) ) {
                    xref.getSecondaryRef().add( secDbRef );
                }
            }
        }

        if (xref.getPrimaryRef() != null) {
            xrefContainer.setXref( xref );
        }
    }

    public static void populateXref( AnnotatedObject<?, ?> annotatedObject, XrefContainer xrefContainer, XrefConverter converter ) {

        // ac - create a xref to the institution db
        String ac = annotatedObject.getAc();
        boolean containsAcXref = false;
        DbReference acRef = null;

        Collection<? extends uk.ac.ebi.intact.model.Xref> xrefs;
        if (converter.isCheckInitializedCollections()){
            xrefs = IntactCore.ensureInitializedXrefs(annotatedObject);
        }
        else {
            xrefs = annotatedObject.getXrefs();
        }

        if (ac != null)  {
            for ( uk.ac.ebi.intact.model.Xref xref : xrefs) {
                if (annotatedObject.getAc().equals(xref.getPrimaryId())) {
                    containsAcXref = true;
                    break;
                }
            }

            if (!containsAcXref) {
                String dbMi = null;
                String db = null;

                // calculate the owner of the interaction, based on the AC prefix first,
                // then in the defaultInstitutionForACs if passed to the ConverterContext or,
                // finally to the Institution in the source section of the PSI-XML
                if (ac.startsWith("EBI")) {
                    dbMi = Institution.INTACT_REF;
                    db = Institution.INTACT.toLowerCase();
                } else if (ac.startsWith("MINT")) {
                    dbMi = Institution.MINT_REF;
                    db = Institution.MINT.toLowerCase();
                } else if (ConverterContext.getInstance().getDefaultInstitutionForAcs() != null){
                    Institution defaultInstitution = ConverterContext.getInstance().getDefaultInstitutionForAcs();
                    dbMi = converter.calculateInstitutionPrimaryId(defaultInstitution);
                    db = defaultInstitution.getShortLabel().toLowerCase();
                }
                else{
                    dbMi = converter.getInstitutionPrimaryId();
                    db = converter.getInstitution().getShortLabel().toLowerCase();
                }

                acRef = new DbReference( db, dbMi, ac,
                        CvXrefQualifier.IDENTITY,
                        CvXrefQualifier.IDENTITY_MI_REF );
            }
        }

        if ( acRef == null && xrefs.isEmpty() ) {
            return;
        }

        Xref xref = xrefContainer.getXref();

        if ( xref == null ) {
            xref = new Xref();
        }

        Collection<DbReference> dbRefs = toDbReferences( annotatedObject, xrefs, converter);

        // normally the primary reference is the identity reference, but for bibliographic references
        // it is the primary-reference and it does not contain secondary refs
        if ( xrefContainer instanceof Bibref ) {
            DbReference primaryRef = getPrimaryReference( dbRefs , CvDatabase.PUBMED_MI_REF );

            if (primaryRef != null) {
                xref.setPrimaryRef( primaryRef );
            } else {
                primaryRef = getPrimaryReference( dbRefs , CvDatabase.DOI_MI_REF );

                if (primaryRef != null) {
                    xref.setPrimaryRef(primaryRef);

                    if (log.isWarnEnabled()) log.warn("Primary-reference (refTypeAc="+ CvXrefQualifier.PRIMARY_REFERENCE_MI_REF+") " +
                            " found in "+xrefContainer.getClass().getSimpleName()+
                            ": "+xrefContainer+", located at: "+ ConverterContext.getInstance().getLocation().getCurrentLocation().pathFromRootAsString()+
                            " is neither a reference to Pubmed (dbAc=" + CvDatabase.PUBMED_MI_REF + ") nor a DOI (dbAc=" + CvDatabase.DOI_MI_REF + ")");


                } else {
                    primaryRef = getPrimaryReference( dbRefs );

                    if (primaryRef != null) {
                        xref.setPrimaryRef(primaryRef);
                    } else  {
                        if (log.isWarnEnabled()) log.warn("No primary-reference (refTypeAc="+ CvXrefQualifier.PRIMARY_REFERENCE_MI_REF+") " +
                                " could be found in "+xrefContainer.getClass().getSimpleName()+
                                ": "+xrefContainer+", located at: "+ ConverterContext.getInstance().getLocation().getCurrentLocation().pathFromRootAsString());
                    }
                }

                // add the secondary xrefs
                xref.getSecondaryRef().addAll(CollectionUtils.subtract(dbRefs, Arrays.asList(primaryRef)));
            }
        } else {
            // remove the primary ref from the collection if it is a experiment
            // so we don't have the same ref in the bibref and the xref sections
            if ( annotatedObject instanceof Experiment ) {
                DbReference bibref = getPrimaryReference(dbRefs, CvDatabase.PUBMED_MI_REF);

                if (bibref == null){
                    bibref = getPrimaryReference(dbRefs, CvDatabase.DOI_MI_REF);
                }

                if (bibref != null) {
                    dbRefs.remove(bibref);
                }
            }

            DbReference primaryRef = getIdentity( dbRefs, acRef );

            if (primaryRef == null){
                primaryRef = getPrimaryReference(dbRefs);
            }

            xref.setPrimaryRef( primaryRef );

            // remove the primary ref
            // from the collection and add the rest as secondary refs
            dbRefs.remove( primaryRef );

            for ( DbReference secDbRef : dbRefs ) {
                if ( !xref.getSecondaryRef().contains( secDbRef ) ) {
                    xref.getSecondaryRef().add( secDbRef );
                }
            }
        }

        if (xref.getPrimaryRef() != null) {
            xrefContainer.setXref( xref );
        }
    }


    public static int populateId( HasId hasIdElement ) {
        if ( hasIdElement.getId() > 0 ) {
            return hasIdElement.getId();
        }

        int id = IdSequenceGenerator.getInstance().nextId();
        hasIdElement.setId( id );

        return id;
    }

    private static void populateAttributes( AnnotatedObject<?, ?> annotatedObject, AttributeContainer attributeContainer ) {
        AnnotationConverter annotationConverter = new AnnotationConverter( annotatedObject.getOwner() );

        AnnotationConverterConfig configAnnotation = ConverterContext.getInstance().getAnnotationConfig();

        for ( Annotation annotation : IntactCore.ensureInitializedAnnotations(annotatedObject) ) {
            if (!configAnnotation.isExcluded(annotation.getCvTopic())) {
                Attribute attribute = annotationConverter.intactToPsi( annotation );
                if (!attributeContainer.getAttributes().contains( attribute )) {
                    attributeContainer.getAttributes().add( attribute );
                }
            }
        }
    }

    public static void populateAttributes( AnnotatedObject<?, ?> annotatedObject, AttributeContainer attributeContainer, AnnotationConverter annotationConverter ) {

        AnnotationConverterConfig configAnnotation = ConverterContext.getInstance().getAnnotationConfig();

        Collection<Annotation> annotations;
        if (annotationConverter.isCheckInitializedCollections()){
            annotations = IntactCore.ensureInitializedAnnotations(annotatedObject);
        }
        else {
            annotations = annotatedObject.getAnnotations();
        }
        for ( Annotation annotation : annotations ) {
            if (!configAnnotation.isExcluded(annotation.getCvTopic())) {
                Attribute attribute = annotationConverter.intactToPsi( annotation );
                if (!attributeContainer.getAttributes().contains( attribute )) {
                    attributeContainer.getAttributes().add( attribute );
                }
            }
        }
    }

    public static CvType toCvType( CvObject cvObject, CvObjectConverter cvConverter, AbstractIntactPsiConverter converter ) {
        if ( cvObject == null ) {
            throw new NullPointerException( "cvObject" );
        }

        CvType cvType = cvConverter.intactToPsi( cvObject );
        populate( cvObject, cvType, converter );

        return cvType;
    }

    public static CvType toCvType( CvObject cvObject, CvObjectConverter cvConverter, AliasConverter aliasConverter, XrefConverter xrefConverter, boolean checkInitializedCollections ) {
        if ( cvObject == null ) {
            throw new NullPointerException( "cvObject" );
        }

        CvType cvType = cvConverter.intactToPsi( cvObject );
        populate( cvObject, cvType, aliasConverter, null, xrefConverter, checkInitializedCollections );

        return cvType;
    }

    public static BiologicalRole createUnspecifiedBiologicalRole() {
        return createUnspecifiedRole( BiologicalRole.class );
    }

    public static ExperimentalRole createUnspecifiedExperimentalRole() {
        return createUnspecifiedRole( ExperimentalRole.class );
    }

    public static Collection<ExperimentDescription> nonRedundantExperimentsFromPsiEntry(Entry psiEntry) {
        Map<Integer,ExperimentDescription> nonRedundantExps = new HashMap<Integer,ExperimentDescription>();

        if( ConverterContext.getInstance().isGenerateCompactXml() ) {
            for (ExperimentDescription expDesc : psiEntry.getExperiments()) {
                nonRedundantExps.put(expDesc.getId(), expDesc);
            }
        } else {
            for (psidev.psi.mi.xml.model.Interaction interaction : psiEntry.getInteractions()) {
                for (ExperimentDescription exp : interaction.getExperiments()) {
                    nonRedundantExps.put(exp.getId(), exp);
                }
            }
        }

        return nonRedundantExps.values();
    }

    public static Collection<psidev.psi.mi.xml.model.Interactor> nonRedundantInteractorsFromPsiEntry(Entry psiEntry) {
        Map<Integer, psidev.psi.mi.xml.model.Interactor> nonRedundantInteractors = new HashMap<Integer, psidev.psi.mi.xml.model.Interactor>();

        if( ConverterContext.getInstance().isGenerateExpandedXml() ) {
            // iterate to get the unique experiments/interactors
            for (psidev.psi.mi.xml.model.Interaction interaction : psiEntry.getInteractions()) {
                for (Participant participant : interaction.getParticipants()) {
                    nonRedundantInteractors.put(participant.getInteractor().getId(), participant.getInteractor());
                }
            }
        } else {
            for ( psidev.psi.mi.xml.model.Interactor interactor : psiEntry.getInteractors() ) {
                nonRedundantInteractors.put( interactor.getId(), interactor );
            }
        }

        return nonRedundantInteractors.values();
    }

    private static <T extends CvType> T createUnspecifiedRole( Class<T> roleClass ) {
        T role;

        try {
            role = roleClass.newInstance();
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

        Names names = new Names();
        names.setShortLabel(CvExperimentalRole.UNSPECIFIED);
        names.setFullName(CvExperimentalRole.UNSPECIFIED);

        role.setNames( names );

        Xref xref = new Xref();

        String unspecifiedRoleMiRef = CvExperimentalRole.UNSPECIFIED_PSI_REF;

        DbReference dbRef = new DbReference( unspecifiedRoleMiRef, CvDatabase.PSI_MI );
        dbRef.setRefType( CvXrefQualifier.IDENTITY );
        dbRef.setRefTypeAc( CvXrefQualifier.IDENTITY_MI_REF );
        dbRef.setDbAc( CvDatabase.PSI_MI_MI_REF );

        xref.setPrimaryRef( dbRef );
        role.setXref( xref );

        return role;
    }

    private static Collection<DbReference> toDbReferences( AnnotatedObject<?, ?> annotatedObject,
                                                           Collection<? extends uk.ac.ebi.intact.model.Xref> intactXrefs ) {

        Collection<DbReference> dbRefs = new HashSet<DbReference>( intactXrefs.size() );

        for ( uk.ac.ebi.intact.model.Xref intactXref : intactXrefs ) {

            if ( annotatedObject instanceof Interactor && !( annotatedObject instanceof Interaction ) ) {
                // We have an interactor that is not an interaction.
                if ( includeInteractorXref( intactXref ) ) {
                    XrefConverter xrefConverter = new XrefConverter( annotatedObject.getOwner(), intactXref.getClass() );
                    DbReference dbRef = xrefConverter.intactToPsi( intactXref );
                    dbRefs.add( dbRef );
                }
            } else {
                XrefConverter xrefConverter = new XrefConverter( annotatedObject.getOwner(), intactXref.getClass() );
                DbReference dbRef = xrefConverter.intactToPsi( intactXref );
                dbRefs.add( dbRef );
            }
        }

        return dbRefs;
    }

    private static Collection<DbReference> toDbReferences( AnnotatedObject<?, ?> annotatedObject,
                                                           Collection<? extends uk.ac.ebi.intact.model.Xref> intactXrefs, XrefConverter xrefConverter ) {

        Collection<DbReference> dbRefs = new HashSet<DbReference>( intactXrefs.size() );

        for ( uk.ac.ebi.intact.model.Xref intactXref : intactXrefs ) {

            if ( annotatedObject instanceof Interactor && !( annotatedObject instanceof Interaction ) ) {
                // We have an interactor that is not an interaction.
                if ( includeInteractorXref( intactXref ) ) {
                    DbReference dbRef = xrefConverter.intactToPsi( intactXref );
                    dbRefs.add( dbRef );
                }
            } else {
                DbReference dbRef = xrefConverter.intactToPsi( intactXref );
                dbRefs.add( dbRef );
            }
        }

        return dbRefs;
    }

    public static Set<DbReference> toDbReferences( Collection<? extends uk.ac.ebi.intact.model.Xref> intactXrefs, XrefConverter xrefConverter ) {

        Set<DbReference> dbRefs = new HashSet<DbReference>( intactXrefs.size() );

        for ( uk.ac.ebi.intact.model.Xref intactXref : intactXrefs ) {

            DbReference dbRef = xrefConverter.intactToPsi( intactXref );
            dbRefs.add( dbRef );
        }

        return dbRefs;
    }

    /**
     * Figures out if the given Xref has to be exported given the InteractorConverterConfig available.
     *
     * @param intactXref the xref under inspection.
     * @return true if the xref has to be converted, false otherwise.
     */
    private static boolean includeInteractorXref( uk.ac.ebi.intact.model.Xref intactXref ) {
        CvDatabase db = intactXref.getCvDatabase();
        CvXrefQualifier qualifier = intactXref.getCvXrefQualifier();
        InteractorConverterConfig configInteractor = ConverterContext.getInstance().getInteractorConfig();

        if ( configInteractor.isExcluded( qualifier ) || configInteractor.isExcluded( db ) ) {
            return false;
        }

        if ( configInteractor.hasIncludesCvDatabase() ) {
            if ( !configInteractor.isIncluded( db ) ) {
                return false;
            }
        }

        if ( configInteractor.hasIncludesCvXrefQualifier() ) {
            if ( !configInteractor.isIncluded( qualifier ) ) {
                return false;
            }
        }

        return true;
    }

    /**
     * The primary ref is the one that contains the qualifier 'identity', choosing the one with uniprot accession
     * if there is more than one "identities"
     *
     * @param dbRefs
     * @return
     */
    protected static DbReference getIdentity( Collection<DbReference> dbRefs, DbReference acRef ) {
        Collection<DbReference> identityRefs = new HashSet<DbReference>();

        DbReference primary = null;

        for ( DbReference dbRef : dbRefs ) {
            if ( dbRef.getRefTypeAc() != null && dbRef.getRefTypeAc().equals( CvXrefQualifier.IDENTITY_MI_REF ) ) {

                if ( (dbRef.getDbAc() != null && dbRef.getDbAc().equals( CvDatabase.PSI_MI_MI_REF )) || (dbRef.getDbAc() == null && dbRef.getDb().equalsIgnoreCase( CvDatabase.PSI_MI)) ) {
                    primary = dbRef;
                    break;
                }

                identityRefs.add( dbRef );
            }
        }

        if ( !identityRefs.isEmpty() && primary == null ) {
            // return the one for uniprot, if present. Otherwise return a random one.
            for ( DbReference dbRef : identityRefs ) {
                if ( (dbRef.getDbAc() != null && dbRef.getDbAc().equals( CvDatabase.UNIPROT_MI_REF )) || (dbRef.getDbAc() == null && dbRef.getDb().equalsIgnoreCase( CvDatabase.UNIPROT )) ) {

                    primary = dbRef;
                    break;
                }
            }

            if (primary == null){
                primary = identityRefs.iterator().next();
            }
        }

        // primaryRef is the ac by default, otherwise the first xref
        if (primary != null){
            if (acRef != null){
                dbRefs.add(acRef);
            }
            return primary;
        }
        else if (acRef != null){
            return acRef;
        }
        else if ( !dbRefs.isEmpty() ) {
            if (acRef != null){
                dbRefs.add(acRef);
            }
            return dbRefs.iterator().next();
        }

        return null;
    }

    private static DbReference getPrimaryReference( Collection<DbReference> dbRefs ) {
        return getPrimaryReference(dbRefs, null);
    }

    private static DbReference getPrimaryReference( Collection<DbReference> dbRefs, String dbAc ) {
        for ( DbReference dbRef : dbRefs ) {
            if ( dbRef.getRefTypeAc() != null && dbRef.getRefTypeAc().equals( CvXrefQualifier.PRIMARY_REFERENCE_MI_REF ) ) {
                if (dbAc != null) {
                    if (dbAc.equals(dbRef.getDbAc())) {
                        return dbRef;
                    }
                } else {
                    return dbRef;
                }
            }
        }

        if ( dbAc == null && !dbRefs.isEmpty() ) {
            return dbRefs.iterator().next();
        }

        return null;
    }

    public static String forXML(String aText){
        if (aText == null){
            return null;
        }

        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character =  iterator.current();
        while (character != CharacterIterator.DONE ){
            if (character == '<') {
                result.append("&lt;");
            }
            else if (character == '>') {
                result.append("&gt;");
            }
            else if (character == '\"') {
                result.append("&quot;");
            }
            else if (character == '\'') {
                result.append("&#039;");
            }
            else if (character == '&') {
                result.append("&amp;");
            }
            else if (!XMLChar.isInvalid(character)){
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    public static boolean contains(Confidence conf, Collection<Confidence> confidences){

        if (conf == null || confidences == null){
            return false;
        }

        for (Confidence confidence : confidences){
            if (areEquals(conf, confidence)){
                return true;
            }
        }

        return false;
    }

    public static boolean areEquals(Confidence conf1, Confidence conf2){

        if (conf1 == null && conf2 == null){
            return true;
        }
        else if (conf1 != null && conf2 == null){
            return false;
        }
        else if (conf1 == null && conf2 != null){
            return false;
        }
        else {
            // units
            Unit unit1 = conf1.getUnit();
            Unit unit2 = conf2.getUnit();
            if (unit1 != null && unit2 == null){
                return false;
            }
            else if (unit1 == null && unit2 != null){
                return false;
            }
            else if (unit1 != null && unit2 != null) {
                // names
                Names names1 = unit1.getNames();
                Names names2 = unit2.getNames();

                if (names1 != null && names2 == null){
                    return false;
                }
                else if (names1 == null && names2 != null){
                    return false;
                }
                else if (names1 != null && names2 != null) {
                    // shortlabel
                    if (names1.getShortLabel() != null && names2.getShortLabel() == null){
                        return false;
                    }
                    else if (names1.getShortLabel() == null && names2.getShortLabel() != null){
                        return false;
                    }
                    else if (names1.getShortLabel() != null && names2.getShortLabel() != null && !names1.getShortLabel().equalsIgnoreCase(names2.getShortLabel())) {
                        return false;
                    }

                    // fullname
                    if (names1.getFullName() != null && names2.getFullName() == null){
                        return false;
                    }
                    else if (names1.getFullName() == null && names2.getFullName() != null){
                        return false;
                    }
                    else if (names1.getFullName() != null && names2.getFullName() != null && !names1.getFullName().equalsIgnoreCase(names2.getFullName())) {
                        return false;
                    }
                }

                // xrefs
                Xref xref1 = unit1.getXref();
                Xref xref2 = unit2.getXref();

                if (xref1 != null && xref2 == null){
                    return false;
                }
                else if (xref1 == null && xref2 != null){
                    return false;
                }
                else if (xref1 != null && xref2 != null) {
                    // primaryRef
                    DbReference ref1 = xref1.getPrimaryRef();
                    DbReference ref2 = xref2.getPrimaryRef();

                    // dbAc
                    if (ref1.getDbAc() != null && ref2.getDbAc() == null){
                        return false;
                    }
                    else if (ref1.getDbAc() == null && ref2.getDbAc() != null){
                        return false;
                    }
                    else if (ref1.getDbAc() != null && ref2.getDbAc() != null && !ref1.getDbAc().equalsIgnoreCase(ref2.getDbAc())) {
                        return false;
                    }

                    // db
                    if (ref1.getDb() != null && ref2.getDb() == null){
                        return false;
                    }
                    else if (ref1.getDb() == null && ref2.getDb() != null){
                        return false;
                    }
                    else if (ref1.getDb() != null && ref2.getDb() != null && !ref1.getDb().equalsIgnoreCase(ref2.getDb())) {
                        return false;
                    }

                    // primaryId
                    if (ref1.getId() != null && ref2.getId() == null){
                        return false;
                    }
                    else if (ref1.getId() == null && ref2.getId() != null){
                        return false;
                    }
                    else if (ref1.getId() != null && ref2.getId() != null && !ref1.getId().equalsIgnoreCase(ref2.getId())) {
                        return false;
                    }

                    // reftype ac
                    if (ref1.getRefTypeAc() != null && ref2.getRefTypeAc() == null){
                        return false;
                    }
                    else if (ref1.getRefTypeAc() == null && ref2.getRefTypeAc() != null){
                        return false;
                    }
                    else if (ref1.getRefTypeAc() != null && ref2.getRefTypeAc() != null && !ref1.getRefTypeAc().equalsIgnoreCase(ref2.getRefTypeAc())) {
                        return false;
                    }

                    // reftype
                    if (ref1.getRefType() != null && ref2.getRefType() == null){
                        return false;
                    }
                    else if (ref1.getRefType() == null && ref2.getRefType() != null){
                        return false;
                    }
                    else if (ref1.getRefType() != null && ref2.getRefType() != null && !ref1.getRefType().equalsIgnoreCase(ref2.getRefType())) {
                        return false;
                    }
                }
            }

            // value
            if (conf1.getValue() != null && conf2.getValue() == null){
                return false;
            }
            else if (conf1.getValue() == null && conf2.getValue() != null){
                return false;
            }
            else if (conf1.getValue() != null && conf2.getValue() != null && !conf1.getValue().equalsIgnoreCase(conf2.getValue())) {
                return false;
            }
        }
        return true;
    }
}