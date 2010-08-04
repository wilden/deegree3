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
package org.deegree.protocol.wps.describeprocess;

import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;
import static org.deegree.protocol.wps.WPSConstants.WPS_PREFIX;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wps.describeprocess.input.BBoxDataDescription;
import org.deegree.protocol.wps.describeprocess.input.ComplexDataDescription;
import org.deegree.protocol.wps.describeprocess.input.DataDescription;
import org.deegree.protocol.wps.describeprocess.input.InputDescription;
import org.deegree.protocol.wps.describeprocess.input.LiteralDataDescription;
import org.deegree.protocol.wps.describeprocess.output.BBoxOutputType;
import org.deegree.protocol.wps.describeprocess.output.ComplexOutputType;
import org.deegree.protocol.wps.describeprocess.output.OutputType;
import org.deegree.protocol.wps.describeprocess.output.LiteralOutputType;
import org.deegree.protocol.wps.describeprocess.output.OutputDescription;
import org.deegree.services.jaxb.wps.Range;

/**
 * Encapsulates the information returned by a <code>DescribeProcess</code> request.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ProcessDetails {

    private static final String owsPrefix = "ows";

    private static final String owsNS = "http://www.opengis.net/ows/1.1";

    private static final String xmlNS = "http://www.w3.org/XML/1998/namespace";

    private static NamespaceContext nsContext;

    private final XMLAdapter omResponse;

    private final Map<CodeType, InputDescription> inputs;

    private final Map<CodeType, OutputDescription> outputs;

    private final boolean storeSupported;

    private final boolean statusSupported;

    static {
        nsContext = new NamespaceContext();
        nsContext.addNamespace( WPS_PREFIX, WPS_100_NS );
        nsContext.addNamespace( owsPrefix, owsNS );
    }

    public ProcessDetails( XMLAdapter describeResponse ) {
        this.omResponse = describeResponse;
        this.inputs = parseInputs();
        this.outputs = parseOutputs();

        XPath xpath = new XPath( "/wps:ProcessDescriptions/ProcessDescription/@storeSupported", nsContext );
        storeSupported = describeResponse.getNodeAsBoolean( describeResponse.getRootElement(), xpath, false );

        xpath = new XPath( "/wps:ProcessDescriptions/ProcessDescription/@statusSupported", nsContext );
        statusSupported = describeResponse.getNodeAsBoolean( describeResponse.getRootElement(), xpath, false );
    }

    /**
     * Returns the input parameter descriptions for the process.
     * 
     * @return the input parameter descriptions, never <code>null</code>
     */
    public Map<CodeType, InputDescription> getInputs() {
        return inputs;
    }

    /**
     * Returns the output parameter descriptions for the process.
     * 
     * @return the output parameter descriptions, never <code>null</code>
     */
    public Map<CodeType, OutputDescription> getOutputs() {
        return outputs;
    }

    private Map<CodeType, InputDescription> parseInputs() {
        XPath xpath = new XPath( "/wps:ProcessDescriptions/ProcessDescription/DataInputs/Input", nsContext );
        List<OMElement> inputs = omResponse.getElements( omResponse.getRootElement(), xpath );
        Map<CodeType, InputDescription> idToInputType = new HashMap<CodeType, InputDescription>();
        for ( OMElement input : inputs ) {
            String minOccurs = input.getAttribute( new QName( null, "minOccurs" ) ).getAttributeValue();
            String maxOccurs = input.getAttribute( new QName( null, "minOccurs" ) ).getAttributeValue();

            CodeType id = parseId( input );
            LanguageString inputTitle = parseLanguageString( input, "Title" );
            LanguageString inputAbstract = parseLanguageString( input, "Abstract" );
            DataDescription data = parseData( input );
            InputDescription inputDescrips = new InputDescription( id, inputTitle, inputAbstract, minOccurs, maxOccurs,
                                                                   data );
            idToInputType.put( id, inputDescrips );
        }
        return idToInputType;
    }

    private Map<CodeType, OutputDescription> parseOutputs() {
        XPath xpath = new XPath( "/wps:ProcessDescriptions/ProcessDescription/ProcessOutputs/Output", nsContext );
        List<OMElement> outputs = omResponse.getElements( omResponse.getRootElement(), xpath );
        Map<CodeType, OutputDescription> idToOutputType = new HashMap<CodeType, OutputDescription>();
        for ( OMElement output : outputs ) {
            CodeType id = parseId( output );
            LanguageString outputTitle = parseLanguageString( output, "Title" );
            LanguageString outputAbstract = parseLanguageString( output, "Abstract" );

            OutputType outputData = parseOutputData( output );
            OutputDescription outputDescrib = new OutputDescription( id, outputTitle, outputAbstract, outputData );
            idToOutputType.put( id, outputDescrib );
        }
        return idToOutputType;
    }

    private OutputType parseOutputData( OMElement output ) {
        OutputType outputData = null;
        OMElement complexData = output.getFirstChildWithName( new QName( null, "ComplexOutput" ) );
        if ( complexData != null ) {
            outputData = parseComplexOutput( complexData );
        }

        OMElement literalData = output.getFirstChildWithName( new QName( null, "LiteralOutput" ) );
        if ( literalData != null ) {
            outputData = parseLiteralOutput( literalData );
        }

        OMElement bboxData = output.getFirstChildWithName( new QName( null, "BoundingBoxOutput" ) );
        if ( bboxData != null ) {
            outputData = parseBBoxOutput( bboxData );
        }

        return outputData;
    }

    private OutputType parseBBoxOutput( OMElement bboxData ) {
        XPath xpath = new XPath( "Default/CRS", nsContext );
        String defaultCrs = omResponse.getElement( bboxData, xpath ).getText();

        xpath = new XPath( "Supported/CRS", nsContext );
        List<OMElement> omSupported = omResponse.getElements( bboxData, xpath );
        String[] supportedCrs = new String[omSupported.size()];
        for ( int i = 0; i < omSupported.size(); i++ ) {
            supportedCrs[i] = omSupported.get( i ).getText();
        }

        return new BBoxOutputType( defaultCrs, supportedCrs );
    }

    private LiteralOutputType parseLiteralOutput( OMElement omLiteral ) {
        OMElement omDataType = omLiteral.getFirstChildWithName( new QName( owsNS, "DataType" ) );
        ValueWithRef<String> dataType = null;
        if ( omDataType != null ) {
            String dataTypeStr = omDataType.getText();
            String dataTypeRefStr = omDataType.getAttributeValue( new QName( owsNS, "reference" ) );
            URI dataTypeRef = null;
            if ( dataTypeRefStr != null ) {
                try {
                    dataTypeRef = new URI( dataTypeRefStr );
                } catch ( URISyntaxException e ) {
                    // dataTypeRef stays null
                }
            }
            dataType = new ValueWithRef<String>( dataTypeStr, dataTypeRef );
        }

        XPath xpath = new XPath( "UOMs/Default/ows:UOM", nsContext );
        OMElement omDefault = omResponse.getElement( omLiteral, xpath );
        ValueWithRef<String> defaultUom = null;
        if ( omDefault != null ) {
            String defaultUomStr = omDefault.getText();
            String defaultUomRefStr = omDefault.getAttributeValue( new QName( owsNS, "reference" ) );
            URI defaultUomRef = null;
            if ( defaultUomRefStr != null ) {
                try {
                    defaultUomRef = new URI( defaultUomRefStr );
                } catch ( URISyntaxException e ) {
                    // defaultUomRef stays null
                }
            }
            defaultUom = new ValueWithRef<String>( defaultUomStr, defaultUomRef );
        }
        xpath = new XPath( "UOMs/Supported/ows:UOM", nsContext );
        List<OMElement> omSupported = omResponse.getElements( omLiteral, xpath );
        ValueWithRef<String>[] supportedUoms = null;
        if ( omSupported != null ) {
            supportedUoms = new ValueWithRef[omSupported.size()];
            for ( int i = 0; i < omSupported.size(); i++ ) {
                OMElement omSupp = omSupported.get( i );
                String supportedRefStr = omSupp.getAttributeValue( new QName( owsNS, "reference" ) );
                URI supportedRef = null;
                if ( supportedRefStr != null ) {
                    try {
                        supportedRef = new URI( supportedRefStr );
                    } catch ( URISyntaxException e ) {
                        // omSupportedRef stays null
                    }
                }
                supportedUoms[i] = new ValueWithRef<String>( omSupp.getText(), supportedRef );
            }
        }
        return new LiteralOutputType( dataType, defaultUom, supportedUoms );
    }

    private OutputType parseComplexOutput( OMElement omComplex ) {
        XPath xpath = new XPath( "Default/Format", nsContext );
        OMElement omDefault = omResponse.getElement( omComplex, xpath );
        String mimeType = omDefault.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();
        OMElement omEncoding = omDefault.getFirstChildWithName( new QName( null, "Encoding" ) );
        String encoding = null;
        if ( omEncoding != null ) {
            encoding = omEncoding.getText();
        }
        OMElement omSchema = omDefault.getFirstChildWithName( new QName( null, "Schema" ) );
        String schema = null;
        if ( omSchema != null ) {
            schema = omSchema.getText();
        }

        ComplexAttributes defaultFormat = new ComplexAttributes( mimeType, encoding, schema );

        xpath = new XPath( "Supported/Format", nsContext );
        List<OMElement> omSupported = omResponse.getElements( omComplex, xpath );
        ComplexAttributes[] supportedFormats = new ComplexAttributes[omSupported.size()];
        for ( int i = 0; i < omSupported.size(); i++ ) {
            OMElement omSupp = omSupported.get( i );
            mimeType = omSupp.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();
            omEncoding = omSupp.getFirstChildWithName( new QName( null, "Encoding" ) );
            encoding = null;
            if ( omEncoding != null ) {
                encoding = omEncoding.getText();
            }
            omSchema = omSupp.getFirstChildWithName( new QName( null, "Schema" ) );
            schema = null;
            if ( omSchema != null ) {
                schema = omSchema.getText();
            }
            supportedFormats[i] = new ComplexAttributes( mimeType, encoding, schema );
        }
        return new ComplexOutputType( defaultFormat, supportedFormats );
    }

    private DataDescription parseData( OMElement input ) {
        DataDescription inputData = null;

        OMElement complexData = input.getFirstChildWithName( new QName( null, "ComplexData" ) );
        if ( complexData != null ) {
            inputData = parseComplexData( complexData );
        }

        OMElement literalData = input.getFirstChildWithName( new QName( null, "LiteralData" ) );
        if ( literalData != null ) {
            inputData = parseLiteralData( literalData );
        }

        OMElement bboxData = input.getFirstChildWithName( new QName( null, "BoundingBoxData" ) );
        if ( bboxData != null ) {
            inputData = parseBBoxData( bboxData );
        }

        return inputData;
    }

    private DataDescription parseBBoxData( OMElement input ) {
        XPath xpath = new XPath( "Default/CRS", nsContext );
        String defaultCRS = omResponse.getElement( input, xpath ).getText();
        xpath = new XPath( "Supported/CRS", nsContext );
        List<OMElement> omSupported = omResponse.getElements( input, xpath );
        String[] supportedCRSs = new String[omSupported.size()];
        for ( int i = 0; i < omSupported.size(); i++ ) {
            supportedCRSs[i] = omSupported.get( i ).getText();
        }

        return new BBoxDataDescription( defaultCRS, supportedCRSs );
    }

    private DataDescription parseLiteralData( OMElement input ) {
        OMElement omDataType = input.getFirstChildWithName( new QName( owsNS, "DataType" ) );
        String dataTypeStr = omDataType.getText();
        String dataTypeRefStr = omDataType.getAttributeValue( new QName( owsNS, "reference" ) );
        URI dataTypeRef = null;
        try {
            dataTypeRef = new URI( dataTypeRefStr );
        } catch ( URISyntaxException e1 ) {
            // dataTypeRef stays null
        }
        ValueWithRef<String> dataType = new ValueWithRef<String>( dataTypeStr, dataTypeRef );

        XPath xpath = new XPath( "UOMs/Default/ows:UOM", nsContext );
        OMElement omDefaultUom = omResponse.getElement( input, xpath );
        ValueWithRef<String> defaultUom = null;
        if ( omDefaultUom != null ) {
            String defaultUomRefStr = omDefaultUom.getAttributeValue( new QName( owsNS, "reference" ) );
            URI defaultUomRef = null;
            if ( defaultUomRefStr != null ) {
                try {
                    defaultUomRef = new URI( defaultUomRefStr );
                } catch ( URISyntaxException e ) {
                    // defaultUomRef stays null
                }
            }
            defaultUom = new ValueWithRef<String>( omDefaultUom.getText(), defaultUomRef );
        }

        xpath = new XPath( "UOMs/Supported/ows:UOM", nsContext );
        List<OMElement> omSupported = omResponse.getElements( input, xpath );
        ValueWithRef<String>[] supportedUom = null;
        if ( omSupported != null ) {
            supportedUom = new ValueWithRef[omSupported.size()];
            for ( int i = 0; i < omSupported.size(); i++ ) {
                OMElement omSupport = omSupported.get( i );
                String supported = omSupport.getText();
                String supportedRefStr = omSupport.getAttributeValue( new QName( owsNS, "reference" ) );
                URI supportedRef = null;
                if ( supportedRefStr != null ) {
                    try {
                        supportedRef = new URI( supportedRefStr );
                    } catch ( URISyntaxException e ) {
                        // supportedRef stays null
                    }
                }
                supportedUom[i] = new ValueWithRef<String>( supported, supportedRef );
            }
        }

        OMElement omAnyValue = input.getFirstChildWithName( new QName( owsNS, "AnyValue" ) );
        boolean anyValue = ( omAnyValue != null );

        OMElement omAllowedValues = input.getFirstChildWithName( new QName( owsNS, "AllowedValues" ) );
        List<String> values = null;
        List<Range> rangeList = null;
        if ( omAllowedValues != null ) {
            QName valueQName = new QName( owsNS, "Value" );
            // safe cast
            @SuppressWarnings( { "cast", "unchecked" })
            Iterator<OMElement> iterator = (Iterator<OMElement>) omAllowedValues.getChildrenWithName( valueQName );
            values = new ArrayList<String>();
            for ( ; iterator.hasNext(); ) {
                values.add( iterator.next().getText() );
            }

            QName rangeQName = new QName( owsNS, "Range" );
            // safe cast
            @SuppressWarnings( { "cast", "unchecked" })
            Iterator<OMElement> iterator2 = (Iterator<OMElement>) omAllowedValues.getChildrenWithName( rangeQName );
            rangeList = new ArrayList<Range>();
            for ( ; iterator2.hasNext(); ) {
                OMElement omRange = iterator2.next();
                Range range = new Range();

                OMElement omMinimum = omRange.getFirstChildWithName( new QName( owsNS, "MinimumValue" ) );
                if ( omMinimum != null ) {
                    range.setMinimumValue( omMinimum.getText() );
                }
                OMElement omMaximum = omRange.getFirstChildWithName( new QName( owsNS, "MaximumValue" ) );
                if ( omMaximum != null ) {
                    range.setMaximumValue( omMaximum.getText() );
                }
                OMElement omSpacing = omRange.getFirstChildWithName( new QName( owsNS, "Spacing" ) );
                if ( omSpacing != null ) {
                    range.setSpacing( omSpacing.getText() );
                }
                String closure = omRange.getAttributeValue( new QName( owsNS, "rangeClosure" ) );
                if ( closure != null ) {
                    range.getRangeClosure().add( closure );
                }
                rangeList.add( range );
            }
        }

        OMElement omValuesReference = input.getFirstChildWithName( new QName( owsNS, "ValuesReference" ) );
        ValueWithRef<URI> valuesRef = null;
        if ( omValuesReference != null ) {
            String valueRefStr = omValuesReference.getAttributeValue( new QName( owsNS, "reference" ) );
            String valueFormStr = omValuesReference.getAttributeValue( new QName( null, "valuesForm" ) );

            URI valueRefUri = null;
            if ( valueRefStr != null ) {
                try {
                    valueRefUri = new URI( valueRefStr );
                } catch ( URISyntaxException e ) {
                    // valueRefUri stays null
                }
            }
            URI valueFormStrUri = null;
            if ( valueFormStr != null ) {
                try {
                    valueFormStrUri = new URI( valueFormStr );
                } catch ( URISyntaxException e ) {
                    // valueFormStrUri stays null
                }
            }
            valuesRef = new ValueWithRef<URI>( valueRefUri, valueFormStrUri );
        }

        String[] valuesArray = null;
        if ( values != null ) {
            valuesArray = values.toArray( new String[values.size()] );
        }
        Range[] rangeArray = null;
        if ( rangeList != null ) {
            rangeArray = rangeList.toArray( new Range[rangeList.size()] );
        }
        return new LiteralDataDescription( dataType, defaultUom, supportedUom, valuesArray, rangeArray, anyValue,
                                           valuesRef );
    }

    private DataDescription parseComplexData( OMElement input ) {
        XPath xpath = new XPath( "Default/Format", nsContext );
        OMElement omDefaultFormat = omResponse.getElement( input, xpath );
        String mimeType = omDefaultFormat.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();

        OMElement omEncoding = omDefaultFormat.getFirstChildWithName( new QName( null, "Encoding" ) );
        String encoding = null;
        if ( omEncoding != null ) {
            encoding = omEncoding.getText();
        }
        OMElement omSchema = omDefaultFormat.getFirstChildWithName( new QName( null, "Schema" ) );
        String schema = null;
        if ( omSchema != null ) {
            schema = omSchema.getText();
        }
        ComplexAttributes defaultFormat = new ComplexAttributes( mimeType, encoding, schema );

        xpath = new XPath( "Supported/Format", nsContext );
        List<OMElement> omFormats = omResponse.getElements( input, xpath );
        ComplexAttributes[] supported = new ComplexAttributes[omFormats.size()];
        for ( int i = 0; i < omFormats.size(); i++ ) {
            OMElement omFormat = omFormats.get( i );
            mimeType = omFormat.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();
            omEncoding = omFormat.getFirstChildWithName( new QName( null, "Encoding" ) );
            encoding = null;
            if ( omEncoding != null ) {
                encoding = omEncoding.getText();
            }
            omSchema = omFormat.getFirstChildWithName( new QName( null, "Schema" ) );
            schema = null;
            if ( omSchema != null ) {
                schema = omSchema.getText();
            }
            supported[i] = new ComplexAttributes( mimeType, encoding, schema );
        }
        return new ComplexDataDescription( defaultFormat, supported );
    }

    private LanguageString parseLanguageString( OMElement omElement, String name ) {
        OMElement omElem = omElement.getFirstChildWithName( new QName( owsNS, name ) );
        if ( omElem != null ) {
            String lang = omElem.getAttributeValue( new QName( xmlNS, "lang" ) );
            return new LanguageString( omElem.getText(), lang );
        }
        return null;
    }

    private CodeType parseId( OMElement omElement ) {
        OMElement omId = omElement.getFirstChildWithName( new QName( owsNS, "Identifier" ) );
        String codeSpace = omId.getAttributeValue( new QName( null, "codeSpace" ) );
        if ( codeSpace != null ) {
            return new CodeType( omId.getText(), codeSpace );
        }
        return new CodeType( omId.getText() );
    }

    public boolean getStoreSupported() {
        return storeSupported;
    }

    public boolean getStatusSupported() {
        return statusSupported;
    }
}