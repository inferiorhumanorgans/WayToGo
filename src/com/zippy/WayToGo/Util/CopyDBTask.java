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
package com.zippy.WayToGo.Util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.zippy.WayToGo.TheApp;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alex
 */
public class CopyDBTask extends AsyncTask<String, Integer, Boolean> {

    final private Context theContext;
    final private CopyDBListener theListener;
    private static final String LOG_NAME = CopyDBTask.class.getCanonicalName();

    public CopyDBTask(Context aContext, CopyDBListener aListener) {
        super();
        theListener = aListener;
        theContext = aContext;
    }

    @Override
    protected Boolean doInBackground(String... theArgs) {
        Boolean ret;
        Log.d(LOG_NAME, "doInBackground called with: " + theArgs[0]);
        try {
            //Open your local db as the input stream
            InputStream myInput = TheApp.getContext().getAssets().open("databases" + File.separator + theArgs[0]);

            // Path to the just created empty db
            String outFileName = TheApp.getQualifiedDatabasePathName(theArgs[0]);

            // Make sure that we actually have a database directory in the first
            // place, as Android won't do this when we request the sanctioned
            // DB path
            final File theOutputFile = new File(outFileName).getParentFile();
            theOutputFile.mkdirs();

            Log.d(LOG_NAME, "Trying to copy to: " + outFileName);

            //Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);

            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
//                this.publishProgress(new Integer(length));
            }
            Log.d(LOG_NAME, "DONE!");

            //Close the streams
            myOutput.flush();
            Log.d(LOG_NAME, "DONE FLUSHING");
            myOutput.close();
            myInput.close();
            ret = Boolean.valueOf(true);
        } catch (IOException theEx) {
            Log.e(LOG_NAME, "Task Failed");
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, theEx);
            ret = Boolean.valueOf(false);
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (theListener != null) {
            theListener.copyingFinished();
        }
    }
}
