/**
 * 
 */
package uk.ac.ebi.intact.interolog.download;

/**
 * Download files from Internet.
 * Copy from http://www.javaspecialists.eu/archive/Issue122.html
 * 
 * @author mmichaut
 * @version $Id$
 * @since 4 juin 07
 */
public class ProgressStats extends Stats{
	
	 private final long contentLength;
	  public ProgressStats(long contentLength) {
	    this.contentLength = contentLength;
	  }
	  public String calculatePercentageComplete(int totalBytes) {
	    return Long.toString((totalBytes * 100L / contentLength));
	  }

}

