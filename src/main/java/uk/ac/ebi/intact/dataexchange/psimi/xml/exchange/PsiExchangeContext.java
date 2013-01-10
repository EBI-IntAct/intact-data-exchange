package uk.ac.ebi.intact.dataexchange.psimi.xml.exchange;

/**
 * Context for PSIExchange
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26-Nov-2010</pre>
 */

public class PsiExchangeContext {

    private boolean isSanityCheckEnabled = true;

    private static ThreadLocal<PsiExchangeContext> instance = new
            ThreadLocal<PsiExchangeContext>() {
                @Override
                protected PsiExchangeContext initialValue() {
                    return new PsiExchangeContext();
                }
            };

    public boolean isSanityCheckEnabled() {
        return isSanityCheckEnabled;
    }

    public void setSanityCheckEnabled(boolean sanityCheckEnabled) {
        isSanityCheckEnabled = sanityCheckEnabled;
    }

    public static PsiExchangeContext getCurrentInstance() {
        return instance.get();
    }
}
