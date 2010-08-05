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
package org.deegree.protocol.wps.execute;

import java.util.LinkedHashMap;
import java.util.Map;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.wps.ProcessExecution;
import org.deegree.protocol.wps.execute.output.ComplexOutput;
import org.deegree.protocol.wps.execute.output.ExecutionOutput;
import org.deegree.protocol.wps.execute.output.LiteralOutput;
import org.deegree.services.wps.output.BoundingBoxOutput;

/**
 * Provides access to the outputs from a {@link ProcessExecution}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExecutionOutputs {

    private final Map<CodeType, ExecutionOutput> paramIdToOutput = new LinkedHashMap<CodeType, ExecutionOutput>();

    /**
     * @param outputs
     */
    public ExecutionOutputs( ExecutionOutput[] outputs ) {
        for ( ExecutionOutput output : outputs ) {
            System.out.println (output.getId());
            paramIdToOutput.put( output.getId(), output );
        }
    }

    /**
     * @return
     */
    public ExecutionOutput[] getAll() {
        return paramIdToOutput.values().toArray( new ExecutionOutput[paramIdToOutput.size()] );
    }

    /**
     * @return
     */
    public ExecutionOutput get( int i ) {
        return getAll()[i];
    }

    /**
     * @param id
     * @param idCodeSpace
     * @return
     */
    public ExecutionOutput get( String id, String idCodeSpace ) {
        return paramIdToOutput.get( new CodeType( id, idCodeSpace ) );
    }

    public LiteralOutput getLiteral( String id, String idCodeSpace ) {
        return (LiteralOutput) paramIdToOutput.get( new CodeType( id, idCodeSpace ) );
    }

    public BoundingBoxOutput getBoundingBox( String id, String idCodeSpace ) {
        return (BoundingBoxOutput) paramIdToOutput.get( new CodeType( id, idCodeSpace ) );
    }

    public ComplexOutput getComplex( String id, String idCodeSpace ) {
        return (ComplexOutput) paramIdToOutput.get( new CodeType( id, idCodeSpace ) );
    }
}
