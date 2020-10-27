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

package com.mkulesh.mmd.widgets;

import android.graphics.RectF;

public class DialogParameters
{

    public enum Type
    {
        SURFACE_SCHEMA, GRAVITY_CHANGE, THERMAL_CHANGE, TIMESTEP_CHANGE, CONDITIONS_CHANGE, POTENTIAL_CHANGE
    }

    public final Type type;
    public final boolean isVertical;
    public final String title;
    public int height, width;
    public int top = -1;
    public int gravity;
    public double min, max;
    public double selectedValue;
    public String valueFormat = "0.00";
    public RectF touchRect;

    /**
     * Default constructor
     */
    public DialogParameters(Type type, boolean isVertical, String title)
    {
        this.type = type;
        this.isVertical = isVertical;
        this.title = title;
    }

    /**
     * Copy constructor
     */
    public DialogParameters(DialogParameters par)
    {
        type = par.type;
        isVertical = par.isVertical;
        title = par.title;
        height = par.height;
        width = par.width;
        top = par.top;
        gravity = par.gravity;
        min = par.min;
        max = par.max;
        selectedValue = par.selectedValue;
        valueFormat = par.valueFormat;
        touchRect = new RectF(par.touchRect);
    }

    public static double progressToDouble(double min, double max, int progress)
    {
        return min + progress * (max - min) / 100;
    }
}
