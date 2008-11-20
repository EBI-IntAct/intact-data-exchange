package uk.ac.ebi.intact.psimitab;

import org.junit.*;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.PsimiXmlWriterException;

import java.util.Collection;
import java.util.Iterator;

/**
 * IntactTab2Xml Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @since 2.0.2
 * @version $Id$
 */
public class IntactTab2XmlTest {

    @Test
    public void convert() throws Exception {
        IntactPsimiTabReader reader = new IntactPsimiTabReader( true );
        final Collection<BinaryInteraction> mitabInteractions =
                reader.read( IntactTab2XmlTest.class.getResourceAsStream( "/mitab_samples/sample_dgi.tsv" ) );

        IntactTab2Xml tab2xml = new IntactTab2Xml();
        final EntrySet entrySet = tab2xml.convert( mitabInteractions );

        Assert.assertNotNull( entrySet );
        Assert.assertEquals(1, entrySet.getEntries().size());
        final Entry entry = entrySet.getEntries().iterator().next();

        final Collection<Interaction> interactions = entry.getInteractions();
        Assert.assertNotNull( interactions );
        Assert.assertEquals( 2, interactions.size() );

        for ( Interaction interaction : interactions ) {
            for ( Participant participant : interaction.getParticipants() ) {
                final Interactor interactor = participant.getInteractor();
                Assert.assertFalse( interactor.getId() == 0 );

                if( "CHEBI:45906".equals( interactor.getNames().getShortLabel() ) ) {
                    Assert.assertEquals( 15, interactor.getAttributes().size() );
                }
            }
        }
    }

    @Test
    public void convert_imatinib() throws Exception {
        IntactPsimiTabReader reader = new IntactPsimiTabReader( true );
        final Collection<BinaryInteraction> mitabInteractions =
                reader.read( IntactTab2XmlTest.class.getResourceAsStream( "/mitab_samples/imatinib_full.txt" ) );

        IntactTab2Xml tab2xml = new IntactTab2Xml();
        final EntrySet entrySet = tab2xml.convert( mitabInteractions );
        //printXml(entrySet);
        
        Assert.assertNotNull( entrySet );
        Assert.assertEquals(1, entrySet.getEntries().size());
        final Entry entry = entrySet.getEntries().iterator().next();
        Assert.assertNotNull( entry );

        // check on the count of interactor's xrefs
        Assert.assertEquals( 11, entry.getInteractions().size() );

        Collection<Interaction> interactions = entry.getInteractions();
        Assert.assertEquals( 2, interactions.iterator().next().getParticipants().size() );

        //small molecules
        Interactor sm = getInteractorByTypeAndLabel( interactions.iterator().next(), "MI:0328","5291");
        Assert.assertEquals( 8, sm.getXref().getAllDbReferences().size() );

        //protein
        Interactor p = getInteractorByShortLabel(interactions.iterator().next(),"P08183");
        Assert.assertEquals( 17, p.getXref().getAllDbReferences().size() );

       /* for ( Interaction interaction : entry.getInteractions() ) {
            Assert.assertEquals( 2, interaction.getParticipants().size() );

            Interactor sm = getInteractorByType( interaction, "MI:0328" );
            Assert.assertEquals( 3, sm.getXref().getAllDbReferences().size() );

            // all proteins have a uniprot and an intact AC.
            Interactor p = getInteractorByType( interaction, "MI:0326" );
            Assert.assertEquals( 2, p.getXref().getAllDbReferences().size() );
        }*/
    }

    private Interactor getInteractorByType( Interaction interaction, String typeMi ) {
        for ( Participant participant : interaction.getParticipants() ) {
            final Interactor interactor = participant.getInteractor();
            if( interactor.getInteractorType().getXref().getPrimaryRef().getId().equals( typeMi ) ) {
                return interactor;
            }
        }
        return null;
    }

     private Interactor getInteractorByTypeAndLabel( Interaction interaction, String typeMi,String shortLabel ) {
        for ( Participant participant : interaction.getParticipants() ) {
            final Interactor interactor = participant.getInteractor();
            if( interactor.getInteractorType().getXref().getPrimaryRef().getId().equals( typeMi ) && interactor.getNames().getShortLabel().equals(shortLabel) ) {
                return interactor;
            }
        }
        return null;
    }

     private Interactor getInteractorByShortLabel( Interaction interaction, String shortLabel ) {
        for ( Participant participant : interaction.getParticipants() ) {
            final Interactor interactor = participant.getInteractor();
            if( interactor.getNames().getShortLabel().equals( shortLabel ) ) {
                return interactor;
            }
        }
        return null;
    }

    private void printXml( EntrySet entrySet ) throws PsimiXmlWriterException {
        PsimiXmlWriter w = new PsimiXmlWriter();
        final String xml = w.getAsString( entrySet );
        System.out.println( xml );
    }

    private void printLine(String mitabLine){
        int column = 0;
        for(String s: mitabLine.split("\t")){
            System.out.println(column + "  "+ s);
            column++;
        }
    }

    @Test
    public void testMitabLineofDgi() throws Exception{
        //Test if the number of PropertiesA (which also includes identifiers) and PropertiesB(+identifiers)
        //equals the number of xrefs of the Interactors after conversion from mitab2xml


        //all the identifiers repeated in properties
        final String imatinibLine = "pubchem:5291|drugbank:DB00619|intact:DGI-337878\tuniprotkb:P08183|intact:DGI-296810\tintact:Imatinib Mesylate(drug brand name)|intact:Imatinib Methansulfonate(drug brand name)|intact:Imatinib(commercial name)|intact:Gleevec(drug brand name)|intact:Glivec(drug brand name)\tuniprotkb:MDR1(gene name synonym)|uniprotkb:PGY1(gene name synonym)|uniprotkb:ATP-binding cassette sub-family B member 1(gene name synonym)|uniprotkb:P-glycoprotein 1(gene name synonym)|uniprotkb:mdr1_human\t-\tuniprotkb:ABCB1\tpsi-mi:\"MI:0045\"(experimental interac)\t-\tpubmed:18048412\ttaxid:-3(unknown)\ttaxid:9606(human)\tpsi-mi:\"MI:0407\"(direct interaction)\tpsi-mi:\"MI:1002\"(DrugBank)\tintact:DGI-337971\t-\tpsi-mi:\"MI:1094\"(drug)\tpsi-mi:\"MI:1095\"(drug target)\tpsi-mi:\"MI:0499\"(unspecified role)\tpsi-mi:\"MI:0499\"(unspecified role)\tdrugbank:APRD01028|pubmed:12869662|pubmed:11175855|pubmed:16779792|pubmed:14988091|pubmed:15980865|din:02253283|pubchem:5291|drugbank:DB00619|intact:DGI-337878\tuniprotkb:P08183|intact:DGI-296810|ensembl:ENSG00000085563|refseq:NP_000918.2|go:\"GO:0009986\"(cell surface)|go:\"GO:0016021\"(integral to membrane)|go:\"GO:0005624\"(membrane fraction)|go:\"GO:0005524\"(ATP binding)|go:\"GO:0042626\"(ATPase activity, coupled to transmembrane movement of substances)|go:\"GO:0005515\"(protein binding)|go:\"GO:0042493\"(response to drug)|go:\"GO:0006810\"(transport)|interpro:IPR003593(AAA+ ATPase, core)|interpro:IPR011527(ABC transporter, transmembrane region, type 1)|interpro:IPR001140(ABC transporter, transmembrane region)|interpro:IPR003439(ABC transporter-like)|uniprotkb:Q12755|uniprotkb:Q14812\tpsi-mi:\"MI:0328\"(small molecule)\tpsi-mi:\"MI:0326\"(protein)\ttaxid:-3(unknown)\t-\tDrugBank - a knowledgebase for drugs, drug actions and drug targets.\tbiotech prep:\"Imatinib is a drug used to treat certain types of cancer. It is currently marketed by Novartis as Gleevec (USA) or Glivec (Europe/Australia) as its mesylate salt, imatinib mesilate (INN). It is occasionally referred to as CGP57148B or STI571 (especially in older publications). It is used in treating chronic myelogenous leukemia (CML), gastrointestinal stromal tumors (GISTs) and a number of other malignancies.It is the first member of a new class of agents that act by inhibiting particular tyrosine kinase enzymes, instead of non-specifically inhibiting rapidly dividing cells.\"|drug type:Small Molecule; Approved|drug category:Antineoplastic Agents; Protein Kinase Inhibitors|disease indication:\"For the treatment of newly diagnosed adult patients with Philadelphia chromosome positive chronic myeloid leukemia (CML). Also indicated for the treatment of pediatric patients with Ph+ chronic phase CML whose disease has recurred after stem cell transplant or who are resistant to interferon-alpha therapy. Also indicated with unresectable and/or metastatic malignant gastrointestinal stromal tumors (GIST).\"|pharmacology:\"Imatinib is an antineoplastic agent used to treat chronic myelogenous leukemia. Imatinib is a 2-phenylaminopyrimidine derivative that functions as a specific inhibitor of a number of tyrosine kinase enzymes. In chronic myelogenous leukemia, the Philadelphia chromosome leads to a fusion protein of Abl with Bcr (breakpoint cluster region), termed Bcr-Abl. As this is now a continuously active tyrosine kinase, Imatinib is used to decrease Bcr-Abl activity.\"|mechanism of action:\"Imatinib mesylate is a protein-tyrosine kinase inhibitor that inhibits the Bcr-Abl tyrosine kinase, the constitutive abnormal tyrosine kinase created by the Philadelphia chromosome abnormality in chronic myeloid leukemia (CML). It inhibits proliferation and induces apoptosis in Bcr-Abl positive cell lines as well as fresh leukemic cells from Philadelphia chromosome positive chronic myeloid leukemia. Imatinib also inhibits the receptor tyrosine kinases for platelet derived growth factor (PDGF) and stem cell factor (SCF) - called c-kit. Imatinib was identified in the late 1990s by Dr Brian J. Druker. Its development is an excellent example of rational drug design. Soon after identification of the bcr-abl target, the search for an inhibitor began. Chemists used a high-throughput screen of chemical libraries to identify the molecule 2-phenylaminopyrimidine. This lead compound was then tested and modified by the introduction of methyl and benzamide groups to give it enhanced binding properties, resulting in imatinib.\"|comment:\"drug absorption (MI:2045): Imatinib is well absorbed with mean absolute bioavailability is 98% with maximum levels achieved within 2-4 hours of dosing\"|toxicity attribute name:\"Side effects include nausea, vomiting, diarrhea, loss of appetite, dry skin, hair loss, swelling (especially in the legs or around the eyes) and muscle cramps\"|comment:\"plasma protein binding (MI:2047):Very high (95%)\"|drug metabolism:Primarily hepatic via CYP3A4. Other cytochrome P450 enzymes, such as CYP1A2, CYP2D6, CYP2C9, and CYP2C19, play a minor role in its metabolism. The main circulating active metabolite in humans is the N-demethylated piperazine derivative, formed predominantly by CYP3A4.|comment:\"elimination half life (MI:2049): 18 hours for Imatinib, 40 hours for its major active metabolite, the N-desmethyl derivative\"|dosage form:Tablet Oral|dosage form:Capsule Oral|organisms affected:Humans and other mammals|food interaction:Take with food to reduce the incidence of gastric irritation. Follow with a large glass of water. A lipid rich meal will slightly reduce and delay absorption. Avoid grapefruit and grapefruit juice throughout treatment, grapefruit can significantly increase serum levels of this product.|drug interaction:Acetaminophen Increased hepatic toxicity of both agents|drug interaction:Anisindione Imatinib increases the anticoagulant effect|drug interaction:Dicumarol Imatinib increases the anticoagulant effect|drug interaction:Acenocoumarol Imatinib increases the anticoagulant effect|drug interaction:Warfarin Imatinib increases the anticoagulant effect|drug interaction:Aprepitant Aprepitant may change levels of chemotherapy agent|drug interaction:Atorvastatin Increases the effect and toxicity of atorvastatin|drug interaction:Carbamazepine Carbamazepine decreases levels of imatinib|drug interaction:Cerivastatin Imatinib increases the effect and toxicity of statin|drug interaction:Cyclosporine Imatinib increases the effect and toxicity of cyclosporine|drug interaction:Dexamethasone Dexamethasone decreases levels of imatinib|drug interaction:Lovastatin Imatinib increases the effect and toxicity of statin|drug interaction:Simvastatin Imatinib increases the effect and toxicity of statin|drug interaction:St. John's Wort St. John's Wort decreases levels of imatinib|drug interaction:Rifampin Rifampin decreases levels of imatinib|drug interaction:Pimozide Increases the effect and toxicity of pimozide|drug interaction:Phenobarbital Phenobarbital decreases levels of imatinib|drug interaction:Nifedipine Imatinib increases the effect and toxicity of nifedipine|drug interaction:Clarithromycin The macrolide increases levels of imatinib|drug interaction:Erythromycin The macrolide increases levels of imatinib|drug interaction:Josamycin The macrolide increases levels of imatinib|drug interaction:Ketoconazole The imidazole increases the levels of imatinib|drug interaction:Itraconazole The imidazole increases the levels of imatinib|drug interaction:Ethotoin The hydantoin decreases the levels of imatinib|drug interaction:Fosphenytoin The hydantoin decreases the levels of imatinib|drug interaction:Mephenytoin The hydantoin decreases the levels of imatinib|drug interaction:Phenytoin The hydantoin decreases the levels of imatinib|inchi id:\"InChI=1/C29H31N7O/c1-21-5-10-25(18-27(21)34-29-31-13-11-26(33-29)24-4-3-12-30-19-24)32-28(37)23-8-6-22(7-9-23)20-36-16-14-35(2)15-17-36/h3-13,18-19H,14-17,20H2,1-2H3,(H,32,37)(H,31,33,34)/f/h32,34H\"|url:\"http://www.rxlist.com/cgi/generic3/gleevec.htm\"|comment:\"melting point (MI:2026): 226 oC (mesylate salt)\"|comment:\"average molecular weight (MI:2155): 493.6027\"|comment:\"monoisotopic molecular weight (MI:2156): 493.2590\"|comment:\"experimental h2o solubility (MI:2157): Very soluble in water at pH < 5.5 (mesylate salt)\"|comment:\"predicted h2o solubility (MI:2158): 1.46e-02 mg/mL [ALOGPS]\"\t-\t-\t-\t-";
        //printLine(imatinibLine);

        String propertiesA = imatinibLine.split("\t")[19];
        Assert.assertNotNull(propertiesA);
        int numOfPropsA = propertiesA.split("\\|").length;
        Assert.assertEquals(10,numOfPropsA); //7+3(identifiers)

        String propertiesB = imatinibLine.split("\t")[20];
        Assert.assertNotNull(propertiesB);
        int numOfPropsB = propertiesB.split("\\|").length;
        Assert.assertEquals(18,numOfPropsB);  //16 properties + 2 identifiers

        PsimiTabReader reader = new IntactPsimiTabReader(false);
        Collection<BinaryInteraction> bis = reader.read(imatinibLine);
        Assert.assertEquals(1, bis.size());
        IntactTab2Xml tab2xml = new IntactTab2Xml();
        final EntrySet entrySet = tab2xml.convert(bis);
        //printXml(entrySet);

        Collection<Interaction> interactions = entrySet.getEntries().iterator().next().getInteractions();
        Assert.assertEquals(1, interactions.size());

        Interaction interaction = interactions.iterator().next();

        Assert.assertEquals(10,getInteractorByShortLabel(interaction,"5291").getXref().getAllDbReferences().size());
        Assert.assertEquals(18,getInteractorByShortLabel(interaction,"P08183").getXref().getAllDbReferences().size());
        
    }

    @Test
    public void convertAfterIncludingIdentifiersInProperties() throws Exception {

        final String mitabLine = "uniprotkb:primId_interactorA|intact:EBI-xxxxxA\tuniprotkb:primId_interactorB|intact:EBI-xxxxxB\tuniprotkb:vaomwwi\tuniprotkb:uiquuxdrn\tuniprotkb:VAOMWWI\tuniprotkb:UIQUUXDRN\tpsi-mi:\"MI:0027\"(cosedimentation)\t-\tpubmed:9524\ttaxid:2(label_duybqpdae)\ttaxid:3(label_feqwg)\tpsi-mi:\"MI:0407\"(direct interaction)\tpsi-mi:\"MI:0469\"(intact)\t-\t-\tpsi-mi:\"MI:0496\"(bait)\tpsi-mi:\"MI:0498\"(prey)\tpsi-mi:\"MI:0499\"(unspecified role)\tpsi-mi:\"MI:0499\"(unspecified role)\tgo:\"GO:0007028\"(some textA)|interpro:IPR000867(see-also)|uniprotkb:P26439(secondary-ac)|uniprotkb:primId_interactorA(identity)\tgo:\"GO:0008032\"(some textB)|uniprotkb:primId_interactorB(identity)\tpsi-mi:\"MI:0326\"(protein)\tpsi-mi:\"MI:0326\"(protein)\ttaxid:1(label_oohpeyhv)\t-\t-\t-\t-\ttemperature:302.0(kelvin)\ttemperature:302.0(kelvin)\t-";

        PsimiTabReader reader = new IntactPsimiTabReader(false);
        Collection<BinaryInteraction> bis = reader.read(mitabLine);
        Assert.assertEquals(1, bis.size());

        IntactBinaryInteraction bi = (IntactBinaryInteraction) bis.iterator().next();

        Assert.assertTrue(bi.getInteractorA().hasProperties());
        Assert.assertTrue(bi.getInteractorB().hasProperties());

        Assert.assertEquals(2, bi.getInteractorA().getIdentifiers().size());
        //4 including the identifier and 3 properties
        Assert.assertEquals(4, bi.getInteractorA().getProperties().size());

        Assert.assertEquals(2, bi.getInteractorB().getIdentifiers().size());
        //2 including the identifier and 1 property
        Assert.assertEquals(2, bi.getInteractorB().getProperties().size());

        IntactTab2Xml tab2xml = new IntactTab2Xml();
        final EntrySet entrySet = tab2xml.convert(bis);

        final Entry entry = entrySet.getEntries().iterator().next();
        Assert.assertNotNull(entry);

        final Collection<Interaction> interactions = entry.getInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(1, interactions.size());

        Interaction interaction = interactions.iterator().next();
        Iterator<Participant> pi = interaction.getParticipants().iterator();
        Participant pA = pi.next();
        Participant pB = pi.next();

        Assert.assertEquals("primId_interactorA",  pA.getInteractor().getNames().getShortLabel() );
        Assert.assertEquals("primId_interactorB",  pB.getInteractor().getNames().getShortLabel() );

        //should be 4, 3 properties and 1 identity
        Assert.assertEquals(4,pA.getInteractor().getXref().getAllDbReferences().size());
        //should be 2, 1 property and 1 identity
        Assert.assertEquals(2,pB.getInteractor().getXref().getAllDbReferences().size());

        //printXml(entrySet);

    }


}
