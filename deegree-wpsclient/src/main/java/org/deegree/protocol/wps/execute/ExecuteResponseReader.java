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
package org.deegree.protocol.wps.execute;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.wps.execute.datatypes.BinaryDataType;
import org.deegree.protocol.wps.execute.datatypes.BoundingBoxDataType;
import org.deegree.protocol.wps.execute.datatypes.ComplexAttributes;
import org.deegree.protocol.wps.execute.datatypes.DataType;
import org.deegree.protocol.wps.execute.datatypes.LiteralDataType;
import org.deegree.protocol.wps.execute.datatypes.XMLDataType;
import org.deegree.protocol.wps.execute.input.ExecuteInput;
import org.deegree.protocol.wps.execute.input.InputReference;
import org.deegree.protocol.wps.execute.output.DocumentOutputDefinition;
import org.deegree.protocol.wps.execute.output.ExecuteOutput;
import org.deegree.protocol.wps.execute.output.ExecuteStatus;
import org.deegree.protocol.wps.execute.output.OutputReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ExecuteResponseReader {

    private static Logger LOG = LoggerFactory.getLogger( ExecuteResponseReader.class );

    private XMLStreamReader reader;

    private static final String xmlNS = "http://www.w3.org/2001/XMLSchema";

    private static final String xlinkNS = "http://www.w3.org/1999/xlink";

    public ExecuteResponseReader( XMLStreamReader reader ) {
        this.reader = reader;
    }

    /**
     * @param reader
     * @return
     * @throws XMLStreamException
     */
    public ExecuteResponse parse100() {
        CodeType processId = null;
        ExecuteStatus status = null;
        List<ExecuteInput> inputs = null;
        List<DocumentOutputDefinition> outputDefs = null;
        List<ExecuteOutput> outputs = null;

        try {
            reader.next();
            processId = parseProcess();

            reader.next();
            status = parseStatus();

            reader.next();
            if ( "DataInputs".equals( reader.getName().getLocalPart() ) ) {
                inputs = parseDataInputs();
            }

            reader.next();
            if ( "OutputDefinitions".equals( reader.getName().getLocalPart() ) ) {
                outputDefs = parseDocumentOutputDefinition();
            }

            reader.next();
            if ( "ProcessOutputs".equals( reader.getName().getLocalPart() ) ) {
                outputs = parseOutputs();
            }
        } catch ( XMLStreamException e ) {
            LOG.error( "Error while parsin Execute reponse coming from WPS. " + e.getMessage() );
        }

        ExecuteInput[] inputsArray = inputs.toArray( new ExecuteInput[inputs.size()] );
        DocumentOutputDefinition[] outputDefsArray = outputDefs.toArray( new DocumentOutputDefinition[outputDefs.size()] );
        ExecuteOutput[] outputsArray = outputs.toArray( new ExecuteOutput[outputs.size()] );
        return new ExecuteResponse( processId, status, inputsArray, outputDefsArray, outputsArray );
    }

    /**
     * @return
     * @throws XMLStreamException
     */
    private List<ExecuteOutput> parseOutputs()
                            throws XMLStreamException {
        List<ExecuteOutput> outputs = new ArrayList<ExecuteOutput>();
        reader.next(); // <Output>
        while ( reader.getName().getLocalPart().equals( "Output" ) ) {
            ExecuteOutput output = null;
            CodeType id = parseIdentifier();

            OutputReference outputRef = null;
            DataType dataType = null;

            while ( !reader.getName().getLocalPart().equals( "Reference" )
                    && !reader.getName().getLocalPart().equals( "Data" ) ) {
                reader.next();
            }
            if ( reader.getName().getLocalPart().equals( "Reference" ) ) {
                String href = reader.getAttributeValue( xmlNS, "href" );
                ComplexAttributes complexAttribs = parseComplexAttributes();
                outputRef = new OutputReference( href, complexAttribs );
                output = new ExecuteOutput( id, outputRef );
                reader.next();
            }
            if ( reader.getName().getLocalPart().equals( "Data" ) ) {
                dataType = parseDataType();
                output = new ExecuteOutput( id, dataType );
                reader.next();
            }

            outputs.add( output );
            reader.next(); // </Output>
            reader.next();
        }

        reader.next(); // </ProcessOutputs>
        return outputs;
    }

    /**
     * @return
     * @throws XMLStreamException
     */
    private List<DocumentOutputDefinition> parseDocumentOutputDefinition()
                            throws XMLStreamException {
        List<DocumentOutputDefinition> outputDefs = new ArrayList<DocumentOutputDefinition>();
        reader.next(); // <Output>
        while ( reader.getName().getLocalPart().equals( "Output" ) ) {
            ComplexAttributes complexAttribs = parseComplexAttributes();
            String uom = reader.getAttributeValue( xmlNS, "uom" );

            Boolean asRef = false;
            String asRefStr = reader.getAttributeValue( xmlNS, "asReference" );
            if ( asRefStr != null ) {
                asRef = Boolean.valueOf( asRefStr );
            }

            reader.next();
            CodeType id = parseIdentifier();
            outputDefs.add( new DocumentOutputDefinition( id, uom, complexAttribs, asRef ) );

            int state = reader.next();
            while ( state != XMLStreamConstants.END_ELEMENT || !reader.getName().getLocalPart().equals( "Output" ) ) {
                state = reader.next();
            }
            reader.next();
        }
        return outputDefs;
    }

    /**
     * @return
     * @throws XMLStreamException
     */
    private List<ExecuteInput> parseDataInputs()
                            throws XMLStreamException {
        List<ExecuteInput> inputs = new ArrayList<ExecuteInput>();
        InputReference inputRef = null;
        DataType dataType = null;
        reader.nextTag(); // "Input"
        while ( "Input".equals( reader.getName().getLocalPart() ) ) {
            ExecuteInput input = null;
            reader.nextTag(); // "Identifier"
            CodeType id = parseIdentifier();
            while ( !"Reference".equals( reader.getName().getLocalPart() )
                    && !"Data".equals( reader.getName().getLocalPart() ) ) {
                reader.nextTag();
            }
            if ( "Reference".equals( reader.getName().getLocalPart() ) ) {
                inputRef = parseInputRef();
                input = new ExecuteInput( id, inputRef );
                reader.nextTag();
            }
            if ( "Data".equals( reader.getName().getLocalPart() ) ) {
                dataType = parseDataType();
                input = new ExecuteInput( id, dataType );
                reader.nextTag();
            }
            inputs.add( input );
        }
        reader.nextTag();
        return inputs;
    }

    /**
     * @return
     * @throws XMLStreamException
     */
    private DataType parseDataType()
                            throws XMLStreamException {
        DataType dataType = null;
        reader.nextTag();
        String localName = reader.getName().getLocalPart();
        if ( "ComplexData".equals( localName ) ) {
            dataType = parseComplexData();
        } else if ( "LiteralData".equals( localName ) ) {
            dataType = parseLiteralData();
        } else if ( "BoundingBoxData".equals( localName ) ) {
            dataType = parseBBoxData();
        }
        reader.next(); // </...>
        return dataType;
    }

    /**
     * @return
     * @throws XMLStreamException
     */
    private DataType parseComplexData()
                            throws XMLStreamException {
        ComplexAttributes complexAttribs = parseComplexAttributes();

        InputStream inStream = new ByteArrayInputStream( reader.getText().getBytes() );
        if ( complexAttribs.getMimeType().startsWith( "text/xml" ) ) {
            return new XMLDataType( inStream, complexAttribs );
        }
        return new BinaryDataType( inStream, complexAttribs );
    }

    /**
     * @return
     * @throws XMLStreamException
     */
    private BoundingBoxDataType parseBBoxData()
                            throws XMLStreamException {
        int dim = 2;
        String dimStr = reader.getAttributeValue( xmlNS, "dimensions" );
        if ( dimStr != null ) {
            dim = Integer.parseInt( dimStr );
        }
        String crs = reader.getAttributeValue( xmlNS, "crs" );

        reader.nextTag(); // <LowerCorner>
        String[] coordStr = reader.getText().split( "\\s" );
        double[] coords = new double[2 * dim];
        for ( int i = 0; i < dim; i++ ) {
            coords[i] = Double.parseDouble( coordStr[i] );
        }

        reader.nextTag(); // </LowerCorner>
        reader.nextTag(); // <UpperCorner>
        coordStr = reader.getText().split( "\\s" );
        for ( int i = dim; i < 2 * dim; i++ ) {
            coords[i] = Double.parseDouble( coordStr[i - dim] );
        }
        return new BoundingBoxDataType( coords, crs, dim );
    }

    /**
     * @return
     */
    private LiteralDataType parseLiteralData() {
        String value = reader.getText();
        String dataType = reader.getAttributeValue( xmlNS, "dataType" );
        String uom = reader.getAttributeValue( xmlNS, "uom" );
        return new LiteralDataType( value, dataType, uom );
    }

    /**
     * @return
     * @throws XMLStreamException
     */
    private InputReference parseInputRef()
                            throws XMLStreamException {
        Map<String, String> headers = new HashMap<String, String>();
        String body = null;
        String bodyXlink = null;
        String xlink;
        String method = "GET";
        ComplexAttributes complexAttribs = null;

        xlink = reader.getAttributeValue( xlinkNS, "href" );
        method = reader.getAttributeValue( xmlNS, "method" );
        complexAttribs = parseComplexAttributes();

        reader.nextTag();
        while ( "Header".equals( reader.getName().getLocalPart() ) ) {
            String key = reader.getAttributeValue( xmlNS, "key" );
            String value = reader.getAttributeValue( xmlNS, "value" );
            headers.put( key, value );
            reader.nextTag(); // </Header>
            reader.nextTag();
        }
        if ( "Body".equals( reader.getName().getLocalPart() ) ) {
            body = reader.getText();
            reader.nextTag(); // </Body>
            reader.nextTag();
        }
        if ( "BodyReference".equals( reader.getName().getLocalPart() ) ) {
            body = reader.getText();
            bodyXlink = reader.getAttributeValue( xlinkNS, "href" );
            reader.nextTag(); // </BodyReference>
            reader.nextTag();
        }
        return new InputReference( headers, body, bodyXlink, xlink, method );
    }

    /**
     * @return
     */
    private ComplexAttributes parseComplexAttributes() {
        String mimeType = reader.getAttributeValue( xmlNS, "mimeType" );
        String encoding = reader.getAttributeValue( xmlNS, "encoding" );
        String schema = reader.getAttributeValue( xmlNS, "schema" );
        return new ComplexAttributes( mimeType, encoding, schema );
    }

    /**
     * @return
     * @throws XMLStreamException
     */
    private ExecuteStatus parseStatus()
                            throws XMLStreamException {
        String statusMsg = null;
        Integer percent = null;
        String creationTime = null;
        String exceptionReport = null;

        String attribute = reader.getAttributeValue( xmlNS, "creationTime" );
        if ( attribute != null ) {
            creationTime = attribute;
        }

        reader.next();
        String localName = reader.getName().getLocalPart();
        if ( "ProcessAccepted".equals( localName ) || "ProcessSucceeded".equals( localName ) ) {
            statusMsg = reader.getText();
        }
        if ( "ProcessStarted".equals( localName ) || "ProcessPaused".equals( localName ) ) {
            statusMsg = reader.getText();
            String percentStr = reader.getAttributeValue( xmlNS, "percentCompleted" );
            if ( percentStr != null ) {
                percent = Integer.parseInt( percentStr );
            }
        }
        if ( "ProcessFailed".equals( localName ) ) {
            exceptionReport = parseException();
        }
        reader.next(); // </Status>
        return new ExecuteStatus( statusMsg, percent, creationTime, exceptionReport );
    }

    /**
     * @return
     * @throws XMLStreamException
     */
    private String parseException()
                            throws XMLStreamException {
        reader.nextTag(); // "ExceptionReport"
        reader.nextTag(); // "Exception"
        reader.nextTag(); // "ExceptionText"
        return reader.getText();
    }

    /**
     * @return
     * @throws XMLStreamException
     */
    private CodeType parseProcess()
                            throws XMLStreamException {
        CodeType id = null;
        reader.next();
        id = parseIdentifier();
        int state = reader.next();
        while ( state != XMLStreamConstants.END_ELEMENT || !reader.getName().getLocalPart().equals( "Process" ) ) {
            state = reader.next();
        }
        return id;
    }

    /**
     * @return
     */
    private CodeType parseIdentifier() {
        String codeSpace = reader.getAttributeValue( xmlNS, "codeSpace" );
        String code = reader.getText();
        return new CodeType( code, codeSpace );
    }
}
