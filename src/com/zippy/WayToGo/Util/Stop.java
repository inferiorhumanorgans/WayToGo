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
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.StringPairList.StringPair;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osmdroid.util.GeoPoint;

/**
 *
 * @author alex
 */
public final class Stop implements Parcelable {

    final private GeoPoint thePoint;
    final private String theName;
    final private String theShortName;
    final private String theId;
    final private Class theAgencyClass;
    final private String theAddress;

    public Stop(final GeoPoint aPoint, final String aName, final String anId, Class anAgencyClass) {
        this(aPoint, aName, anId, null, anAgencyClass);
    }

    public Stop(final GeoPoint aPoint, final String aName, final String anId, String anAddress, Class anAgencyClass) {
        thePoint = aPoint;
        theName = aName;
        theShortName = theName;
        theId = anId;
        theAgencyClass = anAgencyClass;
        theAddress = anAddress;
    }

    public final BaseAgency getTheAgency() {
        return TheApp.theAgencies.get(theAgencyClass.getCanonicalName());
    }

    public final String getGUID() {
        return theAgencyClass.getCanonicalName() + "/" + theId;
    }

    public final String getTheName() {
        return theName;
    }

    public String getTheShortName() {
        return theShortName;
    }

    public final String getTheAddress() {
        return theAddress;
    }

    public final GeoPoint getThePoint() {
        return thePoint;
    }

    public final String getTheId() {
        return theId;
    }

    /**
     * @deprecated Gross hack
     * @return
     */
    @Deprecated
    public final StringPair toStringPair() {
        return new StringPair(theName, theId);
    }

    /**
     * Launches Google Nav so we can get walking directions to a stop
     * @param aStop
     */
    public static void getWalkingDirectionsTo(final Context aContext, final Stop aStop) {
        final double theLat = aStop.getThePoint().getLatitudeE6() / 1E6;
        final double theLng = aStop.getThePoint().getLongitudeE6() / 1E6;
        String url = "google.navigation:mode=w&q=" + theLat + "," + theLng;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        aContext.startActivity(i);
    }

    // Parcelable stuff
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(thePoint.getLatitudeE6());
        out.writeInt(thePoint.getLongitudeE6());
        out.writeString(theAddress);
        out.writeString(theName);
        out.writeString(theShortName);
        out.writeString(theId);
        out.writeString(theAgencyClass.getClass().getCanonicalName());
    }
    public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>() {

        public Stop createFromParcel(Parcel in) {
            return new Stop(in);
        }

        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };

    private Stop(Parcel in) {
        final int lat = in.readInt();
        final int lng = in.readInt();
        thePoint = new GeoPoint(lat, lng);
        theAddress = in.readString();
        theName = in.readString();
        theShortName = in.readString();
        theId = in.readString();
        Class tmpClass;
        try {
            tmpClass = Class.forName(in.readString());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Stop.class.getName()).log(Level.SEVERE, null, ex);
            tmpClass = null;
        }
        theAgencyClass = tmpClass;
    }
}
