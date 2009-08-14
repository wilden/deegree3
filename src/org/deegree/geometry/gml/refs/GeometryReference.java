//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.geometry.gml.refs;

import java.net.URL;

import org.deegree.commons.gml.GMLIdContext;
import org.deegree.commons.types.gml.StandardObjectProperties;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.gml.GML311GeometryDecoder;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a reference to the GML representation of a geometry, which is usually expressed using an
 * <code>xlink:href</code> attribute in GML (may be document-local or remote).
 * 
 * @param <T>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GeometryReference<T extends Geometry> implements Geometry {

    private static final Logger LOG = LoggerFactory.getLogger( GMLIdContext.class );

    protected String href;

    private String gid;

    private T referencedGeometry;

    /** true: local xlink (#...), false: external xlink */
    protected boolean isLocal;

    private String baseURL;

    public GeometryReference( String href, String baseURL ) {
        this.href = href;
        this.baseURL = baseURL;
        int pos = href.lastIndexOf( '#' );
        if ( pos < 0 ) {
            isLocal = false;
        } else {
            isLocal = true;
            gid = href.substring( pos + 1 );
        }
    }

    /**
     * Returns whether the reference is document-local (xlink: #...) or remote.
     * 
     * @return true, if the reference is document-local, false otherwise
     */
    public boolean isLocal() {
        return isLocal;
    }

    public void resolve( T geometry ) {
        if ( this.referencedGeometry != null ) {
            String msg = "Internal error: Geometry reference (" + href + ") has already been resolved.";
            throw new RuntimeException( msg );
        }
        this.referencedGeometry = geometry;
    }

    /**
     * Returns the referenced {@link Geometry} object.
     * 
     * @return the referenced geometry
     */
    @SuppressWarnings("unchecked")
    public T getReferencedGeometry() {
        if ( referencedGeometry == null ) {
            if ( isLocal ) {
                String msg = "Internal error: Reference to local geometry (" + href + ") has not been resolved.";
                throw new RuntimeException( msg );
            }
            LOG.info( "Trying to resolve reference to external geometry: '" + href + "', base system id: " + baseURL );
            GML311GeometryDecoder decoder = new GML311GeometryDecoder();
            try {
                URL resolvedURL = null;
                if ( baseURL != null ) {
                    resolvedURL = new URL( new URL( baseURL ), href );
                } else {
                    resolvedURL = new URL( href );
                }
                XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( resolvedURL );
                xmlReader.nextTag();
                referencedGeometry = (T) decoder.parse( xmlReader, null );
                LOG.debug( "Read GML geometry: '" + referencedGeometry.getClass() + "'" );
                xmlReader.close();
            } catch ( Exception e ) {
                throw new RuntimeException( "Unable to resolve external geometry reference: " + e.getMessage() );
            }
        }
        return referencedGeometry;
    }

    @Override
    public boolean contains( Geometry geometry ) {
        return getReferencedGeometry().contains( geometry );
    }

    @Override
    public boolean crosses( Geometry geometry ) {
        return getReferencedGeometry().crosses( geometry );
    }

    @Override
    public Geometry getDifference( Geometry geometry ) {
        return getReferencedGeometry().getDifference( geometry );
    }

    @Override
    public Measure getDistance( Geometry geometry, Unit requestedUnits ) {
        return getReferencedGeometry().getDistance( geometry, requestedUnits );
    }

    @Override
    public boolean equals( Geometry geometry ) {
        return getReferencedGeometry().equals( geometry );
    }

    @Override
    public Geometry getBuffer( Measure distance ) {
        return getReferencedGeometry().getBuffer( distance );
    }

    @Override
    public Geometry getConvexHull() {
        return getReferencedGeometry().getConvexHull();
    }

    @Override
    public int getCoordinateDimension() {
        return getReferencedGeometry().getCoordinateDimension();
    }

    @Override
    public CRS getCoordinateSystem() {
        return getReferencedGeometry().getCoordinateSystem();
    }

    @Override
    public Envelope getEnvelope() {
        return getReferencedGeometry().getEnvelope();
    }

    @Override
    public GeometryType getGeometryType() {
        return getReferencedGeometry().getGeometryType();
    }

    @Override
    public String getId() {
        if ( isLocal ) {
            return gid;
        }
        return getReferencedGeometry().getId();
    }

    @Override
    public PrecisionModel getPrecision() {
        return getReferencedGeometry().getPrecision();
    }

    @Override
    public Geometry getIntersection( Geometry geometry ) {
        return getReferencedGeometry().getIntersection( geometry );
    }

    @Override
    public boolean intersects( Geometry geometry ) {
        return getReferencedGeometry().intersects( geometry );
    }

    @Override
    public boolean isDisjoint( Geometry geometry ) {
        return getReferencedGeometry().isDisjoint( geometry );
    }

    @Override
    public boolean overlaps( Geometry geometry ) {
        return getReferencedGeometry().overlaps( geometry );
    }

    @Override
    public boolean touches( Geometry geometry ) {
        return getReferencedGeometry().touches( geometry );
    }

    @Override
    public boolean isBeyond( Geometry geometry, Measure distance ) {
        return getReferencedGeometry().isBeyond( geometry, distance );
    }

    @Override
    public boolean isWithin( Geometry geometry ) {
        return getReferencedGeometry().isWithin( geometry );
    }

    @Override
    public boolean isWithinDistance( Geometry geometry, Measure distance ) {
        return getReferencedGeometry().isWithinDistance( geometry, distance );
    }

    @Override
    public Geometry getUnion( Geometry geometry ) {
        return getReferencedGeometry().getUnion( geometry );
    }

    @Override
    public StandardObjectProperties getAttachedProperties() {
        return getReferencedGeometry().getAttachedProperties();
    }

    @Override
    public void setAttachedProperties( StandardObjectProperties standardProps ) {
        getReferencedGeometry().setAttachedProperties( standardProps );
    }

    @Override
    public Point getCentroid() {
        return getReferencedGeometry().getCentroid();
    }

    @Override
    public void setCoordinateSystem( CRS crs ) {
        getReferencedGeometry().setCoordinateSystem( crs );
    }

    @Override
    public void setId( String id ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPrecision( PrecisionModel pm ) {
        getReferencedGeometry().setPrecision( pm );
    }
}
