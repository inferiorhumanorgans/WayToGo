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
package com.inferiorhumanorgans.WayToGo.Agency.NextBus.RouteList;

import android.net.Uri;
import android.util.Log;
import com.inferiorhumanorgans.WayToGo.Exception.AbortXMLParsingException;
import com.inferiorhumanorgans.WayToGo.Agency.NextBus.BaseXMLTask;
import com.inferiorhumanorgans.WayToGo.Agency.NextBus.NextBusAgency;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.Assert;
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
public class RouteListXMLTask extends BaseXMLTask implements RouteListNotification {

    private static final String LOG_NAME = RouteListXMLTask.class.getCanonicalName();
    private RouteListXMLHandler theDataHandler = new RouteListXMLHandler(this);

    @Override
    protected Void doInBackground(final NextBusAgency... someAgencies) {
        Assert.assertEquals(1, someAgencies.length);
        super.doInBackground(someAgencies);
        String theNBName = theAgency.getNextBusName();

        Log.i(LOG_NAME, "Trying to get the route list for " + theNBName + ".");

        InputStream content = null;
        ClientConnectionManager connman = new ThreadSafeClientConnManager(params, registry);
        DefaultHttpClient hc = new DefaultHttpClient(connman, params);

        String theNBURL = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=";
        theNBURL += Uri.encode(theNBName);
        Log.i(LOG_NAME, "Fetching from: " + theNBURL);
        HttpGet getRequest = new HttpGet(theNBURL);
        try {
            content = hc.execute(getRequest).getEntity().getContent();
        } catch (ClientProtocolException ex) {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
            this.cancel(true);
            return null;
        }


        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            XMLReader xr = sp.getXMLReader();

            xr.setContentHandler(theDataHandler);

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
        //Log.i(LOG_NAME + " SAX XML", "Done parsing XML for " + theNBName);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (!isCancelled()) {
            theAgency.setNumberOfExpectedRoutes(theDataHandler.numberOfRoutes);
        }
        theAgency.finishedParsingRouteList();
        theAgency.fetchRouteConfig();
    }
}
