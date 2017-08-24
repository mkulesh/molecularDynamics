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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.SurfaceHolder;

import com.mkulesh.mmd.model.AtomSet;
import com.mkulesh.mmd.model.Constants.EnergyNormType;
import com.mkulesh.mmd.model.Constants.PotentialType;
import com.mkulesh.mmd.utils.ThreadContol;
import com.mkulesh.mmd.utils.ViewUtils;
import com.mkulesh.mmd.widgets.DialogParameters;

public class Experiment implements Runnable, Parcelable
{

    /**
     * State attributes to be stored in Parcel
     */
    public static final String PARCELABLE_ID = "Experiment";
    private AtomSet atomSet = new AtomSet(); // set of the atoms, must be always handled as synchronized

    /**
     * Parcelable interface
     */
    public Experiment(Parcel in)
    {
        super();
        ViewUtils.Debug(this, "created from parcel");
        readFromParcel(in);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(atomSet, flags);
    }

    private void readFromParcel(Parcel in)
    {
        atomSet = in.readParcelable(AtomSet.class.getClassLoader());
    }

    public static final Parcelable.Creator<Experiment> CREATOR = new Parcelable.Creator<Experiment>()
    {
        public Experiment createFromParcel(Parcel in)
        {
            return new Experiment(in);
        }

        public Experiment[] newArray(int size)
        {
            return new Experiment[size];
        }
    };

    /**
     * Options class for this experiment
     */
    public static class Options
    {
        boolean enablePause = false;
        boolean enableInfoPanel = false;
        boolean drawBackground = false;
    }

    /**
     * Private attributes to be created internally
     */
    private Context context = null;
    private ThreadContol threadControl = new ThreadContol();
    private AtomPainter painter = null;
    private Thread thread = null;

    /**
     * Default constructor
     */
    public Experiment(Context context)
    {
        super();
        this.context = context;
        ViewUtils.Debug(this, "created from scratch");
        readParameters();
    }

    /**
     * Initialization method
     */
    public void initialize(Context context, SurfaceHolder holder, Options opt)
    {
        this.context = context;
        painter = new AtomPainter(context, holder);
        if (opt.enablePause)
        {
            atomSet.setPauseHandler(threadControl);
        }
        if (opt.enableInfoPanel)
        {
            painter.enableInfoPanel();
        }
        updateBackgroundMode(opt.drawBackground);
    }

    public void updateBackgroundMode(boolean drawBackground)
    {
        if (painter != null && drawBackground)
        {
            painter.setBackgroundMode();
        }
    }

    /**
     * Procedure sets a pause for calculation thread
     */
    public void pause()
    {
        threadControl.pause();
    }

    /**
     * Procedure resumes a pause from calculation thread
     */
    public void resumePause(boolean resumeAll)
    {
        threadControl.resume(resumeAll);
    }

    /**
     * Procedure starts calculation and painting thread
     */
    public void resume()
    {
        painter.resume();
        synchronized (atomSet)
        {
            painter.put(new AtomSet(atomSet));
        }
        if (thread == null)
        {
            thread = new Thread(this, this.getClass().getSimpleName());
            threadControl.setInterrupted(false);
            thread.start();
        }
    }

    /**
     * Procedure interrupts calculation and painting thread
     */
    synchronized public void stop()
    {
        if (thread != null)
        {
            threadControl.setInterrupted(true);
            threadControl.resume(true);
            thread.interrupt();
            thread = null;
        }
        painter.stop();
    }

    @Override
    public void run()
    {
        ViewUtils.Debug(this, "(pid: " + Thread.currentThread().getId() + "): started");

        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                // check the pause
                synchronized (threadControl)
                {
                    while (threadControl.isPaused())
                    {
                        ViewUtils.Debug(this, "(pid: " + Thread.currentThread().getId() + "): paused");
                        threadControl.wait();
                        ViewUtils.Debug(this, "(pid: " + Thread.currentThread().getId() + "): resumed");
                    }
                    if (threadControl.isInterrupted())
                    {
                        ViewUtils.Debug(this, "(pid: " + Thread.currentThread().getId() + "): interrupted");
                        break;
                    }
                }

                if (Thread.currentThread().isInterrupted())
                {
                    break;
                }

                AtomSet atoms = null;
                synchronized (atomSet)
                {
                    atoms = atomSet.nextStep();
                }
                painter.waitAndPut(atoms);
            }
            catch (Exception ex)
            {
                break;
            }
        }

        ViewUtils.Debug(this, "(pid: " + Thread.currentThread().getId() + "): stopped");
    }

    /**
     * Procedure reads the atom parameters from shared preferences
     */
    public void readParameters()
    {
        threadControl.pause();
        synchronized (atomSet)
        {
            try
            {
                atomSet.readParameters(context);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        threadControl.resume(false);
    }

    /**
     * Procedure writes the atom parameters into shared preferences
     */
    public void writeParameters()
    {
        threadControl.pause();
        synchronized (atomSet)
        {
            atomSet.writeParameters(context);
        }
        threadControl.resume(false);
    }

    /**
     * Procedure writes current experiment to the state
     */
    public void writeToBundle(Bundle outState)
    {
        threadControl.pause();
        synchronized (atomSet)
        {
            ViewUtils.Debug(this, "collecting instance state");
            outState.putParcelable(PARCELABLE_ID, this);
        }
        threadControl.resume(false);
    }

    /**
     * Procedure performs rotation due to screen orientation change
     */
    public void processRotationChange(Integer previousRotation, Integer currentRotation)
    {
        if (previousRotation < 0 || previousRotation == currentRotation)
        {
            return;
        }
        threadControl.pause();
        synchronized (atomSet)
        {
            ViewUtils.Debug(this, "detected rotation change: " + previousRotation + " -> " + currentRotation);
            atomSet.rotate(currentRotation.intValue(), previousRotation.intValue());
            painter.put(new AtomSet(atomSet));
        }
        threadControl.resume(false);
    }

    /**
     * Procedure returns current control value
     */
    public double getControlValue(DialogParameters.Type type)
    {
        double retValue = 0.0;
        threadControl.pause();
        synchronized (atomSet)
        {
            switch (type)
            {
            case SURFACE_SCHEMA:
                break;
            case GRAVITY_CHANGE:
                retValue = atomSet.gravity;
                break;
            case THERMAL_CHANGE:
                retValue = atomSet.thermalChange;
                break;
            case TIMESTEP_CHANGE:
                retValue = atomSet.timeStep;
                break;
            case CONDITIONS_CHANGE:
                retValue = atomSet.energyNorm.value();
            case POTENTIAL_CHANGE:
                retValue = atomSet.potential.getType().value();
            }
        }
        threadControl.resume(false);
        return retValue;
    }

    /**
     * Procedure sets current control value for given type
     */
    public void setControlValue(DialogParameters.Type type, double doubleValue, int intValue)
    {
        threadControl.pause();
        synchronized (atomSet)
        {
            switch (type)
            {
            case SURFACE_SCHEMA:
                break;
            case GRAVITY_CHANGE:
            {
                ViewUtils.Debug(this, "setting gravity value: " + doubleValue);
                atomSet.gravity = doubleValue;
                break;
            }
            case THERMAL_CHANGE:
            {
                ViewUtils.Debug(this, "setting thermal value: " + doubleValue);
                atomSet.thermalChange = doubleValue;
                break;
            }
            case TIMESTEP_CHANGE:
            {
                ViewUtils.Debug(this, "setting time step value: " + doubleValue);
                atomSet.timeStep = doubleValue;
                break;
            }
            case CONDITIONS_CHANGE:
            {
                switch (intValue)
                {
                case R.id.button_norm_fullenergy:
                    atomSet.energyNorm = EnergyNormType.FULL_ENERGY;
                    break;
                case R.id.button_norm_temperature:
                    atomSet.energyNorm = EnergyNormType.TEMPERATURE;
                    break;
                case R.id.button_norm_none:
                    atomSet.energyNorm = EnergyNormType.NONE;
                    break;
                }
                ViewUtils.Debug(this, "setting new boundary conditions: " + atomSet.energyNorm.toString());
                break;
            }
            case POTENTIAL_CHANGE:
            {
                if (intValue >= 0 && intValue < PotentialType.values().length)
                {
                    final PotentialType p = PotentialType.values()[intValue];
                    ViewUtils.Debug(this, "changed potential to: " + p.toString());
                    atomSet.potential.setType(p);
                }
                break;
            }
            }
        }
        threadControl.resume(false);
    }

    /**
     * Procedure sets new scaling parameters
     */
    public void setScaling(double scaleFactor, double dx, double dy)
    {
        double maxScale = Double.parseDouble(context.getResources().getString(R.string.pref_area_zoom_max));
        threadControl.pause();
        synchronized (atomSet)
        {
            atomSet.scale(scaleFactor, maxScale, dx, dy);
            painter.waitAndPut(new AtomSet(atomSet));
        }
        threadControl.resume(false);
    }

    /**
     * Process change of wallpaper offset if app is running as MMDWallpaperEngine
     */
    public void wallpaperOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep)
    {
        if (painter != null)
        {
            synchronized (painter)
            {
                painter.wallpaperOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep);
            }
        }
    }
}
