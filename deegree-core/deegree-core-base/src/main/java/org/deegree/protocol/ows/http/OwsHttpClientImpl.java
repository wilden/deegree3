//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.ows.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link OwsHttpClient}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OwsHttpClientImpl implements OwsHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger( OwsHttpClientImpl.class );

    private final String user;

    private final String pass;

    private final DefaultHttpClient httpClient;

    public OwsHttpClientImpl( String user, String pass ) {
        this.user = user;
        this.pass = pass;
        this.httpClient = initHttpClient();
    }

    private DefaultHttpClient initHttpClient() {
        ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager();
        return new DefaultHttpClient( connManager );
    }

    @Override
    public OwsResponse doGet( URL endPoint, Map<String, String> params, Map<String, String> headers )
                            throws IOException {

        OwsResponse response = null;
        URI query = null;
        try {
            URL normalizedEndpointUrl = normalizeGetUrl( endPoint );
            StringBuilder sb = new StringBuilder( normalizedEndpointUrl.toString() );
            boolean first = true;
            if ( params != null ) {
                for ( Entry<String, String> param : params.entrySet() ) {
                    if ( !first ) {
                        sb.append( '&' );
                    } else {
                        first = false;
                    }
                    sb.append( URLEncoder.encode( param.getKey(), "UTF-8" ) );
                    sb.append( '=' );
                    sb.append( URLEncoder.encode( param.getValue(), "UTF-8" ) );
                }
            }

            query = new URI( sb.toString() );
            setCredentials( endPoint );
            HttpGet httpGet = new HttpGet( query );
            LOG.info( "Performing GET request: " + query );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            response = new OwsResponse( query, httpResponse );
        } catch ( Throwable e ) {
            e.printStackTrace();
            String msg = "Error performing GET request on '" + query + "': " + e.getMessage();
            throw new IOException( msg );
        }
        return response;
    }

    @Override
    public OwsResponse doPost( URL endPoint, String contentType, StreamBufferStore body, Map<String, String> headers )
                            throws IOException {

        OwsResponse response = null;
        try {
            HttpPost httpPost = new HttpPost( endPoint.toURI() );
            LOG.debug( "Performing POST request on " + endPoint );
            LOG.debug( "post size: " + body.size() );
            InputStreamEntity entity = new InputStreamEntity( body.getInputStream(), (long) body.size() );
            entity.setContentType( contentType );
            httpPost.setEntity( entity );
            setCredentials( endPoint );
            HttpResponse httpResponse = httpClient.execute( httpPost );
            response = new OwsResponse( endPoint.toURI(), httpResponse );
        } catch ( Throwable e ) {
            String msg = "Error performing POST request on '" + endPoint + "': " + e.getMessage();
            throw new IOException( msg );
        }
        return response;
    }

    private void setCredentials( URL url ) {
        if ( user != null ) {
            httpClient.getCredentialsProvider().setCredentials( new AuthScope( url.getHost(), url.getPort() ),
                                                                new UsernamePasswordCredentials( user, pass ) );
        }
    }

    protected URL normalizeGetUrl( URL url )
                            throws MalformedURLException {
        // TODO: this method does not work. url.getQuery is the query part not the base url
        String s = url.toString();
        if ( url.getQuery() != null ) {
            if ( !s.endsWith( "&" ) ) {
                s += "&";
            }
        } else {
            if ( !s.endsWith( "?" ) ) {
                s += "?";
            }
        }
        return new URL( s );
    }
}
