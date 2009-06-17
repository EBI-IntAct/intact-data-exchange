/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.dataexchange.psimi.solr.converter;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

/**
 * SolrDocumentConverter Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SolrDocumentConverterTest {

    @Test
    public void testToSolrDocument() throws Exception {
        String rigid = "THIS_IS_A_RIGID";
        String psiMiTabLine = "uniprotkb:P16884\tuniprotkb:Q60824\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)" +
                              "\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739" +
                              "\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)" +
                              "\tintact:EBI-446356|irefindex:"+ rigid +"("+ FieldNames.RIGID+")\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)" +
                              "\tMI:0499(unspecified role)\tinterpro:IPR004829|\tgo:\"GO:0030246\"\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";

        SolrDocumentConverter converter = new SolrDocumentConverter();

        SolrInputDocument doc = converter.toSolrDocument(psiMiTabLine);

        Assert.assertEquals("psi-mi:\"MI:0326\"(protein)", doc.getFieldValue(FieldNames.TYPE_A));
        Assert.assertEquals("psi-mi:\"MI:0326\"(protein)", doc.getFieldValue(FieldNames.TYPE_B));
        Assert.assertEquals(rigid, doc.getFieldValue(FieldNames.RIGID));
    }

    @Test
    public void testToSolrInputDocument2BinaryInteraction() throws Exception {
        String psiMiTabLine = "uniprotkb:P16884\tuniprotkb:Q60824\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)" +
                              "\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739" +
                              "\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)" +
                              "\tintact:EBI-446356|irefindex:arigidblabla("+ FieldNames.RIGID+")\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)" +
                              "\tMI:0499(unspecified role)\tinterpro:IPR004829|\tgo:\"GO:0030246\"\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";

        SolrDocumentConverter converter = new SolrDocumentConverter();
         //mitab to solrinputdoc
        SolrInputDocument inputDoc1 = converter.toSolrDocument(psiMiTabLine);
        //solrinputdoc to binaryinteraction 
        final IntactBinaryInteraction binaryInteraction = (IntactBinaryInteraction) converter.toBinaryInteraction( inputDoc1 );
        
        Assert.assertEquals("MI:0326", binaryInteraction.getInteractorA().getInteractorType().getIdentifier());
        Assert.assertEquals("MI:0326", binaryInteraction.getInteractorB().getInteractorType().getIdentifier());

        //binaryinteraction back to solrinputdoc
        final SolrInputDocument inputDoc2 = converter.toSolrDocument( binaryInteraction );

        Assert.assertEquals(inputDoc1.getFieldValues("idA").size(),inputDoc2.getFieldValues("idA").size());

     }

    @Test
    public void testTypeAndAc() throws Exception {
        String psiMiTabLine = "intact:EBI-12345\tintact:EBI-1443\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)" +
                              "\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739" +
                              "\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)" +
                              "\tintact:EBI-446356|irefindex:arigidblabla(" + FieldNames.RIGID + ")\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)" +
                              "\tMI:0499(unspecified role)\tinterpro:IPR004829|\tgo:\"GO:0030246\"\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";

        SolrDocumentConverter converter = new SolrDocumentConverter();
        SolrInputDocument doc = converter.toSolrDocument(psiMiTabLine);

        final SolrInputField field = doc.getField("intact_byInteractorType_mi0326");
        Assert.assertEquals(2, field.getValueCount());
        Assert.assertTrue(field.getValues().contains("EBI-12345"));
        Assert.assertTrue(field.getValues().contains("EBI-1443"));

    }

    @Test
    public void idSelectiveAdder() throws Exception {
        String psiMiTabLine = "intact:EBI-12345\tintact:EBI-54321|uniprotkb:P12345\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)" +
                              "\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739" +
                              "\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)" +
                              "\tintact:EBI-446356|irefindex:arigidblabla(" + FieldNames.RIGID + ")\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)" +
                              "\tMI:0499(unspecified role)\tinterpro:IPR004829|\tgo:\"GO:0030246\"\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";

        SolrDocumentConverter converter = new SolrDocumentConverter();
        SolrInputDocument doc = converter.toSolrDocument(psiMiTabLine);

        final SolrInputField intactIdField = doc.getField("intact_id");
        Assert.assertEquals(2, intactIdField.getValueCount());
        Assert.assertTrue(intactIdField.getValues().contains("EBI-12345"));
        Assert.assertTrue(intactIdField.getValues().contains("EBI-54321"));
        
        final SolrInputField uniprotIdField = doc.getField("uniprotkb_id");
        Assert.assertEquals(1, uniprotIdField.getValueCount());
        Assert.assertTrue(uniprotIdField.getValues().contains("P12345"));

    }

     @Test
    public void geneName() throws Exception {
        String psiMiTabLine = "intact:EBI-12345\tintact:EBI-54321|uniprotkb:P12345\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)" +
                              "\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739" +
                              "\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)" +
                              "\tintact:EBI-446356|irefindex:arigidblabla(" + FieldNames.RIGID + ")\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)" +
                              "\tMI:0499(unspecified role)\tinterpro:IPR004829|\tgo:\"GO:0030246\"\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";

        SolrDocumentConverter converter = new SolrDocumentConverter();
        SolrInputDocument doc = converter.toSolrDocument(psiMiTabLine);

        final SolrInputField field = doc.getField("geneName");
        Assert.assertEquals(2, field.getValueCount());
        Assert.assertTrue(field.getValues().contains("Nefh"));
        Assert.assertTrue(field.getValues().contains("Dst"));

    }
}
