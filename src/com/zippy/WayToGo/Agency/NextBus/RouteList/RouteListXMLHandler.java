/*
 *  Copyright (C) 2011 Inferior Human Organs Software
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.zippy.WayToGo.Agency.NextBus.RouteList;

import com.zippy.WayToGo.Exception.AbortXMLParsingException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author alex
 */
public class RouteListXMLHandler extends DefaultHandler {

    private RouteListNotification theTV;
    public int numberOfRoutes = 0;
    private static final String ROUTE_TAG = "route";

    public RouteListXMLHandler(RouteListNotification aTV) {
        super();
        theTV = aTV;
    }

    @Override
    public void startDocument() throws SAXException {
        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }
        numberOfRoutes = 0;
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (localName.equals(ROUTE_TAG)) {
            numberOfRoutes++;
        }
    }
}
