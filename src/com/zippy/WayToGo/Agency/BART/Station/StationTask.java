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
import com.zippy.WayToGo.Agency.BART.BaseXMLTask;
import com.zippy.WayToGo.Agency.BARTAgency;
import com.zippy.WayToGo.Exception.AbortXMLParsingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author alex
 */
public class StationTask extends BaseXMLTask implements XMLInterface {

    private static final String LOG_NAME = StationTask.class.getCanonicalName();
    private static final String BART_URL = "http://api.bart.gov/api/stn.aspx?cmd=stns&key=";
    private static final String BART_DETAIL_URL = "http://api.bart.gov/api/stn.aspx?cmd=stninfo&key=";
    private XMLHandler dataHandler = new XMLHandler(this);
    private HashMap<String, ContentValues> theStations = new HashMap<String, ContentValues>(44);

    @Override
    protected Void doInBackground(BARTAgency... someAgencies) {
        super.doInBackground(someAgencies);

        Log.i(LOG_NAME, "Trying to get BART station list.");

        InputStream content = null;
        ClientConnectionManager connman = new ThreadSafeClientConnManager(params, registry);
        DefaultHttpClient hc = new DefaultHttpClient(connman, params);

        Log.i(LOG_NAME, "Fetching basic station info from: " + BART_URL + BARTAgency.API_KEY);
        HttpGet getRequest = new HttpGet(BART_URL + BARTAgency.API_KEY);
        try {
            content = hc.execute(getRequest).getEntity().getContent();
        } catch (ClientProtocolException ex) {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        }
        Log.i(LOG_NAME, "Put the station list in the background.");

        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            XMLReader xr = sp.getXMLReader();

            xr.setContentHandler(dataHandler);

            xr.parse(new InputSource(content));

        } catch (ParserConfigurationException pce) {
            Log.e(LOG_NAME + " SAX XML", "sax parse error", pce);
        } catch (AbortXMLParsingException abrt) {
            Log.i(LOG_NAME + " AsyncXML", "Cancelled!!!!!");
            return null; // Pass on some exception to the caller?
        } catch (SAXException se) {
            Log.e(LOG_NAME + " SAX XML", "sax error", se);
        } catch (IOException ioe) {
            Log.e(LOG_NAME + " SAX XML", "sax parse io error", ioe);
        }
        Log.i(LOG_NAME + " SAX XML", "Done parsing BART station XML");

        final DetailXMLHandler detailDataHandler = new DetailXMLHandler(this);

        for (String aStationTag : theStations.keySet()) {
            getRequest = new HttpGet(BART_DETAIL_URL + BARTAgency.API_KEY + "&orig=" + aStationTag);
            try {
                content = hc.execute(getRequest).getEntity().getContent();
            } catch (ClientProtocolException ex) {
                Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
            }
            Log.i(LOG_NAME, "Put the station detail for " + aStationTag + " in the background.");

            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();

                XMLReader xr = sp.getXMLReader();

                xr.setContentHandler(detailDataHandler);

                xr.parse(new InputSource(content));

            } catch (ParserConfigurationException pce) {
                Log.e(LOG_NAME + " SAX XML", "sax parse error", pce);
            } catch (AbortXMLParsingException abrt) {
                Log.i(LOG_NAME + " AsyncXML", "Cancelled!!!!!");
            } catch (SAXException se) {
                Log.e(LOG_NAME + " SAX XML", "sax error", se);
            } catch (IOException ioe) {
                Log.e(LOG_NAME + " SAX XML", "sax parse io error", ioe);
            }
            Log.i(LOG_NAME + " SAX XML", "Done parsing BART station XML");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        theAgency.addStations(theStations.values());
        theAgency.finishedParsingStations();
    }

    public void addStation(ContentValues aStation) {
        Log.d(LOG_NAME, "Trying to initially add station of: (" + aStation.getAsString("tag") + ") " + aStation);
        theStations.put(aStation.getAsString("tag"), new ContentValues(aStation));
    }

    public void addLocationToStation(final String aStationTag, final int aLatitude, final int aLongitude) {
        Log.d(LOG_NAME, "Trying to update " + aStationTag);
        ContentValues theStation = theStations.get(aStationTag);
        theStation.put("lat", aLatitude);
        theStation.put("lng", aLongitude);
    }
}
