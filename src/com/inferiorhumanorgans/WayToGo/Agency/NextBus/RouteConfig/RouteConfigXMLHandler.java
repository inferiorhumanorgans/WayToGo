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
package com.inferiorhumanorgans.WayToGo.Agency.NextBus.RouteConfig;

import android.content.ContentValues;
import com.inferiorhumanorgans.WayToGo.Exception.AbortXMLParsingException;
import com.inferiorhumanorgans.WayToGo.TheApp;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * http://www.jondev.net/articles/Android_XML_SAX_Parser_Example
 * @author alex
 */
public class RouteConfigXMLHandler extends DefaultHandler {

    private boolean inRoute = false;
    private ContentValues theRoute = null;

    //private boolean inStop = false;
    private ContentValues theStop = null;

    private boolean inDirection = false;
    private ContentValues theDirection = null;

    private RouteConfigNotification theTV = null;


    private static final String ROUTE_TAG = "route";
    private static final String DIRECTION_TAG = "direction";
    private static final String STOP_TAG = "stop";

    private static final String TAG_COLUMN = "tag";
    private static final String TITLE_COLUMN = "title";

    private static final String TAG_ATTRIB = TAG_COLUMN;
    private static final String TITLE_ATTRIB = TITLE_COLUMN;
    private int theStopPosition = 0;

    public RouteConfigXMLHandler(RouteConfigNotification aTV) {
        super();
        theTV = aTV;
        theRoute = new ContentValues();
        theDirection = new ContentValues();
        theStop  = new ContentValues();
    }

    @Override
    public void startDocument() throws SAXException {
        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (localName.equals(ROUTE_TAG)) {
            inRoute = true;

            theRoute.clear();

            theRoute.put(TAG_COLUMN, atts.getValue(TAG_ATTRIB));
            theRoute.put(TITLE_COLUMN, atts.getValue(TITLE_ATTRIB));
            theRoute.put("lat_min", TheApp.toE6(Double.parseDouble(atts.getValue("latMin"))));
            theRoute.put("lat_max", TheApp.toE6(Double.parseDouble(atts.getValue("latMax"))));
            theRoute.put("lng_min", TheApp.toE6(Double.parseDouble(atts.getValue("lonMin"))));
            theRoute.put("lng_max", TheApp.toE6(Double.parseDouble(atts.getValue("lonMax"))));
            theRoute.put("color", atts.getValue("color"));
            theTV.addRoute(theRoute);
        } else if (localName.equals(STOP_TAG)) {
            //inStop = true;

            if (inDirection && inRoute) {
                final String theStopTag = atts.getValue(TAG_ATTRIB);
                final String theDirectionTag = theDirection.getAsString(TAG_ATTRIB);
                theTV.addStopToDirection(theDirectionTag, theStopTag, theStopPosition++);
            } else if (inRoute) {
                String theStopTag = atts.getValue(TAG_ATTRIB);
                theStop.clear();
                theStop.put(TAG_COLUMN, theStopTag);
                theStop.put(TITLE_COLUMN, atts.getValue(TITLE_ATTRIB));
                theStop.put("stop_id", Integer.parseInt(atts.getValue("stopId")));
                theStop.put("lat", TheApp.toE6(Double.parseDouble(atts.getValue("lat"))));
                theStop.put("lng", TheApp.toE6(Double.parseDouble(atts.getValue("lon"))));
                theTV.addStop(theStop);
                theTV.addStopToRoute(theRoute.getAsString(TAG_ATTRIB), theStopTag);
            }

        } else if (localName.equals(DIRECTION_TAG)) {
            inDirection = true;
            if (inRoute) {
                theDirection.clear();
                theStopPosition=0;
                theDirection.put(TAG_COLUMN, atts.getValue(TAG_ATTRIB));
                theDirection.put(TITLE_COLUMN, atts.getValue(TITLE_ATTRIB));
                theDirection.put("direction", atts.getValue("name"));
                theDirection.put("route_tag", theRoute.getAsString("tag"));
                boolean isVisible = Boolean.parseBoolean(atts.getValue("useForUI"));
                theDirection.put("visible", isVisible);
                theTV.addDirection(theDirection);
            }
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (theTV.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        if (localName.equals(ROUTE_TAG)) {
            inRoute = false;
            theTV.finishedWithRoute();
        } else if (localName.equals(STOP_TAG)) {
            //inStop = false;
        } else if (localName.equals(DIRECTION_TAG)) {
            inDirection = false;
        }
    }
}
