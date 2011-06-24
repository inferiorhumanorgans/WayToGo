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

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import com.zippy.WayToGo.Util.StringPairList.StringPair;

/**
 *
 * @author alex
 */
public final class Route implements Parcelable {

    final private String theAgencyName;
    final private String theName;
    final private String theTag;
    final private String theRawTag;
    final private int theColor;
    final private boolean isEmpty;

    public Route() {
        theAgencyName = null;
        theName = null;
        theTag = null;
        theRawTag = null;
        theColor = 0;

        isEmpty = true;
    }

    public Route(final String anAgencyName, final String aName, final String aTag, final String aRawTag) {
        this(anAgencyName, aName, aTag, aRawTag, Color.LTGRAY);
    }

    public Route(final String anAgencyName, final String aName, final String aTag, final String aRawTag, final int aColor) {
        theAgencyName = anAgencyName;
        theName = aName;
        theTag = aTag;
        theRawTag = aRawTag;
        theColor = aColor;
        isEmpty = false;
    }

    public final String getTheAgencyName() {
        return theAgencyName;
    }

    public final String getTheName() {
        return theName;
    }

    public final String getTheTag() {
        return theTag;
    }

    public final String getTheRawTag() {
        return theRawTag;
    }

    public final int getTheColor() {
        return theColor;
    }

    public final boolean isEmpty() {
        return isEmpty;
    }

    /**
     * @deprecated Gross hack
     * @return
     */
    @Deprecated
    public final StringPair getStringPair() {
        return new StringPair(theName, theTag);
    }

    // Parcelable stuff
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(isEmpty == true ? 1 : 0);
        if (!isEmpty) {
            out.writeString(theName);
            out.writeString(theTag);
            out.writeString(theRawTag);
            out.writeString(theAgencyName);
        }

    }
    public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {

        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    private Route(Parcel in) {
        switch (in.readInt()) {
            case 0:
                // not empty
                theName = in.readString();
                theTag = in.readString();
                theRawTag = in.readString();
                theColor = Color.LTGRAY;
                theAgencyName = in.readString();
                isEmpty = false;
                break;
            case 1:
            default:
                // iS empty
                theAgencyName = null;
                theName = null;
                theTag = null;
                theRawTag = null;
                theColor = 0;
                isEmpty = true;
                break;
        }
    }
}
