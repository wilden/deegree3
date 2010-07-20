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

import static org.deegree.protocol.i18n.Messages.get;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wps.getcapabilities.ProcessBrief;
import org.deegree.protocol.wps.getcapabilities.WPSCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WPSClient provides an abstraction layer to access a Web Processing Service (WPS). It may be invoked from a command
 * line tool or from a web application
 * 
 * TODO Enhance Exception handling
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSClient {

    private static Logger LOG = LoggerFactory.getLogger( WPSClient.class );

    private WPSCapabilities serviceCapabilities;

    private Map<String, Process> processIdToProcess = new HashMap<String, Process>();

    private XMLAdapter capabilitesDoc;

    /**
     * Public constructor to access a WPS instance based on its GetCapabilities URL
     * 
     * @param capabilitiesURL
     *            url to a WPS instance
     * @throws MalformedURLException
     *             in case a DescribeProcess URL could not constructed from WPS Capabilities response
     * 
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

        serviceCapabilities = new WPSCapabilities( this.capabilitesDoc );
        for ( ProcessBrief offering : serviceCapabilities.getProcessOfferings() ) {
            String processId = offering.getIdentifier();
            Process process = new Process( capabilitiesURL.toExternalForm(), new CodeType( processId ),
                                           offering.getTitle(), offering.getAbstract() );
            processIdToProcess.put( processId, process );
        }
    }

    /**
     * Basic check for correctness of most important WPS capabilities document values. TODO enhance!
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

    /**
     * 
     * @return the capabilities of a service
     */
    public WPSCapabilities getCapabilities() {
        return serviceCapabilities;
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
     * Retrieve Process by providing its id.
     * 
     * @param processId
     * @return {@link Process} instance containing all relevant process information.
     */
    public Process getProcess( String processId ) {
        if ( !processIdToProcess.containsKey( processId ) ) {
            throw new RuntimeException( "WPS has no registered process with id " + processId );
        }
        return processIdToProcess.get( processId );
    }

    /**
     * Retrieve Process by providing its id.
     * 
     * @param processId
     * @return {@link Process} instance containing all relevant process information.
     */
    public Process getProcess( CodeType processId ) {
        // TODO
        return null;
    }

    // public void executeRequest( String processId, ClientInput[] inputs, ResponseFormType[] outputFormats ) {
    // getProcess( processId ).execute( inputs, outputFormats );
    // }

    /**
     * Retrieve all process ids.
     * 
     * @return a String array of all ids.
     */
    public CodeType[] getProcessIdentifiers() {
        List<CodeType> processIds = new ArrayList<CodeType>();
        Process[] allProcesses = getProcesses();
        for ( int i = 0; i < allProcesses.length; i++ ) {
            processIds.add( allProcesses[i].getId() );
        }
        return processIds.toArray( new CodeType[processIds.size()] );
    }

}
