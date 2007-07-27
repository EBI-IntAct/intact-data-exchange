// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion;

/**
 * Definition of the constants used in the scope of the dataConversion package.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class PsiConstants {

    /**
     * can be used as type of an attribute at the proteinParticipant level to describe the organism in which the protein
     * has been expressed. <br> <b>format of the attribute</b>: &lt;attribute name="expressedIn"&gt;proteinId:bioSourceId&lt;/attribute&gt;
     */
    public static final String EXPRESSED_IN_ATTRIBUTE_NAME = "expressedIn";

    /**
     * can be used as type of an attribute at the interaction level to describe a dissociation constant (also known as
     * Kd). <br> <b>format of the attribute</b>: &lt;attribute name="kd"&gt;1,123&lt;/attribute&gt;
     */
    public static final String KD_ATTRIBUTE_NAME = "kd";

    /**
     * can be used as type of an attribute at the interaction level to describe a dissociation constant (also known as
     * Kd). <br> <b>format of the attribute</b>: &lt;attribute name="dissociation constant"&gt;1,123&lt;/attribute&gt;
     */
    public static final String DISSOCIATION_CONSTANT_ATTRIBUTE_NAME = "dissociation constant";
}