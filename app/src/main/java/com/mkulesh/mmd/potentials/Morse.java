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

public class Morse implements BasePotential
{
    // http://test.kirensky.ru/master/articles/monogr/Book/Chapter_1_9.htm:
    // Material eps alpha sig
    // Na 0.06334 0.58993 5.336
    // Al 0.2703 1.1646 3.253
    // K 0.05424 0.49767 6.369
    // Ca 0.1623 0.80535 4.569
    // Cr 0.4414 1.5721 2.754
    // Fe 0.4174 1.3885 2.845
    // Ni 0.4205 1.4199 2.780
    // Cu 0.3429 1.3588 2.866
    // Rb 0.04644 0.42981 7.207
    // Sr 0.1513 0.73776 4.988
    // Mo 0.8032 1.5079 2.976
    // Ag 0.3323 1.3690 3.115
    // Cs 0.04485 0.41569 7.557
    // Ba 0.1416 0.65698 5.373
    // W 0.9906 1.4116 3.032
    // Pb 0.2348 1.1836 3.733
    // Mo 0.997 1.500 2.800
    // W 1.335 1.200 1.894
    // Au 0.560 1.637 1.922

    private final double eps = 0.2703;
    private final double alpha = 1.1646;
    private final double sig = 3.253; // Al

    /**
     * Default constructor
     */
    public Morse()
    {
        super();
    }

    @Override
    public double getThreshold()
    {
        return 8.0;
    }

    @Override
    public double getOptDistance()
    {
        return sig;
    }

    @Override
    public double getPotentialMin()
    {
        return -eps;
    }

    @Override
    public String getName()
    {
        return "Morse potential";
    }

    @Override
    public double getValue(double r)
    {
        return eps * (Math.exp(-2.0 * alpha * (r - sig)) - 2.0 * Math.exp(-alpha * (r - sig)));
    }

    @Override
    public int getFormulaResourceId(Context context, ValueType type)
    {
        return (type == ValueType.VALUE) ? SVGUtils.getResourceIdFromName("res/raw/formula_pv_morse.svg", context,
                false) : SVGUtils.getResourceIdFromName("res/raw/formula_pd_morse.svg", context, false);
    }

    @Override
    public double getDerivative(double r)
    {
        return eps * (-2.0 * alpha * Math.exp(-2.0 * alpha * (r - sig)) + 2.0 * alpha * Math.exp(-alpha * (r - sig)));
    }

}
