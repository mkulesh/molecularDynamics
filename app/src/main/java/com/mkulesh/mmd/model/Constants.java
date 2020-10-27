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
 * General types and constants
 */
public class Constants
{
    /**
     * The electron volt unit
     */
    static final double EV = 1.602176565e-19;

    /**
     * Coefficient used to obtain dimensionless equation of motions:
     */
    static final double AK = 1.036427e-28;

    /**
     * The Boltzmann constant kB [J/K]
     */
    static final double kB = 1.3806488e-23;

    /**
     * The femtosecond
     */
    static final double FSec = 1.0e-15;

    /**
     * Output value used by different calculation methods
     */
    public enum CalculationType
    {
        SUCCESS, // Calculation was successfully
        EXT_INTERRUPT, // Calculation has been interrupted by an external thread
        NAN_DETECTED // Calculation is interrupted since NaN detection
    }

    /**
     * Type defining the energy normalization
     */
    public enum EnergyNormType
    {
        FULL_ENERGY(0),
        TEMPERATURE(1),
        NONE(2);
        private final int value;

        EnergyNormType(int v)
        {
            this.value = v;
        }

        public int value()
        {
            return this.value;
        }
    }

    /**
     * Type defining the boundary condition
     */
    public enum BoundaryConditionType
    {
        REFLECTION,
        TRANSLATION
    }

    /**
     * Type defining the used potential
     */
    public enum PotentialType
    {
        LENNARD_JONES(0),
        MORSE(1),
        BORN_MAYER(2);
        private final int value;

        PotentialType(int v)
        {
            this.value = v;
        }

        public int value()
        {
            return this.value;
        }
    }

}
