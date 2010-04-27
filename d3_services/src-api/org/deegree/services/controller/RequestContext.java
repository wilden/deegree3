//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

import javax.servlet.http.HttpServletRequest;

/**
 * Encapsulates security and other information that are associated with the currently processed request.
 *
 * @see OGCFrontController#getServletContext()
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class RequestContext {

    private String requestedBaseURL;

    private String username;

    private String password;

    private String tokenId;

    /**
     * Creates a new {@link RequestContext} instance.
     *
     * @param request
     *            request for which the context will be created
     * @param username
     *            user name associated with the request
     * @param password
     *            password associated with the request
     * @param tokenId
     *            id of the security token associated with the request
     */
    RequestContext( HttpServletRequest request, String username, String password, String tokenId ) {
        this.requestedBaseURL = request.getRequestURL().toString();
        this.username = username;
        this.password = password;
        this.tokenId = tokenId;
    }

    /**
     * Returns the user name associated with the request.
     *
     * @return the user name associated with the request (may be null)
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password associated with the request.
     *
     * @return the password associated with the request (may be null)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the security token associated with the request.
     *
     * @return the security token associated with the request (may be null)
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * Returns the base URL that was used to contact the {@link OGCFrontController} and initiated the request.
     *
     * @return the base URL
     */
    public String getRequestedBaseURL() {
        return requestedBaseURL;
    }

    @Override
    public String toString() {
        return "{user=" + username + ",password=" + password + ",tokenID=" + tokenId + ",requestURL="
               + requestedBaseURL + "}";
    }
}
