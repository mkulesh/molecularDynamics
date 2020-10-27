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

package com.mkulesh.mmd.model;

import com.mkulesh.mmd.model.Constants.PotentialType;
import com.mkulesh.mmd.potentials.BasePotential;
import com.mkulesh.mmd.potentials.BornMayer;
import com.mkulesh.mmd.potentials.LennardJones;
import com.mkulesh.mmd.potentials.Morse;

public class Potential
{

    private PotentialType type = PotentialType.LENNARD_JONES;
    private BasePotential function = null;

    public PotentialType getType()
    {
        return type;
    }

    public void setType(PotentialType type)
    {
        if (this.type != type)
        {
            function = null;
            this.type = type;
        }
        this.type = type;
    }

    /**
     * Procedure creates the potential to be used
     */
    public static BasePotential createPotential(PotentialType potentialType)
    {
        switch (potentialType)
        {
        case LENNARD_JONES:
            return new LennardJones();
        case MORSE:
            return new Morse();
        default:
            return new BornMayer();
        }
    }

    /**
     * Procedure returns actual potential function
     */
    BasePotential getFunction()
    {
        if (function == null)
        {
            function = createPotential(type);
        }
        return function;
    }

}
