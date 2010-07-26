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

import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wps.execute.ExecuteRequest;
import org.deegree.protocol.wps.execute.ExecuteResponse;
import org.deegree.protocol.wps.execute.RequestWriter;
import org.deegree.protocol.wps.execute.ResponseReader;
import org.deegree.protocol.wps.execute.input.ExecuteInput;
import org.deegree.protocol.wps.execute.output.ResponseFormat;
import org.deegree.services.controller.ows.OWSException;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProcessInfo object containing all information relevant to a single process.
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Process {

    private static Logger LOG = LoggerFactory.getLogger( Process.class );

    private WPSClient wpsclient;

    private String version;

    private CodeType processId;

    private LanguageString title;

    private LanguageString processAbstract;

    private boolean describeProcessPerformed = false;

    public Process( WPSClient wpsclient, String version, CodeType processId, LanguageString title,
                    LanguageString processAbstract ) {
        this.wpsclient = wpsclient;
        this.version = version;
        this.processId = processId;
        this.title = title;
        this.processAbstract = processAbstract;
    }

    // /**
    // * internally call DescribeProcess when the user needs description parameters beyond GetCapabilities
    // */
    // private void doDescribeProcess() {
    // URL url = new URL( baseURL );
    // URLConnection conn = url.openConnection();
    // conn.setDoOutput( true );
    //
    // }

    public ProcessExecution prepareExecution() {
        return new ProcessExecution( this );
    }

    /**
     * 
     * @param inputList
     * @param responseFormats
     * @return
     * @throws OWSException
     */
    public ExecuteResponse execute( List<ExecuteInput> inputList, ResponseFormat responseFormats )
                            throws OWSException {
        ExecuteResponse response = null;
        try {
            // TODO what if server only supports Get?
            URL url = wpsclient.getExecuteURL( true );

            URLConnection conn = url.openConnection();
            conn.setDoOutput( true );
            conn.setUseCaches( false );
            conn.setRequestProperty( "Content-Type", "application/xml" );

            XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outFactory.createXMLStreamWriter( conn.getOutputStream() );

            // XMLStreamWriter writer = outFactory.createXMLStreamWriter( new FileOutputStream(
            // File.createTempFile(
            // "wpsClientIn",
            // ".xml" ) ) );

            ExecuteRequest executeRequest = new ExecuteRequest( processId, inputList, responseFormats );
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

        } catch ( MalformedURLException e ) {
            LOG.error( "Invalid URL found when connecting to WPS for Execute. " + e.getMessage() );
        } catch ( XMLStreamException e ) {
            LOG.error( "Error during Execute operation. " + e.getMessage() );
        } catch ( IOException e ) {
            LOG.error( "Error during Execute operation. " + e.getMessage() );
        }

        return response;
    }

    private void addNsToXPath( AXIOMXPath xpath )
                            throws JaxenException {
        xpath.addNamespace( "wps", WPS_100_NS );
        xpath.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
        xpath.addNamespace( "xlink", "http://www.w3.org/1999/xlink" );
    }

    public CodeType getId() {
        return processId;
    }

    /**
     * Returns whether the process supports storing the response document (= asynchronous operation).
     * 
     * @return true, if the process supports storing the response document, false otherwise
     */
    public boolean getStoreSupported() {
        return false;
    }

    /**
     * Returns whether the process supports polling status information during asynchronous operation.
     * 
     * @return true, if the process supports polling status information, false otherwise
     */
    public boolean getStatusSupported() {
        return false;
    }

    public String getVersion() {
        return null;
    }

    public LanguageString getTitle() {
        return null;
    }

    public LanguageString getAbstract() {
        return null;
    }

    public Object[] getInputParameters() {
        return null;
    }

    public Object getInputParameter( CodeType paramId ) {
        return null;
    }

    public Object[] getOutputParameters() {
        return null;
    }

    public Object getOutputParameters( CodeType paramId ) {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "ProcessIdentifier: " + this.processId + "\n" );
        return sb.toString();
    }
}
