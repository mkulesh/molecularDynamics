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
 * This solver implements the Runge-Kutta integration method
 */
public class SolverRungeKutta implements SolverBase
{
    // Number of available precision grades of Runge-Kutta method
    static final int maxGrade = 4;

    // Currently used precision grade
    static final int usedGrade = 3;

    // Number of steps for each precision grade
    static final int[] gradeSteps = { 1, 2, 3, 4 };

    // Coefficients of the method
    static final double P1 = 1.0;
    static final double P2 = 0.5;
    static final double[][] A = { { 0, 0, 0, 0, }, { 0, P1, 0, 0, }, { 0, 2.0 / 3.0, 0, 0, },
            { 0, 1.0 / 2.0, 1.0 / 2.0, 1.0, } };
    static final double[][][] B = { { { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
            { { 0, 0, 0, 0 }, { P1, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
            { { 0, 0, 0, 0 }, { 2.0 / 3.0, 0, 0, 0 }, { -1.0 / (4.0 * P2), 1.0 / (4.0 * P2), 0, 0 }, { 0, 0, 0, 0 } },
            { { 0, 0, 0, 0 }, { 1.0 / 2.0, 0, 0, 0 }, { 0, 1.0 / 2.0, 0, 0 }, { 0, 0, 1.0, 0 } } };
    static final double[][] C = { { 1.0, 0, 0, 0, }, { 1.0 - 1.0 / (2.0 * P1), 1.0 / (2.0 * P1), 0, 0, },
            { 1.0 / 4.0 - P2, 3.0 / 4.0, P2, 0, }, { 1.0 / 6.0, 1.0 / 3.0, 1.0 / 3.0, 1.0 / 6.0, } };

    // Helper arrays used to store coordinates and velocities from previous step
    private Vector2D[] X1, V1;

    // Helper matrix where grade-dependent coefficients are stored
    private Vector2D[][] K, P;

    private int lastAtomNumber = 0;

    @Override
    public String getName()
    {
        return "Runge-Kutta of grad " + usedGrade;
    }

    /**
     * Initialization method
     */
    private void initialize(AtomSet atomSet)
    {
        lastAtomNumber = atomSet.getAtoms().size();

        X1 = new Vector2D[lastAtomNumber];
        V1 = new Vector2D[lastAtomNumber];
        K = new Vector2D[gradeSteps[usedGrade]][lastAtomNumber];
        P = new Vector2D[gradeSteps[usedGrade]][lastAtomNumber];
        for (int i = 0; i < lastAtomNumber; i++)
        {
            for (int k = 0; k < gradeSteps[usedGrade]; k++)
            {
                K[k][i] = new Vector2D();
                P[k][i] = new Vector2D();
            }
            X1[i] = new Vector2D();
            V1[i] = new Vector2D();
        }
    }

    @Override
    public Constants.CalculationType calculateNextStep(AtomSet atomSet, double dt)
    {
        ArrayList<Atom> atoms = atomSet.getAtoms();
        if (lastAtomNumber != atoms.size())
        {
            initialize(atomSet);
        }

        int i, j, k;

        // store the coordinates and velocities
        for (i = 0; i < atoms.size(); i++)
        {
            X1[i].assign(atoms.get(i).coordinate);
            V1[i].assign(atoms.get(i).velocity);
        }

        // calculate matrix P and K for each grade step where atoms will be
        // modified with intermediate values for coordinates and velocities
        for (k = 0; k < gradeSteps[usedGrade]; k++)
        {
            // prepare new coordinates and velocities for load calculation
            for (i = 0; i < atoms.size(); i++)
            {
                atoms.get(i).coordinate.assign(X1[i]);
                if (k > 0)
                {
                    atoms.get(i).coordinate.add(P[k - 1][i].prod(A[usedGrade][k]));
                }
                atoms.get(i).velocity.assign(V1[i]);
                for (j = 0; j < k; j++)
                {
                    atoms.get(i).velocity.add(K[j][i].prod(B[usedGrade][k][j]));
                }
            }
            // calculate intermediate load value
            Constants.CalculationType t = atomSet.calculateAcceleration();
            if (t != CalculationType.SUCCESS)
            {
                return t;
            }

            // set matrix P and K for this grade step
            for (i = 0; i < atoms.size(); i++)
            {
                K[k][i].assign(atoms.get(i).acceleration.prod(dt));
                P[k][i].assign(atoms.get(i).velocity.prod(dt));
            }
        }

        // re-calculate coordinates and velocities
        for (i = 0; i < atoms.size(); i++)
        {
            atoms.get(i).velocity.assign(V1[i]);
            atoms.get(i).coordinate.assign(X1[i]);
            for (k = 0; k < gradeSteps[usedGrade]; k++)
            {
                atoms.get(i).velocity.add(K[k][i].prod(C[usedGrade][k]));
                atoms.get(i).coordinate.add(P[k][i].prod(C[usedGrade][k]));
            }
        }

        return CalculationType.SUCCESS;
    }

}
