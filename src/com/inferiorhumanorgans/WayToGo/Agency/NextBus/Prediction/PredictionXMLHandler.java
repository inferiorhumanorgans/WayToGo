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
package com.inferiorhumanorgans.WayToGo.Agency.NextBus.Prediction;

import android.util.Log;
import com.inferiorhumanorgans.WayToGo.Agency.NextBus.NextBusAgency;
import com.inferiorhumanorgans.WayToGo.Exception.AbortXMLParsingException;
import com.inferiorhumanorgans.WayToGo.Util.Prediction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author alex
 */
public class PredictionXMLHandler extends DefaultHandler {

    private static String LOG_NAME = PredictionXMLHandler.class.getCanonicalName();
    private final PredictionInterface theListener;
    private String theRouteTag;
    private final NextBusAgency theAgency;

    public PredictionXMLHandler(final PredictionInterface theaTV, final NextBusAgency anAgency) {
        super();
        theListener = theaTV;
        theAgency = anAgency;
    }

    @Override
    public void endDocument() throws SAXException {
        if (theListener.isCancelled()) {
            throw new AbortXMLParsingException();
        }

        super.endDocument();
    }

    @Override
    public void endElement(final String aUri, String aLocalName, String aQName) throws SAXException {
        if (theListener.isCancelled()) {
            throw new AbortXMLParsingException();
        }
        super.endElement(aUri, aLocalName, aQName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (theListener.isCancelled()) {
            throw new AbortXMLParsingException();
        }
        super.startElement(uri, localName, qName, atts);
        //Log.i(LOG_NAME, "Start ELEMENT: " + localName);
        if (localName.equals("predictions")) {
            theRouteTag = atts.getValue("routeTag");
            //theStopTag = atts.getValue("stopTag");
        } else if (localName.equals("prediction")) {
            //BaseAgency anAgency, final String aRouteTag, final String aStopTag, final String aDirectionTag, final int aMinutes
            final Prediction f = new Prediction(theAgency, theRouteTag, theListener.getStopId(), atts.getValue("dirTag"), Integer.parseInt(atts.getValue("minutes")));
            theListener.addPrediction(f);
        }
    }
}
