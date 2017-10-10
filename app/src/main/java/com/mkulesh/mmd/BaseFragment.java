/*
 * **************************************************************************
 * Molecular Dynamics - Particles under the microscope
 * **************************************************************************
 * Copyright (C) 2014-2017 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * **************************************************************************
 */

package com.mkulesh.mmd;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.View;

abstract public class BaseFragment extends Fragment
{
    /**
     * Class members.
     */
    public final static int INVALID_FRAGMENT_ID = -1;
    public final static int EXPERIMENT_FRAGMENT_ID = 0;
    public final static int POTENTIAL_FRAGMENT_ID = 1;

    protected MainActivity activity = null;
    protected View rootView = null;
    protected int fragmentNumber = INVALID_FRAGMENT_ID;
    protected SharedPreferences preferences = null;

    /**
     * Abstract interface
     */
    abstract public void performAction(int itemId);
    abstract public int getTitleId();
    abstract public int getSubTitleId();

    public int getFragmentNumber()
    {
        return fragmentNumber;
    }

    protected void initializeFragment(int number)
    {
        fragmentNumber = number;
        activity = (MainActivity) getActivity();
        setHasOptionsMenu(true);
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

}
