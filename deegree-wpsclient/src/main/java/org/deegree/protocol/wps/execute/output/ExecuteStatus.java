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
package org.deegree.protocol.wps.execute.output;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExecuteStatus {

    private String statusMsg;

    private Integer percent;

    private String creationTime;

    private String exceptionReport;

    public ExecuteStatus( String statusMsg, Integer percent, String creationTime, String exceptionReport ) {
        this.statusMsg = statusMsg;
        this.percent = percent;
        this.creationTime = creationTime;
        this.exceptionReport = exceptionReport;
    }

    /**
     * Returns the status message beloging to the respective process.
     * 
     * @return message
     */
    public String getStatusMessage() {
        return statusMsg;
    }

    /**
     * Returns what fraction of the process is finished, or null if this does not apply (the execution was synchronous).
     * 
     * @return the percent of the finished process
     */
    public Integer getPercentCompleted() {
        return percent;
    }

    /**
     * @return creation time of the process execution
     */
    public String getCreationTime() {
        return creationTime;
    }

    /**
     * 
     * @return an exception message in case the execution went wrong, null otherwise
     */
    public String getExceptionReport() {
        return exceptionReport;
    }

}
