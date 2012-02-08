/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.intact.dataexchange.cvutils;

import org.apache.log4j.Logger;
import org.bbop.dataadapter.DataAdapterException;
import org.obo.datamodel.*;
import org.obo.datamodel.impl.*;
import uk.ac.ebi.intact.dataexchange.cvutils.model.*;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.io.*;
import java.util.*;

import static java.util.Collections.sort;


/**
 * Contains methods to recreate the OBOSession object from a list of CVObjects
 * The CVObject is stripped and a OBOObject is created which is then added
 * to the OBOSession and finally written to an OBO 1.2 file using a DataAdapter
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public class CvExporter {

    //initialize logger
    protected final static Logger log = Logger.getLogger( CvExporter.class );

    private static final String INTACT_ALIAS_IDENTIFIER = "INTACT-alternate";
    private static final String INTACT_SHORTLABEL_IDENTIFIER = "INTACT-short";

    private static final String MI_ALIAS_IDENTIFIER = "PSI-MI-alternate";
    private static final String MI_SHORTLABEL_IDENTIFIER = "PSI-MI-short";

    private static final String MOD_ALIAS_IDENTIFIER = "PSI-MOD-alternate";
    private static final String MOD_SHORTLABEL_IDENTIFIER = "PSI-MOD-label";

    private Map<CvClassIdentifier, OBOClass> cvToOboCache;

    /**
     * Map containing database MI identifier and the ExternalOntologyConfig object which should be use for this database
     */
    private Map<String, ExternalOntologyConfig> cvToOboMapExternalConfig;
    private ExternalOntologyConfig defaultOntologyConfig;

    private OBOClass rootObj;
    private OBOClass tissueObj;
    private OBOClass cellTypeObj;

    private OBOSession oboSession;

    public CvExporter() {
        ObjectFactory objFactory;
        objFactory = new DefaultObjectFactory();
        oboSession = new OBOSessionImpl( objFactory );

        cvToOboCache = new HashMap<CvClassIdentifier,OBOClass>();
        
        defaultOntologyConfig = new IntactOntologyConfig();
        cvToOboMapExternalConfig = new HashMap<String, ExternalOntologyConfig>();
        initializeExternalOntologyConfig();
    }
    
    private void initializeExternalOntologyConfig() {

        cvToOboMapExternalConfig.put(CvDatabase.PSI_MI_MI_REF, new MiOntologyConfig());
        cvToOboMapExternalConfig.put(CvDatabase.PSI_MOD_MI_REF, new ModOntologyConfig());
    }

    /**
     * Converts a list of Cvs to list of OBOObjects and add it to the OBOSession
     *
     * @param allCvs List of all Cvs
     * @return OBOSession objects with all Cvs converted to OBOObject and added to the OBOsession
     */
    public OBOSession convertToOBOSession( List<CvDagObject> allCvs ) {

        List<CvDagObject> allUniqCvs;
        allUniqCvs = allCvs;

        sort( allUniqCvs, new Comparator<CvObject>() {
            public int compare( CvObject o1, CvObject o2 ) {

                String id1;
                if (o1.getIdentifier() != null){
                   id1 = o1.getIdentifier(); 
                }
                else {
                    id1 = CvObjectUtils.getIdentity( o1 );
                }

                String id2;
                if (o2.getIdentifier() != null){
                    id2 = o2.getIdentifier();
                }
                else {
                    id2 = CvObjectUtils.getIdentity( o2 );
                }

                return id1.compareTo( id2 );
            }
        } );

        //group by mis

        Map<String, HashSet<CvDagObject>> cvMapWithParents = groupByMis( allUniqCvs );

        int counter = 1;
        for ( CvDagObject cvDagObj : allUniqCvs ) {

            if ( CvObjectUtils.getIdentity( cvDagObj ) == null && cvDagObj.getIdentifier() == null ) {
                throw new NullPointerException( "No Identifier for the cvObject " + cvDagObj );
            }
            if ( log.isTraceEnabled() ) log.trace( counter + "  " + CvObjectUtils.getIdentity( cvDagObj ) );

            oboSession.addObject( getRootObject() );
            OBOClass oboObj = convertCv2OBO( cvDagObj, cvMapWithParents );
            oboSession.addObject( oboObj );

            counter++;
        }  //end of for

        if ( log.isDebugEnabled() ) {
            log.debug( "Total CvTerms written " + counter );
        }

        addHeaderInfo();
        addFooterInfo();


        return oboSession;
    }

    public void exportToFile(List<? extends CvObject> cvObjects, File oboFileToExport) throws IOException, DataAdapterException {
        OBOSession oboSession = convertToOBOSession((List<CvDagObject>) cvObjects);
        writeOBOFile(oboSession, oboFileToExport);
    }

    protected Map<String, HashSet<CvDagObject>> groupByMis( List<CvDagObject> allCvs ) {

        Map<String, HashSet<CvDagObject>> cvMapWithParents = new HashMap<String, HashSet<CvDagObject>>();
        for ( CvDagObject cvdag : allCvs ) {
            String id;

            if (cvdag.getIdentifier() != null){
                id = cvdag.getIdentifier();
            }
            else {
                id = CvObjectUtils.getIdentity( cvdag );
            }

            if ( cvMapWithParents.get( id ) == null ) {
                cvMapWithParents.put( id, new HashSet<CvDagObject>( cvdag.getParents() ) );
            } else {

                HashSet<CvDagObject> alreadyExisting = cvMapWithParents.get( id );
                alreadyExisting.addAll( cvdag.getParents() );
                cvMapWithParents.put( id, alreadyExisting );
            }


        }
        return cvMapWithParents;
    }

    protected void addObject( OBOClass oboObj ) {
        oboSession.addObject( oboObj );
    }

    /**
     * The OBOFileAdapter writes the OBOSession object in to the given file specified
     *
     * @param oboSession The OBOsession object with all OBOClass instances added to it
     * @param outFile    The OBO file
     * @throws DataAdapterException refer org.bbop.dataadapter.DataAdapterException
     * @throws IOException          refer java.io.IOException
     */
    public void writeOBOFile( OBOSession oboSession, File outFile ) throws DataAdapterException, IOException {

        // HACK: we write to a temp file, because we need to process the file again
        // to remove some double slashes created by the OBO writer.
        File tempFile = File.createTempFile("obofile-", ".obo");

        OboUtils.saveSession( oboSession, tempFile );

        // rewrite the file (part of the hack)
        FileWriter writer = new FileWriter(outFile);

        BufferedReader reader = new BufferedReader(new FileReader(tempFile));
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.contains("regexp")) {
                line = line.replaceAll("\\\\\\\\\"", "\\\\\"");
            }
            writer.write(line+"\n");
        }

        writer.close();

        tempFile.delete();
    }

    private void addHeaderInfo() {
        
        Collection<ExternalOntologyConfig> ontoConfigs = cvToOboMapExternalConfig.values();

        oboSession.setDefaultNamespace( new Namespace( defaultOntologyConfig.getDefaultNamespace() ) );

        oboSession.addPropertyValue(new PropertyValueImpl("auto-generated-by", "IntAct CvExporter"));

        oboSession.addSynonymCategory(new SynonymCategoryImpl(defaultOntologyConfig.getAliasSynonymCategory(), defaultOntologyConfig.getAliasSynonymDefinition()));
        oboSession.addSynonymCategory(new SynonymCategoryImpl(defaultOntologyConfig.getShortLabelSynonymCategory(), defaultOntologyConfig.getShortLabelSynonymDefinition()));

        for (ExternalOntologyConfig conf : ontoConfigs){
            oboSession.addNamespace( new Namespace( conf.getDefaultNamespace() ) );

            oboSession.addSynonymCategory(new SynonymCategoryImpl(conf.getAliasSynonymCategory(), conf.getAliasSynonymDefinition()));
            oboSession.addSynonymCategory(new SynonymCategoryImpl(conf.getShortLabelSynonymCategory(), conf.getShortLabelSynonymDefinition()));
        }    
    }

    private void addFooterInfo() {
        OBOPropertyImpl partOf = new OBOPropertyImpl("part_of");
        partOf.setName("part of");
        partOf.setTransitive(true);

        oboSession.addObject(partOf);
    }

    /**
     * The List contains duplicates as the method itselfAndChildrenAsList adds
     * itself and the children and again the child gets added.
     * This method removes the dubplicates from the list
     *
     * @param allCvs List of all Cvs with duplicates
     * @return Lists of Uniq Cvs
     */
    protected List<CvDagObject> removeCvsDuplicated( List<CvDagObject> allCvs ) {

        HashMap<String, CvDagObject> cvHash = new HashMap<String, CvDagObject>();
        List<CvDagObject> allUniqCvs = new ArrayList<CvDagObject>();
        for ( CvDagObject cvObj : allCvs ) {
            String id;

            if (cvObj.getIdentifier() != null){
                id = cvObj.getIdentifier();
            }
            else {
                id = CvObjectUtils.getIdentity( cvObj );
            }
            cvHash.put( id, cvObj );
        }


        for ( String s : cvHash.keySet() ) {
            CvDagObject cvDagObject = cvHash.get( s );
            allUniqCvs.add( cvDagObject );
        }

        return allUniqCvs;
    }

    /**
     * Converts cvobject to OBOobject
     *
     * @param dagObj           CvObject that needs to be converted to OBOOBject
     * @param cvMapWithParents a HashMap with CvDag object and all its parents as HashSet
     * @return a OBOClass instance
     */
    protected OBOClass convertCv2OBO( CvDagObject dagObj, Map<String, HashSet<CvDagObject>> cvMapWithParents ) {
        final String cvId;

        if (dagObj.getIdentifier() != null){
            cvId = dagObj.getIdentifier();
        }
        else {
           cvId = CvObjectUtils.getIdentity(dagObj);
        }

        if ( cvId == null ) {
            throw new NullPointerException( "Identifier is null" );
        }

        CvClassIdentifier cvClassId = new CvClassIdentifier(cvId, dagObj.getClass());

        if (cvToOboCache.containsKey(cvClassId)) {
            return cvToOboCache.get(cvClassId);
        }

        OBOClass oboObj = new OBOClassImpl( dagObj.getFullName(), cvId);

        cvToOboCache.put(cvClassId, oboObj);

        CvObjectXref identityRef = null;
        
        //assign Xrefs
        Collection<CvObjectXref> xrefs = dagObj.getXrefs();


        for ( CvObjectXref xref : xrefs ) {
            boolean isIdentity = false;
            boolean isSecondary = false;
            CvXrefQualifier qualifier = xref.getCvXrefQualifier();
            CvDatabase database = xref.getCvDatabase();

            if ( qualifier != null &&
                 qualifier.getIdentifier() != null &&
                 qualifier.getIdentifier().equals( CvXrefQualifier.IDENTITY_MI_REF ) &&
                 xref.getPrimaryId() != null && xref.getPrimaryId().equals(cvId)) {
                isIdentity = true;
                identityRef = xref;
            }
            else if(qualifier != null &&
                    qualifier.getIdentifier() != null &&
                    qualifier.getIdentifier().equals( CvXrefQualifier.SECONDARY_AC_MI_REF )){
                isSecondary = true;
            }
            //end if

            if ( !isIdentity && !isSecondary) {

                String dbx = "";

                //check for pubmed

                if ( database != null && database.getShortLabel() != null ) {
                    if ( database.getShortLabel().equals( CvDatabase.PUBMED ) ) {

                        dbx = "PMID";
                    } else {

                        dbx = database.getShortLabel().toUpperCase();
                    }
                }

                Dbxref dbxref = new DbxrefImpl( dbx, xref.getPrimaryId() );
                dbxref.setType( Dbxref.DEFINITION );

                oboObj.addDefDbxref( dbxref );
            }//end if

        } //end for

        ExternalOntologyConfig currentOntologyConfig = defaultOntologyConfig;
        
        
        if (identityRef != null && identityRef.getCvDatabase() != null && identityRef.getCvDatabase().getIdentifier() != null){
            if (cvToOboMapExternalConfig.containsKey(identityRef.getCvDatabase().getIdentifier())){
                currentOntologyConfig = cvToOboMapExternalConfig.get(identityRef.getCvDatabase().getIdentifier());
            }
        }

        oboObj.setNamespace(new Namespace(currentOntologyConfig.getDefaultNamespace()));

        //assign short label

        if ( dagObj.getShortLabel() != null ) {
            Synonym syn = createSynonym( dagObj.getShortLabel(), defaultOntologyConfig.getShortLabelSynonymCategory() );
            oboObj.addSynonym( syn );
        }
        
        //assign def   from Annotations
        Collection<Annotation> annotations = dagObj.getAnnotations();

        String definitionPrefix = "";
        String definitionSuffix = "";
        for ( Annotation annotation : annotations ) {

            if ( annotation.getCvTopic() != null && annotation.getCvTopic().getShortLabel() != null ) {
                CvTopic cvTopic = annotation.getCvTopic();

                if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.DEFINITION ) ) {
                    definitionPrefix = annotation.getAnnotationText();
                } else if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.URL ) ) {
                    definitionSuffix = "\n" + annotation.getAnnotationText();
                } else if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.SEARCH_URL ) ) {
                    String annotationText = "\""+annotation.getAnnotationText()+"\"";
                    oboObj.addPropertyValue(new PropertyValueImpl("xref", CvTopic.SEARCH_URL+": "+annotationText));
                } else if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.XREF_VALIDATION_REGEXP ) ) {
                    String annotationText = "\\\""+annotation.getAnnotationText()+"\\\"";
                    oboObj.addPropertyValue(new PropertyValueImpl("xref", CvTopic.XREF_VALIDATION_REGEXP+":"+annotationText));
                } else if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.COMMENT ) ) {
                    oboObj.setComment( removeLineReturns(annotation.getAnnotationText() ));
                } else if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.OBSOLETE ) ) {
                    oboObj.setObsolete( true );
                    definitionSuffix = "\n" + annotation.getAnnotationText();
                }
            } //end if
        }//end for
        oboObj.setDefinition( definitionPrefix + definitionSuffix );
        //assign alias

        for ( CvObjectAlias cvAlias : dagObj.getAliases() ) {
            Synonym altSyn = createAlias( cvAlias, currentOntologyConfig.getAliasSynonymCategory() );
            oboObj.addSynonym( altSyn );

        }

        //add children and parents
        //check if root

        if ( checkIfRootMI(cvId) ) {
            addLinkToRootFor(oboObj);
        }

        Set<CvDagObject> cvParents = cvMapWithParents.get( dagObj.getIdentifier() );


        if ( cvParents != null ) {
            for ( CvDagObject cvParentObj : cvParents ) {

                OBOClass isA = convertCv2OBO( cvParentObj, cvMapWithParents );
                addChild(isA, oboObj);
            }//end for
        }

        if (dagObj instanceof CvTissue && !"IAX:0001".equals(dagObj.getIdentifier())) {
            addChild(getOBOTissue(), oboObj);
        } else if (dagObj instanceof CvCellType && !"IAX:0002".equals(dagObj.getIdentifier())) {
            addChild(getOBOCellType(), oboObj);
        }

        return oboObj;
    }//end method

    private void addChild(OBOClass parent, OBOClass child) {
        Link linkToIsA = new OBORestrictionImpl(child);
        linkToIsA.setType( OBOProperty.IS_A );
        parent.addChild( linkToIsA );
    }

    private void addLinkToRootFor(OBOClass oboObj) {
        OBOClass rootObject = getRootObject();
        Link linkToRoot = new OBORestrictionImpl( oboObj );
        OBOProperty oboProp = new OBOPropertyImpl( "part_of" );
        linkToRoot.setType( oboProp );
        rootObject.addChild( linkToRoot );
    }

    private String removeLineReturns(String annotationText) {
        return annotationText.replaceAll("\r\n", " ");
    }

    private OBOClass getRootObject() {
        if (rootObj != null) {
            return rootObj;
        }

        /*
          [Term]
          id: MI:0000
          name: molecular interaction
          def: "Controlled vocabularies originally created for protein protein interactions, extended to other molecules interactions." [PMID:14755292]
          subset: Drugable
          subset: PSI-MI slim
          synonym: "mi" EXACT PSI-MI-short []
        */

        rootObj = new OBOClassImpl( "molecular interaction", "MI:0000" );
        rootObj.setDefinition( "Controlled vocabularies originally created for protein protein interactions, extended to other molecules interactions." );
        //[PMID:14755292]"
        Dbxref dbxref = new DbxrefImpl( "PMID", "14755292" );
        dbxref.setType( Dbxref.DEFINITION );
        rootObj.addDefDbxref( dbxref );

        addPsimiShortSyn(rootObj, "mi");

        return rootObj;
    }//end of method

    private void addPsimiShortSyn(OBOClass oboObj, String synonym) {
        Synonym syn = new SynonymImpl();
        syn.setText( synonym );
        SynonymCategory synCat = new SynonymCategoryImpl();
        synCat.setID(INTACT_SHORTLABEL_IDENTIFIER);
        syn.setSynonymCategory( synCat );
        syn.setScope( 1 );
        oboObj.addSynonym( syn );
    }

    public OBOClass getOBOTissue() {
        if (tissueObj != null) {
            return tissueObj;
        }

        tissueObj = new OBOClassImpl("tissue", "IAX:0001");
        tissueObj.setDefinition("IntAct tissue base object");

        addLinkToRootFor(tissueObj);

        oboSession.addObject(tissueObj);

        return tissueObj;
    }

    public OBOClass getOBOCellType() {
        if (cellTypeObj != null) {
            return cellTypeObj;
        }

        cellTypeObj = new OBOClassImpl("cell type", "IAX:0002");
        cellTypeObj.setDefinition("IntAct cell type base object");

        addLinkToRootFor(cellTypeObj);

        oboSession.addObject(cellTypeObj);

        return cellTypeObj;
    }

    private boolean checkIfRootMI( String mi ) {
        for ( String s : CvObjectOntologyBuilder.mi2Class.keySet() ) {
            if ( mi.equalsIgnoreCase( s ) ) {
                return true;
            } //end if
        }//end for
        return false;
    }

    private Synonym createAlias( CvObjectAlias cvAlias, String aliasId ) {
        Synonym syn = new SynonymImpl();
        syn.setText( cvAlias.getName() );
        SynonymCategory synCat = new SynonymCategoryImpl();
        synCat.setID(aliasId);
        syn.setSynonymCategory( synCat );
        syn.setScope( 1 );
        return syn;
    }

    private Synonym createSynonym( String shortLabel, String shortLabelId ) {
        Synonym syn = new SynonymImpl();
        syn.setText( shortLabel );
        SynonymCategory synCat = new SynonymCategoryImpl();
        synCat.setID(shortLabelId);
        syn.setSynonymCategory( synCat );
        syn.setScope( 1 );
        return syn;
    }

    protected OBOSession getOboSession() {
        return oboSession;
    }

    private static class CvClassIdentifier {

        private String identifier;
        private Class<? extends CvObject> cvClass;

        private CvClassIdentifier(String identifier, Class<? extends CvObject> cvClass) {
            this.identifier = identifier;
            this.cvClass = cvClass;
        }

        public String getIdentifier() {
            return identifier;
        }

        public Class<? extends CvObject> getCvClass() {
            return cvClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CvClassIdentifier that = (CvClassIdentifier) o;

            if (!cvClass.equals(that.cvClass)) return false;
            if (!identifier.equals(that.identifier)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = identifier.hashCode();
            result = 31 * result + cvClass.hashCode();
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(identifier).append('(');
            sb.append(cvClass.getSimpleName());
            sb.append(')');
            return sb.toString();
        }

    }
}
