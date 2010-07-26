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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wps.getcapabilities.WPSCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WPSClient provides an abstraction layer to access a Web Processing Service (WPS). It may be invoked from a command
 * line tool or from a web application
 * 
 * TODO Enhance Exception handling
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

    private WPSCapabilities serviceCapabilities;

    // [0]: Get, [1]: Post
    private URL[] describeProcessURLs = new URL[2];

    // [0]: Get, [1]: Post
    private URL[] executeURLs = new URL[2];

    private Map<CodeType, Process> processIdToProcess = new HashMap<CodeType, Process>();

    private XMLAdapter capabilitesDoc;

    /**
     * Public constructor to access a WPS instance based on its GetCapabilities URL
     * 
     * @param capabilitiesURL
     *            url to a WPS instance
     */
    public WPSClient( URL capabilitiesURL ) {
        try {
            this.capabilitesDoc = new XMLAdapter( capabilitiesURL );
        } catch ( Exception e ) {
            LOG.error( e.getLocalizedMessage(), e );
            throw new NullPointerException( "Could not read from URL: " + capabilitiesURL + " error was: "
                                            + e.getLocalizedMessage() );
        }

        checkCapabilities( this.capabilitesDoc );

        // TODO what if server only supports Get? What is optional and what is mandatory?
        describeProcessURLs[0] = getOperationURL( "DescribeProcess", false );
        describeProcessURLs[1] = getOperationURL( "DescribeProcess", true );
        executeURLs[0] = getOperationURL( "Execute", false );
        executeURLs[1] = getOperationURL( "Execute", true );

        serviceCapabilities = new WPSCapabilities( this, this.capabilitesDoc );
        for ( Process process : serviceCapabilities.getProcessOfferings() ) {
            CodeType processId = process.getId();
            processIdToProcess.put( processId, process );
        }
    }

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
            String msg = "Error in WPS capabilities document. Cannot determine URL for operation '" + operation + "': "
                         + t.getMessage();
            // TODO: maybe go on and use the base URL from the GetCapabilities doc
            throw new RuntimeException( msg );
        }
        return url;
    }

    /**
     * Basic check for correctness of most important WPS capabilities document * values. TODO enhance!
     * 
     * @param capabilities
     */
    private void checkCapabilities( XMLAdapter capabilities ) {
        OMElement root = capabilities.getRootElement();
        String version = root.getAttributeValue( new QName( "version" ) );
        if ( !"1.0.0".equals( version ) ) {
            throw new IllegalArgumentException( get( "WPSCLIENT.WRONG_VERSION_CAPABILITIES", version, "1.0.0" ) );
        }
        String service = root.getAttributeValue( new QName( "service" ) );
        if ( !service.equalsIgnoreCase( "WPS" ) ) {
            throw new IllegalArgumentException( get( "WPSCLIENT.NO_WPS_CAPABILITIES", service, "WPS" ) );
        }
    }

    // /**
    // *
    // * @return the capabilities of a service
    // */
    // public WPSCapabilities getCapabilities() {
    // return serviceCapabilities;
    // }

    public String getServiceVersion() {
        return "1.0.0";
    }

    public LanguageString getServiceTitle() {
        return null;
    }

    public LanguageString getServiceAbstract() {
        return null;
    }

    public URL getDescribeProcessURL( boolean post ) {
        return describeProcessURLs[post ? 1 : 0];
    }

    public URL getExecuteURL( boolean post ) {
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
     * Retrieve Process by providing its id as string.
     * 
     * @param processId
     *            the id as {@link CodeType}
     * @return {@link Process} instance containing all relevant process information.
     */
    public Process getProcess( String id, String idCodeSpace ) {
        if ( !processIdToProcess.containsKey( new CodeType( id, idCodeSpace ) ) ) {
            throw new RuntimeException( "WPS has no registered process with id " + id );
        }
        return processIdToProcess.get( new CodeType( id, idCodeSpace ) );
    }

}
