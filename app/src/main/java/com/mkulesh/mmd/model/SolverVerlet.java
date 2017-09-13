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

import com.mkulesh.mmd.model.Constants.CalculationType;

import java.util.ArrayList;

/**
 * This class implements Velocity Verlet integration method see: http://en.wikipedia.org/wiki/Verlet_integration
 *
 * Note that that this algorithm assumes that acceleration only depends on position, and does not depend on velocity
 */
public class SolverVerlet implements SolverBase
{

    /**
     * Default constructor
     */
    public SolverVerlet()
    {
    }

    @Override
    public String getName()
    {
        return "Velocity Verlet";
    }

    @Override
    public Constants.CalculationType calculateNextStep(AtomSet atomSet, double dt)
    {

        ArrayList<Atom> atoms = atomSet.getAtoms();

        // store the coordinates x(t)
        Vector2D[] xt = new Vector2D[atoms.size()];
        for (int i = 0; i < atoms.size(); i++)
        {
            xt[i] = new Vector2D(atoms.get(i).coordinate);
        }

        // calculate acceleration a(t)
        Constants.CalculationType t = atomSet.calculateAcceleration();
        if (t != CalculationType.SUCCESS)
        {
            return t;
        }

        // calculate v(t + dt/2) and x´(t + dt)
        for (Atom a : atomSet.getAtoms())
        {
            a.velocity.add(a.acceleration.prod(dt / 2.0));
            a.coordinate.add(a.velocity.prod(dt));
        }

        // calculate acceleration a(t + dt)
        // Note that that this algorithm assumes that acceleration only
        // depends on position, and does not depend on velocity
        t = atomSet.calculateAcceleration();
        if (t != CalculationType.SUCCESS)
        {
            return t;
        }

        // calculate v(t + dt)
        for (Atom a : atomSet.getAtoms())
        {
            a.velocity.add(a.acceleration.prod(dt / 2.0));
        }

        // re-calculate coordinates x(t + dt)
        for (int i = 0; i < atoms.size(); i++)
        {
            Atom a = atoms.get(i);
            a.coordinate.assign(xt[i].sum(a.velocity.prod(dt)));
            if (a.velocity.isNaN() || a.coordinate.isNaN())
            {
                return CalculationType.NAN_DETECTED;
            }
        }

        return CalculationType.SUCCESS;
    }

}
