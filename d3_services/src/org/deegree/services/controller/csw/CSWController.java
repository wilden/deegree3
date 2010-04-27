//$HeadURL$ svn+ssh://sthomas@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/controller/csw/CSWController.java $
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
package org.deegree.services.controller.csw;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.csw.CSWConstants.Sections;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.record.persistence.RecordStoreException;
import org.deegree.services.controller.AbstractOGCServiceController;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.RequestContext;
import org.deegree.services.controller.configuration.DeegreeServicesMetadata;
import org.deegree.services.controller.configuration.ServiceIdentificationType;
import org.deegree.services.controller.configuration.ServiceProviderType;
import org.deegree.services.controller.csw.capabilities.GetCapabilities202KVPAdapter;
import org.deegree.services.controller.csw.capabilities.GetCapabilitiesVersionXMLAdapter;
import org.deegree.services.controller.csw.configuration.PublishedInformation;
import org.deegree.services.controller.csw.describerecord.DescribeRecord;
import org.deegree.services.controller.csw.describerecord.DescribeRecordKVPAdapter;
import org.deegree.services.controller.csw.describerecord.DescribeRecordXMLAdapter;
import org.deegree.services.controller.csw.exporthandling.DescribeRecordHandler;
import org.deegree.services.controller.csw.exporthandling.GetCapabilitiesHandler;
import org.deegree.services.controller.csw.exporthandling.GetRecordByIdHandler;
import org.deegree.services.controller.csw.exporthandling.GetRecordsHandler;
import org.deegree.services.controller.csw.exporthandling.TransactionHandler;
import org.deegree.services.controller.csw.getrecordbyid.GetRecordById;
import org.deegree.services.controller.csw.getrecordbyid.GetRecordByIdKVPAdapter;
import org.deegree.services.controller.csw.getrecordbyid.GetRecordByIdXMLAdapter;
import org.deegree.services.controller.csw.getrecords.GetRecords;
import org.deegree.services.controller.csw.getrecords.GetRecordsKVPAdapter;
import org.deegree.services.controller.csw.getrecords.GetRecordsXMLAdapter;
import org.deegree.services.controller.csw.transaction.Transaction;
import org.deegree.services.controller.csw.transaction.TransactionKVPAdapter;
import org.deegree.services.controller.csw.transaction.TransactionXMLAdapter;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.exception.ControllerInitException;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.ows.OWSException110XMLAdapter;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.csw.CSWService;
import org.deegree.services.csw.configuration.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Handles CSW (Cataloge Service for Web) protocol requests and acts as the link between the {@link OGCFrontController}
 * and the {@link CSWService}.
 * <p>
 * Supported CSW protocol versions:
 * <ul>
 * <li>2.0.2</li>
 * </ul>
 * </p>
 * 
 * @see CSWService
 * @see AbstractOGCServiceController
 * @see OGCFrontController
 * 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class CSWController extends AbstractOGCServiceController {

    private static final Logger LOG = LoggerFactory.getLogger( CSWController.class );

    private ServiceIdentificationType identification;

    private ServiceProviderType provider;

    private CSWService service;

    private DescribeRecordHandler describeRecordHandler;

    private GetRecordsHandler getRecordsHandler;

    private TransactionHandler transactionHandler;

    private GetRecordByIdHandler getRecordByIdHandler;

    // private HashMap<String, HashMap> optionalOperations = new HashMap<String, HashMap>();

    private static final ImplementationMetadata<CSWRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<CSWRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_202 };
            handledNamespaces = new String[] { CSW_202_NS };
            handledRequests = CSWRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "0.4.0" ) };
        }
    };

    @Override
    public void init( XMLAdapter controllerConf, DeegreeServicesMetadata serviceMetadata )
                            throws ControllerInitException {
        init( serviceMetadata, IMPLEMENTATION_METADATA, controllerConf );

        LOG.info( "Initializing CSW controller." );

        // check config version
        String configVersion = controllerConf.getRootElement().getAttributeValue( new QName( "configVersion" ) );
        checkConfigVersion( controllerConf.getSystemId(), configVersion );

        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( CSWConstants.CSW_PREFIX, "http://www.deegree.org/services/csw" );

        ServiceConfiguration sc = null;
        PublishedInformation pi = null;

        try {
            XPath xpath = new XPath( "csw:ServiceConfiguration", nsContext );

            final String additionalClasspath = "org.deegree.services.controller.csw.configuration:org.deegree.services.csw.configuration";
            Unmarshaller u = getUnmarshaller( additionalClasspath, null );

            OMElement scElement = controllerConf.getRequiredElement( controllerConf.getRootElement(), xpath );

            // turn the application schema location into an absolute URL
            sc = (ServiceConfiguration) u.unmarshal( scElement.getXMLStreamReaderWithoutCaching() );

            u = JAXBContext.newInstance( "org.deegree.services.controller.csw.configuration" ).createUnmarshaller();

            xpath = new XPath( "csw:PublishedInformation", nsContext );
            OMElement piElement = controllerConf.getRequiredElement( controllerConf.getRootElement(), xpath );
            pi = (PublishedInformation) u.unmarshal( piElement.getXMLStreamReaderWithoutCaching() );
            syncWithMainController( pi );

            service = new CSWService( sc );

        } catch ( XMLParsingException e ) {
            throw new ControllerInitException( "TODO", e );
        } catch ( JAXBException e ) {
            throw new ControllerInitException( "TODO", e );
        } catch ( RecordStoreException e ) {
            e.printStackTrace();
        }

        validateAndSetOfferedVersions( pi.getAcceptVersions().getVersion() );
        describeRecordHandler = new DescribeRecordHandler( service );
        getRecordsHandler = new GetRecordsHandler( service );
        transactionHandler = new TransactionHandler( service );
        getRecordByIdHandler = new GetRecordByIdHandler( service );

    }

    @Override
    protected void doKVP( Map<String, String> normalizedKVPParams, HttpServletRequest request,
                          HttpResponseBuffer response, List<FileItem> multiParts )
                            throws ServletException, IOException {

        RequestContext reqCon = OGCFrontController.getContext();
        // Credentials credentials = null;
        // if ( reqCon != null ) {
        // credentials = new Credentials( reqCon.getUsername(), reqCon.getPassword(), reqCon.getTokenId() );
        // }

        try {
            String rootElement = KVPUtils.getRequired( normalizedKVPParams, "REQUEST" );
            CSWRequestType requestType = getRequestType( rootElement );

            // if ( SecurityManager.getInstance().determineRights( this, requestType.name(), credentials ) ) {
            Version requestVersion = getVersion( normalizedKVPParams.get( "VERSION" ) );

            String serviceAttr = KVPUtils.getRequired( normalizedKVPParams, "SERVICE" );
            if ( !"CSW".equals( serviceAttr ) ) {
                throw new OWSException( "Wrong service attribute: '" + serviceAttr + "' -- must be 'CSW'.",
                                        OWSException.INVALID_PARAMETER_VALUE, "service" );
            }
            if ( requestType != CSWRequestType.GetCapabilities ) {
                checkVersion( requestVersion );
            }

            switch ( requestType ) {

            case GetCapabilities:
                GetCapabilities getCapabilities = GetCapabilities202KVPAdapter.parse( requestVersion,
                                                                                      normalizedKVPParams );
                doGetCapabilities( getCapabilities, request, response, false );
                break;
            case DescribeRecord:

                DescribeRecord descRec = DescribeRecordKVPAdapter.parse( normalizedKVPParams );
                // describeRecordResponse = new DescribeRecordResponseXMLAdapter(service);
                describeRecordHandler.doDescribeRecord( descRec, response, false );
                break;

            case GetRecords:
                GetRecords getRec = GetRecordsKVPAdapter.parse( normalizedKVPParams );
                getRecordsHandler.doGetRecords( getRec, response, false );
                break;
            case GetRecordById:
                GetRecordById getRecBI = GetRecordByIdKVPAdapter.parse( normalizedKVPParams );
                getRecordByIdHandler.doGetRecordById( getRecBI, response, false );
                break;
            case Transaction:
                Transaction trans = TransactionKVPAdapter.parse( normalizedKVPParams );
                transactionHandler.doTransaction( trans, response, false );
                break;
            }
            // }
        } catch ( OWSException e ) {
            sendServiceException( e, response );
        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        } catch ( SQLException e ) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doXML( XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
                          List<FileItem> multiParts )
                            throws ServletException, IOException {

        response.setContentType( "text/xml" );

        try {
            XMLAdapter requestDoc = new XMLAdapter( xmlStream );
            String rootElement = requestDoc.getRootElement().getLocalName();
            CSWRequestType requestType = getRequestType( rootElement );

            // check if requested version is supported and offered (except for GetCapabilities)
            Version requestVersion = getVersion( requestDoc.getRootElement().getAttributeValue( new QName( "version" ) ) );
            if ( requestType != CSWRequestType.GetCapabilities ) {
                checkVersion( requestVersion );
            }

            switch ( requestType ) {

            case GetCapabilities:
                GetCapabilitiesVersionXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesVersionXMLAdapter();
                getCapabilitiesAdapter.setRootElement( requestDoc.getRootElement() );
                GetCapabilities cswRequest = getCapabilitiesAdapter.parse( requestVersion );
                doGetCapabilities( cswRequest, request, response, false );
                break;
            case DescribeRecord:
                DescribeRecordXMLAdapter describeRecordAdapter = new DescribeRecordXMLAdapter();
                describeRecordAdapter.setRootElement( requestDoc.getRootElement() );
                DescribeRecord cswDRRequest = describeRecordAdapter.parse( requestVersion );
                describeRecordHandler.doDescribeRecord( cswDRRequest, response, false );
                break;
            case GetRecords:
                GetRecordsXMLAdapter getRecordsAdapter = new GetRecordsXMLAdapter();
                getRecordsAdapter.setRootElement( requestDoc.getRootElement() );
                GetRecords cswGRRequest = getRecordsAdapter.parse( requestVersion );
                getRecordsHandler.doGetRecords( cswGRRequest, response, false );
                break;
            case GetRecordById:
                GetRecordByIdXMLAdapter getRecordByIdAdapter = new GetRecordByIdXMLAdapter();
                getRecordByIdAdapter.setRootElement( requestDoc.getRootElement() );
                GetRecordById cswGRBIRequest = getRecordByIdAdapter.parse( requestVersion );
                getRecordByIdHandler.doGetRecordById( cswGRBIRequest, response, false );
                break;
            case Transaction:
                TransactionXMLAdapter transAdapter = new TransactionXMLAdapter();
                transAdapter.setRootElement( requestDoc.getRootElement() );
                Transaction cswTRequest = transAdapter.parse( requestVersion );
                transactionHandler.doTransaction( cswTRequest, response, false );
                break;
            }

        } catch ( OWSException e ) {
            sendServiceException( e, response );
        } catch ( MissingParameterException e ) {
            sendServiceException( new OWSException( e ), response );
        } catch ( InvalidParameterValueException e ) {
            sendServiceException( new OWSException( e ), response );
        } catch ( Exception e ) {
            sendServiceException( new OWSException( e.getMessage(), ControllerException.NO_APPLICABLE_CODE ), response );
        }

    }

    @Override
    protected void doSOAP( SOAPEnvelope soapDoc, HttpServletRequest request, HttpResponseBuffer response,
                           List<FileItem> multiParts, SOAPFactory factory )
                            throws OMException, ServletException {

        LOG.trace( "doSOAP invoked" );
        OMElement requestElement = soapDoc.getBody().getFirstElement();
        try {
            String rootElement = requestElement.getLocalName();

            CSWRequestType requestType = getRequestType( rootElement );

            // check if requested version is supported and offered (except for GetCapabilities)
            Version requestVersion = getVersion( requestElement.getAttributeValue( new QName( "version" ) ) );
            if ( requestType != CSWRequestType.GetCapabilities ) {
                checkVersion( requestVersion );
            }

            beginSOAPResponse( response );

            switch ( requestType ) {
            case GetCapabilities:
                GetCapabilitiesVersionXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesVersionXMLAdapter();
                getCapabilitiesAdapter.setRootElement( requestElement );
                GetCapabilities cswRequest = getCapabilitiesAdapter.parse( requestVersion );
                doGetCapabilities( cswRequest, request, response, true );
                break;
            case DescribeRecord:
                DescribeRecordXMLAdapter describeRecordAdapter = new DescribeRecordXMLAdapter();
                describeRecordAdapter.setRootElement( requestElement );
                DescribeRecord cswDRRequest = describeRecordAdapter.parse( requestVersion );
                describeRecordHandler.doDescribeRecord( cswDRRequest, response, true );
                break;
            case GetRecords:
                GetRecordsXMLAdapter getRecordsAdapter = new GetRecordsXMLAdapter();
                getRecordsAdapter.setRootElement( requestElement );
                GetRecords cswGRRequest = getRecordsAdapter.parse( requestVersion );
                getRecordsHandler.doGetRecords( cswGRRequest, response, true );
                break;
            case GetRecordById:
                GetRecordByIdXMLAdapter getRecordByIdAdapter = new GetRecordByIdXMLAdapter();
                getRecordByIdAdapter.setRootElement( requestElement );
                GetRecordById cswGRBIRequest = getRecordByIdAdapter.parse( requestVersion );
                getRecordByIdHandler.doGetRecordById( cswGRBIRequest, response, true );
                break;
            case Transaction:
                TransactionXMLAdapter transAdapter = new TransactionXMLAdapter();
                transAdapter.setRootElement( requestElement );
                Transaction cswTRequest = transAdapter.parse( requestVersion );
                transactionHandler.doTransaction( cswTRequest, response, true );
                break;

            }
            endSOAPResponse( response );
        } catch ( OWSException e ) {
            sendSOAPException( soapDoc.getHeader(), factory, response, e, null, null, null, request.getServerName(),
                               request.getCharacterEncoding() );
        } catch ( XMLStreamException e ) {
            LOG.debug( e.getMessage(), e );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /**
     * Method for mapping the request operation to the implemented operations located in {@link CSWConstants}
     * 
     * @param requestName
     * @return CSWRequestType
     * @throws OWSException
     */
    private CSWRequestType getRequestType( String requestName )
                            throws OWSException {
        CSWRequestType requestType = null;
        try {

            requestType = IMPLEMENTATION_METADATA.getRequestTypeByName( requestName );
        } catch ( IllegalArgumentException e ) {
            throw new OWSException( e.getMessage(), OWSException.OPERATION_NOT_SUPPORTED );
        }
        return requestType;
    }

    /**
     * Exports the correct recognized request.
     * 
     * @param getCapabilitiesRequest
     * @param requestWrapper
     * @param response
     * @throws XMLStreamException
     * @throws IOException
     * @throws OWSException
     */
    private void doGetCapabilities( GetCapabilities getCapabilitiesRequest, HttpServletRequest requestWrapper,
                                    HttpResponseBuffer response, boolean isSoap )
                            throws XMLStreamException, IOException, OWSException {

        checkOrCreateDCPGetURL( requestWrapper );
        checkOrCreateDCPPostURL( requestWrapper );
        Set<Sections> sections = getSections( getCapabilitiesRequest );

        Version negotiatedVersion = negotiateVersion( getCapabilitiesRequest );
        response.setContentType( "text/xml; charset=UTF-8" );

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, null );
        GetCapabilitiesHandler.export( xmlWriter, mainControllerConf, sections, identification, negotiatedVersion,
                                       isSoap );
        xmlWriter.flush();

    }

    /**
     * Returns an <code>XMLStreamWriter</code> for writing an XML response document.
     * 
     * @param writer
     *            writer to write the XML to, must not be null
     * @param schemaLocation
     *            allows to specify a value for the 'xsi:schemaLocation' attribute in the root element, must not be null
     * @return {@link XMLStreamWriter}
     * @throws XMLStreamException
     * @throws IOException
     */
    static XMLStreamWriter getXMLResponseWriter( HttpResponseBuffer writer, String schemaLocation )
                            throws XMLStreamException, IOException {

        if ( schemaLocation == null ) {
            return writer.getXMLWriter();
        }
        return new XMLStreamWriterWrapper( writer.getXMLWriter(), schemaLocation );
    }

    private void sendServiceException( OWSException ex, HttpResponseBuffer response )
                            throws ServletException {

        // TODO correct status code?
        sendException( "application/vnd.ogc.se_xml", "UTF-8", null, 300, new OWSException110XMLAdapter(), ex, response );
    }

    /**
     * Gets the sections described in the GetCapabilities operation.
     * 
     * @param capabilitiesReq
     * @return a set of type sections
     */
    private Set<Sections> getSections( GetCapabilities capabilitiesReq ) {
        Set<String> sections = capabilitiesReq.getSections();
        Set<Sections> result = new HashSet<Sections>();
        if ( !( sections.isEmpty() || sections.contains( "/" ) ) ) {
            final int length = "/CSW_Capabilities/".length();
            for ( String section : sections ) {
                if ( section.startsWith( "/CSW_Capabilities/" ) ) {
                    section = section.substring( length );
                }
                try {
                    result.add( Sections.valueOf( section ) );
                } catch ( IllegalArgumentException ex ) {
                    // unknown section name
                    // the spec does not say what to do, so we ignore it
                }
            }
        }
        return result;
    }

    /**
     * sets the identification to the main controller or it will be synchronized with the maincontroller.
     * 
     * @param publishedInformation
     */
    private void syncWithMainController( PublishedInformation publishedInformation ) {
        if ( identification == null ) {
            if ( publishedInformation == null || publishedInformation.getServiceIdentification() == null ) {
                LOG.info( "Using global service identification because no CSW specific service identification was defined." );
                identification = mainControllerConf.getServiceIdentification();
            } else {
                identification = synchronizeServiceIdentificationWithMainController( publishedInformation.getServiceIdentification() );
            }
        }
        if ( provider == null ) {
            if ( publishedInformation == null || publishedInformation.getServiceProvider() == null ) {
                LOG.info( "Using gloval serviceProvider because no CSW specific service provider was defined." );
                provider = mainControllerConf.getServiceProvider();
            } else {
                provider = synchronizeServiceProviderWithMainControllerConf( publishedInformation.getServiceProvider() );
            }

        }

    }

    /**
     * Parses the version.
     * 
     * @param versionString
     *            that should be parsed
     * @return {@link Version}
     * @throws OWSException
     */
    private Version getVersion( String versionString )
                            throws OWSException {
        Version version = null;
        if ( versionString != null ) {
            try {
                version = Version.parseVersion( versionString );
            } catch ( InvalidParameterValueException e ) {
                throw new OWSException( e );
            }
        }
        return version;
    }

}
