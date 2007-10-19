/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import psidev.psi.mi.search.index.AbstractIndexWriter;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.xml.converter.ConverterException;

/**
 * TODO comment this!
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id: IntActPsimitTabIndexWriter.java 
 */
public class IntActPsimiTabIndexWriter extends AbstractIndexWriter
{

	private static final Log log = LogFactory.getLog(IntActPsimiTabIndexWriter.class);
	
    public IntActPsimiTabIndexWriter()
    {
    }

    public  void index(IndexWriter indexWriter, InputStream is, boolean hasHeaderLine) throws IOException, ConverterException, MitabLineException {
        if (log.isInfoEnabled()) log.info("Starting index creation: "+indexWriter.getDirectory());
        long startTime = System.currentTimeMillis();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        if (hasHeaderLine)
        {
            reader.readLine();
        }

        String line;
        while ((line = reader.readLine()) != null)
        {
            if (log.isTraceEnabled()) log.trace("\tIndexing: "+line);
            
            indexWriter.addDocument(createDocument(line));
        }

        if (log.isInfoEnabled())
        {
            long elapsedTime = (System.currentTimeMillis()-startTime)/1000;
            log.info("Index created. Time: "+elapsedTime+"s");
        }
    }
	
	public Document createDocument(String interaction) throws MitabLineException {
		return IntActDocumentBuilder.createDocumentFromPsimiTabLine(interaction);
	}

}
