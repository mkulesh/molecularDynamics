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

package com.mkulesh.mmd.model;

/**
 * Solver interface used to integrate equations of motion
 */
interface SolverBase
{
    /**
     * Procedure calculates the next atom configuration and returns the overall calculation status
     *
     * It stores the coordinates and velocities for step n into given AtomSet using time step dt
     */
    Constants.CalculationType calculateNextStep(AtomSet atomSet, double dt);
}
