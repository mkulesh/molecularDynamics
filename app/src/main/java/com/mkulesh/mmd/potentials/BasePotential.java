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

package com.mkulesh.mmd.potentials;

import android.content.Context;

public interface BasePotential
{
    enum ValueType
    {
        VALUE, DERIVATIVE
    }

    /**
     * Procedure returns the potential name
     */
    String getName();

    /**
     * Procedure returns the potential threshold
     */
    double getThreshold();

    /**
     * Procedure returns the x-coordinate of the potential well, or the optimal distance between two atoms where no load
     * is present
     */
    double getOptDistance();

    /**
     * Procedure returns the minimum value of the potential
     */
    double getPotentialMin();

    /**
     * Procedure returns the potential value
     */
    double getValue(double r);

    /**
     * Procedure returns resource id for the potential formula
     */
    int getFormulaResourceId(Context context, ValueType type);

    /**
     * Procedure returns the value of potential derivative
     */
    double getDerivative(double r);
}
