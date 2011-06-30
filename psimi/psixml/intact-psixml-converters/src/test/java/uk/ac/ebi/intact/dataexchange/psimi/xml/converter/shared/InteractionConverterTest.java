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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.model.Interaction;
import psidev.psi.mi.xml.model.Parameter;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterMessage;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Confidence;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Range;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.util.XrefUtils;
import uk.ac.ebi.intact.util.psivalidator.PsiValidator;
import uk.ac.ebi.intact.util.psivalidator.PsiValidatorReport;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * InteractionConverter Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionConverterTest extends AbstractConverterTest {

    @Test
    public void psiToIntact_default() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        uk.ac.ebi.intact.model.Interaction interaction = converter.psiToIntact(psiInteraction);

        Assert.assertNotNull(interaction.getCvInteractionType());
        Assert.assertNotNull(interaction.getCvInteractorType());
        Assert.assertNotNull(interaction.getComponents().iterator().next().getInteractor().getOwner());
        Assert.assertEquals("testInstitution", interaction.getComponents().iterator().next().getInteractor().getOwner().getShortLabel());
        Assert.assertEquals( 1, interaction.getConfidences().size());

        Confidence conf = interaction.getConfidences().iterator().next();
        Assert.assertNotNull( conf.getCvConfidenceType());
        Assert.assertEquals("intact conf score", conf.getCvConfidenceType().getShortLabel());
        Assert.assertEquals( "0.8", conf.getValue());
        Assert.assertEquals( interaction, conf.getInteraction());

        Assert.assertEquals(1, interaction.getParameters().size());
        final InteractionParameter param = interaction.getParameters().iterator().next();
        Assert.assertEquals("temperature of inter", param.getCvParameterType().getShortLabel());
        Assert.assertEquals("MI:0836", param.getCvParameterType().getIdentifier());
        Assert.assertEquals("kelvin", param.getCvParameterUnit().getShortLabel());
        Assert.assertEquals("MI:0838", param.getCvParameterUnit().getIdentifier());
        Assert.assertEquals(275d, param.getFactor(), 0d);
    }

    @Test
    public void psiToIntact_imexXref() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();
        psiInteraction.setImexId("IM-0000");

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        uk.ac.ebi.intact.model.Interaction interaction = converter.psiToIntact(psiInteraction);

        Xref imexXref = null;

        for (Xref xref : interaction.getXrefs()) {
            if (CvDatabase.IMEX_MI_REF.equals(xref.getCvDatabase().getIdentifier())) {
                if (imexXref != null) {
                    Assert.fail("More than one IMEx xrefs found");
                }
                imexXref = xref;
            }
        }

        Assert.assertNotNull(imexXref);
        Assert.assertEquals("IM-0000", imexXref.getPrimaryId());
        Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, imexXref.getCvXrefQualifier().getIdentifier());
    }

    @Test
    public void psiToIntact_imexXref_redundant() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();
        psiInteraction.getXref().getSecondaryRef().add(PsiMockFactory.createDbReferenceDatabaseOnly("IM-0000", CvDatabase.IMEX_MI_REF, CvDatabase.IMEX));
        psiInteraction.setImexId("IM-0000");

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        uk.ac.ebi.intact.model.Interaction interaction = converter.psiToIntact(psiInteraction);

        Xref imexXref = null;

        for (Xref xref : interaction.getXrefs()) {
            if (CvDatabase.IMEX_MI_REF.equals(xref.getCvDatabase().getIdentifier())) {
                if (imexXref != null) {
                    Assert.fail("More than one IMEx xrefs found");
                }
                imexXref = xref;
            }
        }

        Assert.assertNotNull(imexXref);
        Assert.assertEquals("IM-0000", imexXref.getPrimaryId());
    }

    @Test (expected = PsiConversionException.class)
    public void psiToIntact_noInteractionType() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();
        psiInteraction.getInteractionTypes().clear();

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        uk.ac.ebi.intact.model.Interaction interaction = converter.psiToIntact(psiInteraction);
    }

    @Test
    public void psiToIntact_noPartDetMethodInExp() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();

        for (ExperimentDescription expDesc : psiInteraction.getExperiments()) {
            expDesc.setParticipantIdentificationMethod(null);
        }

        for (Participant part : psiInteraction.getParticipants()) {
            part.getParticipantIdentificationMethods().clear();
            part.getParticipantIdentificationMethods().add(PsiMockFactory.createCvType(ParticipantIdentificationMethod.class, CvIdentification.PREDETERMINED_MI_REF, CvIdentification.PREDETERMINED));
        }

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        uk.ac.ebi.intact.model.Interaction interaction = converter.psiToIntact(psiInteraction);

        Assert.assertNotNull(interaction.getCvInteractionType());

        for (Experiment exp : interaction.getExperiments()) {
            Assert.assertNotNull(exp.getCvIdentification());
            Assert.assertEquals(CvIdentification.PREDETERMINED_MI_REF, exp.getCvIdentification().getIdentifier());
        }
    }
    
    @Test
    public void psiToIntact_partDetMethodInExp() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();

        for (ExperimentDescription expDesc : psiInteraction.getExperiments()) {
            expDesc.setParticipantIdentificationMethod(PsiMockFactory.createCvType(ParticipantIdentificationMethod.class, "MI:0000", "hello"));
        }

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        uk.ac.ebi.intact.model.Interaction interaction = converter.psiToIntact(psiInteraction);

        Assert.assertNotNull(interaction.getCvInteractionType());

        for (Experiment exp : interaction.getExperiments()) {
            Assert.assertNotNull(exp.getCvIdentification());
            Assert.assertEquals("MI:0000", exp.getCvIdentification().getIdentifier());
        }
    }

    @Test
    public void psiToIntact_noPartDetMethodInExp_differentDetMethods() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();

        for (ExperimentDescription expDesc : psiInteraction.getExperiments()) {
            expDesc.setParticipantIdentificationMethod(null);
        }

        int i=0;
        for (Participant part : psiInteraction.getParticipants()) {
            part.getParticipantIdentificationMethods().clear();

            if (i == 0) part.getParticipantIdentificationMethods().add(PsiMockFactory.createCvType(ParticipantIdentificationMethod.class, "MI:0427", "mass spectrometry"));
            if (i == 1) part.getParticipantIdentificationMethods().add(PsiMockFactory.createCvType(ParticipantIdentificationMethod.class, "MI:0421", "identification by antibody"));
            i++;
        }

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        uk.ac.ebi.intact.model.Interaction interaction = converter.psiToIntact(psiInteraction);

        Assert.assertEquals("MI:0661", interaction.getExperiments().iterator().next().getCvIdentification().getIdentifier());
    }

    @Test
    public void psiToIntact_noPartDetMethodInExp_norInParticipants() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();

        for (ExperimentDescription expDesc : psiInteraction.getExperiments()) {
            expDesc.setParticipantIdentificationMethod(null);
        }

        int i=0;
        for (Participant part : psiInteraction.getParticipants()) {
            part.getParticipantIdentificationMethods().clear();
        }

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        uk.ac.ebi.intact.model.Interaction interaction = converter.psiToIntact(psiInteraction);

        Assert.assertEquals("MI:0661", interaction.getExperiments().iterator().next().getCvIdentification().getIdentifier());
    }

    @Test
    public void psiToIntact_fixSourceReferenceXrefs() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();

        final DbReference dbRef = PsiMockFactory.createDbReference(CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF, CvDatabase.PSI_MI, CvDatabase.PSI_MI_MI_REF);
//        final DbReference dbRef = PsiMockFactory.createDbReference(CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF, CvDatabase.DIP, CvDatabase.DIP_MI_REF);
        dbRef.setId(CvDatabase.DIP_MI_REF);
        psiInteraction.getXref().getSecondaryRef().clear();
        psiInteraction.getXref().setPrimaryRef(dbRef);

        final Institution dip = new Institution(Institution.DIP);
        dip.addXref(XrefUtils.createIdentityXrefPsiMi(dip, Institution.DIP_REF));

        InteractionConverter converter = new InteractionConverter(dip);
        uk.ac.ebi.intact.model.Interaction interaction = converter.psiToIntact(psiInteraction);

        Assert.assertEquals(1, interaction.getXrefs().size());
        Assert.assertEquals(CvXrefQualifier.SOURCE_REFERENCE_MI_REF, interaction.getXrefs().iterator().next().getCvXrefQualifier().getIdentifier());

        Assert.assertEquals(2, ConverterContext.getInstance().getReport().getMessages().size());
    }

    @Test
    public void psiToIntact_fixSourceReferenceXrefs2() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();

        psidev.psi.mi.xml.model.Xref sourceMiRef = PsiMockFactory.createPsiMiXref();
        sourceMiRef.getPrimaryRef().setId(CvDatabase.DIP_MI_REF);

        Entry entry = PsiMockFactory.createMockEntry();
        entry.getSource().setXref(sourceMiRef);
        entry.getInteractions().clear();
        entry.getExperiments().clear();
        entry.getInteractors().clear();

        entry.getInteractions().add(psiInteraction);

        final DbReference dbRef = PsiMockFactory.createDbReference(CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF, CvDatabase.PSI_MI, CvDatabase.PSI_MI_MI_REF);
        dbRef.setId(CvDatabase.DIP_MI_REF);
        psiInteraction.getXref().getSecondaryRef().clear();
        psiInteraction.getXref().setPrimaryRef(dbRef);

        final Institution dip = new Institution(Institution.DIP);
        dip.addXref(XrefUtils.createIdentityXrefPsiMi(dip, Institution.DIP_REF));

        EntryConverter converter = new EntryConverter();
        IntactEntry ientry = converter.psiToIntact(entry);

        for (ConverterMessage msg : ConverterContext.getInstance().getReport().getMessages()) {
//            System.out.println(msg);
        }

        Assert.assertEquals(2, ConverterContext.getInstance().getReport().getMessages().size());
    }



    @Test
    public void psiToIntact_linkedFeatures() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction(2);

        Iterator<Participant> iterator = psiInteraction.getParticipants().iterator();
        Participant participantFirst = iterator.next();
        Participant participantSec = iterator.next();

        participantFirst.getFeatures().clear();
        participantSec.getFeatures().clear();

        psidev.psi.mi.xml.model.Feature featureFirst = PsiMockFactory.createFeature();
        psidev.psi.mi.xml.model.Feature featureSec = PsiMockFactory.createFeature();

        participantFirst.getFeatures().add(featureFirst);
        participantSec.getFeatures().add(featureSec);

        InferredInteraction inferredInteraction = new InferredInteraction();
        inferredInteraction.getParticipant().add(new InferredInteractionParticipant(featureFirst));
        inferredInteraction.getParticipant().add(new InferredInteractionParticipant(featureSec));

        psiInteraction.getInferredInteractions().add(inferredInteraction);

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        uk.ac.ebi.intact.model.Interaction intactInteraction = converter.psiToIntact(psiInteraction);

        Assert.assertEquals(2, intactInteraction.getComponents().size());

        Iterator<Component> iterator1 = intactInteraction.getComponents().iterator();
        Component compFirst = iterator1.next();
        Component compSec = iterator1.next();

        Assert.assertEquals(1, compFirst.getFeatures().size());
        Assert.assertEquals(1, compSec.getFeatures().size());

        Feature intactFeatureFirst = compFirst.getFeatures().iterator().next();
        Feature intactFeatureSec = compSec.getFeatures().iterator().next();

        Assert.assertTrue(intactFeatureFirst.getBoundDomain().equals(intactFeatureSec));
        Assert.assertTrue(intactFeatureSec.getBoundDomain().equals(intactFeatureFirst));
    }

    @Test
    public void psiToIntact_linkedFeatures_readFromFile() throws Exception {
        PsimiXmlReader reader = new PsimiXmlReader( PsimiXmlVersion.VERSION_253 );
        URL url = this.getClass().getResource("/xml/14729917_simplified.xml");
        EntrySet entrySet = reader.read(new File(url.getFile()));

        EntryConverter entryConverter = new EntryConverter();
        IntactEntry intactEntry = entryConverter.psiToIntact(entrySet.getEntries().iterator().next());

        uk.ac.ebi.intact.model.Interaction intactInteraction = intactEntry.getInteractions().iterator().next();
        Assert.assertEquals(2, intactInteraction.getComponents().size());

        Iterator<Component> iterator1 = intactInteraction.getComponents().iterator();
        Component compFirst = iterator1.next();
        Component compSec = iterator1.next();

        Assert.assertEquals(1, compFirst.getFeatures().size());
        Assert.assertEquals(1, compSec.getFeatures().size());

        Feature intactFeatureFirst = compFirst.getFeatures().iterator().next();
        Feature intactFeatureSec = compSec.getFeatures().iterator().next();

        Assert.assertTrue(intactFeatureFirst.getBoundDomain().equals(intactFeatureSec));
        Assert.assertTrue(intactFeatureSec.getBoundDomain().equals(intactFeatureFirst));
    }

    @Test
    public void intactTopsi_default() throws Exception {
        uk.ac.ebi.intact.model.Interaction intactInteraction = new IntactMockBuilder().createDeterministicInteraction();

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        Interaction psiInteraction = converter.intactToPsi( intactInteraction);

        Assert.assertEquals( 1, psiInteraction.getConfidences().size());
        Assert.assertNotNull( psiInteraction.getConfidences().iterator().next().getUnit());
        Assert.assertEquals( intactInteraction.getConfidences().iterator().next().getValue(),  psiInteraction.getConfidences().iterator().next().getValue());
        Assert.assertEquals( intactInteraction.getConfidences().iterator().next().getCvConfidenceType().getShortLabel(), psiInteraction.getConfidences().iterator().next().getUnit().getNames().getShortLabel());

        Assert.assertEquals(1, psiInteraction.getParameters().size());
        Parameter param = psiInteraction.getParameters().iterator().next();
        Assert.assertEquals(302d, param.getFactor(), 0d);
        Assert.assertEquals("temperature", param.getTerm());
        Assert.assertEquals("MI:0836", param.getTermAc());
        Assert.assertEquals("kelvin", param.getUnit());
        Assert.assertEquals("MI:0838", param.getUnitAc());
    }

    @Test
    public void intactToPsi_ac_intactOrMint() throws Exception {
        uk.ac.ebi.intact.model.Interaction intactInteraction = new IntactMockBuilder().createDeterministicInteraction();
        Assert.assertEquals( 0, intactInteraction.getXrefs().size() );
        intactInteraction.setAc("EBI-12345");

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        Interaction psiInteraction = converter.intactToPsi( intactInteraction);

        Assert.assertNotNull(psiInteraction.getXref());
        Assert.assertEquals(Institution.INTACT_REF, psiInteraction.getXref().getPrimaryRef().getDbAc());
    }

    @Test
    public void intactToPsi_ac_other() throws Exception {
        uk.ac.ebi.intact.model.Interaction intactInteraction = new IntactMockBuilder().createDeterministicInteraction();
        intactInteraction.setAc("OTHER-12345");

        ConverterContext.getInstance().setDefaultInstitutionForAcs( new Institution("intact") );

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        Interaction psiInteraction = converter.intactToPsi( intactInteraction);

        Assert.assertNotNull(psiInteraction.getXref());
        Assert.assertEquals("intact", psiInteraction.getXref().getPrimaryRef().getDb());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void intactTopsi_linkedFeatures() throws Exception {
        TransactionStatus status = getDataContext().beginTransaction();

        IntactMockBuilder intactMockBuilder = new IntactMockBuilder();

        uk.ac.ebi.intact.model.Interaction intactInteraction = intactMockBuilder.createDeterministicInteraction();

        Iterator<Component> componentIterator = intactInteraction.getComponents().iterator();
        Component firstComponent = componentIterator.next();
        Component secComponent = componentIterator.next();

        firstComponent.getFeatures().clear();
        secComponent.getFeatures().clear();

        Feature feature = createFeature("feature1", firstComponent, 1, 1,5, 5, intactMockBuilder);
        Feature feature2 = createFeature("feature2", secComponent, 1, 3,5, 5, intactMockBuilder);
        Feature feature3 = createFeature("feature3", firstComponent, 1, 1,4, 5, intactMockBuilder);
        Feature feature4 = createFeature("feature4", secComponent, 1, 1,5, 10, intactMockBuilder);


        feature.setBoundDomain(feature2);
        feature2.setBoundDomain(feature);

        feature3.setBoundDomain(feature4);
        feature4.setBoundDomain(feature3);


        firstComponent.getFeatures().add(feature);
        firstComponent.getFeatures().add(feature3);
        secComponent.getFeatures().add(feature2);
        secComponent.getFeatures().add(feature4);

        getCorePersister().saveOrUpdate(intactInteraction);
        getDataContext().commitTransaction(status);

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        Interaction psiInteraction = converter.intactToPsi( intactInteraction);

        Assert.assertEquals(2, psiInteraction.getInferredInteractions().size());

        InferredInteractionParticipant iParticipant = new InferredInteractionParticipant((psidev.psi.mi.xml.model.Feature) ConversionCache.getElement(feature));
        InferredInteractionParticipant iParticipant2 = new InferredInteractionParticipant((psidev.psi.mi.xml.model.Feature) ConversionCache.getElement(feature2));

        InferredInteractionParticipant iParticipant3 = new InferredInteractionParticipant((psidev.psi.mi.xml.model.Feature) ConversionCache.getElement(feature3));
        InferredInteractionParticipant iParticipant4 = new InferredInteractionParticipant((psidev.psi.mi.xml.model.Feature) ConversionCache.getElement(feature4));

        for(InferredInteraction interaction : psiInteraction.getInferredInteractions()){

            Assert.assertEquals(2, interaction.getParticipant().size());

            // test if feature is linked with feature2 and feature3 is linked with feature4
            Assert.assertTrue((interaction.getParticipant().contains(iParticipant) && interaction.getParticipant().contains(iParticipant2)) ||
                              (interaction.getParticipant().contains(iParticipant3) && interaction.getParticipant().contains(iParticipant4)));
        }
    }

    @Test
    public void intactTopsi_linkedFeatures_writeFile() throws Exception {
        PsimiXmlReader reader = new PsimiXmlReader( PsimiXmlVersion.VERSION_253 );
        URL url = this.getClass().getResource("/xml/14729917_simplified.xml");
        EntrySet entrySet = reader.read(new File(url.getFile()));

        EntryConverter entryConverter = new EntryConverter();
        IntactEntry intactEntry = entryConverter.psiToIntact(entrySet.getEntries().iterator().next());

        Entry psiEntry = entryConverter.intactToPsi(intactEntry);

        EntrySet psiEntrySet = new EntrySet(Arrays.asList(psiEntry), 2, 5, 3);
        PsimiXmlWriter writer = new PsimiXmlWriter(PsimiXmlVersion.VERSION_254);

        String xml = writer.getAsString(psiEntrySet);
    }

    private Feature createFeature(String name, Component component, int fromIntervalStart, int fromIntervalEnd,
                                  int toIntervalStart, int toIntervalEnd, IntactMockBuilder intactMockBuilder){
        CvFeatureType featureType = intactMockBuilder.createCvObject(CvFeatureType.class, CvFeatureType.EXPERIMENTAL_FEATURE_MI_REF, CvFeatureType.EXPERIMENTAL_FEATURE);
        Feature feature = new Feature(name, component, featureType);

        Range range = intactMockBuilder.createRange(fromIntervalStart, fromIntervalEnd, toIntervalStart, toIntervalEnd);
        feature.addRange(range);
        return feature;
    }

    @Test
    public void roundtrip_baitPraySameInteractor() throws Exception {
        Protein prot = getMockBuilder().createProteinRandom();
        Protein protCopy = new IntactCloner().clone(prot);

        getCorePersister().saveOrUpdate(protCopy);

        Component bait = getMockBuilder().createComponentBait(prot);
        Component prey = getMockBuilder().createComponentPrey(prot);

        uk.ac.ebi.intact.model.Interaction intactInteraction = getMockBuilder().createInteraction(bait, prey);

        Assert.assertEquals(2, intactInteraction.getComponents().size());
        Assert.assertEquals(1, getDaoFactory().getProteinDao().countAll());

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        Interaction psiInteraction = converter.intactToPsi( intactInteraction);

        Assert.assertEquals(2, psiInteraction.getParticipants().size());

        uk.ac.ebi.intact.model.Interaction reInteraction = converter.psiToIntact(psiInteraction);

        Assert.assertEquals(2, reInteraction.getComponents().size());

        getCorePersister().saveOrUpdate(reInteraction);

        Assert.assertEquals(1, getDaoFactory().getInteractionDao().countAll());
        Assert.assertEquals(1, getDaoFactory().getProteinDao().countAll());
        Assert.assertEquals(2, getDaoFactory().getComponentDao().countAll());
    }

    @Test
    public void roudtrip() throws Exception {

        File file = new File( InteractionConverterTest.class.getResource( "/xml/17845854.xml" ).getFile());
        Assert.assertNotNull( file );

        final EntrySet entrySet = new PsimiXmlReader().read( file );
        Assert.assertNotNull( entrySet );
        Assert.assertEquals( 1, entrySet.getEntries().size() );
        final Entry entry = entrySet.getEntries().iterator().next();

        EntryConverter entryConverter = new EntryConverter( );
        final IntactEntry intactEntry = entryConverter.psiToIntact( entry );
        final Entry roundtripedEntry = entryConverter.intactToPsi( intactEntry );

        Assert.assertNotNull( roundtripedEntry );
        Assert.assertEquals( 0, roundtripedEntry.getExperiments().size() );
        Assert.assertEquals( 0, roundtripedEntry.getInteractors().size() );
        Assert.assertEquals( 1, roundtripedEntry.getInteractions().size() );
        Interaction roundtripedInteraction = roundtripedEntry.getInteractions().iterator().next();
        Assert.assertEquals( 1, roundtripedInteraction.getExperiments().size() );
        Assert.assertEquals( 2, roundtripedInteraction.getParticipants().size() );

        Participant smp = getParticipantByMoleculeType( roundtripedInteraction, "small molecule", "MI:0328" );
        Assert.assertNotNull( smp );

        Assert.assertEquals( 1, smp.getNames().getAliases().size() );
        Assert.assertTrue( smp.hasXref() );
        Assert.assertEquals( "EBI-999", smp.getXref().getPrimaryRef().getId() );
        
        Assert.assertEquals( 0, roundtripedInteraction.getAttributes().size() );

        // TODO check with Anna that we actually need that feature before updating intact-core (major work!)
        // confidence values
//        Assert.assertEquals( 1, roundtripedInteraction.getConfidences().size() );
    }

    private Participant getParticipantByMoleculeType( Interaction interaction, String moleculeType, String moleculeTypeAc ) {
        for ( Participant p : interaction.getParticipants() ) {
            final InteractorType type = p.getInteractor().getInteractorType();
            if( ( moleculeType != null && moleculeType.equals( type.getNames().getShortLabel() ) )
                ||
                ( moleculeTypeAc != null && moleculeTypeAc.equals( type.getXref().getPrimaryRef().getId() ) ) ) {
                return p;
            }
        }
        return null;
    }
}