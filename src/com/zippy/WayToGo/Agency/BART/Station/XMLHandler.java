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
package com.zippy.WayToGo.Agency.BART.Station;

import android.content.ContentValues;
import android.util.Log;
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
    private static final String STATION_TAG = "station";
    private static final String NAME_TAG = "name";
    private static final String TAG_TAG = "abbr";
    private static final String ADDRESS_TAG = "address";
    private static final String CITY_TAG = "city";
    private static final String NAME_COLUMN = "name";
    private static final String TAG_COLUMN = "tag";
    private static final String ADDRESS_COLUMN = "address";
    private ContentValues theValues = new ContentValues();
    private boolean inStation = false, inName = false, inAbbr = false;
    private boolean inCity = false, inAddress = false;
    private StringBuilder theInnerText = null;
    private String theAddress = null, theCity = null;

    public XMLHandler(XMLInterface aTV) {
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

        if (localName.equals(STATION_TAG)) {
            theValues.clear();
            inStation = true;
        } else if (localName.equals(NAME_TAG)) {
            inName = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(TAG_TAG)) {
            inAbbr = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(ADDRESS_TAG)) {
            inAddress = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(CITY_TAG)) {
            inCity = true;
            theInnerText = new StringBuilder();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);

        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (inName || inAbbr || inCity || inAddress) {
            //Log.d(LOG_NAME, "theInnerText: " + theInnerText + " inName: " + inName + " inAbbr: " + inAbbr + " inCity: " + inCity + " inAddress: " + inAddress);
            theInnerText.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (localName.equals(STATION_TAG)) {
            theValues.put("address", theAddress + ", " + theCity);
            theTV.addStation(theValues);
            theValues.clear();
            theCity = null;
            theAddress = null;
            inStation = false;
        } else if (localName.equals(NAME_TAG)) {
            theValues.put(NAME_COLUMN, theInnerText.toString());
            theInnerText = null;
            inName = false;
        } else if (localName.equals(TAG_TAG)) {
            theValues.put(TAG_COLUMN, theInnerText.toString());
            theInnerText = null;
            inAbbr = false;
        } else if (localName.equals(ADDRESS_TAG)) {
            theAddress = theInnerText.toString();
            theInnerText = null;
            inAddress = false;
        } else if (localName.equals(CITY_TAG)) {
            theCity = theInnerText.toString();
            theInnerText = null;
            inCity = false;
        }

    }
}
