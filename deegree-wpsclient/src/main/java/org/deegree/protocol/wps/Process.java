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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.protocol.wps.describeprocess.DescribeProcessExecution;
import org.deegree.protocol.wps.describeprocess.InputDescription;
import org.deegree.protocol.wps.describeprocess.output.OutputDescription;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
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

    private Map<CodeType, InputDescription> allowedInputs;

    private Map<CodeType, OutputDescription> outputFormats;

    private boolean describeProcessPerformed = false;

    Process( WPSClient wpsclient, String version, CodeType processId, LanguageString title,
             LanguageString processAbstract ) {
        this.wpsclient = wpsclient;
        this.version = version;
        this.processId = processId;
        this.title = title;
        this.processAbstract = processAbstract;
    }

    /**
     * perform DescribeProcess
     */
    private void doDescribeProcess() {
        URL url = wpsclient.getDescribeProcessURL( false );
        String finalURLStr;
        try {
            finalURLStr = url.toExternalForm() + "?service=WPS&version=" + URLEncoder.encode( version, "UTF-8" )
                          + "&request=DescribeProcess&identifier="
                          + URLEncoder.encode( concatenateCodeType( processId ), "UTF-8" );
            URL finalURL = new URL( finalURLStr );
            DescribeProcessExecution dpExecution = new DescribeProcessExecution( finalURL );
            allowedInputs = dpExecution.parseInputs();
            outputFormats = dpExecution.parseOutputs();
        } catch ( UnsupportedEncodingException e ) {
            e.printStackTrace();
            throw new RuntimeException( "DescribeProcess request failed as the operation URL could not be encode. " );
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            throw new RuntimeException( "DescribeProcess request failed as the operation URL could not be encode. " );
        }

    }

    /**
     * Create a {@link ProcessExecution} instance that will manage the execution of the process.
     * 
     * @return a {@link ProcessExecution} instance
     */
    public ProcessExecution prepareExecution() {
        return new ProcessExecution( this );
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

    public InputDescription[] getInputTypes() {
        if ( allowedInputs == null ) {
            doDescribeProcess();
        }
        Collection<InputDescription> collection = allowedInputs.values();
        return collection.toArray( new InputDescription[collection.size()] );
    }

    public InputDescription getInputType( String paramId, String codeSpace ) {
        if ( allowedInputs == null ) {
            doDescribeProcess();
        }
        return allowedInputs.get( new CodeType( paramId, codeSpace ) );
    }

    public OutputDescription[] getOutputTypes() {
        if ( outputFormats == null ) {
            doDescribeProcess();
        }
        Collection<OutputDescription> collection = outputFormats.values();
        return collection.toArray( new OutputDescription[collection.size()] );
    }

    public OutputDescription getOutputType( String paramId, String codeSpace ) {
        if ( outputFormats == null ) {
            doDescribeProcess();
        }
        return outputFormats.get( new CodeType( paramId, codeSpace ) );
    }

    /**
     * @param id
     *            a {@link CodeType}
     * @return string representation of codetype: either codeSpace:code or code
     */
    // TODO this method feels that it doesn't belong here (but where?)
    public String concatenateCodeType( CodeType id ) {
        String codeSpace = id.getCodeSpace();
        if ( codeSpace == null || "".equals( codeSpace ) ) {
            return id.getCode();
        }
        return codeSpace + ":" + id.getCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "ProcessIdentifier: " + this.processId + "\n" );
        return sb.toString();
    }

    WPSClient getWPSClient() {
        return wpsclient;
    }
}
