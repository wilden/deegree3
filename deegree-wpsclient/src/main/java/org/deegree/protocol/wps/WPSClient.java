//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.protocol.wps;

import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.protocol.i18n.Messages.get;
import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;
import static org.deegree.protocol.wps.WPSConstants.WPS_PREFIX;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.services.jaxb.main.AddressType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.main.ServiceContactType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;
import org.deegree.services.jaxb.main.ServiceProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to the WPS Client API. Initialization through GetCapabilities URL of the service. Access to service
 * metadata. Point to retrieve the registered processes and gain access to further execution operations.
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSClient {

    private static Logger LOG = LoggerFactory.getLogger( WPSClient.class );

    private DeegreeServicesMetadataType metadata;

    // [0]: Get, [1]: Post
    private URL[] describeProcessURLs = new URL[2];

    // [0]: Get, [1]: Post
    private URL[] executeURLs = new URL[2];

    private Map<CodeType, Process> processIdToProcess = new HashMap<CodeType, Process>();

    private XMLAdapter capabilitesDoc;

    private static NamespaceContext nsContext;

    private static final String owsPrefix = "ows";

    private static final String owsNS = "http://www.opengis.net/ows/1.1";

    private static final String xmlNS = "http://www.w3.org/XML/1998/namespace";

    private static final String xlinkNS = "http://www.w3.org/1999/xlink";

    static {
        nsContext = new NamespaceContext();
        nsContext.addNamespace( WPS_PREFIX, WPS_100_NS );
        nsContext.addNamespace( owsPrefix, owsNS );
        nsContext.addNamespace( "xlink", xlinkNS );
    }

    /**
     * Public constructor to access a WPS instance based on its Capabilities document
     * 
     * @param capabilitiesURL
     *            url to a WPS capabilities document, usually using a GetCapabilities request, must not be null
     * @throws RuntimeException
     */
    public WPSClient( URL capabilitiesURL ) throws RuntimeException {

        try {
            this.capabilitesDoc = new XMLAdapter( capabilitiesURL );
        } catch ( Exception e ) {
            LOG.error( e.getLocalizedMessage(), e );
            throw new RuntimeException( "Could not read from URL: " + capabilitiesURL + " error was: "
                                        + e.getLocalizedMessage() );
        }

        OMElement root = capabilitesDoc.getRootElement();
        String version = root.getAttributeValue( new QName( "version" ) );
        if ( !"1.0.0".equals( version ) ) {
            throw new IllegalArgumentException( get( "WPSCLIENT.WRONG_VERSION_CAPABILITIES", version, "1.0.0" ) );
        }
        String service = root.getAttributeValue( new QName( "service" ) );
        if ( !service.equalsIgnoreCase( "WPS" ) ) {
            throw new IllegalArgumentException( get( "WPSCLIENT.NO_WPS_CAPABILITIES", service, "WPS" ) );
        }

        // TODO what if server only supports Get? What is optional and what is mandatory?
        describeProcessURLs[0] = getOperationURL( "DescribeProcess", false );
        describeProcessURLs[1] = getOperationURL( "DescribeProcess", true );
        executeURLs[0] = getOperationURL( "Execute", false );
        executeURLs[1] = getOperationURL( "Execute", true );

        extractProcesses( root );
        extractMetadata( root );
    }

    /**
     * @param root
     */
    private void extractMetadata( OMElement root ) {
        metadata = new DeegreeServicesMetadataType();
        XPath xpath = new XPath( "/wps:Capabilities/ows:ServiceIdentification", nsContext );
        OMElement omServiceIdentification = capabilitesDoc.getElement( root, xpath );
        ServiceIdentificationType serviceIdentification = new ServiceIdentificationType();
        // serviceIdentification.getAbstract().add( e );

        metadata.setServiceIdentification( serviceIdentification );
        ServiceProviderType serviceProvider = new ServiceProviderType();

        xpath = new XPath( "/wps:Capabilities/ows:ServiceProvider/ows:ProviderName", nsContext );
        serviceProvider.setProviderName( capabilitesDoc.getRequiredNodeAsString( root, xpath ) );

        xpath = new XPath( "/wps:Capabilities/ows:ServiceProvider/ows:ProviderSite/@xlink:href", nsContext );
        serviceProvider.setProviderSite( capabilitesDoc.getRequiredNodeAsString( root, xpath ) );

        xpath = new XPath( "/wps:Capabilities/ows:ServiceProvider/ows:ServiceContact", nsContext );
        OMElement omServiceContact = capabilitesDoc.getRequiredElement( root, xpath );
        ServiceContactType serviceContact = new ServiceContactType();

        AddressType address = new AddressType();
        xpath = new XPath( "ows:ContactInfo/ows:Address/ows:AdministrativeArea", nsContext );
        address.setAdministrativeArea( capabilitesDoc.getRequiredNodeAsString( omServiceContact, xpath ) );
        xpath = new XPath( "ows:ContactInfo/ows:Address/ows:City", nsContext );
        address.setCity( capabilitesDoc.getRequiredNodeAsString( omServiceContact, xpath ) );
        xpath = new XPath( "ows:ContactInfo/ows:Address/ows:Country", nsContext );
        address.setCountry( capabilitesDoc.getRequiredNodeAsString( omServiceContact, xpath ) );
        xpath = new XPath( "ows:ContactInfo/ows:Address/ows:PostalCode", nsContext );
        address.setPostalCode( capabilitesDoc.getRequiredNodeAsString( omServiceContact, xpath ) );
        serviceContact.setAddress( address );

        xpath = new XPath( "ows:ContactInfo/ows:ContactInstructions", nsContext );
        serviceContact.setContactInstructions( capabilitesDoc.getNodeAsString( omServiceContact, xpath, null ) );
        xpath = new XPath( "ows:ContactInfo/ows:Phone/ows:Facsimile", nsContext );
        serviceContact.setFacsimile( capabilitesDoc.getRequiredNodeAsString( omServiceContact, xpath ) );
        xpath = new XPath( "ows:ContactInfo/ows:HoursOfService", nsContext );
        serviceContact.setHoursOfService( capabilitesDoc.getNodeAsString( omServiceContact, xpath, null ) );
        xpath = new XPath( "ows:IndividualName", nsContext );
        serviceContact.setIndividualName( capabilitesDoc.getRequiredNodeAsString( omServiceContact, xpath ) );
        xpath = new XPath( "ows:ContactInfo/ows:OnlineResource/@xlink:href", nsContext );
        serviceContact.setOnlineResource( capabilitesDoc.getNodeAsString( omServiceContact, xpath, null ) );
        xpath = new XPath( "ows:ContactInfo/ows:Phone/ows:Voice", nsContext );
        serviceContact.setPhone( capabilitesDoc.getRequiredNodeAsString( omServiceContact, xpath ) );
        xpath = new XPath( "ows:PositionName", nsContext );
        serviceContact.setPositionName( capabilitesDoc.getRequiredNodeAsString( omServiceContact, xpath ) );
        xpath = new XPath( "ows:Role", nsContext );
        serviceContact.setRole( capabilitesDoc.getNodeAsString( omServiceContact, xpath, null ) );

        serviceProvider.setServiceContact( serviceContact );

        metadata.setServiceProvider( serviceProvider );
    }

    /**
     * @param root
     */
    private void extractProcesses( OMElement root ) {
        XPath xpath = new XPath( "/wps:Capabilities/wps:ProcessOfferings/wps:Process", nsContext );
        List<OMElement> omProcesses = capabilitesDoc.getElements( root, xpath );
        for ( OMElement omProcess : omProcesses ) {
            String version = omProcess.getAttributeValue( new QName( WPS_100_NS, "processVersion" ) );
            CodeType id = parseId( omProcess );
            LanguageString processTitle = parseLanguageString( omProcess, "Title" );
            LanguageString processAbstract = parseLanguageString( omProcess, "Abstract" );

            Process process = new Process( this, version, id, processTitle, processAbstract );
            processIdToProcess.put( id, process );
        }
    }

    private LanguageString parseLanguageString( OMElement omElement, String name ) {
        OMElement omElem = omElement.getFirstChildWithName( new QName( owsNS, name ) );
        if ( omElem != null ) {
            String lang = omElem.getAttributeValue( new QName( xmlNS, "lang" ) );
            return new LanguageString( omElem.getText(), lang );
        }
        return null;
    }

    /**
     * @param omProcess
     * @return
     */
    private CodeType parseId( OMElement omProcess ) {
        OMElement omId = omProcess.getFirstChildWithName( new QName( owsNS, "Identifier" ) );
        String codeSpace = omId.getAttributeValue( new QName( null, "codeSpace" ) );
        if ( codeSpace != null ) {
            return new CodeType( omId.getText(), codeSpace );
        }
        return new CodeType( omId.getText() );
    }

    /**
     * @param operation
     * @param post
     * @return in case the URL that appears in GetCapabilities ends in ? then this method will strip it
     */
    private URL getOperationURL( String operation, boolean post ) {
        String xpathStr = "/wps:Capabilities/ows:OperationsMetadata/ows:Operation[@name='" + operation
                          + "']/ows:DCP/ows:HTTP/ows:" + ( post ? "Post" : "Get" ) + "/@xlink:href";
        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( "wps", WPS_100_NS );
        nsContext.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
        nsContext.addNamespace( "xlink", XLNNS );

        URL url = null;
        try {
            String href = capabilitesDoc.getRequiredNodeAsString( capabilitesDoc.getRootElement(),
                                                                  new XPath( xpathStr, nsContext ) );
            if ( href.endsWith( "?" ) ) {
                href = href.substring( 0, href.length() - 1 );
            }
            url = new URL( href );
        } catch ( Throwable t ) {
            String msg = "URL for operation '" + operation + " was not found. ': " + t.getMessage();
            LOG.warn( msg );
        }
        return url;
    }

    /**
     * Retrieve version of the service.
     * 
     * @return version
     */
    public String getServiceVersion() {
        return "1.0.0";
    }

    /**
     * Retrieve service metadata from Capabilities document.
     * 
     * @return a {@link DeegreeServicesMetadataType} instance with the service metadata
     */
    public DeegreeServicesMetadataType getMetadata() {
        return metadata;
    }

    URL getDescribeProcessURL( boolean post ) {
        return describeProcessURLs[post ? 1 : 0];
    }

    URL getExecuteURL( boolean post ) {
        return executeURLs[post ? 1 : 0];
    }

    /**
     * Retrieve all Processes.
     * 
     * @return an {@link Process} array
     */
    public Process[] getProcesses() {
        return processIdToProcess.values().toArray( new Process[processIdToProcess.size()] );
    }

    /**
     * Retrieve Process by providing its id (and codespace, if needed).
     * 
     * @param id
     *            id as String
     * @param codeSpace
     *            codespace as String, may be null
     * @return {@link Process} instance containing all relevant process information.
     */
    public Process getProcess( String id, String codeSpace ) {
        if ( !processIdToProcess.containsKey( new CodeType( id, codeSpace ) ) ) {
            throw new RuntimeException( "WPS has no registered process with id " + id );
        }
        return processIdToProcess.get( new CodeType( id, codeSpace ) );
    }

}
