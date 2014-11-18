package uk.ac.ebi.intact.dataexchange.structuredabstract.utils;

import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Xref;

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

    private static final String ourEmptyPidLink = "http://www.ebi.ac.uk/intact/molecule/";

    /**
     * The pattern to replace spaces in the Primary Id.
     */
    private static Pattern ourPrimaryIDPat = Pattern.compile("\\s");

    /**
     * The pattern to replace the ac.
     */
    private static Pattern ourSearchUrlPat = Pattern.compile("(\\$)?\\{ac\\}");

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
    public static String getPrimaryIdLink(String ac, Xref xref, Map<String, String> cvTermsUrlsCache) {
        if (ac == null && xref == null){
            return null;
        }
        else if (xref == null){
            return ourEmptyPidLink+ac;
        }

        // The short label of the database of the xref.
        String dbname = xref.getDatabase().getShortName();

        // Set it to the value from the cache.
        String searchUrl = cvTermsUrlsCache.get(dbname);

        // Is it in the cache?
        if (searchUrl == null) {
            // Not in the cache; create it and store in the cache.

            // Loop through annotations looking for search-url.
            Annotation searchUrlAnnot = psidev.psi.mi.jami.utils.AnnotationUtils.collectFirstAnnotationWithTopic(xref.
                    getDatabase().getAnnotations(), Annotation.SEARCH_URL_MI, Annotation.SEARCH_URL);
            if (searchUrlAnnot != null){
                searchUrl = searchUrlAnnot.getValue();
            }

            if (searchUrl == null && ac != null) {
                // The db has no annotation "search-url". Don't search again in
                // the future.
                return ourEmptyPidLink+ac;
            }
            else if (searchUrl == null){
                return null;
            }
            // Cache the url.
            cvTermsUrlsCache.put(dbname, searchUrl);
        }
        // Match against the spaces.
        Matcher pidmatch = ourPrimaryIDPat.matcher(xref.getId());

        // The primary id after replacing spaces with %20 characters.
        String pid = pidmatch.replaceAll("%20");

        Matcher matcher = ourSearchUrlPat.matcher(searchUrl);
        // After replacing the ac with primary id.
        return matcher.replaceAll(pid);
    }
}
