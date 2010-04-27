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
package org.deegree.test.services.hash;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.deegree.commons.utils.ByteUtils;
import org.deegree.tools.annotations.Tool;
import org.slf4j.Logger;

/**
 * <code>HashValidator</code> can be used to test a service's response by using hash codes. Useful for binary responses
 * such as WMS, but also for others.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@Tool("Validates requests against a service using expected md5 hashes.")
public class HashValidator {

    private static final Logger LOG = getLogger( HashValidator.class );

    private File dir;

    private String url;

    private HashValidator( String url, File dir ) {
        this.url = url;
        this.dir = dir;
    }

    private boolean validate()
                            throws IOException, NoSuchAlgorithmException {
        HttpClient client = new HttpClient();
        File[] fs = dir.listFiles();
        if ( fs == null ) {
            return true;
        }
        for ( File f : fs ) {
            String fileName = f.getName();
            if ( fileName.endsWith( ".md5" ) ) {
                continue;
            }
            if ( !new File( dir, fileName.substring( 0, fileName.length() - 4 ) + ".md5" ).exists() ) {
                continue;
            }

            HttpMethodBase mth;
            BufferedReader in = new BufferedReader( new FileReader( f ) );
            String req = in.readLine();
            String contentType = req.startsWith( "<?xml" ) ? "text/xml" : null;
            in.close();
            if ( contentType == null ) {
                mth = new GetMethod( url + "?" + req );
            } else {
                mth = new PostMethod( url );
                ( (PostMethod) mth ).setRequestEntity( new FileRequestEntity( f, contentType ) );
            }
            client.executeMethod( mth );
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            InputStream responseIn = mth.getResponseBodyAsStream();
            byte[] buf = new byte[65536];
            int read;
            while ( ( read = responseIn.read( buf ) ) != -1 ) {
                md.update( buf, 0, read );
            }
            responseIn.close();
            String md5 = ByteUtils.encode( md.digest() );
            File md5f = new File( f.getParent(), fileName.substring( 0, fileName.length() - 4 ) + ".md5" );
            in = new BufferedReader( new FileReader( md5f ) );
            String othermd5 = in.readLine();
            in.close();
            if ( !md5.equalsIgnoreCase( othermd5.trim() ) ) {
                LOG.info( "Got hash '{}', expected hash '{}'.", md5, othermd5 );
                LOG.warn( "'{}' produced an unexpected response against '{}'!", f, url );
                return false;
            }
        }

        return true;
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {
        if ( args.length == 0 ) {
            LOG.error( "You have to specify a service base URL." );
            LOG.error( "HashValidator <baseurl> [dir] [dir] ..." );
        }
        String url = args[0];
        for ( String dir : args ) {
            if ( dir == url ) {
                continue;
            }
            File file = new File( dir );
            if ( !file.exists() ) {
                LOG.warn( "'{}' does not exist.", dir );
                continue;
            }
            if ( !file.isDirectory() ) {
                LOG.warn( "'{}' is not a directory.", dir );
                continue;
            }

            boolean vald;
            try {
                vald = new HashValidator( url, file ).validate();
            } catch ( NoSuchAlgorithmException e ) {
                LOG.warn( "Validating procuced an error: '{}'", e.getLocalizedMessage() );
                LOG.debug( "Stack trace", e );
                vald = false;
            } catch ( IOException e ) {
                LOG.warn( "Validating procuced an error: '{}'", e.getLocalizedMessage() );
                LOG.debug( "Stack trace", e );
                vald = false;
            }
            if ( vald ) {
                LOG.info( "'{}' validated fine against '{}'", dir, url );
            } else {
                LOG.warn( "'{}' did not validate against '{}'!", dir, url );
            }
        }
    }

}
