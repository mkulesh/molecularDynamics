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

package com.mkulesh.mmd;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.mkulesh.mmd.fragments.Experiment;
import com.mkulesh.mmd.utils.ViewUtils;

import androidx.preference.PreferenceManager;

public class MMDWallpaperService extends WallpaperService
{

    @Override
    public Engine onCreateEngine()
    {
        return new MMDWallpaperEngine();
    }

    private class MMDWallpaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener
    {

        Context context = null;
        private Experiment exp = null;
        private boolean preferenceChanged = false;
        private Integer currentRotation = null;
        private Display display = null;
        OrientationEventListener orientationEventListener = null;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder)
        {
            super.onCreate(surfaceHolder);
            ViewUtils.Debug(this, "Wallpaper started");
            display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
            context = MMDWallpaperService.this;
            PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
            if (exp == null)
            {
                exp = new Experiment(context);
            }
            Experiment.Options opt = new Experiment.Options();
            opt.enablePause = false;
            opt.enableInfoPanel = false;
            opt.drawBackground = true;
            exp.initialize(context, surfaceHolder, opt);

            // orientation change
            orientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL)
            {
                @Override
                public void onOrientationChanged(int orientation)
                {
                    processRotation(true);
                }
            };
        }

        @Override
        public void onDestroy()
        {
            ViewUtils.Debug(this, "onDestroy");
            exp.stop();
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible)
        {
            ViewUtils.Debug(this, "onVisibilityChanged=" + visible + ", current rotation = " + display.getRotation());
            if (visible)
            {
                if (preferenceChanged)
                {
                    exp.readParameters(context);
                    exp.updateBackgroundMode(true);
                    preferenceChanged = false;
                }
                if (currentRotation == null)
                {
                    currentRotation = display.getRotation();
                }
                else
                {
                    processRotation(false);
                }
                exp.resumePause(true);
                exp.resume();
                if (orientationEventListener.canDetectOrientation())
                {
                    orientationEventListener.enable();
                }
            }
            else
            {
                preferenceChanged = false;
                exp.pause();
                exp.stop();
                orientationEventListener.disable();
            }
            super.onVisibilityChanged(visible);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            ViewUtils.Debug(this, "detected change of shared preference: " + arg1);
            preferenceChanged = true;
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep,
                                     int xPixelOffset, int yPixelOffset)
        {
            if (exp != null)
            {
                exp.wallpaperOffsetsChanged(xOffset, yOffset);
            }
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
        }

        private void processRotation(boolean symmOnly)
        {
            synchronized (currentRotation)
            {
                int rotation = display.getRotation();
                boolean isRotaded = (symmOnly) ? (Math.abs(rotation - currentRotation) == 2)
                        : (rotation != currentRotation);
                if (exp != null && isRotaded)
                {
                    Integer previousRotation = currentRotation;
                    currentRotation = rotation;
                    exp.processRotationChange(previousRotation, currentRotation);
                }
            }
        }
    }

}
