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

public class BornMayer implements BasePotential
{
    private final double eps = 0.1;
    private final double rho = 10.34;
    private final double sig = 2.5;

    /**
     * Default constructor
     */
    public BornMayer()
    {
        super();
    }

    @Override
    public double getThreshold()
    {
        return 6.0;
    }

    @Override
    public double getOptDistance()
    {
        return 4.5;
    }

    @Override
    public double getPotentialMin()
    {
        return 0.1;
    }

    @Override
    public String getName()
    {
        return "Born-Mayer potential";
    }

    @Override
    public double getValue(double r)
    {
        return eps * Math.exp(-rho * (r - sig) / sig);
    }

    @Override
    public int getFormulaResourceId(Context context, ValueType type)
    {
        return (type == ValueType.VALUE) ? SVGUtils.getResourceIdFromName("res/raw/formula_pv_born_mayer.svg", context,
                false) : SVGUtils.getResourceIdFromName("res/raw/formula_pd_born_mayer.svg", context, false);
    }

    @Override
    public double getDerivative(double r)
    {
        return (-eps * rho / sig) * Math.exp(-rho * (r - sig) / sig);
    }
}
