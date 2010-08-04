//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.protocol.ows;

import static org.deegree.commons.xml.CommonNamespaces.OWS_11_NS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.services.jaxb.main.AddressType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.main.ServiceContactType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;
import org.deegree.services.jaxb.main.ServiceProviderType;

/**
 * Extracts metadata from OGC service capabilities documents that comply to the OWS 1.1.0 specification.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OWS110CapabilitiesAdapter extends XMLAdapter {

    private final NamespaceContext nsContext = new NamespaceContext();

    /**
     * Creates a new {@link OWS110CapabilitiesAdapter} instance.
     */
    public OWS110CapabilitiesAdapter() {
        nsContext.addNamespace( "wps", WPS_100_NS );
        nsContext.addNamespace( "ows", OWS_11_NS );
        nsContext.addNamespace( "xlink", XLNNS );
    }

    /**
     * Returns the service metadata.
     * 
     * @return the service metadata, never <code>null</code>
     */
    public DeegreeServicesMetadataType parseMetadata() {

        OMElement rootEl = getRootElement();

        ServiceIdentificationType serviceId = null;
        OMElement serviceIdEl = getElement( rootEl, new XPath( "ows:ServiceIdentification", nsContext ) );
        if ( serviceIdEl != null ) {
            serviceId = parseServiceIdentification( serviceIdEl );
        }

        ServiceProviderType serviceProvider = null;
        OMElement serviceProviderEl = getElement( rootEl, new XPath( "ows:ServiceProvider", nsContext ) );
        if ( serviceProviderEl != null ) {
            serviceProvider = parseServiceProvider( serviceProviderEl );
        }

        DeegreeServicesMetadataType metadata = new DeegreeServicesMetadataType();
        metadata.setServiceIdentification( serviceId );
        metadata.setServiceProvider( serviceProvider );

        return metadata;
    }

    /**
     * Returns the URL for the specified operation and HTTP method.
     * 
     * @param operation
     *            name of the operation, must not be <code>null</code>
     * @param post
     *            if set to true, the URL for POST requests will be returned, otherwise the URL for GET requests will be
     *            returned
     * @return the operation URL (trailing question marks are stripped), can be <code>null</code> (if the
     *         operation/method is not announced by the service)
     * @throws MalformedURLException
     *             if the announced URL is malformed
     */
    public URL getOperationURL( String operation, boolean post )
                            throws MalformedURLException {

        String xpathStr = "ows:OperationsMetadata/ows:Operation[@name='" + operation + "']/ows:DCP/ows:HTTP/ows:"
                          + ( post ? "Post" : "Get" ) + "/@xlink:href";
        URL url = null;
        String href = getNodeAsString( getRootElement(), new XPath( xpathStr, nsContext ), null );
        if ( href != null ) {
            if ( href.endsWith( "?" ) ) {
                href = href.substring( 0, href.length() - 1 );
            }
            url = new URL( href );
        }
        return url;
    }

    private ServiceIdentificationType parseServiceIdentification( OMElement serviceIdEl ) {

        ServiceIdentificationType serviceId = new ServiceIdentificationType();

        List<OMElement> titleEls = getElements( serviceIdEl, new XPath( "ows:Title", nsContext ) );
        for ( OMElement titleEl : titleEls ) {
            // TODO what about the language information (xml:lang)?
            serviceId.getTitle().add( titleEl.getText() );
        }

        List<OMElement> abstractEls = getElements( serviceIdEl, new XPath( "ows:Abstract", nsContext ) );
        for ( OMElement abstractEl : abstractEls ) {
            // TODO what about the language information (xml:lang)?
            serviceId.getAbstract().add( abstractEl.getText() );
        }

        List<OMElement> keywordsEls = getElements( serviceIdEl, new XPath( "ows:Keywords", nsContext ) );
        for ( OMElement keywordsEl : keywordsEls ) {
            // TODO
        }

        // TODO
        String[] profiles = getNodesAsStrings( serviceIdEl, new XPath( "ows:Profiles", nsContext ) );

        String fees = getNodeAsString( serviceIdEl, new XPath( "ows:Fees", nsContext ), null );
        serviceId.setFees( fees );

        String[] constraints = getNodesAsStrings( serviceIdEl, new XPath( "ows:AccessConstraints", nsContext ) );
        for ( String constraint : constraints ) {
            serviceId.getAccessConstraints().add( constraint );
        }
        return serviceId;
    }

    private ServiceProviderType parseServiceProvider( OMElement serviceProviderEl ) {

        ServiceProviderType serviceProvider = new ServiceProviderType();

        XPath xpath = new XPath( "ows:ProviderName", nsContext );
        serviceProvider.setProviderName( getNodeAsString( serviceProviderEl, xpath, null ) );

        xpath = new XPath( "ows:ProviderSite/@xlink:href", nsContext );
        serviceProvider.setProviderSite( getNodeAsString( serviceProviderEl, xpath, null ) );

        xpath = new XPath( "ows:ServiceContact", nsContext );
        OMElement serviceContactEl = getElement( serviceProviderEl, xpath );
        if ( serviceContactEl != null ) {
            ServiceContactType serviceContact = parseServiceContact( serviceContactEl );
            serviceProvider.setServiceContact( serviceContact );
        }

        return serviceProvider;
    }

    private ServiceContactType parseServiceContact( OMElement serviceContactEl ) {

        ServiceContactType serviceContact = new ServiceContactType();

        XPath xpath = new XPath( "ows:IndividualName", nsContext );
        serviceContact.setIndividualName( getNodeAsString( serviceContactEl, xpath, null ) );

        xpath = new XPath( "ows:PositionName", nsContext );
        serviceContact.setPositionName( getNodeAsString( serviceContactEl, xpath, null ) );

        xpath = new XPath( "ows:PositionName", nsContext );
        serviceContact.setPositionName( getNodeAsString( serviceContactEl, xpath, null ) );

        xpath = new XPath( "ows:ContactInfo", nsContext );
        OMElement contactInfoEl = getElement( serviceContactEl, xpath );
        if ( contactInfoEl != null ) {
            xpath = new XPath( "ows:Phone/ows:Voice", nsContext );
            // TODO maxOccurs="unbounded"
            serviceContact.setPhone( getNodeAsString( contactInfoEl, xpath, null ) );

            xpath = new XPath( "ows:Phone/ows:Facsimile", nsContext );
            // TODO maxOccurs="unbounded"
            serviceContact.setFacsimile( getNodeAsString( contactInfoEl, xpath, null ) );

            xpath = new XPath( "ows:Address", nsContext );
            OMElement addressEl = getElement( contactInfoEl, xpath );
            if ( addressEl != null ) {
                serviceContact.setAddress( parseAddress( addressEl ) );
            }

            xpath = new XPath( "ows:Address/ows:ElectronicMailAddress", nsContext );
            String[] eMails = getNodesAsStrings( contactInfoEl, xpath );
            for ( String eMail : eMails ) {
                serviceContact.getElectronicMailAddress().add( eMail );
            }

            xpath = new XPath( "ows:OnlineResource/@xlink:href", nsContext );
            serviceContact.setOnlineResource( getNodeAsString( contactInfoEl, xpath, null ) );

            xpath = new XPath( "ows:HoursOfService", nsContext );
            serviceContact.setHoursOfService( getNodeAsString( contactInfoEl, xpath, null ) );

            xpath = new XPath( "ows:ContactInstructions", nsContext );
            serviceContact.setContactInstructions( getNodeAsString( contactInfoEl, xpath, null ) );
        }

        xpath = new XPath( "ows:Role", nsContext );
        OMElement roleEl = getElement( serviceContactEl, xpath );
        if ( roleEl != null ) {
            // TODO
        }

        return serviceContact;
    }

    private AddressType parseAddress( OMElement addressEl ) {

        AddressType address = new AddressType();

        XPath xpath = new XPath( "ows:DeliveryPoint", nsContext );
        String[] deliveryPoints = getNodesAsStrings( addressEl, xpath );
        for ( String deliveryPoint : deliveryPoints ) {
            address.getDeliveryPoint().add( deliveryPoint );
        }

        xpath = new XPath( "ows:City", nsContext );
        address.setCity( getNodeAsString( addressEl, xpath, null ) );

        xpath = new XPath( "ows:AdministrativeArea", nsContext );
        address.setAdministrativeArea( getNodeAsString( addressEl, xpath, null ) );

        xpath = new XPath( "ows:PostalCode", nsContext );
        address.setPostalCode( getNodeAsString( addressEl, xpath, null ) );

        xpath = new XPath( "ows:Country", nsContext );
        address.setCountry( getNodeAsString( addressEl, xpath, null ) );

        return address;
    }
}
