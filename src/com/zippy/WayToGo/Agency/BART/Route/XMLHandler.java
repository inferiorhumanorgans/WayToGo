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
package com.zippy.WayToGo.Agency.BART.Route;

import android.content.ContentValues;
import com.zippy.WayToGo.Exception.AbortXMLParsingException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author alex
 */
public class XMLHandler extends DefaultHandler {
    private static String LOG_NAME = XMLHandler.class.getSimpleName();
    private XMLInterface theTV;

    private static final String ROUTE_TAG = "route";
    private boolean inRoute = false;

    private static final String NAME_COLUMN = "name";
    private static final String NAME_TAG = "name";
    private boolean inName = false;

    private static final String TAG_COLUMN = "tag";
    private static final String TAG_TAG = "abbr";
    private boolean inAbbr = false;

    private static final String ROUTEID_COLUMN = "id";
    private static final String ROUTEID_TAG = "routeID";
    private boolean inRouteId = false;

    private static final String NUMBER_COLUMN = "number";
    private static final String NUMBER_TAG = "number";
    private boolean inNumber = false;

    private static final String COLOR_COLUMN = "color";
    private static final String COLOR_TAG = "color";
    private boolean inColor = false;

    private ContentValues theValues = new ContentValues();
    private StringBuilder theInnerText = null;


    protected XMLHandler(XMLInterface aTV) {
        super();
        theTV = aTV;
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        super.startElement(uri, localName, qName, atts);

        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (localName.equals(ROUTE_TAG)) {
            theValues.clear();
            inRoute = true;
        } else if (localName.equals(NAME_TAG)) {
            inName = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(TAG_TAG)) {
            inAbbr = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(ROUTEID_TAG)) {
            inRouteId = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(NUMBER_TAG)) {
            inNumber = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(COLOR_TAG)) {
            inColor = true;
            theInnerText = new StringBuilder();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (inName || inAbbr || inRouteId || inNumber || inColor) {
            theInnerText.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (localName.equals(ROUTE_TAG)) {
            theTV.addRoute(theValues);
            theValues.clear();
            inRoute = false;
        } else if (localName.equals(NAME_TAG)) {
            theValues.put(NAME_COLUMN, theInnerText.toString());
            theInnerText = null;
            inName = false;
        } else if (localName.equals(TAG_TAG)) {
            theValues.put(TAG_COLUMN, theInnerText.toString());
            theInnerText = null;
            inAbbr = false;
        } else if (localName.equals(ROUTEID_TAG)) {
            theValues.put(ROUTEID_COLUMN, theInnerText.toString());
            theInnerText = null;
            inRouteId = false;
        } else if (localName.equals(NUMBER_TAG)) {
            theValues.put(NUMBER_COLUMN, Integer.parseInt(theInnerText.toString()));
            theInnerText = null;
            inNumber = false;
        } else if (localName.equals(COLOR_TAG)) {
            theValues.put(COLOR_COLUMN, theInnerText.toString());
            theInnerText = null;
            inColor = false;
        }
    }
}
