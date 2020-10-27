/*
 * Molecular Dynamics - Particles under the microscope
 * Copyright (C) 2014-2020 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */

package com.mkulesh.mmd.fragments;

import android.content.SharedPreferences;
import android.view.View;

import com.mkulesh.mmd.MainActivity;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public abstract class BaseFragment extends Fragment
{
    /**
     * Class members.
     */
    public final static int INVALID_FRAGMENT_ID = -1;
    final static int EXPERIMENT_FRAGMENT_ID = 0;
    final static int POTENTIAL_FRAGMENT_ID = 1;

    MainActivity activity = null;
    View rootView = null;
    private int fragmentNumber = INVALID_FRAGMENT_ID;
    SharedPreferences preferences = null;

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

    void initializeFragment(int number)
    {
        fragmentNumber = number;
        activity = (MainActivity) getActivity();
        setHasOptionsMenu(true);
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

}
