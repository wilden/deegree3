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

package org.deegree.services.wps.output;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.services.controller.wps.storage.OutputStorage;
import org.deegree.services.wps.processdefinition.ComplexOutputDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Identifies this {@link ProcessletOutput} to be a complex data structure encoded in XML (e.g., using GML), and
 * provides a sink for writing it.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ComplexOutputImpl extends ProcessletOutputImpl implements ComplexOutput {

    private static final Logger LOG = LoggerFactory.getLogger( ComplexOutputImpl.class );

    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    private static XMLOutputFactory outputFactory;

    private OutputStorage location;

    private OutputStream os;

    private BufferedInputStream is;

    private XMLStreamWriter streamWriter;

    private XMLStreamReader streamReader;

    private final String requestedMimeType;

    private final String requestedSchema;

    private final String requestedEncoding;

    static {
        outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", Boolean.TRUE );
    }

    public ComplexOutputImpl( ComplexOutputDefinition outputType, OutputStorage location, boolean isRequested,
                              String requestedMimeType, String requestedSchema, String requestedEncoding )
                            throws FileNotFoundException, XMLStreamException {
        super( outputType, isRequested );
        LOG.debug( "Creating sink for complex output at location '" + location + "'" );
        this.location = location;
        os = location.getOutputStream();
        this.requestedMimeType = requestedMimeType;
        this.requestedSchema = requestedSchema;
        this.requestedEncoding = requestedEncoding;
    }

    public ComplexOutputImpl( ComplexOutputDefinition outputType, OutputStream outputStream, boolean isRequested,
                              String requestedMimeType, String requestedSchema, String requestedEncoding ) {
        super( outputType, isRequested );
        LOG.debug( "Creating direct sink for complex output" );
        os = outputStream;
        this.requestedMimeType = requestedMimeType;
        this.requestedSchema = requestedSchema;
        this.requestedEncoding = requestedEncoding;        
    }

    /**
     * Returns the stream to write the output.
     * 
     * @return the stream to write the output
     */
    public OutputStream getBinaryOutputStream() {
        return os;
    }

    public XMLStreamWriter getXMLStreamWriter()
                            throws XMLStreamException {
        streamWriter = outputFactory.createXMLStreamWriter( new BufferedOutputStream( os ), "UTF-8" );
        streamWriter.writeStartDocument( "UTF-8", "1.0" );
        return streamWriter;
    }

    @Override
    public String getRequestedMimeType() {
        return requestedMimeType;
    }

    @Override
    public String getRequestedSchema() {
        return requestedSchema;
    }

    /**
     * Returns the requested encoding for the complex value, it is guaranteed that the encoding is supported for this
     * parameter (according to the process description).
     * 
     * @return the requested encoding, may be <code>null</code> (as a complex output format may omit encoding
     *         information)
     */
    public String getRequestedEncoding() {
        return requestedEncoding;
    }

    public XMLStreamReader getStreamReader() {
        return streamReader;
    }

    public void close()
                            throws XMLStreamException, IOException {

        if ( streamWriter != null ) {
            LOG.debug( "Closing sink for xml output at location '" + location + "'" );
            streamWriter.writeEndDocument();
            streamWriter.flush();
            streamWriter.close();
            if ( location != null ) {
                streamReader = inputFactory.createXMLStreamReader( new BufferedInputStream( location.getInputStream() ) );
            }
        } else {
            if ( location != null ) {
                LOG.debug( "Closing sink for raw output at location '" + location + "'" );
                os.flush();
                os.close();
                is = new BufferedInputStream( location.getInputStream() );
            }
        }
    }

    /**
     * Returns the stream to read the value.
     * 
     * @return the stream to read the value
     */
    public InputStream getInputStream() {
        return is;
    }

    public String getWebURL() {
        return location.getWebURL();
    }
}
