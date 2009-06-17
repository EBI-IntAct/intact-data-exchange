/**
 * 
 */
package uk.ac.ebi.intact.interolog.download;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * 
 * Download files from Internet.
 * Copy from http://www.javaspecialists.eu/archive/Issue122.html
 * 
 * @author mmichaut
 * @version $Id$
 * @since 4 juin 07
 */
public abstract class Stats {
	
	  private volatile int totalBytes;
	  private long start = System.currentTimeMillis();
	  public int seconds() {
	    int result = (int) ((System.currentTimeMillis() - start) / 1000);
	    return result == 0 ? 1 : result; // avoid div by zero
	  }
	  public void bytes(int length) {
	    totalBytes += length;
	  }
	  public void print() {
	    int kbpersecond = (int) (totalBytes / seconds() / 1024);
	    System.out.printf("%10d KB%5s%%  (%d KB/s)%n", totalBytes/1024,
	        calculatePercentageComplete(totalBytes), kbpersecond);
	  }

	  public abstract String calculatePercentageComplete(int bytes);

	  public static Stats make(URL url) throws IOException {
	    System.out.println(new Date() + " Opening connection to URL");
	    URLConnection con = url.openConnection();
	    System.out.println(new Date() + " Getting content length");
	    int size = con.getContentLength();
	    return size == -1 ? new BasicStats() : new ProgressStats(size);
	  }

}
