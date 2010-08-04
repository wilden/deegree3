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
import org.deegree.protocol.wps.execute.ExceptionReport;
import org.deegree.protocol.wps.execute.ExecuteWriter;
import org.deegree.protocol.wps.execute.ExecutionResults;
import org.deegree.protocol.wps.execute.ResponseReader;
import org.deegree.protocol.wps.execute.datatypes.BinaryDataType;
import org.deegree.protocol.wps.execute.datatypes.BoundingBoxDataType;
import org.deegree.protocol.wps.execute.datatypes.LiteralDataType;
import org.deegree.protocol.wps.execute.datatypes.XMLDataType;
import org.deegree.protocol.wps.execute.input.ExecuteInput;
import org.deegree.protocol.wps.execute.output.ExecutionStatus;
import org.deegree.protocol.wps.execute.output.OutputDefinition;
import org.deegree.protocol.wps.execute.output.ExecuteOutputs;
import org.deegree.protocol.wps.execute.output.ResponseFormat;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.wps.ProcessExecution.ExecutionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an execution context for a {@link Process}.
 * <p>
 * NOTE: This class is not thread-safe.
 * </p>
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

    private ExecutionStatus status;

    private ExecuteOutputs processOutputs;

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
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param value
     *            value of the literal input, must not be <code>null</code>
     * @param type
     *            data type in which the value should be considered, may be <code>null</code> (this means it matches the
     *            data type as defined by the process description)
     * @param uom
     *            unit of measure of the value, may be <code>null</code> (this means it matches the data type as defined
     *            by the process description)
     */
    public void addLiteralInput( String id, String idCodeSpace, String value, String type, String uom ) {
        inputs.add( new ExecuteInput( new CodeType( id, idCodeSpace ), new LiteralDataType( value, type, uom ) ) );
    }

    /**
     * Adds a bounding box input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param lower
     *            coordinates of the lower point, must not be <code>null</code>
     * @param upper
     *            coordinates of the upper point, must not be <code>null</code> and length must match lower point
     * @param crs
     *            coordinate system, may be <code>null</code> (indicates that the default crs from the parameter
     *            description applies)
     */
    public void addBBoxInput( String id, String idCodeSpace, double[] lower, double[] upper, String crs ) {
        inputs.add( new ExecuteInput( new CodeType( id, idCodeSpace ), new BoundingBoxDataType( lower, upper, crs ) ) );
    }

    /**
     * Adds an XML-valued complex input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param url
     *            {@link URL} reference to the xml resource, must not be <code>null</code> (and must not be
     *            web-accessible)
     * @param mimeType
     *            mime type, may be <code>null</code> (indicates that the default mime type from the parameter
     *            description applies)
     * @param encoding
     *            encoding, may be <code>null</code> (indicates that the default encoding from the parameter description
     *            applies)
     * @param schema
     *            schema, may be <code>null</code> (indicates that the default schema from the parameter description
     *            applies)
     */
    public void addXMLInput( String id, String idCodeSpace, URL url, String mimeType, String encoding, String schema ) {
        XMLDataType xmlData = new XMLDataType( url, false, mimeType, encoding, schema );
        inputs.add( new ExecuteInput( new CodeType( id, idCodeSpace ), xmlData ) );
    }

    /**
     * Adds an XML-valued complex input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param reader
     *            {@link XMLStreamReader} to the xml data, must not be <code>null</code> and point to the START_ELEMENT
     *            event
     * @param mimeType
     *            mime type, may be <code>null</code> (indicates that the default mime type from the parameter
     *            description applies)
     * @param encoding
     *            encoding, may be <code>null</code> (indicates that the default encoding from the parameter description
     *            applies)
     * @param schema
     *            schema, may be <code>null</code> (indicates that the default schema from the parameter description
     *            applies)
     */
    public void addXMLInput( String id, String idCodeSpace, XMLStreamReader reader, String mimeType, String encoding,
                             String schema ) {
        XMLDataType xmlDataType = new XMLDataType( reader, mimeType, encoding, schema );
        inputs.add( new ExecuteInput( new CodeType( id, idCodeSpace ), xmlDataType ) );
    }

    /**
     * Adds a binary-valued complex input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param url
     *            {@link URL} reference to the binary resource, must not be <code>null</code> (and must not be
     *            web-accessible)
     * @param mimeType
     *            mime type, may be <code>null</code> (indicates that the default mime type from the parameter
     *            description applies)
     * @param encoding
     *            encoding, may be <code>null</code> (indicates that the default encoding from the parameter description
     *            applies)
     */
    public void addBinaryInput( String id, String idCodeSpace, URL url, String mimeType, String encoding ) {
        BinaryDataType binaryData = new BinaryDataType( url, false, mimeType, encoding );
        inputs.add( new ExecuteInput( new CodeType( id, idCodeSpace ), binaryData ) );
    }

    /**
     * Adds a binary-valued complex input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param inputStream
     *            input stream to the binary data, must not be <code>null</code>
     * @param mimeType
     *            mime type, may be <code>null</code> (indicates that the default mime type from the parameter
     *            description applies)
     * @param encoding
     *            encoding, may be <code>null</code> (indicates that the default encoding from the parameter description
     *            applies)
     */
    public void addBinaryInput( String id, String idCodeSpace, InputStream inputStream, String mimeType, String encoding ) {
        BinaryDataType binaryData = new BinaryDataType( inputStream, mimeType, encoding );
        inputs.add( new ExecuteInput( new CodeType( id, idCodeSpace ), binaryData ) );
    }

    /**
     * Adds the specified parameter to the list of explicitly requested output parameters.
     * <p>
     * Calling this method sets the <code>ResponseForm</code> to <code>ResponseDocument</code>.
     * </p>
     * 
     * @param id
     *            identifier of the output parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param uom
     *            requested unit of measure, may be <code>null</code> (indicates that the default mime type from the
     *            parameter description applies). This parameter only applies for literal outputs.
     * @param asRef
     *            if true, the output should be returned by the process as a reference, otherwise it will be embedded in
     *            the response document
     * @param mimeType
     *            requested mime type, may be <code>null</code> (indicates that the default mime type from the parameter
     *            description applies)
     * @param encoding
     *            requested encoding, may be <code>null</code> (indicates that the default encoding from the parameter
     *            description applies)
     * @param schema
     *            requested schema, may be <code>null</code> (indicates that the default schema from the parameter
     *            description applies)
     */
    public void addOutput( String id, String idCodeSpace, String uom, boolean asRef, String mimeType, String encoding,
                           String schema ) {
        outputDefs.add( new OutputDefinition( new CodeType( id ), uom, asRef, mimeType, encoding, schema ) );
        rawOutput = false;
    }

    /**
     * Sets an explicitly requested output parameter and sets the <code>ResponseForm</code> of the <code>Execute</code>
     * request to <code>RawOutput</code>.
     * <p>
     * By calling this method, the server will only be able to respond with a single output parameter. If you require
     * multiple output parameters, use {@link #addOutput(String, String, String, boolean, String, String, String)}.
     * </p>
     * 
     * @param id
     *            identifier of the output parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param mimeType
     *            mimeType of the data, may be null
     * @param encoding
     *            encoding of data, may be null
     * @param schema
     *            schema of data, in case it is an XML document
     */
    public void setRawOutput( String id, String idCodeSpace, String mimeType, String encoding, String schema ) {
        outputDefs.add( new OutputDefinition( new CodeType( id ), null, false, mimeType, encoding, schema ) );
        rawOutput = true;
        if ( outputDefs.size() > 1 ) {
            throw new RuntimeException( "A raw response can be delivered only for one output parameter." );
        }
    }

    /**
     * Executes the process and returns the outputs.
     * 
     * @return process outputs, never <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     * @throws XMLStreamException
     */
    public ExecuteOutputs execute()
                            throws OWSException, IOException, XMLStreamException {

        responseFormat = new ResponseFormat( rawOutput, false, false, false, outputDefs );

        ExecutionResults response = null;
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

        ExecuteWriter executer = new ExecuteWriter( writer );
        executer.write100( process.getId(), inputs, responseFormat );
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
            LOG.debug( "WPS response can be found at " + logOutputFile.toString() );
            straightWriter.close();

            reader = XMLInputFactory.newInstance().createXMLStreamReader( new FileInputStream( logOutputFile ) );
        }

        ResponseReader responseReader = new ResponseReader( reader );
        response = responseReader.parse100();
        reader.close();

        status = response.getStatus();
        processOutputs = new ExecuteOutputs( response.getOutputs() );

        return processOutputs;
    }

    /**
     * Executes the process asynchronously.
     * <p>
     * This method issues the <code>Execute</code> request against the server and returns immediately.
     * </p>
     * 
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public void startAsync()
                            throws IOException, OWSException {
        throw new UnsupportedOperationException( "Async execution is not implemented yet." );
    }

    /**
     * Returns the outputs of the process execution.
     * 
     * @return the outputs of the process execution, or <code>null</code> if the current state is not
     *         {@link ExecutionState#SUCCEEDED}
     */
    public ExecuteOutputs getOutputs() {
        return processOutputs;
    }

    /**
     * Returns the current state of the execution.
     * 
     * @return state of the execution, or <code>null</code> if the execution has not been started yet
     */
    public ExecutionState getState() {
        if ( status == null ) {
            return null;
        }
        return status.getState();
    }

    /**
     * Returns the status message.
     * 
     * @return status message, or <code>null</code> if the execution has not been started yet or no status message
     *         available
     */
    public String getStatusMessage() {
        if ( status == null ) {
            return null;
        }
        return status.getStatusMessage();
    }

    /**
     * Returns the percentage of the process that has been completed.
     * 
     * @return the completed percentage of the process, or <code>null</code> if the execution has not been started yet
     *         or no completion percentage provided by the process
     */
    public Integer getPercentCompleted() {
        if ( status == null ) {
            return null;
        }
        return status.getPercentCompleted();
    }

    /**
     * @return creation time of the process execution, never <code>null</code>
     */
    public String getCreationTime() {
        if ( status == null ) {
            return null;
        }
        return status.getCreationTime();
    }

    /**
     * Returns the exception report.
     * <p>
     * NOTE: An exception report is only available if state is {@link ExecutionState#FAILED}.
     * </p>
     * 
     * @return an exception message in case the execution failed, <code>null</code> otherwise
     */
    public ExceptionReport getExceptionReport() {
        if ( status == null ) {
            return null;
        }
        return status.getExceptionReport();
    }
}
