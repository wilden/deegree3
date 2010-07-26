//$HeadURL: https://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.wps.getcapabilities;

import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wps.Process;
import org.deegree.protocol.wps.WPSClient;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * WPSCapabilities encapsulates information contained within an WPS Capabilities response
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class WPSCapabilities {

    private static final NamespaceContext NS_CONTEXT;

    private static Logger LOG = LoggerFactory.getLogger( WPSCapabilities.class );

    // TODO OperationsMetadata
    // TODO ServiceProvider / ServiceContact

    private XMLAdapter capabilitiesDoc;

    private List<Process> processes;

    private String service;

    private String version;

    private String updateSequence;

    private String lang;

    private String schemaLocation;

    private static final String owsNS = "http://www.opengis.net/ows/1.1";

    private static final String xmlNS = "http://www.w3.org/XML/1998/namespace";

    /*
     * Add namespaces relevant for the WPSCapabilities, i.e. wps and ows
     */
    static {
        NS_CONTEXT = new NamespaceContext();
        NS_CONTEXT.addNamespace( "wps", WPS_100_NS );
        NS_CONTEXT.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
    }

    /**
     * Public constructor to be initialized with a capabilities document
     * 
     * @param capabilitiesDoc
     *            an WPS capabilities document to be parsed
     * @throws JaxenException
     */
    public WPSCapabilities( WPSClient wpsclient, XMLAdapter capabilitiesDoc ) {
        LOG.debug( "WPSCapabilities initialized" );
        this.capabilitiesDoc = capabilitiesDoc;
        OMElement rootEl = capabilitiesDoc.getRootElement();
        this.service = rootEl.getAttributeValue( new QName( "service" ) );
        this.lang = rootEl.getAttributeValue( new QName( "lang" ) );
        this.version = rootEl.getAttributeValue( new QName( "version" ) );
        this.schemaLocation = rootEl.getAttributeValue( new QName( "schemaLocation" ) );
        this.updateSequence = rootEl.getAttributeValue( new QName( "updateSequence" ) );

        // find out using XPath
        URL executeURL = null;

        try {
            processes = new ArrayList<Process>();
            AXIOMXPath xpath;
            xpath = new AXIOMXPath( "wps:ProcessOfferings/wps:Process" );
            addNsToXPath( xpath );
            List<OMElement> nodes = xpath.selectNodes( rootEl );
            for ( OMElement node : nodes ) {
                xpath = new AXIOMXPath( "@wps:processVersion" );
                addNsToXPath( xpath );
                OMAttribute omVersion = (OMAttribute) xpath.selectSingleNode( node );
                String version = null;
                if ( omVersion != null ) {
                    version = omVersion.getAttributeValue();
                }

                xpath = new AXIOMXPath( "ows:Identifier" );
                addNsToXPath( xpath );
                OMElement omId = (OMElement) xpath.selectSingleNode( node );
                String codeSpace = omId.getAttributeValue( new QName( "codeSpace" ) );
                CodeType id = new CodeType( omId.getText(), codeSpace );

                xpath = new AXIOMXPath( "ows:Title" );
                addNsToXPath( xpath );
                OMElement omTitle = (OMElement) xpath.selectSingleNode( node );
                String lang = omTitle.getAttributeValue( new QName( xmlNS, "lang" ) );
                LanguageString title = new LanguageString( omTitle.getText(), lang );

                xpath = new AXIOMXPath( "ows:Abstract" );
                addNsToXPath( xpath );
                OMElement omAbstract = (OMElement) xpath.selectSingleNode( node );
                lang = omTitle.getAttributeValue( new QName( xmlNS, "lang" ) );
                LanguageString processAbstract = new LanguageString( omAbstract.getText(), lang );

                processes.add( new Process( wpsclient, version, id, title, processAbstract ) );
            }
        } catch ( JaxenException e ) {
            LOG.error( e.getMessage() );
        }

    }

    public XMLAdapter getCapabilitiesAsXMLAdapter() {
        return capabilitiesDoc;
    }

    public List<Process> getProcessOfferings() {
        return processes;
    }

    /**
     * @throws JaxenException
     * 
     */
    private void addNsToXPath( AXIOMXPath xpath )
                            throws JaxenException {
        xpath.addNamespace( "wps", WPS_100_NS );
        xpath.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "Service: " + this.service + "\n" );
        sb.append( "Version: " + this.version + "\n" );
        sb.append( "updateSequence: " + this.updateSequence + "\n" );
        sb.append( "lang: " + this.lang + "\n" );
        sb.append( "schemaLocation: " + this.schemaLocation + "\n" );
        return sb.toString();
    }

}
