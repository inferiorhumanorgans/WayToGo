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
import android.net.Uri;
import android.util.Log;
import com.inferiorhumanorgans.WayToGo.Exception.AbortXMLParsingException;
import com.inferiorhumanorgans.WayToGo.Agency.NextBus.BaseXMLTask;
import com.inferiorhumanorgans.WayToGo.Agency.NextBus.NextBusAgency;
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
    private static final String LOG_NAME = RouteConfigXMLTask.class.getCanonicalName();
    private static final String NB_URL_BASE = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&verbose=true&a=";

    @Override
    protected Void doInBackground(final NextBusAgency... someAgencies) {
        super.doInBackground(someAgencies);

        final String ourNBName = theAgency.getNextBusName();

        Log.i(LOG_NAME, "Trying to get the route config for " + ourNBName + ".");
        Log.i(LOG_NAME, "Fetching from: " + NB_URL_BASE + ourNBName);
        InputStream ourInputStream = null;
        ClientConnectionManager ourConnectionManager = new ThreadSafeClientConnManager(params, registry);
        DefaultHttpClient ourHttpClient = new DefaultHttpClient(ourConnectionManager, params);

        final HttpGet getRequest = new HttpGet(NB_URL_BASE + Uri.encode(ourNBName));
        try {
            ourInputStream = ourHttpClient.execute(getRequest).getEntity().getContent();
        } catch (ClientProtocolException ex) {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        }
        Log.i(LOG_NAME, "Done with the route config for " + ourNBName + ".");

        try {
            SAXParserFactory ourParserFactory = SAXParserFactory.newInstance();
            SAXParser ourParser = ourParserFactory.newSAXParser();

            XMLReader xr = ourParser.getXMLReader();

            RouteConfigXMLHandler ourDataHandler = new RouteConfigXMLHandler(this);
            xr.setContentHandler(ourDataHandler);

            xr.parse(new InputSource(ourInputStream));

        } catch (ParserConfigurationException pce) {
            Log.e(LOG_NAME + "SAX XML", "sax parse error", pce);
        } catch (AbortXMLParsingException abrt) {
            Log.i(LOG_NAME + "AsyncXML", "Cancelled!!!!!");
        } catch (SAXException se) {
            Log.e(LOG_NAME + "SAX XML", "sax error", se);
        } catch (IOException ioe) {
            Log.e(LOG_NAME + "SAX XML", "sax parse io error", ioe);
        }
        Log.i(LOG_NAME + "SAX XML", "Done parsing XML for " + ourNBName);
        return null;
    }

    public void finishedWithRoute() {
        theAgency.finishedRoute();
        publishProgress(++numberOfCompletedRoutes);
    }

    @Override
    protected void onProgressUpdate(final Object... someValues) {
        theAgency.bumpProgress();
        return;
    }

    @Override
    protected void onPostExecute(final Void aResult) {
        theAgency.finishedParsingRouteConfig();
    }

    public void addRoute(final ContentValues aRoute) {
        theAgency.addRoute(aRoute);
    }

    public void addStopToRoute(final String aRouteTag, final String aStopTag) {
        theAgency.addStopToRoute(aRouteTag, aStopTag);
    }

    public void addDirection(final ContentValues aDirection) {
        theAgency.addDirection(aDirection);
    }

    public void addStopToDirection(final String aDirectionTag, final String aStopTag, final int aPosition) {
        theAgency.addStopToDirection(aDirectionTag, aStopTag, aPosition);
    }

    public void addStop(final ContentValues aStop) {
        theAgency.addStop(aStop);
    }

    public void addPath(final ContentValues aPath) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
