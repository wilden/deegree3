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
package org.deegree.protocol.wps.execute.output;

import java.util.LinkedHashMap;
import java.util.Map;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.wps.ProcessExecution;
import org.deegree.protocol.wps.execute.datatypes.BinaryDataType;
import org.deegree.protocol.wps.execute.datatypes.BoundingBoxDataType;
import org.deegree.protocol.wps.execute.datatypes.LiteralDataType;
import org.deegree.protocol.wps.execute.datatypes.XMLDataType;

/**
 * Provides access to the outputs from a {@link ProcessExecution}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExecuteOutputs {

    private final Map<CodeType, ExecuteOutput> paramIdToOutput = new LinkedHashMap<CodeType, ExecuteOutput>();

    /**
     * @param outputs
     */
    public ExecuteOutputs( ExecuteOutput[] outputs ) {
        for ( ExecuteOutput output : outputs ) {
            paramIdToOutput.put( output.getId(), output );
        }
    }

    /**
     * @return
     */
    public ExecuteOutput[] getAll() {
        return paramIdToOutput.values().toArray( new ExecuteOutput[paramIdToOutput.size()] );
    }

    /**
     * @return
     */
    public ExecuteOutput get( int i ) {
        return getAll()[i];
    }

    /**
     * @param id
     * @param idCodeSpace
     * @return
     */
    public ExecuteOutput get( String id, String idCodeSpace ) {
        return paramIdToOutput.get( new CodeType( id, idCodeSpace ) );
    }

    public LiteralDataType getLiteral( String id, String idCodeSpace ) {
        return null;
    }

    public BoundingBoxDataType getBoundingBox( String id, String idCodeSpace ) {
        return null;
    }

    public XMLDataType getXML( String id, String idCodeSpace ) {
        return null;
    }

    public BinaryDataType getBinary( String id, String idCodeSpace ) {
        return null;
    }
}
