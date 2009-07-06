/**
 * RegIMExService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package uk.ac.ebi.intact.imex.idservice.keyassigner.generated;

public interface RegIMExService extends javax.xml.rpc.Service {
    public java.lang.String getRegIMExAddress();

    public RegIMEx_PortType getRegIMEx() throws javax.xml.rpc.ServiceException;

    public RegIMEx_PortType getRegIMEx( java.net.URL portAddress ) throws javax.xml.rpc.ServiceException;
}
