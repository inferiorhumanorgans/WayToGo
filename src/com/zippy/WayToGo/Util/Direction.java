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

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * @author alex
 */
public class Direction implements Parcelable {

    private final String theAgencyClassName;
    private final String theRouteTag;
    private final String theTag;
    private final String theTitle;
    private final String theShortTitle;

    /**
     * 
     * @param anAgencyClassName
     * @param aTag
     * @param aTitle
     */
    public Direction(final String anAgencyClassName, final String aRouteTag, final String aTag, final String aTitle) {
        this(anAgencyClassName, aRouteTag, aTag, aTitle, aTitle);
    }

    public Direction(final String anAgencyClassName, final String aRouteTag, final String aTag, final String aTitle, final String aShortTitle) {
        theAgencyClassName = anAgencyClassName;
        theRouteTag = aRouteTag;
        theTag = aTag;
        theTitle = aTitle;
        theShortTitle = aShortTitle;
    }

    public String agencyClassName() {
        return theAgencyClassName;
    }

    public String routeTag() {
        return theRouteTag;
    }

    public String tag() {
        return theTag;
    }

    public String title() {
        return theTitle;
    }

    public String shortTitle() {
        return theShortTitle;
    }

    @Override
    public final synchronized boolean equals(final Object anOtherDirection) {
        if (!(anOtherDirection instanceof Direction)) {
            return false;
        }
        final Direction ourOtherDirection = (Direction) anOtherDirection;
        return ((this.theAgencyClassName.equals(ourOtherDirection.agencyClassName()))
                && (this.theRouteTag.equals(ourOtherDirection.routeTag()))
                && (this.theTitle.equals(ourOtherDirection.title())));
    }

    @Override
    /**
     * Whoops, this won't work on Android since Dalvik ignores assertions
     * by default.
     */
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }


    // Parcelable stuff begins here

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(final Parcel out, final int flags) {
        out.writeString(theAgencyClassName);
        out.writeString(theRouteTag);
        out.writeString(theTag);
        out.writeString(theTitle);
        out.writeString(theShortTitle);
    }
    public static final Parcelable.Creator<Direction> CREATOR = new Parcelable.Creator<Direction>() {

        public Direction createFromParcel(final Parcel in) {
            return new Direction(in);
        }

        public Direction[] newArray(final int size) {
            return new Direction[size];
        }
    };

    private Direction(final Parcel in) {
        theAgencyClassName = in.readString();
        theRouteTag = in.readString();
        theTag = in.readString();
        theTitle = in.readString();
        theShortTitle = in.readString();

    }
}
