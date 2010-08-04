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
package org.deegree.protocol.wps.describeprocess.input;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;

/**
 * The <code>InputDescription</code> class represents the input properties of a process parameter. It is filled out from
 * a DescribeProcess response.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public abstract class InputType {

    private CodeType id;

    private LanguageString inputTitle;

    private LanguageString inputAbstract;

    private String minOccurs;

    private String maxOccurs;

    protected InputType( CodeType id, LanguageString inputTitle, LanguageString inputAbstract, String minOccurs,
                                String maxOccurs ) {
        this.id = id;
        this.inputTitle = inputTitle;
        this.inputAbstract = inputAbstract;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }

    /**
     * 
     * @return the title for this input
     */
    public LanguageString getTitle() {
        return inputTitle;
    }

    /**
     * 
     * @return the abstract entry for this input
     */
    public LanguageString getAbstract() {
        return inputAbstract;
    }

    /**
     * Returns the least number of occurrences possible for this input parameter in the process. Can be "0" (no
     * occurrence possible), more, or null (meaning that it can appear no more than maxOccurs, if that is specified):
     * 
     * @return minimum occurrences of this parameter
     */
    public String getMinOccurs() {
        return minOccurs;
    }

    /**
     * Returns the maximum number of occurrences possible for this input parameter in the process. Can be "unbounded"
     * (can appear infinitely many times) or a smaller strictly positive integer value (given as string).
     * 
     * @return maximum number of occurrences of this parameter
     */
    public String getMaxOccurs() {
        return maxOccurs;
    }
}
