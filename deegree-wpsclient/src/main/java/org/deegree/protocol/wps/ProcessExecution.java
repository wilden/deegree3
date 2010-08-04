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
package org.deegree.protocol.wps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wps.execute.ExecuteRequest;
import org.deegree.protocol.wps.execute.ExecuteResponse;
import org.deegree.protocol.wps.execute.RequestWriter;
import org.deegree.protocol.wps.execute.ResponseReader;
import org.deegree.protocol.wps.execute.datatypes.BinaryDataType;
import org.deegree.protocol.wps.execute.datatypes.BoundingBoxDataType;
import org.deegree.protocol.wps.execute.datatypes.LiteralDataType;
import org.deegree.protocol.wps.execute.datatypes.XMLDataType;
import org.deegree.protocol.wps.execute.input.ExecuteInput;
import org.deegree.protocol.wps.execute.output.OutputDefinition;
import org.deegree.protocol.wps.execute.output.ResponseFormat;
import org.deegree.services.controller.ows.OWSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an execution context for a {@link Process}.
 * 
 * @see Process
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ProcessExecution {

    private static Logger LOG = LoggerFactory.getLogger( ProcessExecution.class );

    private final WPSClient client;

    private final Process process;

    private List<ExecuteInput> inputs;

    private List<OutputDefinition> outputDefs;

    private ResponseFormat responseFormat;

    private boolean rawOutput;

    /**
     * Creates a new {@link ProcessExecution} instance.
     * 
     * @param client
     *            associated WPS client instance, must not be <code>null</code>
     * @param process
     *            associated process instance, must not be <code>null</code>
     */
    ProcessExecution( WPSClient client, Process process ) {
        this.client = client;
        this.process = process;
        inputs = new ArrayList<ExecuteInput>();
        outputDefs = new ArrayList<OutputDefinition>();
    }

    /**
     * Adds a literal input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param codeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (no codespace)
     * @param value
     *            value of the literal input, must not be <code>null</code>
     * @param type
     *            data type in which the value should be considered, may be <code>null</code> (this means it matches the
     *            data type as defined by the process description)
     * @param uom
     *            unit of measure of the value, may be <code>null</code> (this means it matches the
     *            data type as defined by the process description)
     */
    public void addLiteralInput( String id, String codeSpace, String value, String type, String uom ) {
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), new LiteralDataType( value, type, uom ) ) );
    }

    /**
     * Add Bounding box input data under the specified id.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param codeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (no codespace)
     * @param coordinates
     *            {@link double} array of coordinates: x0, y0, x1, x2, etc.
     * @param crs
     *            coordinates system of the bbox, as string, may be null
     * @param dim
     *            dimension of the bbox, e.g. 2 for plane coordinates
     */
    public void addBBoxInput( String id, String codeSpace, double[] coordinates, String crs, int dim ) {
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), new BoundingBoxDataType( coordinates, crs, dim ) ) );
    }

    /**
     * Add XML input data under the specified id as URL.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param codeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (no codespace)
     * @param url
     *            {@link URL} to the xml data, must not be null
     * @param mimeType
     *            mime type of the xml data, may be null
     * @param encoding
     *            encoding of the xml data, may be null
     * @param schema
     *            schema of the xml fragment, may be null
     */
    public void addXMLInput( String id, String codeSpace, URL url, String mimeType, String encoding, String schema ) {
        XMLDataType xmlData = new XMLDataType( url, false, mimeType, encoding, schema );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), xmlData ) );
    }

    /**
     * Add XML input data under the specified id as XML stream reader.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param codeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (no codespace)
     * @param reader
     *            {@link XMLStreamReader} to the xml data, mustn't be null
     * @param mimeType
     *            mime type of the xml data, may be null
     * @param encoding
     *            encoding of the xml data, may be null
     * @param schema
     *            schema of the xml fragment, may be null
     */
    public void addXMLInput( String id, String codeSpace, XMLStreamReader reader, String mimeType, String encoding,
                             String schema ) {
        XMLDataType xmlDataType = new XMLDataType( reader, mimeType, encoding, schema );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), xmlDataType ) );
    }

    /**
     * Add binary input data under the specified id as URL.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param codeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (no codespace)
     * @param url
     *            {@link URL} to the binary data, mustn't be null
     * @param mimeType
     *            mime type of the binary data, may be null
     * @param encoding
     *            encoding of the binary data, may be null
     */
    public void addBinaryInput( String id, String codeSpace, URL url, String mimeType, String encoding ) {
        BinaryDataType binaryData = new BinaryDataType( url, false, mimeType, encoding );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), binaryData ) );
    }

    /**
     * Add binary input data under the specified id as input stream.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param codeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (no codespace)
     * @param inputStream
     *            input stream to the binary data, mustn't be null
     * @param mimeType
     *            mime type of the binary data, may be null
     * @param encoding
     *            encoding of the binary data, may be null
     */
    public void addBinaryInput( String id, String codeSpace, InputStream inputStream, String mimeType, String encoding ) {
        BinaryDataType binaryData = new BinaryDataType( inputStream, mimeType, encoding );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), binaryData ) );
    }

    /**
     * Set format of the identified output as an XML response document. Use this method or
     * {@link #setRawOutput(String, String, String, String, String) } to specify the format in which the output should be
     * presented.
     * 
     * @param id
     *            identifier of the output parameter, must not be <code>null</code>
     * @param codeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (no codespace)
     * @param uom
     *            unit of measure, in case it is a Literal Output, otherwise null
     * @param asRef
     *            return output as an URL
     * @param mimeType
     *            mimeType of the data, may be null
     * @param encoding
     *            encoding of data, may be null
     * @param schema
     */
    public void setRequestedOutput( String id, String codeSpace, String uom, boolean asRef, String mimeType,
                                    String encoding, String schema ) {
        outputDefs.add( new OutputDefinition( new CodeType( id ), uom, asRef, mimeType, encoding, schema ) );
    }

    /**
     * Set format of the identified output as raw data. Use this method or
     * {@link #setRequestedOutput(String, String, String, boolean, String, String, String)} to specify the format in
     * which the output should be presented.
     * 
     * @param id
     *            identifier of the output parameter, must not be <code>null</code>
     * @param codeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (no codespace)
     * @param mimeType
     *            mimeType of the data, may be null
     * @param encoding
     *            encoding of data, may be null
     * @param schema
     *            schema of data, in case it is an XML document
     * @throws Exception
     */
    public void setRawOutput( String id, String codeSpace, String mimeType, String encoding, String schema )
                            throws Exception {
        outputDefs.add( new OutputDefinition( new CodeType( id ), null, false, mimeType, encoding, schema ) );
        rawOutput = true;
        if ( outputDefs.size() > 1 ) {
            throw new Exception( "A raw response can be delivered only for one output parameter." );
        }
    }

    /**
     * Perform the execute request synchronously.
     * 
     * @return {@link ExecuteResponse} instance that provides access to the output data.
     * @throws OWSException
     * @throws IOException
     * @throws XMLStreamException
     */
    public ExecuteResponse execute()
                            throws OWSException, IOException, XMLStreamException {

        responseFormat = new ResponseFormat( rawOutput, false, false, false, outputDefs );

        ExecuteResponse response = null;
        // TODO what if server only supports Get?
        URL url = client.getExecuteURL( true );

        URLConnection conn = url.openConnection();
        conn.setDoOutput( true );
        conn.setUseCaches( false );
        // TODO does this need configurability?
        conn.setRequestProperty( "Content-Type", "application/xml" );

        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = outFactory.createXMLStreamWriter( conn.getOutputStream() );

        // XMLStreamWriter writer = outFactory.createXMLStreamWriter( new FileOutputStream(
        // File.createTempFile(
        // "wpsClientIn",
        // ".xml" ) ) );

        String version = client.getServiceVersion();
        ExecuteRequest executeRequest = new ExecuteRequest( process.getId(), version, inputs, responseFormat );
        RequestWriter executer = new RequestWriter( writer );
        executer.write100( executeRequest );
        writer.flush();
        writer.close();

        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = inFactory.createXMLStreamReader( conn.getInputStream() );

        reader.nextTag(); // so that it points to START_ELEMENT, hence prepared to be processed by XMLAdapter

        if ( LOG.isDebugEnabled() ) {
            File logOutputFile = File.createTempFile( "wpsClient", "Out.xml" );
            OutputStream outStream = new FileOutputStream( logOutputFile );
            XMLStreamWriter straightWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( outStream );
            XMLAdapter.writeElement( straightWriter, reader );
            LOG.debug( "Service output can be found at " + logOutputFile.toString() );
            straightWriter.close();

            reader = XMLInputFactory.newInstance().createXMLStreamReader( new FileInputStream( logOutputFile ) );
        }

        ResponseReader responseReader = new ResponseReader( reader );
        response = responseReader.parse100();
        reader.close();

        return response;
    }

    /**
     * @param updateStatus
     */
    public void executeAsync( boolean updateStatus ) {
        throw new UnsupportedOperationException( "Async execution is not implemented yet." );
    }
}
