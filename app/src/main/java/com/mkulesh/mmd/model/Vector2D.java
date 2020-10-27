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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class that implements a 2D vector
 */
public class Vector2D implements Parcelable
{
    /**
     * State attributes to be stored in Parcel
     */
    public double x = 0.0;
    public double y = 0.0;

    /**
     * Parcelable interface
     */
    private Vector2D(Parcel in)
    {
        super();
        readFromParcel(in);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeDouble(x);
        dest.writeDouble(y);
    }

    void readFromParcel(Parcel in)
    {
        x = in.readDouble();
        y = in.readDouble();
    }

    public static final Parcelable.Creator<Vector2D> CREATOR = new Parcelable.Creator<Vector2D>()
    {
        public Vector2D createFromParcel(Parcel in)
        {
            return new Vector2D(in);
        }

        public Vector2D[] newArray(int size)
        {
            return new Vector2D[size];
        }
    };

    /**
     * Default constructor
     */
    public Vector2D()
    {
        super();
    }

    /**
     * Copy constructor
     */
    Vector2D(Vector2D p)
    {
        super();
        assign(p);
    }

    /**
     * Initialization constructor
     */
    public Vector2D(double x, double y)
    {
        super();
        this.x = x;
        this.y = y;
    }

    /**
     * Setter procedure
     */
    public void set(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Assign procedure
     */
    public void assign(Vector2D p)
    {
        this.x = p.x;
        this.y = p.y;
    }

    /**
     * Procedure sets all data to zero
     */
    void erase()
    {
        x = 0.0;
        y = 0.0;
    }

    /**
     * Checks whether this point is equal to the given point
     */
    public boolean isEqual(Vector2D p)
    {
        return (x == p.x && y == p.y);
    }

    /**
     * Procedure sets NaN value to this vector
     */
    void invalidate()
    {
        x = Double.NaN;
        y = Double.NaN;
    }

    /**
     * Checks whether this vector has a NaN value
     */
    boolean isNaN()
    {
        return (Double.isNaN(x) || Double.isNaN(y)) ? true : false;
    }

    /**
     * Procedure calculates vector modulus
     */
    double mod()
    {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Procedure normalizes this vector to given length
     */
    public Vector2D normalize(double length)
    {
        double m = mod();
        if (m == 0.0)
        {
            return new Vector2D(0.0, 0.0);
        }
        return new Vector2D(length * x / m, length * y / m);
    }

    /**
     * Procedure calculates vector power of two
     */
    double pow2()
    {
        return x * x + y * y;
    }

    /**
     * Procedure calculates distance between this point and given point p
     */
    double distance(Vector2D p)
    {
        return Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
    }

    /**
     * Procedure adds given vector p to this vector
     */
    public void add(Vector2D p)
    {
        x += p.x;
        y += p.y;
    }

    /**
     * Procedure returns composition of this vector with given vector p
     */
    public Vector2D sum(Vector2D p)
    {
        return new Vector2D(x + p.x, y + p.y);
    }

    /**
     * Procedure subtracts given vector p from this vector
     */
    void substract(Vector2D p)
    {
        x -= p.x;
        y -= p.y;
    }

    /**
     * Procedure multiplies this vector with given constant p
     */
    void multiply(double p)
    {
        x *= p;
        y *= p;
    }

    /**
     * Procedure returns production of this vector with given constant p
     */
    public Vector2D prod(double p)
    {
        return new Vector2D(x * p, y * p);
    }

    /**
     * Procedure divides this vector by given constant p
     */
    void divide(double p)
    {
        x /= p;
        y /= p;
    }

    /**
     * Procedure divides this vector by given vector p component-wise
     */
    void divide(Vector2D p)
    {
        x /= p.x;
        y /= p.y;
    }

    /**
     * Procedure returns division of this vector to given constant p
     */
    Vector2D division(double p)
    {
        return new Vector2D(x / p, y / p);
    }

    /**
     * Procedure rotates the vector based on two rotation parameters
     */
    void rotate(int previousRotation, int currentRotation)
    {
        int r = previousRotation - currentRotation;
        double d = (r > 0) ? -1.0 : 1.0;
        for (int k = 0; k < Math.abs(r); k++)
        {
            double xprev = x;
            x = d * y;
            y = -1.0 * d * xprev;
        }
    }
}
