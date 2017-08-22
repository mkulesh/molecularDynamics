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

import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.mkulesh.mmd.model.Constants.PotentialType;
import com.mkulesh.mmd.utils.ViewUtils;
import com.mkulesh.mmd.widgets.DialogParameters;

public class MainFragmentExperiment extends BaseFragment
{

    private static final int SETTINGS_ACTIVITY_REQUEST = 1;

    /**
     * State attributes to be stored in Parcel
     */
    private boolean runPressed = false;
    private Integer currentRotation = null;

    /**
     * Experiment layout
     */
    private Experiment exp = null;
    private SurfaceView surface = null;
    private SurfaceTouchListener touchListener = null;

    OrientationEventListener orientationEventListener = null;

    public MainFragmentExperiment()
    {
        // Empty constructor required for fragment subclasses
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_experiment, container, false);
        initializeFragment(EXPERIMENT_FRAGMENT_ID);

        // get rotation properties
        currentRotation = activity.getDisplay().getRotation();

        // orientation change
        orientationEventListener = new OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL)
        {
            @Override
            public void onOrientationChanged(int orientation)
            {
                synchronized (currentRotation)
                {
                    int rotation = activity.getDisplay().getRotation();
                    if (exp != null && Math.abs(rotation - currentRotation) == 2)
                    {
                        Integer previousRotation = currentRotation;
                        currentRotation = rotation;
                        exp.processRotationChange(previousRotation, currentRotation);
                    }
                }
            }
        };

        // surface preparation
        surface = (SurfaceView) rootView.findViewById(R.id.experiment_view);
        //surface.setZOrderOnTop(true);
        surface.getHolder().setFormat(PixelFormat.TRANSPARENT);
        touchListener = new SurfaceTouchListener(activity, surface);
        surface.setOnTouchListener(touchListener);

        if (savedInstanceState != null)
        {
            onRestoreInstanceState(savedInstanceState);
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_play).setVisible(true);
        menu.findItem(R.id.action_pause).setVisible(true);
        menu.findItem(R.id.action_settings).setVisible(true);
        menu.findItem(R.id.action_help).setVisible(true);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ViewUtils.Debug(this, "onResume");

        // activate experiment
        if (exp == null)
        {
            exp = new Experiment(activity);
        }

        // restore potential since it can be changed in other activities
        exp.setControlValue(
                DialogParameters.Type.POTENTIAL_CHANGE,
                0.0,
                PotentialType.valueOf(preferences.getString(SettingsActivity.KEY_POTENTIAL,
                                getResources().getString(R.string.pref_potential_default))).value());

        // restore pause state
        exp.resumePause(true);
        if (!runPressed)
        {
            exp.pause();
        }

        // setup experiment GUI
        touchListener.setExperiment(exp);
        Experiment.Options opt = new Experiment.Options();
        opt.enablePause = true;
        opt.enableInfoPanel = true;
        opt.drawBackground = true;
        exp.initialize(activity, surface.getHolder(), opt);
        exp.resume();

        // enable orientation sensor
        if (orientationEventListener.canDetectOrientation())
        {
            orientationEventListener.enable();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        ViewUtils.Debug(this, "onPause");
        orientationEventListener.disable();
        touchListener.disable();
        exp.stop();
        exp.writeParameters();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        ViewUtils.Debug(this, "onSaveInstanceState");
        try
        {
            exp.writeToBundle(outState);
            outState.putBoolean("activity_main_button_run", runPressed);
        outState.putInt("activity_main_rotation", currentRotation);
        }
        catch (Exception e)
        {
            ViewUtils.Debug(this, "cannot save state: " + e.getLocalizedMessage());
        }
    }

    public void onRestoreInstanceState(Bundle outState)
    {
        ViewUtils.Debug(this, "onRestoreInstanceState");
        try
        {
            exp = outState.getParcelable(Experiment.PARCELABLE_ID);
            runPressed = outState.getBoolean("activity_main_button_run", false);
            Integer previousRotation = outState.getInt("activity_main_rotation", currentRotation);
            exp.processRotationChange(previousRotation, currentRotation);
        }
        catch (Exception e)
        {
            ViewUtils.Debug(this, "cannot restore state: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ViewUtils.Debug(this, "onDestroy");
        exp = null;
    }

    @Override
    public void performAction(int itemId)
    {
        // Handle item selection
        switch (itemId)
        {
        case R.id.action_help:
            touchListener.showHelpDialog();
            break;
        case R.id.action_play:
            if (!runPressed)
            {
                exp.resumePause(false);
                runPressed = true;
            }
            break;
        case R.id.action_pause:
            if (runPressed)
            {
                exp.pause();
                runPressed = false;
            }
            break;
        case R.id.action_settings:
            Intent i = new Intent(activity, SettingsActivity.class);
            startActivityForResult(i, SETTINGS_ACTIVITY_REQUEST);
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SETTINGS_ACTIVITY_REQUEST)
        {
            exp.readParameters();
        }
    }

}
