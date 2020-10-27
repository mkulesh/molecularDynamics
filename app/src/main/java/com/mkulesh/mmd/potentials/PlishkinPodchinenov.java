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

@SuppressWarnings("unused")
public class PlishkinPodchinenov implements BasePotential
{
    private double Ro, r0, D0, Al, r1, A0, A1, A2, A3, A4;

    /**
     * Default constructor
     */
    public PlishkinPodchinenov(double Ro, double r0, double D0, double Al, double r1, double A0, double A1, double A2,
                               double A3, double A4)
    {
        super();
        this.Ro = Ro;
        this.r0 = r0;
        this.D0 = D0;
        this.Al = Al;
        this.r1 = r1;
        this.A0 = A0;
        this.A1 = A1;
        this.A2 = A2;
        this.A3 = A3;
        this.A4 = A4;
    }

    @Override
    public double getThreshold()
    {
        return 40.0;
    }

    @Override
    public double getOptDistance()
    {
        return 2.0;
    }

    @Override
    public double getPotentialMin()
    {
        return 0.0;
    }

    @Override
    public String getName()
    {
        return "Plishkin-Podchinenov potential";
    }

    @Override
    public double getValue(double r)
    {
        if (r < 2.03)
        {
            return A0 * Math.exp(Ro * (r - r0) / r0);
        }
        else if (r < 2.866)
        {
            return D0 * (Math.exp(-2.0 * Al * (r - r1)) - 2.0 * Math.exp(-Al * (r - r1)));
        }
        else if (r < 4.034)
        {
            return A4 * r * r * r + A3 * r * r + A2 * r + A1;
        }
        return 0.0;
    }

    @Override
    public int getFormulaResourceId(Context context, ValueType type)
    {
        return 0;
    }

    @Override
    public double getDerivative(double r)
    {
        if (r < 2.03)
        {
            return A0 * Ro * Math.exp(Ro * (r - r0) / r0) / r0;
        }
        else if (r < 2.866)
        {
            return D0 * (-2.0 * Al * Math.exp(-2.0 * Al * (r - r1)) + 2.0 * Al * Math.exp(-Al * (r - r1)));
        }
        else if (r < 4.034)
        {
            return 3.0 * A4 * r * r + 2.0 * A3 * r + A2;
        }
        return 0.0;
    }
}
