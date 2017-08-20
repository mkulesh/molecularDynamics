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
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.SurfaceView;

import com.mkulesh.mmd.model.Constants.PotentialType;
import com.mkulesh.mmd.utils.ViewUtils;
import com.mkulesh.mmd.widgets.DialogParameters;

public class MainActivity extends MmdActivity
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

    /**
     * Dummy array used to avoid lint warning about unused resources
     */
    public final static int [] isedResources = {
            R.drawable.anonymous_amibe, R.drawable.atom_blue_red, R.drawable.ball_volley_ball,
            R.drawable.atom_gelb, R.drawable.autumn_leaf_01, R.drawable.autumn_leaf_02,
            R.drawable.ball_blue_ball, R.drawable.ball_soccer, R.drawable.ball_tennis,
            R.drawable.ball_waves_sketch, R.drawable.bird_carton_style, R.raw.convert,
            R.raw.convert_file, R.drawable.drop_water_blue, R.drawable.drop_water_green,
            R.raw.formula_pd_born_mayer, R.raw.formula_pd_lennard_jones, R.raw.formula_pd_morse,
            R.raw.formula_pv_born_mayer, R.raw.formula_pv_lennard_jones, R.raw.formula_pv_morse,
            R.drawable.ic_boundary1, R.raw.ic_boundary1, R.drawable.ic_boundary2,
            R.raw.ic_boundary2, R.drawable.ic_boundary3, R.raw.ic_boundary3,
            R.drawable.ic_grid_diagonal, R.drawable.ic_grid_square, R.drawable.molecule_6atoms,
            R.raw.ic_launcher, R.drawable.molecule_water, R.raw.multitouch_drag,
            R.raw.multitouch_pinch, R.raw.multitouch_simpletap, R.drawable.planet_earth_01,
            R.drawable.planet_earth_02, R.drawable.planet_red_planet, R.drawable.planet_saturn,
            R.drawable.snow_flake, R.drawable.ufo_cartoon_style, R.drawable.whirlpool,

    };

    @Override
    protected int getContentLayoutId()
    {
        navigationItemIndex = 0;
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // get rotation properties
        currentRotation = display.getRotation();

        // orientation change
        orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL)
        {
            @Override
            public void onOrientationChanged(int orientation)
            {
                synchronized (currentRotation)
                {
                    int rotation = display.getRotation();
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
        surface = (SurfaceView) findViewById(R.id.activity_main_view);
        surface.setZOrderOnTop(true);
        surface.getHolder().setFormat(PixelFormat.TRANSPARENT);
        touchListener = new SurfaceTouchListener(this, surface);
        surface.setOnTouchListener(touchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        ViewUtils.Debug(this, "onResume");

        // activate experiment
        if (exp == null)
        {
            exp = new Experiment(this);
        }

        // restore potential since it can be changed in other activities
        exp.setControlValue(
                DialogParameters.Type.POTENTIAL_CHANGE,
                0.0,
                PotentialType.valueOf(
                        PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_POTENTIAL,
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
        opt.drawBackground = false;
        exp.initialize(this, surface.getHolder(), opt);
        exp.resume();

        // enable orientation sensor
        if (orientationEventListener.canDetectOrientation())
        {
            orientationEventListener.enable();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        ViewUtils.Debug(this, "onPause");
        orientationEventListener.disable();
        touchListener.disable();
        exp.stop();
        exp.writeParameters();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        ViewUtils.Debug(this, "onSaveInstanceState");
        exp.writeToBundle(outState);
        outState.putBoolean("activity_main_button_run", runPressed);
        outState.putInt("activity_main_rotation", currentRotation);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        ViewUtils.Debug(this, "onRestoreInstanceState");
        exp = outState.getParcelable(Experiment.PARCELABLE_ID);
        runPressed = outState.getBoolean("activity_main_button_run", false);
        Integer previousRotation = outState.getInt("activity_main_rotation", currentRotation);
        exp.processRotationChange(previousRotation, currentRotation);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ViewUtils.Debug(this, "onDestroy");
        exp = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
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
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, SETTINGS_ACTIVITY_REQUEST);
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SETTINGS_ACTIVITY_REQUEST)
        {
            exp.readParameters();
        }
    }

}
