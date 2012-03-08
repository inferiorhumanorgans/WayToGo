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
package com.inferiorhumanorgans.WayToGo.Agency.BART.Prediction;

import android.content.ContentValues;
import android.util.Log;
import com.inferiorhumanorgans.WayToGo.Exception.AbortXMLParsingException;
import com.inferiorhumanorgans.WayToGo.TheApp;
import com.inferiorhumanorgans.WayToGo.Util.Prediction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author alex
 */
public class XMLHandler extends DefaultHandler {

    private static String LOG_NAME = XMLHandler.class.getCanonicalName();
    private XMLInterface theTV;
    private String theDestination = null;
    private ContentValues theEstimateValues = new ContentValues();
    private boolean inEtd = false, inDestination = false, inAbbr = false;
    private boolean inEstimate = false, inMinutes = false, inPlatform = false;
    private boolean inDirection = false, inLength = false;
    private boolean inColor = false, inHexColor = false, inBikeFlag = false, inStation=false;
    private static final String TAG_ETD = "etd";
    private static final String TAG_DESTINATION = "destination";
    private static final String TAG_ABBR = "abbr";
    private static final String TAG_ESTIMATE = "estimate";
    private static final String TAG_MINUTES = "minutes";
    private static final String TAG_PLATFORM = "platform";
    private static final String TAG_DIRECTION = "direction";
    private static final String TAG_LENGTH = "length";
    private static final String TAG_COLOR = "color";
    private static final String TAG_HEXCOLOR = "hexcolor";
    private static final String TAG_BIKEFLAG = "bikeflag";
    private static final String TAG_STATION = "station";
    private StringBuilder theInnerText = null;
    private String theStationName = null;

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

        if (localName.equals(TAG_ETD)) {
            theDestination = null;
            inEtd = true;
        } else if (localName.equals(TAG_STATION)) {
            inStation = true;
        } else if (localName.equals(TAG_DESTINATION)) {
            inDestination = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(TAG_ABBR)) {
            inAbbr = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(TAG_ESTIMATE)) {
            theEstimateValues.clear();
            inEstimate = true;
        } else if (localName.equals(TAG_MINUTES)) {
            inMinutes = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(TAG_PLATFORM)) {
            inPlatform = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(TAG_DIRECTION)) {
            inDirection = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(TAG_LENGTH)) {
            inLength = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(TAG_COLOR)) {
            inColor = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(TAG_HEXCOLOR)) {
            inHexColor = true;
            theInnerText = new StringBuilder();
        } else if (localName.equals(TAG_BIKEFLAG)) {
            inBikeFlag = true;
            theInnerText = new StringBuilder();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);

        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (inDestination || inAbbr || inMinutes || inPlatform || inLength || inColor || inHexColor || inBikeFlag) {
            theInnerText.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (localName.equals(TAG_ETD)) {
            theDestination = null;
            inEtd = false;
        } else if (localName.equals(TAG_STATION)) {
            inStation = false;
        } else if (localName.equals(TAG_DESTINATION)) {
            theDestination = theInnerText.toString();
            //Log.i(LOG_NAME, "END DESTINATION: " + theDestination);
            inDestination = false;
            theInnerText = null;
        } else if (localName.equals(TAG_ABBR)) {
            if (inStation && !inEtd) {
                theStationName = theInnerText.toString();
            }
            inAbbr = false;
            theInnerText = null;
        } else if (localName.equals(TAG_ESTIMATE)) {
            theEstimateValues.put(TAG_DESTINATION, theDestination);

            /**
             * BaseAgency anAgency
             * String aRouteTag
             * String aStopTag
             * String aSubStopTag
             * String aDirectionTag
             * int aMinutes
             */
            final Prediction ourPred = new Prediction(
                    TheApp.theAgencies.get("com.inferiorhumanorgans.WayToGo.Agency.BARTAgency"),
                    theEstimateValues.getAsString(TAG_HEXCOLOR),
                    theStationName,
                    "Platform " + theEstimateValues.getAsInteger(TAG_PLATFORM),
                    theEstimateValues.getAsString(TAG_DESTINATION),
                    theEstimateValues.getAsInteger(TAG_MINUTES));
            ourPred.setFlags(String.valueOf(theEstimateValues.getAsInteger(TAG_LENGTH)));
            //Log.d(LOG_NAME, "Creating BART prediction from: " + theEstimateValues);
            theTV.addPrediction(ourPred);
            theEstimateValues.clear();
            inEstimate = false;
        } else if (localName.equals(TAG_MINUTES)) {
            String theText = theInnerText.toString();
            if (theText.equals("Arrived")) {
                theEstimateValues.put(TAG_MINUTES, 0);
            } else {
                theEstimateValues.put(TAG_MINUTES, Integer.parseInt(theText));
            }
            inMinutes = false;
            theInnerText = null;
        } else if (localName.equals(TAG_PLATFORM)) {
            theEstimateValues.put(TAG_PLATFORM, Integer.parseInt(theInnerText.toString()));
            inPlatform = false;
            theInnerText = null;
        } else if (localName.equals(TAG_DIRECTION)) {
            // Ignore
            inDirection = false;
            theInnerText = null;
        } else if (localName.equals(TAG_LENGTH)) {
            theEstimateValues.put(TAG_LENGTH, Integer.parseInt(theInnerText.toString()));
            inLength = false;
            theInnerText = null;
        } else if (localName.equals(TAG_COLOR)) {
            theEstimateValues.put(TAG_COLOR, theInnerText.toString());
            inColor = false;
            theInnerText = null;
        } else if (localName.equals(TAG_HEXCOLOR)) {
            theEstimateValues.put(TAG_HEXCOLOR, theInnerText.toString());
            inHexColor = false;
            theInnerText = null;
        } else if (localName.equals(TAG_BIKEFLAG)) {
            theEstimateValues.put(TAG_BIKEFLAG, Integer.parseInt(theInnerText.toString()));
            inBikeFlag = false;
            theInnerText = null;
        }
    }
}
