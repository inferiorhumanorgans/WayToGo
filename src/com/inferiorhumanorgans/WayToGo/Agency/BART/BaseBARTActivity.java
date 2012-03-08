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
package com.inferiorhumanorgans.WayToGo.Agency.BART;

import android.app.ProgressDialog;
import android.os.Bundle;
import com.inferiorhumanorgans.WayToGo.Agency.BARTAgency;
import com.inferiorhumanorgans.WayToGo.BaseActivity;
import com.inferiorhumanorgans.WayToGo.TheApp;

/**
 *
 * @author alex
 */
public class BaseBARTActivity extends BaseActivity {
    protected ProgressDialog theProgressDialog = null;

    @Override
    public void onCreate(final Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        theAgencyClassName = BARTAgency.class.getCanonicalName();
    }

    @Override
    protected BARTAgency theAgency() {
        return (BARTAgency) TheApp.theAgencies.get(theAgencyClassName);
    }
}
