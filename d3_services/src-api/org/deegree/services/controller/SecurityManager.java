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
package org.deegree.services.controller;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.wfs.WFSConstants.WFSRequestType;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.slf4j.Logger;

/**
 * Determines if a user is able to access the service with the request. If a user has rights to access a CSW but no
 * rights to request a WFS then this should be determined here.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class SecurityManager {
    private static final Logger LOG = getLogger( SecurityManager.class );

    private static List<String> WFS_OPERATIONS = new ArrayList<String>( 4 );

    private static List<String> CSW_OPERATIONS = new ArrayList<String>( 4 );

    private static List<String> WMS_OPERATIONS = new ArrayList<String>( 4 );

    private static Map<Credentials, List<String>> credRepository = new HashMap<Credentials, List<String>>( 3 );

    static {

        for ( WFSRequestType req : WFSRequestType.values() ) {
            WFS_OPERATIONS.add( req.name() );
        }

        for ( CSWRequestType req : CSWRequestType.values() ) {
            CSW_OPERATIONS.add( req.name() );
        }

        for ( WMSRequestType req : WMSRequestType.values() ) {
            WMS_OPERATIONS.add( req.name() );
        }

        credRepository.put( new Credentials( "User1", "pass1" ), WFS_OPERATIONS );
        credRepository.put( new Credentials( "User2", "pass2" ), CSW_OPERATIONS );
        credRepository.put( new Credentials( "User3", "pass3" ), WMS_OPERATIONS );
    }

    public static boolean determineRights( AbstractOGCServiceController abstractController, String operation,
                                           Credentials credentialRequest ) {
        boolean hasRights = false;

        // TODO should delegate to a databasetable in the security module in core
        // testRepository with user and password

        // List<AbstractOGCServiceController> serviceList = new ArrayList<AbstractOGCServiceController>();
        // serviceList.add( contro );

        if ( credentialRequest != null && operation != null ) {
            if ( credRepository.get( credentialRequest ) != null ) {
                LOG.debug( "there are credentials available" );
                List<String> ops = credRepository.get( credentialRequest );
                for ( String op : ops ) {
                    if ( op.equalsIgnoreCase( operation ) ) {
                        return true;
                    }
                }
            }

        } else {
            // if there is no security specified
            hasRights = true;
            LOG.debug( "no credentials available" );
        }

        return hasRights;

    }
}
