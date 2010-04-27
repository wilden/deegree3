//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/svn_classfile_header_template.xml $
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

package org.deegree.services.controller.wps.execute;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.wps.WPSConstants;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.BoundingBoxInput;
import org.deegree.services.wps.input.BoundingBoxInputImpl;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.InputReference;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.LiteralInputImpl;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.input.ReferencedComplexInput;
import org.deegree.services.wps.processdefinition.BoundingBoxInputDefinition;
import org.deegree.services.wps.processdefinition.ComplexFormatType;
import org.deegree.services.wps.processdefinition.ComplexInputDefinition;
import org.deegree.services.wps.processdefinition.ComplexOutputDefinition;
import org.deegree.services.wps.processdefinition.LiteralInputDefinition;
import org.deegree.services.wps.processdefinition.ProcessDefinition;
import org.deegree.services.wps.processdefinition.ProcessletInputDefinition;
import org.deegree.services.wps.processdefinition.ProcessletOutputDefinition;
import org.deegree.services.wps.processdefinition.LiteralInputDefinition.OtherUOM;
import org.deegree.services.wps.processdefinition.ProcessDefinition.InputParameters;
import org.deegree.services.wps.processdefinition.ProcessDefinition.OutputParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser and validator for incoming WPS <code>Execute</code> KVP requests.
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExecuteRequestKVPAdapter {

    // http://127.0.0.1:8080/services/services?service=WPS&request=Execute&version=1.0.0
    // http://127.0.0.1:8080/services/services?service=WPS&request=Execute&version=1.0.0&identifier=TestProcess
    // http://127.0.0.1:8080/services/services?service=WPS&request=Execute&version=1.0.0&identifier=TestProcess&DataInputs=LiteralInput=5
    // http://127.0.0.1:8080/services/services?service=WPS&request=Execute&version=1.0.0&identifier=TestProcess&DataInputs=LiteralInput=5@uom=seconds@datatype=integer
    // http://127.0.0.1:8080/services/services?service=WPS&request=Execute&version=1.0.0&identifier=TestProcess&DataInputs=LiteralInput=5@uom=seconds@datatype=integer;BBOXInput=0,0,90,180,EPSG:4326
    // http://127.0.0.1:8080/services/services?service=WPS&request=Execute&version=1.0.0&identifier=TestProcess&DataInputs=LiteralInput=5@uom=seconds@datatype=integer;BBOXInput=0,0,90,180,EPSG:4326;XMLInput=@xlink:href=http%3A//testing.deegree.org/deegree-wfs/services%3FREQUEST%3DGetCapabilities%26version%3D1.1.0%26service%3DWFS
    // http://127.0.0.1:8080/services/services?service=WPS&request=Execute&version=1.0.0&identifier=TestProcess&DataInputs=LiteralInput=5@uom=seconds@datatype=integer;BBOXInput=0,0,90,180,EPSG:4326;XMLInput=@xlink:href=http%3A//testing.deegree.org/deegree-wfs/services%3FREQUEST%3DGetCapabilities%26version%3D1.1.0%26service%3DWFS;BinaryInput=@xlink:href=http%3A//www.deegree.org/deegree/images/deegree/logo-deegree.png
    // http://127.0.0.1:8080/services/services?service=WPS&request=Execute&version=1.0.0&identifier=TestProcess&DataInputs=LiteralInput=5@uom=seconds@datatype=integer;BBOXInput=0,0,90,180,EPSG:4326;XMLInput=@xlink:href=http%3A//testing.deegree.org/deegree-wfs/services%3FREQUEST%3DGetCapabilities%26version%3D1.1.0%26service%3DWFS;BinaryInput=@xlink:href=http%3A//www.deegree.org/deegree/images/deegree/logo-deegree.png&RawDataOutput=XMLOutput
    // http://127.0.0.1:8080/services/services?service=WPS&request=Execute&version=1.0.0&identifier=TestProcess&DataInputs=LiteralInput=5@uom=seconds@datatype=integer;BBOXInput=0,0,90,180,EPSG:4326;XMLInput=@xlink:href=http%3A//testing.deegree.org/deegree-wfs/services%3FREQUEST%3DGetCapabilities%26version%3D1.1.0%26service%3DWFS;BinaryInput=@xlink:href=http%3A//www.deegree.org/deegree/images/deegree/logo-deegree.png&RawDataOutput=BinaryOutput
    // http://127.0.0.1:8080/services/services?service=WPS&request=Execute&version=1.0.0&identifier=TestProcess&DataInputs=LiteralInput=5@uom=seconds@datatype=integer;BBOXInput=0,0,90,180,EPSG:4326;XMLInput=@xlink:href=http%3A//testing.deegree.org/deegree-wfs/services%3FREQUEST%3DGetCapabilities%26version%3D1.1.0%26service%3DWFS;BinaryInput=@xlink:href=http%3A//www.deegree.org/deegree/images/deegree/logo-deegree.png&ResponseDocument=BinaryOutput;XMLOutput&storeExecuteResponse=true
    // http://127.0.0.1:8080/services/services?service=WPS&request=Execute&version=1.0.0&identifier=TestProcess&DataInputs=LiteralInput=5@uom=seconds@datatype=integer;BBOXInput=0,0,90,180,EPSG:4326;XMLInput=@xlink:href=http%3A//testing.deegree.org/deegree-wfs/services%3FREQUEST%3DGetCapabilities%26version%3D1.1.0%26service%3DWFS;BinaryInput=@xlink:href=http%3A//www.deegree.org/deegree/images/deegree/logo-deegree.png&ResponseDocument=BinaryOutput;XMLOutput&storeExecuteResponse=true&lineage=true

    private static final Logger LOG = LoggerFactory.getLogger( ExecuteRequestKVPAdapter.class );

    private static final GeometryFactory geomFac = new GeometryFactory();

    /**
     * Parses the given WPS 1.0.0 <code>ExecuteRequest</code> KVP request.
     * <p>
     * Prerequisites (not checked by this method):
     * <ul>
     * <li>Key 'SERVICE' has value 'WPS'</li>
     * <li>Key 'REQUEST' has value 'Execute'</li>
     * <li>Key 'VERSION' has value '1.0.0'</li>
     * </ul>
     * </p>
     * 
     * @param kvpParams
     *            key-value pairs, keys must be uppercase
     * @param idToProcessDefinition
     *            key: process identifier, value: process definition
     * @return corresponding {@link ExecuteRequest} object
     * @throws OWSException
     * @throws UnknownCRSException
     */
    public static ExecuteRequest parse100( Map<String, String> kvpParams,
                                           Map<CodeType, ProcessDefinition> idToProcessDefinition )
                            throws OWSException, UnknownCRSException {

        LOG.debug( "parse100" );

        // IDENTIFIER (mandatory)
        String identifierString = kvpParams.get( "IDENTIFIER" );
        LOG.debug( "IDENTIFIER=" + identifierString );
        if ( identifierString == null ) {
            throw new OWSException( "MissingParameterValue: Identifier", OWSException.MISSING_PARAMETER_VALUE,
                                    "IDENTIFIER" );
        }
        CodeType processId = new CodeType( identifierString );
        ProcessDefinition processDef = lookupProcessDefinition( processId, idToProcessDefinition );

        // "LANGUAGE" (optional)
        String language = kvpParams.get( "LANGUAGE" );

        // "DATAINPUTS" (optional)
        ProcessletInputs inputs = null;
        String dataInputsString = kvpParams.get( "DATAINPUTS" );
        if ( dataInputsString != null ) {
            inputs = parseDataInputs( dataInputsString, processDef );
        }

        // choice: "RESPONSEDOCUMENT" or "RAWDATAOUTPUT" (or none)
        ResponseForm responseForm = null;
        String responseDocumentString = kvpParams.get( "RESPONSEDOCUMENT" );
        String rawDataOutputString = kvpParams.get( "RAWDATAOUTPUT" );
        if ( responseDocumentString != null && rawDataOutputString != null ) {
            throw new OWSException(
                                    "InvalidParameterValue: 'ResponseDocument' and 'RawDataOutput' parameters are mutually exclusive.",
                                    OWSException.INVALID_PARAMETER_VALUE );
        } else if ( rawDataOutputString != null ) {
            responseForm = parseRawDataOutput( rawDataOutputString, processDef );
        } else {
            // NOTE: responseDocumentString may be null here
            responseForm = parseResponseDocument( responseDocumentString, kvpParams, processDef );
        }

        return new ExecuteRequest( WPSConstants.VERSION_100, language, processDef, inputs, responseForm );
    }

    private static ProcessletInputs parseDataInputs( String dataInputsString, ProcessDefinition processDef )
                            throws OWSException {

        // a semicolon (;) is used to separate one input from the next
        String[] encodedInputs = dataInputsString.split( ";" );

        LOG.debug( "DATAINPUTS=" );
        for ( String encodedParameter : encodedInputs ) {
            LOG.debug( "- " + encodedParameter );
        }

        // key: id of input parameter, value: number of occurences
        Map<CodeType, Integer> inputIdToCount = new HashMap<CodeType, Integer>();

        List<ProcessletInput> processInputs = new ArrayList<ProcessletInput>( encodedInputs.length );
        for ( String encodedParameter : encodedInputs ) {
            AttributedParameter parameter = AttributedParameter.valueOf( encodedParameter );
            LOG.debug( "AttributedParameter: " + parameter );
            ProcessletInput input = parseDataInput( parameter, processDef );
            processInputs.add( input );

            CodeType inputId = input.getIdentifier();
            Integer count = inputIdToCount.get( inputId );
            if ( count == null ) {
                count = 1;
            } else {
                count++;
            }
            inputIdToCount.put( inputId, count );
        }

        // validate cardinalities of present input parameters
        for ( JAXBElement<? extends ProcessletInputDefinition> el : processDef.getInputParameters().getProcessInput() ) {
            ProcessletInputDefinition inputDef = el.getValue();
            CodeType inputId = new CodeType( inputDef.getIdentifier().getValue(),
                                             inputDef.getIdentifier().getCodeSpace() );
            int minOccurs = inputDef.getMinOccurs() != null ? inputDef.getMinOccurs().intValue() : 1;
            int maxOccurs = inputDef.getMaxOccurs() != null ? inputDef.getMaxOccurs().intValue() : 1;
            int actualOccurs = inputIdToCount.get( inputId ) != null ? inputIdToCount.get( inputId ) : 0;
            if ( actualOccurs < minOccurs ) {
                String msg = "Input parameter '" + inputId + "' is present " + actualOccurs + " times, but at least "
                             + minOccurs + " occurrence(s) is/are required according to the corresponding parameter "
                             + "definition.";
                throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + inputId );
            }
            if ( actualOccurs > minOccurs ) {
                String msg = "Input parameter '" + inputId + "' is present " + actualOccurs + " times, but only "
                             + maxOccurs + " occurrence(s) is/are allowed according to the corresponding parameter "
                             + "definition.";
                throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + inputId );
            }
        }

        return new ProcessletInputs( processInputs );
    }

    private static ProcessletInput parseDataInput( AttributedParameter encodedParameter, ProcessDefinition processDef )
                            throws OWSException {

        CodeType inputId = encodedParameter.getParameterId();
        ProcessletInputDefinition definition = lookupInputDefinition( encodedParameter.getParameterId(), processDef );

        ProcessletInput input = null;
        if ( definition instanceof LiteralInputDefinition ) {
            input = parseLiteralInput( inputId, (LiteralInputDefinition) definition, encodedParameter );
        } else if ( definition instanceof BoundingBoxInputDefinition ) {
            input = parseBoundingBoxInput( inputId, (BoundingBoxInputDefinition) definition, encodedParameter );
        } else if ( definition instanceof ComplexInputDefinition ) {
            input = parseComplexInput( inputId, (ComplexInputDefinition) definition, encodedParameter );
        }
        return input;
    }

    private static ComplexInput parseComplexInput( CodeType inputId, ComplexInputDefinition definition,
                                                   AttributedParameter parameter )
                            throws OWSException {

        if ( parameter.getHref() == null ) {
            String msg = "Value for complex input parameter '" + inputId
                         + "' does not specify an 'xlink:href' attribute. Inline complex values "
                         + "are not supported for KVP Execute requests.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + inputId );
        }
        URL url = null;
        try {
            url = new URL( parameter.getHref() );
        } catch ( MalformedURLException e ) {
            String msg = "Value for attribute 'xlink:href' (='" + parameter.getHref()
                         + "' of complex input parameter '" + inputId + "' does not denote a valid URL: "
                         + e.getMessage();
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + inputId );
        }

        ComplexFormatType format = new ComplexFormatType();
        format.setMimeType( parameter.getMimeType() );
        format.setEncoding( parameter.getEncoding() );
        format.setSchema( parameter.getSchema() );
        ComplexFormatType augmentedFormat = validateAndAugmentFormat( format, definition );

        InputReference reference = new InputReference( url, new HashMap<String, String>() );

        return new ReferencedComplexInput( definition, null, null, augmentedFormat, reference );
    }

    private static ComplexFormatType validateAndAugmentFormat( ComplexFormatType format,
                                                               ComplexInputDefinition definition )
                            throws OWSException {

        LOG.debug( "Looking up compatible format ('" + toString( format ) + "') in parameter definition." );
        List<ComplexFormatType> equalMimeType = null;
        if ( format.getMimeType() == null ) {
            // not specified -> assume mime type from default format
            equalMimeType = Collections.singletonList( definition.getDefaultFormat() );
        } else {
            equalMimeType = new LinkedList<ComplexFormatType>();
            if ( format.getMimeType().equals( definition.getDefaultFormat().getMimeType() ) ) {
                equalMimeType.add( definition.getDefaultFormat() );
            }
            for ( ComplexFormatType otherFormat : definition.getOtherFormats() ) {
                if ( format.getMimeType().equals( otherFormat.getMimeType() ) ) {
                    equalMimeType.add( format );
                }
            }
        }

        // no matching formats (mime type) found?
        if ( equalMimeType.isEmpty() ) {
            CodeType identifier = new CodeType( definition.getIdentifier().getValue(),
                                                definition.getIdentifier().getCodeSpace() );
            String msg = "Mime type (='" + format.getMimeType() + "') is not supported for input parameter '"
                         + identifier + "' according to the corresponding parameter definition.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + identifier );
        }

        List<ComplexFormatType> equalMimeTypeAndSchema = new LinkedList<ComplexFormatType>();
        for ( ComplexFormatType candidateFormat : equalMimeType ) {
            if ( format.getSchema() == null || format.getSchema().equals( candidateFormat.getSchema() ) ) {
                equalMimeTypeAndSchema.add( candidateFormat );
            }
        }

        // no matching formats (mime type and schema) found?
        if ( equalMimeTypeAndSchema.isEmpty() ) {
            CodeType identifier = new CodeType( definition.getIdentifier().getValue(),
                                                definition.getIdentifier().getCodeSpace() );
            String msg = "Combination of mime type (='" + format.getMimeType() + "') and schema (='"
                         + format.getSchema() + "') is not supported for input parameter '" + identifier
                         + "' according to the corresponding parameter definition.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + identifier );
        }

        List<ComplexFormatType> matchingFormats = new LinkedList<ComplexFormatType>();
        for ( ComplexFormatType candidateFormat : equalMimeTypeAndSchema ) {
            if ( format.getEncoding() == null || format.getEncoding().equals( candidateFormat.getEncoding() ) ) {
                matchingFormats.add( candidateFormat );
            }
        }

        // no formats with specified mime type, schema and encoding found?
        if ( matchingFormats.isEmpty() ) {
            CodeType identifier = new CodeType( definition.getIdentifier().getValue(),
                                                definition.getIdentifier().getCodeSpace() );
            String msg = "Combination of mime type (='" + format.getMimeType() + "'), schema (='" + format.getSchema()
                         + "') and encoding (='" + format.getEncoding() + "') is not supported for input parameter '"
                         + identifier + "' according to the corresponding parameter definition.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + identifier );
        }

        if ( matchingFormats.size() > 0 ) {
            CodeType identifier = new CodeType( definition.getIdentifier().getValue(),
                                                definition.getIdentifier().getCodeSpace() );
            String msg = "Format specification for complex input parameter '" + identifier
                         + "' is not unique. Using first match: '" + matchingFormats.get( 0 ) + "'.";
            LOG.warn( msg );
        }

        ComplexFormatType matchingFormat = matchingFormats.get( 0 );
        LOG.debug( "Augmented format: '" + toString( matchingFormat ) + "'" );
        return matchingFormat;
    }

    private static ComplexFormatType validateAndAugmentFormat( ComplexFormatType format,
                                                               ComplexOutputDefinition definition )
                            throws OWSException {

        LOG.debug( "Looking up compatible format ('" + toString( format ) + "') in parameter definition." );
        List<ComplexFormatType> equalMimeType = null;
        if ( format.getMimeType() == null ) {
            // not specified -> assume mime type from default format
            equalMimeType = Collections.singletonList( definition.getDefaultFormat() );
        } else {
            equalMimeType = new LinkedList<ComplexFormatType>();
            if ( format.getMimeType().equals( definition.getDefaultFormat().getMimeType() ) ) {
                equalMimeType.add( definition.getDefaultFormat() );
            }
            for ( ComplexFormatType otherFormat : definition.getOtherFormats() ) {
                if ( format.getMimeType().equals( otherFormat.getMimeType() ) ) {
                    equalMimeType.add( format );
                }
            }
        }

        // no matching formats (mime type) found?
        if ( equalMimeType.isEmpty() ) {
            CodeType identifier = new CodeType( definition.getIdentifier().getValue(),
                                                definition.getIdentifier().getCodeSpace() );
            String msg = "Mime type (='" + format.getMimeType() + "') is not supported for output parameter '"
                         + identifier + "' according to the corresponding parameter definition.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + identifier );
        }

        List<ComplexFormatType> equalMimeTypeAndSchema = new LinkedList<ComplexFormatType>();
        for ( ComplexFormatType candidateFormat : equalMimeType ) {
            if ( format.getSchema() == null || format.getSchema().equals( candidateFormat.getSchema() ) ) {
                equalMimeTypeAndSchema.add( candidateFormat );
            }
        }

        // no matching formats (mime type and schema) found?
        if ( equalMimeTypeAndSchema.isEmpty() ) {
            CodeType identifier = new CodeType( definition.getIdentifier().getValue(),
                                                definition.getIdentifier().getCodeSpace() );
            String msg = "Combination of mime type (='" + format.getMimeType() + "') and schema (='"
                         + format.getSchema() + "') is not supported for output parameter '" + identifier
                         + "' according to the corresponding parameter definition.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + identifier );
        }

        List<ComplexFormatType> matchingFormats = new LinkedList<ComplexFormatType>();
        for ( ComplexFormatType candidateFormat : equalMimeTypeAndSchema ) {
            if ( format.getEncoding() == null || format.getEncoding().equals( candidateFormat.getEncoding() ) ) {
                matchingFormats.add( candidateFormat );
            }
        }

        // no formats with specified mime type, schema and encoding found?
        if ( matchingFormats.isEmpty() ) {
            CodeType identifier = new CodeType( definition.getIdentifier().getValue(),
                                                definition.getIdentifier().getCodeSpace() );
            String msg = "Combination of mime type (='" + format.getMimeType() + "'), schema (='" + format.getSchema()
                         + "') and encoding (='" + format.getEncoding() + "') is not supported for output parameter '"
                         + identifier + "' according to the corresponding parameter definition.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + identifier );
        }

        if ( matchingFormats.size() > 0 ) {
            CodeType identifier = new CodeType( definition.getIdentifier().getValue(),
                                                definition.getIdentifier().getCodeSpace() );
            String msg = "Format specification for complex output parameter '" + identifier
                         + "' is not unique. Using first match: '" + matchingFormats.get( 0 ) + "'.";
            LOG.warn( msg );
        }

        ComplexFormatType matchingFormat = matchingFormats.get( 0 );
        LOG.debug( "Augmented format: '" + toString( matchingFormat ) + "'" );
        return matchingFormat;
    }

    private static String toString( ComplexFormatType format ) {
        return "mimeType: " + format.getMimeType() + ", encoding: " + format.getEncoding() + ", schema: "
               + format.getSchema();
    }

    private static BoundingBoxInput parseBoundingBoxInput( CodeType inputId, BoundingBoxInputDefinition definition,
                                                           AttributedParameter parameter )
                            throws OWSException {

        String[] parts = parameter.getValue().split( "," );
        LOG.warn( "Assuming two-dimensional coordinates in BBOX string: '" + parameter.getValue() + "'" );
        if ( parts.length < 4 ) {
            String msg = "Value for bounding box input parameter '" + inputId + "' (='" + parts
                         + "') does not specify enough coordinates.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + inputId );
        }

        double lowerX = -1;
        double lowerY = -1;
        double upperX = -1;
        double upperY = -1;
        try {
            lowerX = Double.parseDouble( parts[0] );
            lowerY = Double.parseDouble( parts[1] );
            upperX = Double.parseDouble( parts[2] );
            upperY = Double.parseDouble( parts[3] );
        } catch ( NumberFormatException e ) {
            String msg = "Value for bounding box input parameter '" + inputId + "' (='" + parts
                         + "') does not contain valid coordinates.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "" + inputId );
        }

        String crsName = definition.getDefaultCRS();
        if ( parts.length > 4 ) {
            crsName = parts[4];
        }

        // validate against parameter definition
        Set<String> supportedCRS = new HashSet<String>();
        supportedCRS.add( definition.getDefaultCRS() );
        for ( String otherCRS : definition.getOtherCRS() ) {
            supportedCRS.add( otherCRS );
        }
        if ( !supportedCRS.contains( crsName ) ) {
            String msg = "Value of crs attribute (='" + crsName + "') for input parameter '" + inputId
                         + "' is not supported according the corresponding parameter definition.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, inputId.toString() );
        }

        CRS crs = null;
        if ( crsName != null ) {
            crs = new CRS( crsName );
        }

        Envelope bbox = geomFac.createEnvelope( lowerX, lowerY, upperX, upperY, crs );
        return new BoundingBoxInputImpl( definition, null, null, bbox );
    }

    private static LiteralInput parseLiteralInput( CodeType inputId, LiteralInputDefinition definition,
                                                   AttributedParameter parameter )
                            throws OWSException {
        // "dataType" attribute (optional)
        String dataType = parameter.getDataType();
        String definedDataType = definition.getDataType() != null ? definition.getDataType().getValue() : dataType;
        if ( dataType != null && !dataType.equals( definedDataType ) ) {
            String msg = "Value of datatype attribute (='" + dataType + "') for input parameter '" + inputId
                         + "' does not match the datatype from the corresponding parameter definition (='"
                         + definedDataType + "')";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, inputId.toString() );
        }

        // "uom" attribute (optional)
        String uom = parameter.getUom();
        if ( uom == null ) {
            // not specified -> use default UOM from parameter definition
            uom = definition.getDefaultUOM() != null ? definition.getDefaultUOM().getValue() : null;
        } else {
            // validate against parameter definition
            Set<String> supportedUOMs = new HashSet<String>();
            if ( definition.getDefaultUOM() != null ) {
                supportedUOMs.add( definition.getDefaultUOM().getValue() );
                for ( OtherUOM otherUOM : definition.getOtherUOM() ) {
                    supportedUOMs.add( otherUOM.getValue() );
                }
            }
            if ( !supportedUOMs.contains( uom ) ) {
                String msg = "Value of uom attribute (='" + uom + "') for input parameter '" + inputId
                             + "' is not supported according the corresponding parameter definition.";
                throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, inputId.toString() );
            }
        }
        return new LiteralInputImpl( definition, null, null, parameter.getValue(), uom );
    }

    private static ResponseForm parseResponseDocument( String responseDocumentString, Map<String, String> kvpParams,
                                                       ProcessDefinition processDef )
                            throws OWSException {

        try {
            // "STOREEXECUTERESPONSE" (optional)
            boolean storeExecuteResponse = KVPUtils.getBoolean( kvpParams, "STOREEXECUTERESPONSE", false );

            // "LINEAGE" (optional)
            boolean lineage = KVPUtils.getBoolean( kvpParams, "LINEAGE", false );

            // "STATUS" (optional)
            boolean status = KVPUtils.getBoolean( kvpParams, "STATUS", false );

            List<RequestedOutput> outputs = new ArrayList<RequestedOutput>();
            if ( responseDocumentString != null ) {
                String[] encodedOutputs = responseDocumentString.split( ";" );
                outputs = new ArrayList<RequestedOutput>( encodedOutputs.length );
                for ( String encodedOutput : encodedOutputs ) {
                    AttributedParameter output = AttributedParameter.valueOf( encodedOutput );
                    outputs.add( parseOutput( output, processDef ) );
                }
            }

            return new ResponseDocument( outputs, storeExecuteResponse, lineage, status );
        } catch ( InvalidParameterValueException e ) {
            throw new OWSException( e.getMessage(), OWSException.INVALID_PARAMETER_VALUE );
        }
    }

    private static RequestedOutput parseOutput( AttributedParameter parameter, ProcessDefinition processDef )
                            throws OWSException {

        CodeType outputId = parameter.getParameterId();
        ProcessletOutputDefinition definition = lookupOutputDefinition( outputId, processDef );

        boolean asReference = false;
        if ( parameter.getAsReference() != null ) {
            asReference = Boolean.parseBoolean( parameter.getAsReference() );
        }

        // TODO validate against offered uoms
        
        ComplexFormatType format = new ComplexFormatType();
        format.setMimeType( parameter.getMimeType() );
        format.setEncoding( parameter.getEncoding() );
        format.setSchema( parameter.getSchema() );
        if ( definition instanceof ComplexOutputDefinition ) {
            format = validateAndAugmentFormat( format, (ComplexOutputDefinition) definition );
        }

        return new RequestedOutput( definition, asReference, format.getMimeType(), format.getEncoding(),
                                    format.getSchema(), parameter.getUom(), null, null );
    }

    private static RawDataOutput parseRawDataOutput( String rawDataOutputString, ProcessDefinition process )
                            throws OWSException {

        AttributedParameter parameter = AttributedParameter.valueOf( rawDataOutputString );
        CodeType outputId = parameter.getParameterId();
        ProcessletOutputDefinition outputDefinition = lookupOutputDefinition( outputId, process );

        ComplexFormatType format = new ComplexFormatType();
        format.setMimeType( parameter.getMimeType() );
        format.setEncoding( parameter.getEncoding() );
        format.setSchema( parameter.getSchema() );
        if ( outputDefinition instanceof ComplexOutputDefinition ) {
            format = validateAndAugmentFormat( format, (ComplexOutputDefinition) outputDefinition );
        }

        RequestedOutput requestedOutput = new RequestedOutput( outputDefinition, false, format.getMimeType(),
                                                               format.getEncoding(), format.getSchema(),
                                                               parameter.getUom(), null, null );

        return new RawDataOutput( requestedOutput );
    }

    private static ProcessDefinition lookupProcessDefinition( CodeType identifier,
                                                              Map<CodeType, ProcessDefinition> idToProcessDefinition )
                            throws OWSException {
        ProcessDefinition processDef = idToProcessDefinition.get( identifier );
        if ( processDef == null ) {
            String msg = "No process with identifier '" + identifier + "' is known to the WPS.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "ows:Identifier" );
        }
        return processDef;
    }

    private static ProcessletInputDefinition lookupInputDefinition( CodeType identifier, ProcessDefinition processDef )
                            throws OWSException {

        LOG.trace( "Looking up input type: " + identifier );
        ProcessletInputDefinition inputType = null;
        InputParameters inputParams = processDef.getInputParameters();
        for ( JAXBElement<? extends ProcessletInputDefinition> el : inputParams.getProcessInput() ) {
            LOG.trace( "Defined input type: " + el.getValue().getIdentifier().getValue() );
            org.deegree.services.wps.processdefinition.CodeType inputId = el.getValue().getIdentifier();
            if ( equals( identifier, inputId ) ) {
                inputType = el.getValue();
            }
        }
        if ( inputType == null ) {
            String msg = "Process '" + processDef.getIdentifier().getValue()
                         + "' has no input parameter with identifier '" + identifier + "'.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, identifier.getCode() );
        }
        return inputType;
    }

    private static ProcessletOutputDefinition lookupOutputDefinition( CodeType identifier, ProcessDefinition processDef )
                            throws OWSException {

        ProcessletOutputDefinition outputType = null;
        OutputParameters outputParams = processDef.getOutputParameters();
        for ( JAXBElement<? extends ProcessletOutputDefinition> el : outputParams.getProcessOutput() ) {
            org.deegree.services.wps.processdefinition.CodeType outputId = el.getValue().getIdentifier();
            if ( equals( identifier, outputId ) ) {
                outputType = el.getValue();
            }
        }
        if ( outputType == null ) {
            String msg = "Process '" + processDef.getIdentifier().getValue()
                         + "' has no output parameter with identifier '" + identifier + "'.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, identifier.getCode() );
        }
        return outputType;
    }

    private static boolean equals( CodeType codeType, org.deegree.services.wps.processdefinition.CodeType codeType2 ) {
        if ( codeType2.getValue().equals( codeType.getCode() ) ) {
            if ( codeType2.getCodeSpace() == null ) {
                return codeType.getCodeSpace() == null;
            }
            return codeType2.getCodeSpace().equals( codeType.getCodeSpace() );
        }
        return false;
    }
}
