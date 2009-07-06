/**
 * SubmissionAxis.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package uk.ac.ebi.intact.imex.idservice.keyassigner.generated;

public class SubmissionAxis implements java.io.Serializable {
    private java.lang.String partner;

    private long keyRangeFrom;

    private long keyRangeTo;

    private long submissionId;

    public SubmissionAxis() {
    }

    public SubmissionAxis(
            java.lang.String partner,
            long keyRangeFrom,
            long keyRangeTo,
            long submissionId ) {
        this.partner = partner;
        this.keyRangeFrom = keyRangeFrom;
        this.keyRangeTo = keyRangeTo;
        this.submissionId = submissionId;
    }


    /**
     * Gets the partner value for this SubmissionAxis.
     *
     * @return partner
     */
    public java.lang.String getPartner() {
        return partner;
    }


    /**
     * Sets the partner value for this SubmissionAxis.
     *
     * @param partner
     */
    public void setPartner( java.lang.String partner ) {
        this.partner = partner;
    }


    /**
     * Gets the keyRangeFrom value for this SubmissionAxis.
     *
     * @return keyRangeFrom
     */
    public long getKeyRangeFrom() {
        return keyRangeFrom;
    }


    /**
     * Sets the keyRangeFrom value for this SubmissionAxis.
     *
     * @param keyRangeFrom
     */
    public void setKeyRangeFrom( long keyRangeFrom ) {
        this.keyRangeFrom = keyRangeFrom;
    }


    /**
     * Gets the keyRangeTo value for this SubmissionAxis.
     *
     * @return keyRangeTo
     */
    public long getKeyRangeTo() {
        return keyRangeTo;
    }


    /**
     * Sets the keyRangeTo value for this SubmissionAxis.
     *
     * @param keyRangeTo
     */
    public void setKeyRangeTo( long keyRangeTo ) {
        this.keyRangeTo = keyRangeTo;
    }


    /**
     * Gets the submissionId value for this SubmissionAxis.
     *
     * @return submissionId
     */
    public long getSubmissionId() {
        return submissionId;
    }


    /**
     * Sets the submissionId value for this SubmissionAxis.
     *
     * @param submissionId
     */
    public void setSubmissionId( long submissionId ) {
        this.submissionId = submissionId;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals( java.lang.Object obj ) {
        if ( !( obj instanceof SubmissionAxis ) ) {
            return false;
        }
        SubmissionAxis other = (SubmissionAxis) obj;
        if ( obj == null ) {
            return false;
        }
        if ( this == obj ) {
            return true;
        }
        if ( __equalsCalc != null ) {
            return ( __equalsCalc == obj );
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
                  ( ( this.partner == null && other.getPartner() == null ) ||
                    ( this.partner != null &&
                      this.partner.equals( other.getPartner() ) ) ) &&
                                                                    this.keyRangeFrom == other.getKeyRangeFrom() &&
                                                                    this.keyRangeTo == other.getKeyRangeTo() &&
                                                                    this.submissionId == other.getSubmissionId();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;

    public synchronized int hashCode() {
        if ( __hashCodeCalc ) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if ( getPartner() != null ) {
            _hashCode += getPartner().hashCode();
        }
        _hashCode += new Long( getKeyRangeFrom() ).hashCode();
        _hashCode += new Long( getKeyRangeTo() ).hashCode();
        _hashCode += new Long( getSubmissionId() ).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc( SubmissionAxis.class, true );

    static {
        typeDesc.setXmlType( new javax.xml.namespace.QName( "http://imex.org/registry", "SubmissionAxis" ) );
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "partner" );
        elemField.setXmlName( new javax.xml.namespace.QName( "", "partner" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "string" ) );
        elemField.setNillable( false );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "keyRangeFrom" );
        elemField.setXmlName( new javax.xml.namespace.QName( "", "keyRangeFrom" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "long" ) );
        elemField.setNillable( false );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "keyRangeTo" );
        elemField.setXmlName( new javax.xml.namespace.QName( "", "keyRangeTo" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "long" ) );
        elemField.setNillable( false );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "submissionId" );
        elemField.setXmlName( new javax.xml.namespace.QName( "", "submissionId" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "long" ) );
        elemField.setNillable( false );
        typeDesc.addFieldDesc( elemField );
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
            java.lang.String mechType,
            java.lang.Class _javaType,
            javax.xml.namespace.QName _xmlType ) {
        return
                new org.apache.axis.encoding.ser.BeanSerializer(
                        _javaType, _xmlType, typeDesc );
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
            java.lang.String mechType,
            java.lang.Class _javaType,
            javax.xml.namespace.QName _xmlType ) {
        return
                new org.apache.axis.encoding.ser.BeanDeserializer(
                        _javaType, _xmlType, typeDesc );
    }

}
