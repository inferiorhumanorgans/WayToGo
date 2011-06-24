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
package com.zippy.WayToGo.Agency.NextBus.RouteConfig;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;
import com.zippy.WayToGo.Exception.AbortXMLParsingException;
import com.zippy.WayToGo.Agency.NextBus.BaseXMLTask;
import com.zippy.WayToGo.Agency.NextBus.NextBusAgency;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.xml.sax.*;

/**
 *
 * @author alex
 */
public class RouteConfigXMLTask extends BaseXMLTask implements RouteConfigNotification {

    private int numberOfCompletedRoutes = 0;
    private static String LOG_NAME = "RouteConfigXMLTask";
    private static final String NB_URL_BASE = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&verbose=true&a=";

    @Override
    protected Void doInBackground(NextBusAgency... someAgencies) {
        super.doInBackground(someAgencies);

        String theNBName = theAgency.getNextBusName();

        Log.i(LOG_NAME, "Trying to get the route config for " + theNBName + ".");
        Log.i(LOG_NAME, "Fetching from: " + NB_URL_BASE + theNBName);
        InputStream content = null;
        ClientConnectionManager connman = new ThreadSafeClientConnManager(params, registry);
        DefaultHttpClient hc = new DefaultHttpClient(connman, params);

        HttpGet getRequest = new HttpGet(NB_URL_BASE + Uri.encode(theNBName));
        try {
            content = hc.execute(getRequest).getEntity().getContent();
        } catch (ClientProtocolException ex) {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        }
        Log.i(LOG_NAME, "Done with the route config for " + theNBName + ".");

        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            XMLReader xr = sp.getXMLReader();

            RouteConfigXMLHandler dataHandler = new RouteConfigXMLHandler(this);
            xr.setContentHandler(dataHandler);

            xr.parse(new InputSource(content));

        } catch (ParserConfigurationException pce) {
            Log.e(LOG_NAME + "SAX XML", "sax parse error", pce);
        } catch (AbortXMLParsingException abrt) {
            Log.i(LOG_NAME + "AsyncXML", "Cancelled!!!!!");
        } catch (SAXException se) {
            Log.e(LOG_NAME + "SAX XML", "sax error", se);
        } catch (IOException ioe) {
            Log.e(LOG_NAME + "SAX XML", "sax parse io error", ioe);
        }
        Log.i(LOG_NAME + "SAX XML", "Done parsing XML for " + theNBName);
        return null;
    }

    public void finishedWithRoute() {
        theAgency.finishedRoute();
        publishProgress(++numberOfCompletedRoutes);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        theAgency.bumpProgress();
        return;
    }

    @Override
    protected void onPostExecute(Void result) {
        theAgency.finishedParsingRouteConfig();
    }

    public void addRoute(ContentValues aRoute) {
        theAgency.addRoute(aRoute);
    }

    public void addStopToRoute(String aRouteTag, String aStopTag) {
        theAgency.addStopToRoute(aRouteTag, aStopTag);
    }

    public void addDirection(ContentValues aDirection) {
        theAgency.addDirection(aDirection);
    }

    public void addStopToDirection(String aDirectionTag, String aStopTag, int aPosition) {
        theAgency.addStopToDirection(aDirectionTag, aStopTag, aPosition);
    }

    public void addStop(ContentValues aStop) {
        theAgency.addStop(aStop);
    }

    public void addPath(ContentValues aPath) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
