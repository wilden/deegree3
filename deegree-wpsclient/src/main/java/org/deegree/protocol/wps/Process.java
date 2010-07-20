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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.protocol.wps.execute.ExecuteRequest;
import org.deegree.protocol.wps.execute.ExecuteRequestWriter;
import org.deegree.protocol.wps.execute.ExecuteResponse;
import org.deegree.protocol.wps.execute.ExecuteResponseReader;
import org.deegree.protocol.wps.execute.input.ExecuteInput;
import org.deegree.protocol.wps.execute.output.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProcessInfo object containing all information relevant to a single process.
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Process {

    private static Logger LOG = LoggerFactory.getLogger( Process.class );

    private String baseURL;

    private CodeType processId;

    private String title;

    private String processAbstract;

    private boolean describeProcessPerformed = false;

    public Process( String baseURL, CodeType processId, String title, String processAbstract ) {
        this.baseURL = baseURL;
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

    /**
     * TODO adjust return type and implement me!
     * 
     * @param inputParams
     * @return
     */
    public ExecuteResponse execute( List<ExecuteInput> inputList, ResponseFormat executeOutputFormatList ) {
        URL url;
        ExecuteResponse response = null;
        try {
            url = new URL( baseURL );
            URLConnection conn = url.openConnection();
            conn.setDoOutput( true );

            XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outFactory.createXMLStreamWriter( conn.getOutputStream() );

            ExecuteRequest executeRequest = new ExecuteRequest( processId, inputList, executeOutputFormatList );
            ExecuteRequestWriter executer = new ExecuteRequestWriter( writer );
            executer.write100( executeRequest );
            writer.flush();
            writer.close();

            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = inFactory.createXMLStreamReader( conn.getInputStream() );
            ExecuteResponseReader responseReader = new ExecuteResponseReader( reader );
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
