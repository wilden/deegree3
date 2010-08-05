//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.wps.process.ProcessInfo;
import org.deegree.protocol.wps.wps100.WPS100CapabilitiesAdapter;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API-level client for accessing services that implement the <a
 * href="http://www.opengeospatial.org/standards/wps">WebProcessingService (WPS) 1.0.0</a> protocol.
 * 
 * <h4>Initialization</h4> In the initial step, one constructs a new {@link WPSClient} instance by invoking the
 * constructor with a URL to a WPS capabilities document. In most cases, this will be a GetCapabilities request
 * (including necessary parameters) to a WPS service.
 * 
 * <pre>
 * ...
 *   URL processUrl = new URL( "http://...?service=WPS&version=1.0.0&request=GetCapabilities" );
 *   WPSClient wpsClient = new WPSClient( processUrl );
 * ...
 * </pre>
 * 
 * Afterwards, the {@link WPSClient} instance is bound to the specified service and allows to access announced service
 * metadata, process information as well as the execution of processes.
 * 
 * <h4>Accessing service metadata</h4> The method {@link #getMetadata()} allows to access the metadata announced by the
 * service, such as title, abstract, provider etc.
 * 
 * <h4>Getting process information</h4> The method {@link #getProcesses()} allows to find out about the processes
 * offered by the service. Additionally (if one knows the identifier of a process beforehand, one can use
 * {@link #getProcess(String, String)} to retrieve a specific process). The {@link Process} class allows to execute a
 * process and offers methods to access detail information such as title, abstract, input parameter types and output
 * parameters types:
 * 
 * <pre>
 * ...
 *   Process buffer = wpsClient.getProcess ("Buffer", null);
 *   System.out.println ("Abstract for Buffer process: " + buffer.getAbstract());
 *   System.out.println ("Number of input parameters: " + buffer.getInputTypes().length);
 *   System.out.println ("Number of output parameters: " + buffer.getOutputTypes().length);
 * ...
 * </pre>
 * 
 * <h4>Executing a process</h4> When executing a request, the method {@link Process#prepareExecution()} must be used to
 * create a {@link ProcessExecution} context first. This context provides methods for setting the input parameters,
 * controlling the desired output parameter behaviour and invoking the execution.
 * 
 * <pre>
 * ...
 *   Process buffer = wpsClient.getProcess ("Buffer", null);
 * 
 *   // get execution context
 *   ProcessExecution execution = buffer.prepareExecution();
 *   
 *   // add input parameters
 *   execution.addLiteralInput( "BufferDistance", null, "0.1", "double", "unity" );
 *   execution.addXMLInput( "GMLInput", null, gmlFileUrl, "text/xml", null, null );
 *   
 *   // perform execution
 *   ExecutionOutputs outputs = execution.execute();
 *   
 *   // access individual output values
 *   ComplexOutput bufferedGeometry = outputs.getXML ("BufferedGeometry", null);
 *   XMLStreamReader xmlStream = bufferedGeometry.getAsXMLStream();
 * ...
 * </pre>
 * 
 * <h4>Executing a process asynchronously</h4>
 * 
 * <pre>
 * ...
 *   Process buffer = wpsClient.getProcess ("Buffer", null);
 * 
 *   // get execution context
 *   ProcessExecution execution = buffer.prepareExecution();
 *   
 *   // add input parameters
 *   execution.addLiteralInput( "BufferDistance", null, "0.1", "double", "unity" );
 *   execution.addXMLInput( "GMLInput", null, gmlFileUrl, "text/xml", null, null );
 *   
 *   // invoke asynchronous execution (returns immediately)
 *   execution.executeAsync();
 *   
 *   // do other stuff
 *   ...
 *   
 *   // check execution state
 *   if (execution.getState() == SUCCEEDED) {
 *       ExecutionOutputs outputs = execution.getOutputs();
 *       ...
 *       // access the outputs as in synchronous case
 *       ComplexOutput bufferedGeometry = outputs.getXML ("BufferedGeometry", null);
 *       XMLStreamReader xmlStream = bufferedGeometry.getAsXMLStream();
 *   }
 * ...
 * </pre>
 * 
 * <h4>Implementation notes</h4>
 * <ul>
 * <li>Supported protocol versions: WPS 1.0.0</li>
 * <li>The implementation is thread-safe, a single {@link WPSClient} instance can be shared among multiple threads.</li>
 * </ul>
 *
 * <h4>TODOs</h4>
 * <ul>
 * <li>Handle raw output in the ResponseReader.</li>
 * <li>Implement input parameter passing for POST-references.</li>
 * <li>Cope with exceptions reports that are returned for <code>GetCapabilities</code> and <code>DescribeProcess</code>
 * requests.</li>
 * <li>Clean up exception handling.</li>
 * <li>Enable/document a way to set connection parameters (timeout, proxy settings, ...)</li>
 * <li>Support metadata in multiple languages (as mandated by the WPS spec).</li>
 * <li>Check validity (cardinality, order) of input and output parameters in {@link ProcessExecution}.</li>
 * </ul>
 * 
 * @see Process
 * @see ProcessExecution
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSClient {

    private static Logger LOG = LoggerFactory.getLogger( WPSClient.class );

    private final DeegreeServicesMetadataType metadata;

    // [0]: GET, [1]: POST
    private final URL[] describeProcessURLs = new URL[2];

    // [0]: GET, [1]: POST
    private final URL[] executeURLs = new URL[2];

    // using LinkedHashMap because it keeps insertion order
    private final Map<CodeType, Process> processIdToProcess = new LinkedHashMap<CodeType, Process>();

    /**
     * Creates a new {@link WPSClient} instance.
     * 
     * @param capabilitiesURL
     *            url of a WPS capabilities document, usually this is a GetCapabilities request to a WPS service, must
     *            not be <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public WPSClient( URL capabilitiesURL ) throws IOException, OWSException {

        WPS100CapabilitiesAdapter capabilitiesDoc = retrieveCapabilities( capabilitiesURL );

        // TODO what if server only supports Get? What is optional and what is mandatory?
        describeProcessURLs[0] = capabilitiesDoc.getOperationURL( "DescribeProcess", false );
        describeProcessURLs[1] = capabilitiesDoc.getOperationURL( "DescribeProcess", true );
        executeURLs[0] = capabilitiesDoc.getOperationURL( "Execute", false );
        executeURLs[1] = capabilitiesDoc.getOperationURL( "Execute", true );

        metadata = capabilitiesDoc.parseMetadata();

        for ( ProcessInfo processInfo : capabilitiesDoc.getProcesses() ) {
            Process process = new Process( this, processInfo );
            processIdToProcess.put( process.getId(), process );
        }
    }

    private WPS100CapabilitiesAdapter retrieveCapabilities( URL capabilitiesURL )
                            throws IOException {

        WPS100CapabilitiesAdapter capabilitiesDoc = null;
        try {
            LOG.trace( "Retrieving capabilities document from {}", capabilitiesURL );
            capabilitiesDoc = new WPS100CapabilitiesAdapter();
            capabilitiesDoc.load( capabilitiesURL );
        } catch ( Exception e ) {
            String msg = "Unable to retrieve/parse capabilities document from URL '" + capabilitiesURL + "': "
                         + e.getMessage();
            throw new IOException( msg );
        }

        OMElement root = capabilitiesDoc.getRootElement();
        String protocolVersion = root.getAttributeValue( new QName( "version" ) );
        if ( !"1.0.0".equals( protocolVersion ) ) {
            String msg = "Capabilities document has unsupported version " + protocolVersion + ".";
            throw new UnsupportedOperationException( msg );
        }
        String service = root.getAttributeValue( new QName( "service" ) );
        if ( !service.equalsIgnoreCase( "WPS" ) ) {
            String msg = "Capabilities document is not a WPS capabilities document.";
            throw new IllegalArgumentException( msg );
        }
        return capabilitiesDoc;
    }

    /**
     * Returns the WPS protocol version in use.
     * 
     * @return the WPS protocol version in use, never <code>null</code>
     */
    public String getServiceVersion() {
        // currently, this is always "1.0.0" (as the client only supports this version)
        return "1.0.0";
    }

    /**
     * Returns the metadata (ServiceIdentification, ServiceProvider) of the service.
     * 
     * @return the metadata of the service, never <code>null</code>
     */
    public DeegreeServicesMetadataType getMetadata() {
        return metadata;
    }

    /**
     * Returns all processes offered by the service.
     * 
     * @return all processes offered by the service, may be empty, but never <code>null</code>
     */
    public Process[] getProcesses() {
        return processIdToProcess.values().toArray( new Process[processIdToProcess.size()] );
    }

    /**
     * Returns the specified process instance.
     * 
     * @param id
     *            process identifier, never <code>null</code>
     * @param idCodeSpace
     *            codespace of the process identifier, may be <code>null</code> (for identifiers that don't use a code
     *            space)
     * @return process instance, can be <code>null</code> (if no process with the specified identifier and code space is
     *         offered by the services)
     */
    public Process getProcess( String id, String idCodeSpace ) {
        if ( !processIdToProcess.containsKey( new CodeType( id, idCodeSpace ) ) ) {
            throw new RuntimeException( "WPS has no registered process with id " + id );
        }
        return processIdToProcess.get( new CodeType( id, idCodeSpace ) );
    }

    /**
     * Returns the URL announced by the service for issuing <code>DescribeProcess</code> requests.
     * 
     * @param post
     *            if set to true, the URL for POST requests will be returned, otherwise the URL for GET requests will be
     *            returned
     * @return the <code>DescribeProcess</code> URL, may be <code>null</code> (if the server doesn't provide a binding
     *         for the specified request method)
     */
    URL getDescribeProcessURL( boolean post ) {
        return describeProcessURLs[post ? 1 : 0];
    }

    /**
     * Returns the URL announced by the service for issuing <code>Execute</code> requests.
     * 
     * @param post
     *            if set to true, the URL for POST requests will be returned, otherwise the URL for GET requests will be
     *            returned
     * @return the <code>Execute</code> URL, may be <code>null</code> (if the server doesn't provide a binding for the
     *         specified request method)
     */
    URL getExecuteURL( boolean post ) {
        return executeURLs[post ? 1 : 0];
    }
}
