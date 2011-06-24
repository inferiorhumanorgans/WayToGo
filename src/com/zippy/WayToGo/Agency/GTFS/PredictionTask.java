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
package com.zippy.WayToGo.Agency.GTFS;

import android.os.AsyncTask;
import com.zippy.WayToGo.Agency.PredictionListener;
import com.zippy.WayToGo.Util.Prediction;
import com.zippy.WayToGo.Util.Stop;

/**
 * @note If we get canceled, don't close up the DB helper, assume someone did
 * that for us already.
 * @author alex
 */
public class PredictionTask extends AsyncTask<Stop,Prediction,Void> {

    private final GTFSAgency theAgency;
    private final PredictionListener theListener;
    private GTFSDataHelper theDBHelper;

    public PredictionTask(final GTFSAgency anAgency, final PredictionListener aListener) {
        super();
        theAgency = anAgency;
        theListener = aListener;
    }

    @Override
    protected Void doInBackground(Stop... args) {
        theListener.startPredictionFetch();
        theDBHelper = new GTFSDataHelper(null, theAgency);
        for (int i=0; i < args.length; i++) {
            for (final Prediction aPrediction : theDBHelper.getPredictionsForStop(args[i])) {
                if (isCancelled()) {
                    return null;
                }
                this.publishProgress(aPrediction);
            }
        }
        theDBHelper.cleanUp();
        return null;
    }



    @Override
    protected void onPostExecute(Void result) {
        theListener.finishedPullingPredictions(isCancelled());
    }

    @Override
    protected void onProgressUpdate(Prediction... values) {
        for (int i=0; i < values.length; i++) {
            if (isCancelled()) {
                return;
            }
            theListener.addPrediction(values[i]);
        }
    }

}
