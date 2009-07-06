/**
 * RegIMExServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package uk.ac.ebi.intact.imex.idservice.keyassigner.generated;

public class RegIMExServiceLocator extends org.apache.axis.client.Service
        implements RegIMExService {

    public RegIMExServiceLocator() {
    }


    public RegIMExServiceLocator( org.apache.axis.EngineConfiguration config ) {
        super( config );
    }

    public RegIMExServiceLocator( java.lang.String wsdlLoc, javax.xml.namespace.QName sName ) throws javax.xml.rpc.ServiceException {
        super( wsdlLoc, sName );
    }

    // Use to get a proxy class for RegIMEx
    private java.lang.String RegIMEx_address = null;//"https://imex.mbi.ucla.edu:50006/RegIMEx/services/RegIMEx";

    public java.lang.String getRegIMExAddress() {
        return RegIMEx_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String RegIMExWSDDServiceName = "RegIMEx";

    public java.lang.String getRegIMExWSDDServiceName() {
        return RegIMExWSDDServiceName;
    }

    public void setRegIMExWSDDServiceName( java.lang.String name ) {
        RegIMExWSDDServiceName = name;
    }

    public RegIMEx_PortType getRegIMEx() throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL( RegIMEx_address );
        }
        catch ( java.net.MalformedURLException e ) {
            throw new javax.xml.rpc.ServiceException( e );
        }
        return getRegIMEx( endpoint );
    }

    public RegIMEx_PortType getRegIMEx( java.net.URL portAddress ) throws javax.xml.rpc.ServiceException {
        try {
            RegIMExSoapBindingStub _stub = new RegIMExSoapBindingStub( portAddress, this );
            _stub.setPortName( getRegIMExWSDDServiceName() );
            return _stub;
        }
        catch ( org.apache.axis.AxisFault e ) {
            return null;
        }
    }

    public void setRegIMExEndpointAddress( java.lang.String address ) {
        RegIMEx_address = address;
    }

    /**
     * For the given interface, get the stub implementation. If this service has no port for the given interface, then
     * ServiceException is thrown.
     */
    public java.rmi.Remote getPort( Class serviceEndpointInterface ) throws javax.xml.rpc.ServiceException {
        try {
            if ( RegIMEx_PortType.class.isAssignableFrom( serviceEndpointInterface ) )
            {
                RegIMExSoapBindingStub _stub = new RegIMExSoapBindingStub( new java.net.URL( RegIMEx_address ), this );
                _stub.setPortName( getRegIMExWSDDServiceName() );
                return _stub;
            }
        }
        catch ( java.lang.Throwable t ) {
            throw new javax.xml.rpc.ServiceException( t );
        }
        throw new javax.xml.rpc.ServiceException( "There is no stub implementation for the interface:  " + ( serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName() ) );
    }

    /**
     * For the given interface, get the stub implementation. If this service has no port for the given interface, then
     * ServiceException is thrown.
     */
    public java.rmi.Remote getPort( javax.xml.namespace.QName portName, Class serviceEndpointInterface ) throws javax.xml.rpc.ServiceException {
        if ( portName == null ) {
            return getPort( serviceEndpointInterface );
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ( "RegIMEx".equals( inputPortName ) ) {
            return getRegIMEx();
        } else {
            java.rmi.Remote _stub = getPort( serviceEndpointInterface );
            ( (org.apache.axis.client.Stub) _stub ).setPortName( portName );
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName( "http://imex.org/registry", "RegIMExService" );
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if ( ports == null ) {
            ports = new java.util.HashSet();
            ports.add( new javax.xml.namespace.QName( "http://imex.org/registry", "RegIMEx" ) );
        }
        return ports.iterator();
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress( java.lang.String portName, java.lang.String address ) throws javax.xml.rpc.ServiceException {

        if ( "RegIMEx".equals( portName ) ) {
            setRegIMExEndpointAddress( address );
        } else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException( " Cannot set Endpoint Address for Unknown Port" + portName );
        }
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress( javax.xml.namespace.QName portName, java.lang.String address ) throws javax.xml.rpc.ServiceException {
        setEndpointAddress( portName.getLocalPart(), address );
    }

}
