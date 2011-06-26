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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;

/**
 *
 * @author alex
 */
public final class PredictionSummary {

    protected final Context theContext;
    protected final int[] theMinutes;
    protected String theFlags;
    protected final boolean isEmpty;
    private SpannableString theSpannable;
    protected final String theDirectionTitle;

    public PredictionSummary(final Context aContext) {
        this(aContext, null);
    }

    public PredictionSummary(final Context aContext, final PredictionGroup aPredictionGroup) {
        theContext = aContext;
        if (aPredictionGroup == null) {
            theMinutes = null;
            theDirectionTitle = null;
            isEmpty = true;
            theFlags = "";
        } else {
            final BaseAgency ourAgency = aPredictionGroup.stop().agency();
            final Route theRoute = ourAgency.getRouteFromTag(aPredictionGroup.routeTag());
            //final String theStopTag = aPredictionGroup.stopTag();
            final Direction theDirection = ourAgency.getDirectionFromTag(aPredictionGroup.directionTag());
            final String theDirectionName = theDirection.shortTitle();

            theMinutes = aPredictionGroup.minutes();
            
            if (theRoute.isEmpty()) {
                theDirectionTitle = "To " + theDirectionName;
            } else {
                theDirectionTitle = theRoute.tag() + " to " + theDirectionName;
            }

            isEmpty = false;
            theFlags = "";
        }
    }

    public String flags() {
        return theFlags;
    }

    public void setFlags(final String someFlags) {
        theSpannable = null;
        if (someFlags == null) {
            theFlags = "";
        } else {
            this.theFlags = someFlags;
        }
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    private String minutesString() {
        StringBuilder sb = new StringBuilder();
        for (final int minutes : theMinutes) {
            switch (minutes) {
                case 0:
                    sb.append("Arriving");
                    break;
                default:
                    if (minutes > 999) {
                        if ((minutes/60) == 1) {
                            sb.append("1 hour");
                        } else {
                            sb.append((minutes/60));
                            sb.append(" hours");
                        }
                    } else {
                        sb.append(minutes);
                    }
                    break;
            }
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        if (sb.toString().equals("1")) {
            sb.append(" minute");
        } else if (!sb.toString().equals("Arriving") && !sb.toString().contains("hour")) {
            sb.append(" minutes");
        }
        return sb.toString();
    }

    public final SpannableString toStyledText() {
        if (theSpannable != null) {
            return theSpannable;
        }
        final TextAppearanceSpan theBigStyle = new TextAppearanceSpan(theContext, android.R.style.TextAppearance_Large);
        //final TextAppearanceSpan theMediumStyle = new TextAppearanceSpan(theContext, android.R.style.TextAppearance_Medium);
        final TextAppearanceSpan theSmallStyle = new TextAppearanceSpan(theContext, android.R.style.TextAppearance_Small);

        if (isEmpty) {
            theSpannable = new SpannableString(TheApp.getResString(R.string.text_no_predictions));
            theSpannable.setSpan(theBigStyle, 0, theSpannable.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            return theSpannable;
        }
        if (theFlags.equals("legit")) {
            theSpannable = new SpannableString(minutesString());
            theSpannable.setSpan(theBigStyle, 0, theSpannable.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            return theSpannable;
        } else {
            final StringBuilder sb = new StringBuilder();
            sb.append(minutesString());
            int startOfSmall = sb.length();
            sb.append("\n");
            sb.append(theDirectionTitle);

            theSpannable = new SpannableString(sb.toString());
            theSpannable.setSpan(theSmallStyle, startOfSmall, theSpannable.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            return theSpannable;
        }
    }
}
