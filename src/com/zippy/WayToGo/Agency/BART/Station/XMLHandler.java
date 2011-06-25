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
    private XMLInterface theListener;
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

    public XMLHandler(final XMLInterface aListener) {
        super();
        theListener = aListener;
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        if (theListener.isCancelled()) {
            throw new AbortXMLParsingException();
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        if (theListener.isCancelled()) {
            throw new AbortXMLParsingException();
        }
    }

    @Override
    public void startElement(final String aUri, final String aLocalName, final String aQName, final Attributes someAtts) throws SAXException {
        super.startElement(aUri, aLocalName, aQName, someAtts);

        if (theListener.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (aLocalName.equals(STATION_TAG)) {
            theValues.clear();
            inStation = true;
        } else if (aLocalName.equals(NAME_TAG)) {
            inName = true;
            theInnerText = new StringBuilder();
        } else if (aLocalName.equals(TAG_TAG)) {
            inAbbr = true;
            theInnerText = new StringBuilder();
        } else if (aLocalName.equals(ADDRESS_TAG)) {
            inAddress = true;
            theInnerText = new StringBuilder();
        } else if (aLocalName.equals(CITY_TAG)) {
            inCity = true;
            theInnerText = new StringBuilder();
        }
    }

    @Override
    public void characters(final char[] aCharacter, final int aStartPosition, final int aLength) throws SAXException {
        super.characters(aCharacter, aStartPosition, aLength);

        if (theListener.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (inName || inAbbr || inCity || inAddress) {
            //Log.d(LOG_NAME, "theInnerText: " + theInnerText + " inName: " + inName + " inAbbr: " + inAbbr + " inCity: " + inCity + " inAddress: " + inAddress);
            theInnerText.append(aCharacter, aStartPosition, aLength);
        }
    }

    @Override
    public void endElement(final String aUri, final String aLocalName, final String aQName) throws SAXException {
        super.endElement(aUri, aLocalName, aQName);

        if (theListener.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (aLocalName.equals(STATION_TAG)) {
            theValues.put("address", theAddress + ", " + theCity);
            theListener.addStation(theValues);
            theValues.clear();
            theCity = null;
            theAddress = null;
            inStation = false;
        } else if (aLocalName.equals(NAME_TAG)) {
            theValues.put(NAME_COLUMN, theInnerText.toString());
            theInnerText = null;
            inName = false;
        } else if (aLocalName.equals(TAG_TAG)) {
            theValues.put(TAG_COLUMN, theInnerText.toString());
            theInnerText = null;
            inAbbr = false;
        } else if (aLocalName.equals(ADDRESS_TAG)) {
            theAddress = theInnerText.toString();
            theInnerText = null;
            inAddress = false;
        } else if (aLocalName.equals(CITY_TAG)) {
            theCity = theInnerText.toString();
            theInnerText = null;
            inCity = false;
        }

    }
}
