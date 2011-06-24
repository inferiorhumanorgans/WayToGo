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
package com.zippy.WayToGo.Agency.NextBus.Prediction;

import android.net.Uri;
import android.util.Log;
import com.zippy.WayToGo.Exception.AbortXMLParsingException;
import com.zippy.WayToGo.Agency.NextBus.BaseXMLTask;
import com.zippy.WayToGo.Agency.NextBus.NextBusAgency;
import com.zippy.WayToGo.Agency.PredictionListener;
import com.zippy.WayToGo.Util.Prediction;
import com.zippy.WayToGo.Util.Stop;
import java.io.BufferedInputStream;
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
public class PredictionXMLTask extends BaseXMLTask implements PredictionInterface {

    private static final String LOG_NAME = PredictionXMLTask.class.getCanonicalName();
    private static final String NB_URL_BASE = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=";
    private String theStopId = null;
    private final PredictionListener theListener;

    public PredictionXMLTask(final Stop aStop, final PredictionListener aListener) {
        super();
        theListener = aListener;
        theStopId = aStop.getTheId();
    }

    public final String getStopId() {
        return theStopId;
    }

    @Override
    protected Void doInBackground(NextBusAgency... someAgencies) {
        super.doInBackground(someAgencies);
        theListener.startPredictionFetch();

        final String theNBName = theAgency.getNextBusName();
        String theURL = NB_URL_BASE + Uri.encode(theNBName) + "&stopId=" + Uri.encode(theStopId);

        Log.d(LOG_NAME, "Trying to get the predictions for " + theNBName + ".");
        Log.i(LOG_NAME, "Fetching predictions from: " + theURL);
        InputStream content;
        BufferedInputStream bufferedInput;

        ClientConnectionManager connman = new ThreadSafeClientConnManager(params, registry);
        DefaultHttpClient hc = new DefaultHttpClient(connman, params);

        HttpGet getRequest = new HttpGet(theURL);
        try {
            content = hc.execute(getRequest).getEntity().getContent();
            bufferedInput = new BufferedInputStream(content);
        } catch (ClientProtocolException ex) {
            content = null;
            bufferedInput = null;
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            content = null;
            bufferedInput = null;
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        }
        //Log.d(LOG_NAME, "Backgrounding the stop/route predictions for " + theNBName + "/" + theStopId);

        
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            XMLReader xr = sp.getXMLReader();

            PredictionXMLHandler dataHandler = new PredictionXMLHandler(this, theAgency);
            xr.setContentHandler(dataHandler);

            xr.parse(new InputSource(bufferedInput));

        } catch (ParserConfigurationException pce) {
            Log.e(LOG_NAME + "SAX XML", "sax parse error", pce);
            cancel(true);
        } catch (AbortXMLParsingException abrt) {
            Log.d(LOG_NAME + "AsyncXML", "Cancelled!!!!!");
            getRequest.abort();
            return null;
        } catch (SAXException se) {
            Log.e(LOG_NAME + "SAX XML", "sax error", se);
            cancel(true);
        } catch (IOException ioe) {
            Log.e(LOG_NAME + "SAX XML", "sax parse io error", ioe);
            cancel(true);
        }
        Log.d(LOG_NAME + " SAX XML", "Done parsing XML for " + theNBName);
        return null;
    }

    public synchronized void addPrediction(final Prediction aPrediction) {
            publishProgress(aPrediction);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        //Log.d(LOG_NAME, "Adding aprediction object for: " + thePrediction.getRouteTag() + "/" + thePrediction.getDirectionTag() + "/" + thePrediction.getMinutes());
        for (int i=0; i < values.length; i++) {
            theListener.addPrediction((Prediction) values[i]);
        }
        return;
    }

    @Override
    protected void onPostExecute(Void result) {
        theListener.finishedPullingPredictions(isCancelled());
    }
}
