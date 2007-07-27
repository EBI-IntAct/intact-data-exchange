/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import uk.ac.ebi.intact.persistence.util.CgLibUtil;

/**
 * Class describing the source of the association between a CV class and a node Name.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27-Jun-2005</pre>
 */
public class CvClass2Source {
    private Class clazz;
    private String parentNodeName;

    public CvClass2Source( Class clazz, String parentNodeName ) {
        this.clazz = CgLibUtil.removeCglibEnhanced(clazz);
        this.parentNodeName = parentNodeName;
    }

    public CvClass2Source( Class clazz ) {
        this.clazz = CgLibUtil.removeCglibEnhanced(clazz);
        this.parentNodeName = null;
    }

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof CvClass2Source ) ) {
            return false;
        }

        final CvClass2Source cvClass2Source = (CvClass2Source) o;

        if ( !clazz.equals( cvClass2Source.clazz ) ) {
            return false;
        }
        if ( parentNodeName != null ? !parentNodeName.equals( cvClass2Source.parentNodeName ) : cvClass2Source.parentNodeName != null ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = clazz.hashCode();
        result = 29 * result + ( parentNodeName != null ? parentNodeName.hashCode() : 0 );
        return result;
    }
}
