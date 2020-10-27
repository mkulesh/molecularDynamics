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

import androidx.annotation.NonNull;
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
import com.mkulesh.mmd.widgets.FloatingButtonsSet;

import java.util.concurrent.atomic.AtomicInteger;

public class MainFragmentExperiment extends BaseFragment implements View.OnClickListener
{

    private static final int SETTINGS_ACTIVITY_REQUEST = 1;

    /**
     * State attributes to be stored in Parcel
     */
    private boolean runPressed = false;
    private final AtomicInteger currentRotation = new AtomicInteger();

    /**
     * Experiment layout
     */
    private SurfaceView surface = null;
    private SurfaceTouchListener touchListener = null;
    private OrientationEventListener orientationEventListener = null;
    private FloatingButtonsSet primaryButtonsSet = null;

    public MainFragmentExperiment()
    {
        // Empty constructor required for fragment subclasses
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewUtils.Debug(this, "onCreateView, savedInstanceState = " + savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_experiment, container, false);
        initializeFragment(EXPERIMENT_FRAGMENT_ID);

        // get rotation properties
        currentRotation.set(activity.getDisplay().getRotation());

        // orientation change
        orientationEventListener = new OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL)
        {
            @Override
            public void onOrientationChanged(int orientation)
            {
                synchronized (currentRotation)
                {
                    int rotation = activity.getDisplay().getRotation();
                    final Experiment exp = activity.getExperiment();
                    if (exp != null && Math.abs(rotation - currentRotation.get()) == 2)
                    {
                        int previousRotation = currentRotation.get();
                        currentRotation.set(rotation);
                        exp.processRotationChange(previousRotation, currentRotation.get());
                    }
                }
            }
        };

        primaryButtonsSet = rootView.findViewById(R.id.main_flb_set_primary);

        // surface preparation
        surface = rootView.findViewById(R.id.experiment_view);
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_settings).setTitle(activity.getResources().getString(R.string.action_settings));
        ViewUtils.setMenuIconColor(activity, menu, R.id.action_settings);
        menu.findItem(R.id.action_help).setVisible(true);
        ViewUtils.setMenuIconColor(activity, menu, R.id.action_help);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ViewUtils.Debug(this, "onResume");
        activity.updateFragmentInfo(this);

        final Experiment exp = activity.getExperiment();

        // restore potential since it can be changed in other activities
        exp.setControlValue(
                DialogParameters.Type.POTENTIAL_CHANGE,
                0.0,
                PotentialType.valueOf(preferences.getString(SettingsActivity.KEY_POTENTIAL,
                        getResources().getString(R.string.pref_potential_default))).value());

        // restore pause state
        if (runPressed)
        {
            setInOperation(true, /*resumeMode=*/ true);
        }
        else
        {
            setInOperation(false, /*resumeMode=*/ false);
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
        activity.getExperiment().stop();
        activity.getExperiment().writeParameters();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        ViewUtils.Debug(this, "onSaveInstanceState");
        try
        {
            activity.getExperiment().writeToBundle(outState);
            outState.putBoolean("activity_main_button_run", runPressed);
            outState.putInt("activity_main_rotation", currentRotation.get());
        }
        catch (Exception e)
        {
            ViewUtils.Debug(this, "cannot save state: " + e.getLocalizedMessage());
        }
    }

    private void onRestoreInstanceState(Bundle inState)
    {
        ViewUtils.Debug(this, "onRestoreInstanceState");
        try
        {
            Experiment exp = inState.getParcelable(Experiment.PARCELABLE_ID);
            activity.setExperiment(exp);
            runPressed = inState.getBoolean("activity_main_button_run", false);
            int previousRotation = inState.getInt("activity_main_rotation", currentRotation.get());
            //noinspection ConstantConditions
            exp.processRotationChange(previousRotation, currentRotation.get());
        }
        catch (Exception e)
        {
            ViewUtils.Debug(this, "cannot restore state: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void performAction(int itemId)
    {
        // Handle item selection
        switch (itemId)
        {
        case R.id.action_settings:
            Intent i = new Intent(activity, SettingsActivity.class);
            startActivityForResult(i, SETTINGS_ACTIVITY_REQUEST);
            break;
        case R.id.action_help:
            touchListener.showHelpDialog();
            break;
        }
    }

    @Override
    public int getTitleId()
    {
        return R.string.drawer_experiment;
    }

    @Override
    public int getSubTitleId()
    {
        return R.string.drawer_experiment_subtitle;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SETTINGS_ACTIVITY_REQUEST)
        {
            activity.readExperiment();
        }
    }

    private void setInOperation(boolean inOperation, boolean resumeMode)
    {
        if (inOperation)
        {
            activity.getExperiment().resumePause(resumeMode);
            primaryButtonsSet.activate(R.id.main_flb_action_stop, this);
        }
        else
        {
            activity.getExperiment().pause();
            primaryButtonsSet.activate(R.id.main_flb_action_play, this);
        }
    }

    @Override
    public void onClick(View b)
    {
        if (b.getId() == R.id.main_flb_action_play && !runPressed)
        {
            setInOperation(true, /*resumeMode=*/ false);
            runPressed = true;
        }
        else if (b.getId() == R.id.main_flb_action_stop && runPressed)
        {
            setInOperation(false, /*resumeMode=*/ false);
            runPressed = false;
        }
    }
}
