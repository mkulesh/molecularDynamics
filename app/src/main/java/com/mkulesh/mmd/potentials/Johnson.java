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

public class Johnson implements BasePotential
{
    /**
     * Default constructor
     */
    public Johnson()
    {
        super();
        // empty
    }

    @Override
    public double getThreshold()
    {
        return 5;
    }

    @Override
    public double getOptDistance()
    {
        return 2.6;
    }

    @Override
    public double getPotentialMin()
    {
        return 0.0;
    }

    @Override
    public String getName()
    {
        return "Johnson potential";
    }

    @Override
    public double getValue(double r)
    {
        if (r < 2.4)
        {
            return -2.195976 * Math.pow(r - 3.097910, 3.0) + 2.704060 * r - 7.436448;
        }
        else if (r < 3.0)
        {
            return -0.639230 * Math.pow(r - 3.115829, 3.0) + 0.477871 * r - 1.581570;
        }
        else if (r < 3.44)
        {
            return -1.115035 * Math.pow(r - 3.066403, 3.0) + 0.466892 * r - 1.547967;
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
        if (r < 2.4)
        {
            return -6.587910 * Math.pow(r - 3.097910, 2.0) + 2.704060;
        }
        else if (r < 3.0)
        {
            return -1.917690 * Math.pow(r - 3.115829, 2.0) + 0.477871;
        }
        else if (r < 3.44)
        {
            return -3.345090 * Math.pow(r - 3.066403, 2.0) + 0.466892;
        }
        return 0.0;
    }

}
