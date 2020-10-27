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

import android.graphics.RectF;
import androidx.core.view.MotionEventCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.mkulesh.mmd.utils.ViewUtils;
import com.mkulesh.mmd.widgets.ControlDialog;
import com.mkulesh.mmd.widgets.DialogChangeListener;
import com.mkulesh.mmd.widgets.DialogParameters;

import java.util.ArrayList;

class SurfaceTouchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener implements OnTouchListener,
        DialogChangeListener
{

    private final AppCompatActivity activity;
    private final SurfaceView surface;
    private Experiment exp = null;
    private final ScaleGestureDetector scaleGestureDetector;
    private final ArrayList<DialogParameters> controlDialogs = new ArrayList<>(4);

    // The ‘active pointer’ is the one currently moving our object.
    private float mLastTouchX = -1, mLastTouchY = -1;

    private class Pointer
    {
        static final int INVALID_ID = -1;
        int activeId = INVALID_ID;
    }

    private final Pointer pointer = new Pointer();

    public SurfaceTouchListener(AppCompatActivity activity, SurfaceView surface)
    {
        this.surface = surface;
        this.activity = activity;
        scaleGestureDetector = new ScaleGestureDetector(activity, this);
    }

    public void setExperiment(Experiment exp)
    {
        this.exp = exp;
    }

    public void disable()
    {
        controlDialogs.clear();
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev)
    {
        if (exp == null)
        {
            return true;
        }
        // Let the ScaleGestureDetector inspect all events.
        scaleGestureDetector.onTouchEvent(ev);
        synchronized (pointer)
        {
            return onMoveEvent(ev);
        }
    }

    private boolean onMoveEvent(MotionEvent ev)
    {
        final int action = ev.getActionMasked();
        final int pointerIndex = ev.getActionIndex();
        final int pointerCount = ev.getPointerCount();
        if (pointerIndex != 0 || pointerCount != 1)
        {
            return true;
        }

        switch (action)
        {
        case MotionEvent.ACTION_DOWN:
            mLastTouchX = ev.getX(pointerIndex);
            mLastTouchY = ev.getY(pointerIndex);

            if (pointer.activeId == Pointer.INVALID_ID)
            {
                if (showControlDialog(mLastTouchX, mLastTouchY))
                {
                    return true;
                }
            }
            pointer.activeId = ev.getPointerId(0);
            break;

        case MotionEvent.ACTION_MOVE:
            if (pointer.activeId != Pointer.INVALID_ID)
            {

                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                // Calculate the distance moved
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                if (dx != 0.0 || dy != 0.0)
                {
                    onDrag(dx, dy);
                }

                // Remember this touch position for the next move event
                mLastTouchX = x;
                mLastTouchY = y;
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_POINTER_UP:
            pointer.activeId = Pointer.INVALID_ID;
            break;
        }

        return true;
    }

    private void onDrag(float dx, float dy)
    {
        double surfaceWidth = surface.getWidth();
        double surfaceHeight = surface.getHeight();
        exp.setScaling(1.0, dx / surfaceWidth, -dy / surfaceHeight);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector)
    {
        synchronized (pointer)
        {
            pointer.activeId = Pointer.INVALID_ID;
        }
        exp.setScaling(detector.getScaleFactor(), 0.0, 0.0);
        return true;
    }

    @Override
    public void onSeekBarChange(DialogParameters.Type type, double value)
    {
        exp.setControlValue(type, value, 0);
    }

    @Override
    public void onButtonChange(int buttonId)
    {
        exp.setControlValue(DialogParameters.Type.CONDITIONS_CHANGE, 0.0, buttonId);
    }

    private int getTitleBarHeight()
    {
        return (activity.getWindow().getAttributes().y + activity.getSupportActionBar().getHeight());
    }

    private void prepareDialogs()
    {
        int surfaceWidth = surface.getWidth();
        int surfaceHeight = surface.getHeight();
        // surface schema dialog
        {
            DialogParameters par = new DialogParameters(DialogParameters.Type.SURFACE_SCHEMA, true, null);
            par.width = surfaceWidth;
            par.height = surfaceHeight;
            par.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            par.top = getTitleBarHeight();
            // can be only called externally
            par.touchRect = new RectF(-300f, -300f, -100f, -100f);
            controlDialogs.add(par);
        }
        // gravity change dialog
        {
            DialogParameters par = new DialogParameters(DialogParameters.Type.GRAVITY_CHANGE, true, activity
                    .getResources().getString(R.string.gravity_change_dialog));
            par.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            par.height = 2 * surfaceHeight / 3;
            par.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            par.min = Double.parseDouble(activity.getResources().getString(R.string.pref_bound_gravity_min));
            par.max = Double.parseDouble(activity.getResources().getString(R.string.pref_bound_gravity_max));
            par.valueFormat = activity.getResources().getString(R.string.pref_bound_gravity_value_format);
            par.touchRect = new RectF(0.9f * surfaceWidth, 0.3f * surfaceHeight, 1.0f * surfaceWidth,
                    0.7f * surfaceHeight);
            controlDialogs.add(par);
        }
        // thermal change dialog
        {
            DialogParameters par = new DialogParameters(DialogParameters.Type.THERMAL_CHANGE, true, activity
                    .getResources().getString(R.string.thermal_change_dialog));
            par.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            par.height = 2 * surfaceHeight / 3;
            par.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            par.min = Double.parseDouble(activity.getResources().getString(R.string.pref_bound_thermal_change_min));
            par.max = Double.parseDouble(activity.getResources().getString(R.string.pref_bound_thermal_change_max));
            par.valueFormat = activity.getResources().getString(R.string.pref_bound_thermal_change_value_format);
            par.touchRect = new RectF(0.0f * surfaceWidth, 0.3f * surfaceHeight, 0.1f * surfaceWidth,
                    0.7f * surfaceHeight);
            controlDialogs.add(par);
        }
        // time step change dialog
        {
            DialogParameters par = new DialogParameters(DialogParameters.Type.TIMESTEP_CHANGE, false, activity
                    .getResources().getString(R.string.timestep_change_dialog));
            par.width = 2 * surfaceWidth / 3;
            par.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            par.gravity = Gravity.BOTTOM | Gravity.CENTER_VERTICAL;
            par.min = Double.parseDouble(activity.getResources().getString(R.string.pref_calc_time_step_min));
            par.max = Double.parseDouble(activity.getResources().getString(R.string.pref_calc_time_step_max));
            par.valueFormat = activity.getResources().getString(R.string.pref_calc_time_step_value_format);
            par.touchRect = new RectF(0.3f * surfaceWidth, 0.9f * surfaceHeight, 0.7f * surfaceWidth,
                    1.0f * surfaceHeight);
            controlDialogs.add(par);
        }
        // conditions change
        {
            DialogParameters par = new DialogParameters(DialogParameters.Type.CONDITIONS_CHANGE, false, activity
                    .getResources().getString(R.string.conditions_change_dialog));
            par.width = 2 * surfaceWidth / 3;
            par.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            par.top = getTitleBarHeight();
            par.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            par.touchRect = new RectF(0.3f * surfaceWidth, 0.0f * surfaceHeight, 0.7f * surfaceWidth,
                    0.1f * surfaceHeight);
            controlDialogs.add(par);
        }
        for (DialogParameters par : controlDialogs)
        {
            ViewUtils.Debug(this,
                    "preparing control dialog: " + par.type.toString() + ", touch area " + par.touchRect.toString());
        }

    }

    private boolean showControlDialog(float x, float y)
    {
        if (controlDialogs.isEmpty())
        {
            prepareDialogs();
        }
        for (DialogParameters par : controlDialogs)
        {
            if (par.touchRect.contains(x, y))
            {
                ViewUtils.Debug(this, "activating control dialog: " + par.type.toString());
                DialogParameters dPar = new DialogParameters(par);
                dPar.selectedValue = exp.getControlValue(dPar.type);
                ControlDialog d = new ControlDialog(activity, dPar, this);
                d.show();
                return true;
            }
        }
        return false;
    }

    public void showHelpDialog()
    {
        showControlDialog(-200f, -200f);
    }

    @Override
    public void onListItemChange(int itemIndex)
    {
        // is not used within this context
    }

}
