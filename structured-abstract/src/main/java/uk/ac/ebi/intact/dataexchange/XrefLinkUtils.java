package uk.ac.ebi.intact.dataexchange;

import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.Xref;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting links from xrefs
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15/07/13</pre>
 */

public class XrefLinkUtils {

    private static final String ourEmptyPidLink = "---";

    /**
     * The pattern to replace spaces in the Primary Id.
     */
    private static Pattern ourPrimaryIDPat = Pattern.compile("\\s");

    /**
     * The pattern to replace the ac.
     */
    private static Pattern ourSearchUrlPat = Pattern.compile("\\$\\{ac\\}");

    /**
     * Return the primary id as a link. Only used when viewing a xref.
     *
     * @param xref
     *            Xrefenece object to access primary id and other information.
     * @return the primary id as a link only if the primary id is not null and
     *         'search-url' is found among the annotations.
     *         {@link #ourEmptyPidLink} is returned for a null primary id. The
     *         primary id is returned as the link if there is 'search-url' is
     *         found among the annotations for given xreference.
     */
    public static String getPrimaryIdLink(Xref xref, Map<String, String> cvTermsUrlsCache) {
        // Return the empty link if there is no primary id. This is a raw
        // pid (may contains spaces in its name).
        String pidraw = xref.getPrimaryId();
        if (pidraw == null) {
            return ourEmptyPidLink;
        }
        // The short label of the database of the xref.
        String dbname = xref.getCvDatabase().getShortLabel();

        // Set it to the value from the cache.
        String searchUrl = cvTermsUrlsCache.get(dbname);

        // Is it in the cache?
        if (searchUrl == null) {
            // Not in the cache; create it and store in the cache.

            // Loop through annotations looking for search-url.
            Collection<Annotation> annots = xref.getCvDatabase()
                    .getAnnotations();
            for (Annotation annot : annots) {
                if (annot.getCvTopic() != null && annot.getCvTopic().getShortLabel().equalsIgnoreCase("search-url")) {
                    // save searchUrl for future use
                    searchUrl = annot.getAnnotationText();
                    break;
                }
            }
            if (searchUrl == null) {
                // The db has no annotation "search-url". Don't search again in
                // the future.
                return ourEmptyPidLink;
            }
            // Cache the url.
            cvTermsUrlsCache.put(dbname, searchUrl);
        }
        // Match against the spaces.
        Matcher pidmatch = ourPrimaryIDPat.matcher(pidraw);

        // The primary id after replacing spaces with %20 characters.
        String pid = pidmatch.replaceAll("%20");

        Matcher matcher = ourSearchUrlPat.matcher(searchUrl);
        // After replacing the ac with primary id.
        return matcher.replaceAll(pid);
    }
}
