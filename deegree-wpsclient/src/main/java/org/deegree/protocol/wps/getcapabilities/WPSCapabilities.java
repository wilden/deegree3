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

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
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

    private String service;

    private String version;

    private String updateSequence;

    private String lang;

    private String schemaLocation;

    /*
     * Add namespaces relevant for the WPSCapabilities, i.e. wps and ows
     */
    static {
        NS_CONTEXT = new NamespaceContext();
        NS_CONTEXT.addNamespace( "wps", WPS_100_NS );
        NS_CONTEXT.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
    }

    public XMLAdapter getCapabilities() {
        return capabilitiesDoc;
    }

    public List<ProcessBrief> getProcessOfferings() {
        // TODO
        return null;
    }

    /**
     * Public constructor to be initialized with a capabilities document
     * 
     * @param capabilitiesDoc
     *            an WPS capabilities document to be parsed
     */
    public WPSCapabilities( XMLAdapter capabilitiesDoc ) {
        LOG.debug( "WPSCapabilities initialized" );
        this.capabilitiesDoc = capabilitiesDoc;
        OMElement rootElement = capabilitiesDoc.getRootElement();
        this.service = rootElement.getAttributeValue( new QName( "service" ) );
        this.lang = rootElement.getAttributeValue( new QName( "lang" ) );
        this.version = rootElement.getAttributeValue( new QName( "version" ) );
        this.schemaLocation = rootElement.getAttributeValue( new QName( "schemaLocation" ) );
        this.updateSequence = rootElement.getAttributeValue( new QName( "updateSequence" ) );
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
