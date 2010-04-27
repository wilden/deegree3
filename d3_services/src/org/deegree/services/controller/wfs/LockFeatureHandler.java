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

package org.deegree.services.controller.wfs;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.deegree.feature.types.FeatureType;
import org.deegree.protocol.wfs.lockfeature.LockFeature;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wfs.WFService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link LockFeature} requests for the {@link WFSController}.
 * 
 * @see WFSController
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class LockFeatureHandler extends GetFeatureHandler {

    private static final Logger LOG = LoggerFactory.getLogger( LockFeatureHandler.class );

    /**
     * Creates a new {@link LockFeatureHandler} instance that uses the given service to lookup requested
     * {@link FeatureType}s.
     * 
     * @param master
     * 
     * @param service
     *            WFS instance used to lookup the feature types
     * @param streamMode
     *            if <code>true</code>, features are streamed (implies that the FeatureCollection's boundedBy-element
     *            cannot be populated and that the numberOfFeatures attribute cannot be written)
     */
    LockFeatureHandler( WFSController master, WFService service, boolean streamMode ) {
        super( master, service, streamMode );
    }

    /**
     * Performs the given {@link LockFeature} request.
     * 
     * @param request
     *            request to be handled
     * @param response
     *            response that is used to write the result
     * @throws OWSException
     *             if a WFS specific exception occurs, e.g. a requested feature type is not served
     * @throws IOException
     * @throws XMLStreamException
     */
    void doLockFeature( LockFeature request, HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException {

        LOG.debug( "doLockFeature: " + request );
        /*
         * try { // TODO strategy for multiple LockManagers / feature stores LockManager manager = null; try { manager =
         * service.getStores()[0].getLockManager(); } catch ( FeatureStoreException e ) { throw new OWSException(
         * "Cannot acquire lock manager: " + e.getMessage(), ControllerException.NO_APPLICABLE_CODE ); }
         * 
         * // default: lock all boolean lockAll = true; if ( request.getLockAll() != null ) { lockAll =
         * request.getLockAll(); }
         * 
         * // default: 5 minutes int expiry = 5 * 60 * 1000; if ( request.getExpiry() != null ) { expiry =
         * request.getExpiry() * 60 * 1000; }
         * 
         * Lock lock = manager.acquireLock( request.getLocks(), lockAll, expiry );
         * 
         * if ( VERSION_110.equals( request.getVersion() ) ) { String ns = WFS_NS; String schemaLocation = ns + " " +
         * WFS_110_SCHEMA_URL;
         * 
         * response.setContentType( "text/xml" ); XMLStreamWriter writer = WFSController.getXMLResponseWriter( response,
         * schemaLocation ); writer.writeStartDocument(); writer.writeStartElement( "wfs", "LockFeatureResponse", ns );
         * writer.setPrefix( "ogc", OGCNS ); writer.setPrefix( "wfs", WFS_NS ); XMLAdapter.writeElement( writer, WFS_NS,
         * "LockId", lock.getId() );
         * 
         * if ( lock.getNumLocked() > 0 ) { writer.writeStartElement( "wfs", "FeaturesLocked", ns );
         * CloseableIterator<String> fidIter = lock.getLockedFeatures(); while ( fidIter.hasNext() ) { String fid =
         * fidIter.next(); writer.writeEmptyElement( OGCNS, "FeatureId" ); writer.writeAttribute( "fid", fid ); }
         * writer.writeEndElement(); fidIter.close(); }
         * 
         * if ( lock.getNumFailedToLock() > 0 ) { writer.writeStartElement( "wfs", "FeaturesNotLocked", ns );
         * CloseableIterator<String> fidIter = lock.getFailedToLockFeatures(); while ( fidIter.hasNext() ) { String fid
         * = fidIter.next(); writer.writeStartElement( "ogc", "FeatureId", OGCNS ); writer.writeCharacters( fid );
         * writer.writeEndElement(); } writer.writeEndElement(); fidIter.close(); }
         * 
         * // close LockFeatureResponse writer.writeEndElement(); writer.writeEndDocument(); writer.flush(); } else {
         * throw new RuntimeException( "Not implemented yet." ); } } catch ( FeatureStoreException e ) { throw new
         * OWSException( e.getMessage(), ControllerException.NO_APPLICABLE_CODE ); }
         */
    }
}
