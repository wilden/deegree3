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
import java.net.URLEncoder;
import java.util.Collection;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wps.describeprocess.ProcessDetails;
import org.deegree.protocol.wps.describeprocess.input.InputDescription;
import org.deegree.protocol.wps.describeprocess.output.OutputDescription;
import org.deegree.services.controller.ows.OWSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the properties of a process offered by a WPS instance (id, title, input format, output format, etc.) and
 * provides access to a {@link ProcessExecution} context for executing it.
 * 
 * @see WPSClient
 * @see ProcessExecution
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Process {

    private static final Logger LOG = LoggerFactory.getLogger( Process.class );

    private final WPSClient wpsclient;

    private final String version;

    private final CodeType processId;

    private final LanguageString title;

    private final LanguageString processAbstract;

    private ProcessDetails processDetails;

    /**
     * Creates a new {@link Process} instance.
     * 
     * @param wpsclient
     *            associated client instance, must not be <code>null</code>
     * @param version
     *            process version, must not be <code>null</code>
     * @param processId
     *            process id, must not be <code>null</code>
     * @param title
     *            process title, must not be <code>null</code>
     * @param processAbstract
     *            process abstract, can be <code>null</code>
     */
    Process( WPSClient wpsclient, String version, CodeType processId, LanguageString title,
             LanguageString processAbstract ) {
        this.wpsclient = wpsclient;
        this.version = version;
        this.processId = processId;
        this.title = title;
        this.processAbstract = processAbstract;
    }

    /**
     * Returns the identifier of the process.
     * 
     * @return the identifier, never <code>null</code>
     */
    public CodeType getId() {
        return processId;
    }

    /**
     * Returns the version of the process.
     * 
     * @return the version, never <code>null</code>
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the title of the process.
     * 
     * @return the title, never <code>null</code>
     */
    public LanguageString getTitle() {
        return title;
    }

    /**
     * Returns the abstract of the process.
     * 
     * @return the abstract, can be <code>null</code> (if the process description does not define an abstract)
     */
    public LanguageString getAbstract() {
        return processAbstract;
    }

    /**
     * Returns the descriptions for all input parameters of the process.
     * 
     * @return the descriptions for all input parameters, can be empty, but never <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public InputDescription[] getInputTypes()
                            throws IOException, OWSException {
        Collection<InputDescription> collection = getProcessDetails().getInputs().values();
        return collection.toArray( new InputDescription[collection.size()] );
    }

    /**
     * Returns the description for the input parameter with the specified identifier.
     * 
     * @param id
     *            parameter identifier, never <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers that don't use a code
     *            space)
     * @return input parameter description, can be <code>null</code> (if no such parameter exists)
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public InputDescription getInputType( String id, String idCodeSpace )
                            throws IOException, OWSException {
        return getProcessDetails().getInputs().get( new CodeType( id, idCodeSpace ) );
    }

    /**
     * Returns the descriptions for all output parameters of the process.
     * 
     * @return the descriptions for all output parameters, can be empty, but never <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public OutputDescription[] getOutputTypes()
                            throws IOException, OWSException {
        Collection<OutputDescription> collection = getProcessDetails().getOutputs().values();
        return collection.toArray( new OutputDescription[collection.size()] );
    }

    /**
     * Returns the description for the output parameter with the specified identifier.
     * 
     * @param id
     *            parameter identifier, never <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers that don't use a code
     *            space)
     * @return output parameter description, can be <code>null</code> (if no such parameter exists)
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public OutputDescription getOutputType( String id, String idCodeSpace )
                            throws OWSException, IOException {
        return getProcessDetails().getOutputs().get( new CodeType( id, idCodeSpace ) );
    }

    /**
     * Returns whether the process supports storing of the response document (=asynchronous execution).
     * 
     * @return true, if the process supports storing the response document, false otherwise
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public boolean getStoreSupported()
                            throws OWSException, IOException {
        return getProcessDetails().getStoreSupported();
    }

    /**
     * Returns whether the process supports polling of status information during asynchronous execution.
     * 
     * @return true, if the process supports polling status information, false otherwise
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public boolean getStatusSupported()
                            throws OWSException, IOException {
        return getProcessDetails().getStatusSupported();
    }

    /**
     * Prepares a new {@link ProcessExecution} context that allows to execute the process.
     * 
     * @return new process execution context, never <code>null</code>
     */
    public ProcessExecution prepareExecution() {
        return new ProcessExecution( wpsclient, this );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "ProcessIdentifier: " + this.processId + "\n" );
        return sb.toString();
    }

    /**
     * Returns the (cached) {@link ProcessDetails} for this process.
     * 
     * @return process details, never <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    private ProcessDetails getProcessDetails()
                            throws IOException, OWSException {

        if ( processDetails == null ) {
            URL url = wpsclient.getDescribeProcessURL( false );

            if ( processId.getCodeSpace() != null ) {
                LOG.warn( "Performing DescribeProcess using GET, but process identifier ('" + processId
                          + "') has a code space (which cannot be expressed). "
                          + "Omitting the code space and hoping for the best..." );
            }

            String finalURLStr = url.toExternalForm() + "?service=WPS&version=" + URLEncoder.encode( version, "UTF-8" )
                                 + "&request=DescribeProcess&identifier="
                                 + URLEncoder.encode( processId.getCode(), "UTF-8" );
            URL finalURL = new URL( finalURLStr );
            XMLAdapter describeProcessResponse = new XMLAdapter( finalURL );
            processDetails = new ProcessDetails( describeProcessResponse );
        }
        return processDetails;
    }
}
