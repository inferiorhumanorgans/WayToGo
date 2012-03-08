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
package com.inferiorhumanorgans.WayToGo.Agency.BART.Station;

import android.content.ContentValues;
import com.inferiorhumanorgans.WayToGo.Exception.AbortXMLParsingException;
import com.inferiorhumanorgans.WayToGo.TheApp;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author alex
 */
public class DetailXMLHandler extends DefaultHandler {

    private static String LOG_NAME = DetailXMLHandler.class.getCanonicalName();
    private XMLInterface theListener;
    private static final String STATION_TAG = "station";
    private static final String TAG_TAG = "abbr";
    private static final String LAT_TAG = "gtfs_latitude";
    private static final String LNG_TAG = "gtfs_longitude";
    private static final String LAT_COLUMN = "latitude";
    private static final String LNG_COLUMN = "longitude";
    private ContentValues theValues = new ContentValues();
    private boolean inStation = false, inAbbr = false, inLat = false, inLng=false;
    private StringBuilder theInnerText = null;
    private String theTag;
    private int theLatitude, theLongitude;

    public DetailXMLHandler(XMLInterface aListener) {
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
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        super.startElement(uri, localName, qName, atts);

        if (theListener.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (localName.equals(STATION_TAG)) {
            theValues.clear();
            inStation = true;
        } else if (localName.equals(TAG_TAG)) {
            inAbbr = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(LAT_TAG)) {
            inLat = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(LNG_TAG)) {
            inLng = true;
            theInnerText = new StringBuilder();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);

        if (theListener.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (inAbbr || inLat || inLng) {
            theInnerText.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (theListener.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (localName.equals(STATION_TAG)) {
            theListener.addLocationToStation(theTag, theLatitude, theLongitude);
            theValues.clear();
            inStation = false;
        } else if (localName.equals(TAG_TAG)) {
            theTag = theInnerText.toString();
            theInnerText = null;
            inAbbr = false;
        } else if (localName.equals(LAT_TAG)) {
            theLatitude = TheApp.toE6(Double.parseDouble(theInnerText.toString()));
            theInnerText = null;
            inLat = false;
        } else if (localName.equals(LNG_TAG)) {
            theLongitude = TheApp.toE6(Double.parseDouble(theInnerText.toString()));
            theInnerText = null;
            inLng = false;
        }

    }
}
