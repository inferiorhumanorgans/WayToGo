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

/**
 *
 * @author alex
 */
public class Direction {

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

    public String getTheAgencyClassName() {
        return theAgencyClassName;
    }

    public String getTheRouteTag() {
        return theRouteTag;
    }

    public String getTheTag() {
        return theTag;
    }

    public String getTheTitle() {
        return theTitle;
    }

    public String getTheShortTitle() {
        return theShortTitle;
    }

    public final synchronized boolean equals(final Direction anOtherDirection) {
        return ((this.theAgencyClassName.equals(anOtherDirection.getTheAgencyClassName()))
                && (this.theRouteTag.equals(anOtherDirection.getTheRouteTag()))
                && (this.theTitle.equals(anOtherDirection.getTheTitle())));
    }

    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }
}
