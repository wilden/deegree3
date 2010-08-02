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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wps.ComplexAttributes;
import org.deegree.protocol.wps.describeprocess.output.BBoxOutput;
import org.deegree.protocol.wps.describeprocess.output.ComplexOutput;
import org.deegree.protocol.wps.describeprocess.output.GenericOutput;
import org.deegree.protocol.wps.describeprocess.output.LiteralOutput;
import org.deegree.protocol.wps.describeprocess.output.OutputDescription;

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
public class DescribeProcessExecution {

    private XMLAdapter omResponse;

    private static final String owsPrefix = "ows";

    private static final String owsNS = "http://www.opengis.net/ows/1.1";

    private static final String xmlNS = "http://www.w3.org/XML/1998/namespace";

    private static NamespaceContext nsContext;

    static {
        nsContext = new NamespaceContext();
        nsContext.addNamespace( WPS_PREFIX, WPS_100_NS );
        nsContext.addNamespace( owsPrefix, owsNS );
    }

    public DescribeProcessExecution( URL requestURL ) {
        omResponse = new XMLAdapter( requestURL );
    }

    /**
     * @return
     */
    public Map<CodeType, InputDescription> parseInputs() {
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

    /**
     * @return
     */
    public Map<CodeType, OutputDescription> parseOutputs() {
        XPath xpath = new XPath( "/wps:ProcessDescriptions/ProcessDescription/ProcessOutputs/Output", nsContext );
        List<OMElement> outputs = omResponse.getElements( omResponse.getRootElement(), xpath );
        Map<CodeType, OutputDescription> idToOutputType = new HashMap<CodeType, OutputDescription>();
        for ( OMElement output : outputs ) {
            CodeType id = parseId( output );
            LanguageString outputTitle = parseLanguageString( output, "Title" );
            LanguageString outputAbstract = parseLanguageString( output, "Abstract" );

            GenericOutput outputData = parseOutputData( output );
            OutputDescription outputDescrib = new OutputDescription( id, outputTitle, outputAbstract, outputData );
            idToOutputType.put( id, outputDescrib );
        }
        return idToOutputType;
    }

    /**
     * @return
     */
    private GenericOutput parseOutputData( OMElement output ) {
        GenericOutput outputData = null;
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

    /**
     * @param bboxData
     * @return
     */
    private GenericOutput parseBBoxOutput( OMElement bboxData ) {
        XPath xpath = new XPath( "Default/CRS", nsContext );
        String defaultCrs = omResponse.getElement( bboxData, xpath ).getText();

        xpath = new XPath( "Supported/CRS", nsContext );
        List<OMElement> omSupported = omResponse.getElements( bboxData, xpath );
        String[] supportedCrs = new String[omSupported.size()];
        for ( int i = 0; i < omSupported.size(); i++ ) {
            supportedCrs[i] = omSupported.get( i ).getText();
        }

        return new BBoxOutput( defaultCrs, supportedCrs );
    }

    /**
     * @param output
     * @return
     */
    private LiteralOutput parseLiteralOutput( OMElement omLiteral ) {
        OMElement omDataType = omLiteral.getFirstChildWithName( new QName( null, "DataType" ) );
        String dataType = omDataType.getText();
        String dataTypeRefStr = omDataType.getAttributeValue( new QName( owsNS, "reference" ) );
        URL dataTypeRef;
        try {
            dataTypeRef = new URL( dataTypeRefStr );
        } catch ( MalformedURLException e ) {
            dataTypeRef = null;
        }

        XPath xpath = new XPath( "UOMs/Default/UOM", nsContext );
        OMElement omDefault = omResponse.getElement( omLiteral, xpath );
        String defaultUom = omDefault.getText();
        xpath = new XPath( "UOMs/Supported/UOM", nsContext );
        List<OMElement> omSupported = omResponse.getElements( omLiteral, xpath );
        String[] supportedUoms = new String[omSupported.size()];
        for ( int i = 0; i < omSupported.size(); i++ ) {
            supportedUoms[i] = omSupported.get( i ).getText();
        }

        return new LiteralOutput( dataType, dataTypeRef, defaultUom, supportedUoms );
    }

    /**
     * @param output
     * @return
     */
    private GenericOutput parseComplexOutput( OMElement omComplex ) {
        XPath xpath = new XPath( "Default/Format", nsContext );
        OMElement omDefault = omResponse.getElement( omComplex, xpath );
        String mimeType = omDefault.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();
        String encoding = omDefault.getFirstChildWithName( new QName( null, "Encoding" ) ).getText();
        String schema = omDefault.getFirstChildWithName( new QName( null, "Schema" ) ).getText();
        ComplexAttributes defaultFormat = new ComplexAttributes( mimeType, encoding, schema );

        xpath = new XPath( "Supported/Format", nsContext );
        List<OMElement> omSupported = omResponse.getElements( omComplex, xpath );
        ComplexAttributes[] supportedFormats = new ComplexAttributes[omSupported.size()];
        for ( int i = 0; i < omSupported.size(); i++ ) {
            OMElement omSupp = omSupported.get( i );
            mimeType = omSupp.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();
            encoding = omSupp.getFirstChildWithName( new QName( null, "Encoding" ) ).getText();
            schema = omSupp.getFirstChildWithName( new QName( null, "Schema" ) ).getText();
            supportedFormats[i] = new ComplexAttributes( mimeType, encoding, schema );
        }

        return new ComplexOutput( defaultFormat, supportedFormats );
    }

    /**
     * @param input
     * @return
     */
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

    /**
     * @param input
     * @return
     */
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

    /**
     * @param input
     * @return
     */
    private DataDescription parseLiteralData( OMElement input ) {
        OMElement omDataType = input.getFirstChildWithName( new QName( owsNS, "DataType" ) );
        String dataType = omDataType.getText();
        String dataTypeRefStr = omDataType.getAttributeValue( new QName( owsNS, "reference" ) );
        URL dataTypeRef;
        try {
            dataTypeRef = new URL( dataTypeRefStr );
        } catch ( MalformedURLException e ) {
            dataTypeRef = null;
        }

        XPath xpath = new XPath( "UOMs/Default/ows:UOM", nsContext );
        OMElement omDefaultUom = omResponse.getElement( input, xpath );
        String defaultUom = omDefaultUom.getText();
        String defaultUomRef = omDefaultUom.getAttributeValue( new QName( owsNS, "reference" ) );

        xpath = new XPath( "UOMs/Supported/ows:UOM", nsContext );
        List<OMElement> omSupported = omResponse.getElements( input, xpath );
        // standard suppress warnings for casting declaration of array of generic type
        @SuppressWarnings( { "cast", "unchecked" })
        Pair<String, String>[] supportedUom = (Pair<String, String>[]) new Pair[omSupported.size()];
        for ( int i = 0; i < omSupported.size(); i++ ) {
            OMElement omSupport = omSupported.get( i );
            String supported = omSupport.getText();
            String supportedRef = omSupport.getAttributeValue( new QName( owsNS, "reference" ) );
            supportedUom[i] = new Pair<String, String>( supported, supportedRef );
        }

        OMElement omAnyValue = input.getFirstChildWithName( new QName( owsNS, "AnyValue" ) );
        boolean anyValue = ( omAnyValue != null );

        OMElement omAllowedValues = input.getFirstChildWithName( new QName( owsNS, "AllowedValues" ) );
        List<String> values = null;
        List<Triple<String, String, String>> range = null;
        if ( omAllowedValues != null ) {
            QName valueQName = new QName( owsNS, "Value" );
            // safe
            @SuppressWarnings( { "cast", "unchecked" })
            Iterator<OMElement> iterator = (Iterator<OMElement>) omAllowedValues.getChildrenWithName( valueQName );
            values = new ArrayList<String>();
            for ( ; iterator.hasNext(); ) {
                values.add( iterator.next().getText() );
            }

            QName rangeQName = new QName( owsNS, "Range" );
            // safe
            @SuppressWarnings( { "cast", "unchecked" })
            Iterator<OMElement> iterator2 = (Iterator<OMElement>) omAllowedValues.getChildrenWithName( rangeQName );
            range = new ArrayList<Triple<String, String, String>>();
            for ( ; iterator2.hasNext(); ) {
                OMElement omRange = iterator2.next();
                String minimum = omRange.getFirstChildWithName( new QName( owsNS, "MinimumValue" ) ).getText();
                String maximum = omRange.getFirstChildWithName( new QName( owsNS, "MaximumValue" ) ).getText();
                boolean areInteger = determineValueType( minimum, maximum );

                String spacing = omRange.getFirstChildWithName( new QName( owsNS, "Spacing" ) ).getText();
                if ( spacing == null ) {
                    if ( areInteger ) {
                        spacing = "1";
                    } else {
                        spacing = "0";
                    }
                }
                String closure = omRange.getAttributeValue( new QName( owsNS, "rangeClosure" ) );
                if ( "open".equals( closure ) && areInteger ) {
                    minimum = String.valueOf( Integer.parseInt( minimum ) + 1 );
                    maximum = String.valueOf( Integer.parseInt( minimum ) - 1 );
                }
                if ( "open-closed".equals( closure ) && areInteger ) {
                    minimum = String.valueOf( Integer.parseInt( minimum ) + 1 );
                }
                if ( "closed-open".equals( closure ) && areInteger ) {
                    maximum = String.valueOf( Integer.parseInt( minimum ) - 1 );
                }
                range.add( new Triple<String, String, String>( minimum, maximum, spacing ) );
            }
        }

        OMElement omValuesReference = input.getFirstChildWithName( new QName( owsNS, "ValuesReference" ) );
        URL valuesRef = null;
        URL valuesForm = null;
        if ( omValuesReference != null ) {
            String valuesRefStr = omValuesReference.getAttributeValue( new QName( owsNS, "reference" ) );
            try {
                valuesRef = new URL( valuesRefStr );
            } catch ( MalformedURLException e ) {
                // valuesRef stays null
            }

            String valuesFormStr = omValuesReference.getAttributeValue( new QName( null, "valuesForm" ) );
            try {
                valuesForm = new URL( valuesFormStr );
            } catch ( MalformedURLException e ) {
                // valuesRef stays null
            }
        }

        String[] valuesArray = null;
        if ( values != null ) {
            valuesArray = values.toArray( new String[values.size()] );
        }
        Triple<String, String, String>[] rangeArray = null;
        if ( range != null ) {
            rangeArray = range.toArray( new Triple[range.size()] );
        }
        return new LiteralDataDescription( dataType, dataTypeRef, defaultUomRef, defaultUomRef, supportedUom,
                                           valuesArray, rangeArray, anyValue, valuesRef, valuesForm );
    }

    /**
     * @param a
     * @param b
     * @return true if the values appear to be ints, false for floats
     */
    private boolean determineValueType( String a, String b ) {
        // TODO is this check enough to ensure the values are integer or floats?
        if ( !a.contains( "." ) && !b.contains( "." ) ) {
            return true;
        }
        return false;
    }

    /**
     * @return
     */
    private DataDescription parseComplexData( OMElement input ) {
        XPath xpath = new XPath( "Default/Format", nsContext );
        OMElement omDefaultFormat = omResponse.getElement( input, xpath );
        String mimeType = omDefaultFormat.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();
        String encoding = omDefaultFormat.getFirstChildWithName( new QName( null, "Encoding" ) ).getText();
        String schema = omDefaultFormat.getFirstChildWithName( new QName( null, "Schema" ) ).getText();
        ComplexAttributes defaultFormat = new ComplexAttributes( mimeType, encoding, schema );

        xpath = new XPath( "Supported/Format", nsContext );
        List<OMElement> omFormats = omResponse.getElements( input, xpath );
        ComplexAttributes[] supported = new ComplexAttributes[omFormats.size()];
        for ( int i = 0; i < omFormats.size(); i++ ) {
            OMElement omFormat = omFormats.get( i );
            mimeType = omFormat.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();
            encoding = omFormat.getFirstChildWithName( new QName( null, "Encoding" ) ).getText();
            schema = omFormat.getFirstChildWithName( new QName( null, "Schema" ) ).getText();
            supported[i] = new ComplexAttributes( mimeType, encoding, schema );
        }
        return new ComplexDataDescription( defaultFormat, supported );
    }

    /**
     * @param omElement
     * @return
     */
    private LanguageString parseLanguageString( OMElement omElement, String name ) {
        OMElement omElem = omElement.getFirstChildWithName( new QName( owsNS, name ) );
        if ( omElem != null ) {
            String lang = omElem.getAttributeValue( new QName( xmlNS, "lang" ) );
            return new LanguageString( omElem.getText(), lang );
        }
        return null;
    }

    /**
     * @return
     */
    private CodeType parseId( OMElement omElement ) {
        OMElement omId = omElement.getFirstChildWithName( new QName( owsNS, "Identifier" ) );
        String codeSpace = omId.getAttributeValue( new QName( null, "codeSpace" ) );
        if ( codeSpace != null ) {
            return new CodeType( omId.getText(), codeSpace );
        }
        return new CodeType( omId.getText() );
    }
}
