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

package com.mkulesh.mmd.potentials;

import android.content.Context;

import com.mkulesh.mmd.utils.SVGUtils;

public class LennardJones implements BasePotential
{
    // http://test.kirensky.ru/master/articles/monogr/Book/Chapter_1_9.htm:
    // Material eps sig
    // Ne 0.31E-02 2.74
    // Ar 1.04E-02 3.40
    // Kr 1.4E-02 3.65
    // Xe 1.997E-02 3.98
    private double eps = 0.01997, sig = 3.98; // Xe

    /**
     * Default constructor
     */
    public LennardJones()
    {
        super();
    }

    @Override
    public double getThreshold()
    {
        return 12.0;
    }

    @Override
    public double getOptDistance()
    {
        return sig * Math.pow(2.0, 1.0 / 6.0);
    }

    @Override
    public double getPotentialMin()
    {
        return -eps;
    }

    @Override
    public String getName()
    {
        return "Lennard-Jones potential";
    }

    @Override
    public double getValue(double r)
    {
        return 4.0 * eps * (Math.pow(sig / r, 12.0) - Math.pow(sig / r, 6.0));
    }

    @Override
    public int getFormulaResourceId(Context context, ValueType type)
    {
        return (type == ValueType.VALUE) ? SVGUtils.getResourceIdFromName("res/raw/formula_pv_lennard_jones.svg",
                context, false) : SVGUtils
                .getResourceIdFromName("res/raw/formula_pd_lennard_jones.svg", context, false);
    }

    @Override
    public double getDerivative(double r)
    {
        return -24.0 * eps * (2.0 * Math.pow(sig / r, 12.0) - Math.pow(sig / r, 6.0)) / r;
    }
}
