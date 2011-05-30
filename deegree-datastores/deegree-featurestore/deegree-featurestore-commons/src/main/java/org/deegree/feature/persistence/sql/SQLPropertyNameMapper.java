//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.sql;

import org.deegree.feature.persistence.sql.xpath.MappedXPath;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.PropertyNameMapper;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.TableAliasManager;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.geometry.Geometry;

/**
 * {@link PropertyNameMapper} for {@link SQLFeatureStore} implementations.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SQLPropertyNameMapper implements PropertyNameMapper {

    private final MappedApplicationSchema schema;

    private final FeatureTypeMapping ftMapping;

    public SQLPropertyNameMapper( MappedApplicationSchema schema, FeatureTypeMapping ftMapping ) {
        this.schema = schema;
        this.ftMapping = ftMapping;
    }

    @Override
    public PropertyNameMapping getMapping( PropertyName propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException, UnmappableException {
        return new MappedXPath( schema, ftMapping, propName, aliasManager ).getPropertyNameMapping();
    }

    @Override
    public Object getSQLValue( Literal<?> literal, PropertyName propName )
                            throws FilterEvaluationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getSQLValue( Geometry literal, PropertyName propName )
                            throws FilterEvaluationException {
        throw new UnsupportedOperationException();
    }
}