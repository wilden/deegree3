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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.protocol.wps.describeprocess.ComplexAttributes;
import org.deegree.protocol.wps.execute.datatypes.BinaryDataType;
import org.deegree.protocol.wps.execute.datatypes.BoundingBoxDataType;
import org.deegree.protocol.wps.execute.datatypes.DataType;
import org.deegree.protocol.wps.execute.datatypes.LiteralDataType;
import org.deegree.protocol.wps.execute.datatypes.XMLDataType;
import org.deegree.protocol.wps.execute.input.ExecuteInput;
import org.deegree.protocol.wps.execute.input.InputReference;
import org.deegree.protocol.wps.execute.output.ExecuteOutput;
import org.deegree.protocol.wps.execute.output.ExecuteStatus;
import org.deegree.protocol.wps.execute.output.OutputDefinition;
import org.deegree.services.controller.ows.OWSException;
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
public class ResponseReader {

    private static Logger LOG = LoggerFactory.getLogger( ResponseReader.class );

    private XMLStreamReader reader;

    private static final String owsNS = "http://www.opengis.net/ows/1.1";

    private static final String xlinkNS = "http://www.w3.org/1999/xlink";

    private static final String xmlNS = "http://www.w3.org/XML/1998/namespace";

    public ResponseReader( XMLStreamReader reader ) {
        this.reader = reader;
    }

    /**
     * @param reader
     * @return
     * @throws OWSException
     * @throws XMLStreamException
     */
    public ExecuteResponse parse100()
                            throws OWSException {
        CodeType processId = null;
        ExecuteStatus status = null;
        List<ExecuteOutput> outputs = null;

        try {
            StAXParsingHelper.nextElement( reader );
            int state = reader.getEventType();
            if ( new QName( owsNS, "ExceptionReport" ).equals( reader.getName() ) ) {
                ExceptionReport excep = parseException();
                LOG.error( "Service returned returned OWSException. " + excep.getMessage() );
                throw new OWSException( excep.getMessage(), excep.getCode(), excep.getLocator() );
            }

            while ( state != XMLStreamConstants.START_ELEMENT || !reader.getName().getLocalPart().equals( "Status" ) ) {
                state = reader.next();
            }
            status = parseStatus();

            while ( state != XMLStreamConstants.START_ELEMENT
                    || !reader.getName().getLocalPart().equals( "ProcessOutputs" ) ) {
                state = reader.next();
            }
            outputs = parseOutputs();

        } catch ( XMLStreamException e ) {
            LOG.error( "Error while parsing Execute reponse coming from WPS. " + e.getMessage() );
            e.printStackTrace();
        }

        ExecuteOutput[] outputsArray = null;
        if ( outputs != null ) {
            outputsArray = outputs.toArray( new ExecuteOutput[outputs.size()] );
        }
        return new ExecuteResponse( status, outputsArray );
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:ProcessOutputs&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event
     * (&lt;/wps:ProcessOutputs&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private List<ExecuteOutput> parseOutputs()
                            throws XMLStreamException {
        List<ExecuteOutput> outputs = new ArrayList<ExecuteOutput>();
        try {
            StAXParsingHelper.nextElement( reader );
            while ( START_ELEMENT == reader.getEventType() && "Output".equals( reader.getName().getLocalPart() ) ) {
                ExecuteOutput output = null;
                StAXParsingHelper.nextElement( reader );
                CodeType id = parseIdentifier();

                DataType data = null;

                int eventType;
                String localName = null;
                do {
                    eventType = reader.next();
                    if ( eventType == START_ELEMENT || eventType == END_ELEMENT ) {
                        localName = reader.getName().getLocalPart();
                    }
                } while ( eventType != START_ELEMENT
                          || ( !localName.equals( "Reference" ) && !localName.equals( "Data" ) ) );

                if ( reader.getName().getLocalPart().equals( "Reference" ) ) {
                    String href = reader.getAttributeValue( null, "href" );
                    ComplexAttributes attribs = parseComplexAttributes();
                    String mimeType = attribs.getMimeType();

                    if ( mimeType != null && mimeType.startsWith( "text/xml" ) ) {
                        data = new XMLDataType( new URL( href ), true, mimeType, attribs.getEncoding(),
                                                attribs.getSchema() );
                    } else {
                        data = new BinaryDataType( new URL( href ), true, mimeType, attribs.getEncoding() );
                    }
                    output = new ExecuteOutput( id, data );
                    StAXParsingHelper.nextElement( reader );
                }
                if ( reader.getName().getLocalPart().equals( "Data" ) ) {
                    data = parseDataType();
                    output = new ExecuteOutput( id, data );
                    StAXParsingHelper.nextElement( reader );
                }

                outputs.add( output );
                StAXParsingHelper.nextElement( reader ); // </Output>
                StAXParsingHelper.nextElement( reader );
            }
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
        }

        return outputs;
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;OutputDefinitions&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/OutputDefinitions&gt;)
     * </li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private List<OutputDefinition> parseOutputDefinition()
                            throws XMLStreamException {
        List<OutputDefinition> outputDefs = new ArrayList<OutputDefinition>();
        StAXParsingHelper.nextElement( reader ); // <Output>
        while ( reader.getName().getLocalPart().equals( "Output" ) ) {
            ComplexAttributes complexAttribs = parseComplexAttributes();
            String uom = reader.getAttributeValue( null, "uom" );

            StAXParsingHelper.nextElement( reader );
            LanguageString outputTitle = null;
            if ( "Title".equals( reader.getName().getLocalPart() ) ) {
                String lang = reader.getAttributeValue( xmlNS, "lang" );
                outputTitle = new LanguageString( reader.getElementText(), lang );
                StAXParsingHelper.nextElement( reader );
            }
            LanguageString outputAbstract = null;
            if ( "Abstract".equals( reader.getName().getLocalPart() ) ) {
                String lang = reader.getAttributeValue( xmlNS, "lang" );
                outputAbstract = new LanguageString( reader.getElementText(), lang );
                StAXParsingHelper.nextElement( reader );
            }

            boolean asRef = false;
            String asRefStr = reader.getAttributeValue( null, "asRef" );
            if ( asRefStr != null ) {
                asRef = Boolean.valueOf( asRefStr );
            }

            StAXParsingHelper.nextElement( reader );
            CodeType id = parseIdentifier();
            outputDefs.add( new OutputDefinition( id, uom, asRef, complexAttribs.getMimeType(),
                                                  complexAttribs.getEncoding(), complexAttribs.getSchema() ) );

            StAXParsingHelper.nextElement( reader );
            int state = reader.getEventType();
            while ( state != XMLStreamConstants.END_ELEMENT || !reader.getName().getLocalPart().equals( "Output" ) ) {
                StAXParsingHelper.nextElement( reader );
                state = reader.getEventType();
            }
        }
        StAXParsingHelper.nextElement( reader );
        return outputDefs;
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:DataInputs&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wps:DataInputs&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private List<ExecuteInput> parseDataInputs()
                            throws XMLStreamException {
        List<ExecuteInput> inputs = new ArrayList<ExecuteInput>();
        InputReference inputRef = null;
        DataType dataType = null;
        StAXParsingHelper.nextElement( reader ); // "Input"

        while ( "Input".equals( reader.getName().getLocalPart() ) ) {

            ExecuteInput input = null;
            StAXParsingHelper.nextElement( reader ); // "Identifier"
            CodeType id = parseIdentifier();
            while ( !"Reference".equals( reader.getName().getLocalPart() )
                    && !"Data".equals( reader.getName().getLocalPart() ) ) {
                StAXParsingHelper.nextElement( reader );
            }

            if ( "Reference".equals( reader.getName().getLocalPart() ) ) {
                inputRef = parseInputRef();
                input = new ExecuteInput( id, inputRef );
                StAXParsingHelper.nextElement( reader );
            }

            if ( "Data".equals( reader.getName().getLocalPart() ) ) {
                dataType = parseDataType();
                input = new ExecuteInput( id, dataType );
                StAXParsingHelper.nextElement( reader );
            }
            inputs.add( input );
            StAXParsingHelper.nextElement( reader ); // </Input>
            StAXParsingHelper.nextElement( reader );
        }
        return inputs;
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event of either (&lt;wps:ComplexData&gt;),
     * (&lt;wps:LiteralData&gt;) or (&lt;wps:BoundingBoxData&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event of either
     * (&lt;/wps:ComplexData&gt;) , (&lt;/wps:LiteralData&gt;) or (&lt;/wps:BoundingBoxData&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private DataType parseDataType()
                            throws XMLStreamException {
        DataType dataType = null;
        StAXParsingHelper.nextElement( reader );
        String localName = reader.getName().getLocalPart();
        if ( "ComplexData".equals( localName ) ) {
            dataType = parseComplexData();
        } else if ( "LiteralData".equals( localName ) ) {
            dataType = parseLiteralData();
        } else if ( "BoundingBoxData".equals( localName ) ) {
            dataType = parseBBoxData();
        }
        return dataType;
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:ComplexData&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wps:ComplexData&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     * @throws IOException
     */
    private DataType parseComplexData()
                            throws XMLStreamException {
        ComplexAttributes attribs = parseComplexAttributes();

        BinaryDataType data = null;
        File tmpFile = null;
        try {
            if ( attribs.getMimeType().startsWith( "text/xml" ) || attribs.getMimeType().startsWith( "application/xml" ) ) {
                tmpFile = File.createTempFile( "output", ".xml" );
                OutputStream sink = new FileOutputStream( tmpFile );
                XMLOutputFactory fac = XMLOutputFactory.newInstance();
                fac.setProperty( XMLOutputFactory.IS_REPAIRING_NAMESPACES, true );
                XMLStreamWriter xmlWriter = fac.createXMLStreamWriter( sink, "UTF-8" );

                xmlWriter.writeStartDocument( "UTF-8", "1.0" );
                XMLAdapter.writeElement( xmlWriter, reader );
                xmlWriter.writeEndDocument();
                xmlWriter.close();
                return new XMLDataType( tmpFile.toURI().toURL(), false, attribs.getMimeType(), attribs.getEncoding(),
                                        attribs.getSchema() );
            }

            tmpFile = File.createTempFile( "output", ".bin" );
            OutputStream sink = new FileOutputStream( tmpFile );

            if ( "base64".equals( attribs.getEncoding() ) ) {
                String base64String = reader.getElementText();
                byte[] bytes = Base64.decodeBase64( base64String );
                sink.write( bytes );
            } else {
                LOG.warn( "The encoding of binary data (found at response location "
                          + reader.getLocation()
                          + ") is not base64. Currently only from this format the decoding can be performed. Skipping the data." );
            }
            sink.close();

            data = new BinaryDataType( tmpFile.toURI().toURL(), false, attribs.getMimeType(), attribs.getEncoding() );
            LOG.info( "Wrote decoded binary data into " + tmpFile.toString() );
        } catch ( IOException e ) {
            LOG.error( e.getMessage() );
        }

        return data;
    }

    class XMLDataNamespaceContext implements NamespaceContext {

        private Map<String, String> nsMap = new HashMap<String, String>();

        public void addNamespace( String prefix, String ns ) {
            nsMap.put( prefix, ns );
        }

        @Override
        public String getNamespaceURI( String prefix ) {
            return nsMap.get( prefix );
        }

        @Override
        public String getPrefix( String ns ) {
            Set<Entry<String, String>> entrySet = nsMap.entrySet();
            for ( Entry<String, String> entry : entrySet ) {
                if ( entry.getValue().equals( ns ) ) {
                    return entry.getKey();
                }
            }
            return null;
        }

        @Override
        public Iterator getPrefixes( String arg0 ) {
            return nsMap.entrySet().iterator();
        }
    };

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:BoundingBoxData&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event
     * (&lt;/wps:BoundingBoxData&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private BoundingBoxDataType parseBBoxData()
                            throws XMLStreamException {

        String crs = reader.getAttributeValue( null, "crs" );

        StAXParsingHelper.nextElement( reader ); // <LowerCorner>
        String[] coordStr = reader.getElementText().split( "\\s" );
        double[] lower = new double[coordStr.length];
        for ( int i = 0; i < lower.length; i++ ) {
            lower[i] = Double.parseDouble( coordStr[i] );
        }

        StAXParsingHelper.nextElement( reader ); // <UpperCorner>
        coordStr = reader.getElementText().split( "\\s" );
        double[] upper = new double[coordStr.length];
        for ( int i = 0; i < upper.length; i++ ) {
            upper[i] = Double.parseDouble( coordStr[i] );
        }
        StAXParsingHelper.nextElement( reader );
        return new BoundingBoxDataType( lower, upper, crs);
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:LiteralData&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wps:LiteralData&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private LiteralDataType parseLiteralData()
                            throws XMLStreamException {
        String dataType = reader.getAttributeValue( null, "dataType" );
        String uom = reader.getAttributeValue( null, "uom" );
        String value = reader.getElementText();
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
        method = reader.getAttributeValue( null, "method" );
        complexAttribs = parseComplexAttributes();

        StAXParsingHelper.nextElement( reader );
        while ( "Header".equals( reader.getName().getLocalPart() ) ) {
            String key = reader.getAttributeValue( null, "key" );
            String value = reader.getAttributeValue( null, "value" );
            headers.put( key, value );
            StAXParsingHelper.nextElement( reader ); // </Header>
            StAXParsingHelper.nextElement( reader );
        }

        if ( "Body".equals( reader.getName().getLocalPart() ) ) {
            body = reader.getElementText();
            StAXParsingHelper.nextElement( reader );
        }

        if ( "BodyReference".equals( reader.getName().getLocalPart() ) ) {
            bodyXlink = reader.getAttributeValue( xlinkNS, "href" );
            body = reader.getElementText();
            StAXParsingHelper.nextElement( reader );
        }
        return new InputReference( headers, body, bodyXlink, xlink, method );
    }

    /**
     * @return
     */
    private ComplexAttributes parseComplexAttributes() {
        String mimeType = reader.getAttributeValue( null, "mimeType" );
        String encoding = reader.getAttributeValue( null, "encoding" );
        String schema = reader.getAttributeValue( null, "schema" );
        return new ComplexAttributes( mimeType, encoding, schema );
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:Status&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wps:Status&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private ExecuteStatus parseStatus()
                            throws XMLStreamException {
        String statusMsg = null;
        Integer percent = null;
        String creationTime = null;
        String exceptionReport = null;

        String attribute = reader.getAttributeValue( null, "creationTime" );
        if ( attribute != null ) {
            creationTime = attribute;
        }

        StAXParsingHelper.nextElement( reader );
        String localName = reader.getName().getLocalPart();
        if ( "ProcessAccepted".equals( localName ) || "ProcessSucceeded".equals( localName ) ) {
            statusMsg = reader.getElementText();
        }
        if ( "ProcessStarted".equals( localName ) || "ProcessPaused".equals( localName ) ) {
            statusMsg = reader.getElementText();
            String percentStr = reader.getAttributeValue( null, "percentCompleted" );
            if ( percentStr != null ) {
                percent = Integer.parseInt( percentStr );
            }
            StAXParsingHelper.nextElement( reader );
        }
        // if ( "ProcessFailed".equals( localName ) ) {
        // exceptionReport = parseException();
        // }
        StAXParsingHelper.nextElement( reader ); // </Status>
        return new ExecuteStatus( statusMsg, percent, creationTime, exceptionReport );
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:Exception&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wps:Exception&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private ExceptionReport parseException() {
        String code = null;
        String locator = null;
        String message = null;
        try {
            StAXParsingHelper.nextElement( reader ); // "Exception"
            StAXParsingHelper.nextElement( reader ); // "ExceptionText"
            code = reader.getAttributeValue( null, "exceptionCode" );
            locator = reader.getAttributeValue( null, "locator" );
            message = reader.getElementText();
        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        }
        return new ExceptionReport( message, code, locator );
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:Process&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wps:Process&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private CodeType parseProcess()
                            throws XMLStreamException {
        CodeType id = null;
        StAXParsingHelper.nextElement( reader );
        id = parseIdentifier();
        int state = reader.next();
        return id; // TODO maybe create a process bean and return it
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:Identifier&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wps:Identifier&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private CodeType parseIdentifier()
                            throws XMLStreamException {
        String codeSpace = reader.getAttributeValue( null, "codeSpace" );
        String code = reader.getElementText();
        return new CodeType( code, codeSpace );
    }
}
