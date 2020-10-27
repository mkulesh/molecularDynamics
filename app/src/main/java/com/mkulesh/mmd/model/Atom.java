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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class that implements a single atom
 */
public class Atom implements Parcelable
{

    /**
     * Public types and constants
     */
    public enum VectorType
    {
        COORDINATE, VELOCITY, ACCELERATION
    }

    /**
     * State attributes to be stored in Parcel
     */
    public Vector2D coordinate = new Vector2D(); // Cartesian coordinates vector
    Vector2D velocity = new Vector2D(); // velocity vector
    Vector2D acceleration = new Vector2D(); // acceleration vector
    private boolean translated = false;

    /**
     * Parcelable interface
     */
    private Atom(Parcel in)
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
        coordinate.writeToParcel(dest, flags);
        velocity.writeToParcel(dest, flags);
        acceleration.writeToParcel(dest, flags);
        dest.writeString(String.valueOf(translated));
    }

    private void readFromParcel(Parcel in)
    {
        coordinate.readFromParcel(in);
        velocity.readFromParcel(in);
        acceleration.readFromParcel(in);
        //noinspection ConstantConditions
        translated = Boolean.getBoolean(in.readString());
    }

    public static final Parcelable.Creator<Atom> CREATOR = new Parcelable.Creator<Atom>()
    {
        public Atom createFromParcel(Parcel in)
        {
            return new Atom(in);
        }

        public Atom[] newArray(int size)
        {
            return new Atom[size];
        }
    };

    /**
     * Default constructor
     */
    public Atom()
    {
        super();
    }

    /**
     * Copy constructor
     */
    public Atom(Atom a)
    {
        super();
        coordinate = new Vector2D(a.coordinate);
        velocity = new Vector2D(a.velocity);
        acceleration = new Vector2D(a.acceleration);
        translated = a.translated;
    }

    /**
     * Assign procedure
     */
    void assign(Atom a)
    {
        coordinate.assign(a.coordinate);
        velocity.assign(a.velocity);
        acceleration.assign(a.acceleration);
        translated = a.translated;
    }

    /**
     * Procedure returns atom attribute with given type
     */
    public Vector2D getAttribute(VectorType vectorType)
    {
        switch (vectorType)
        {
        case COORDINATE:
            return new Vector2D(coordinate);
        case VELOCITY:
            return new Vector2D(velocity);
        case ACCELERATION:
            return new Vector2D(acceleration);
        }
        return null;
    }

    /**
     * Procedure performs reflection of the atom from a bound within given area
     */
    void reflect(PhysicalArea area, double offset)
    {
        // x - coordinate
        if (coordinate.x < area.getMin().x + offset)
        {
            coordinate.x = area.getMin().x + offset;
            velocity.x *= -1.0;
        }
        if (coordinate.x > area.getMax().x - offset)
        {
            coordinate.x = area.getMax().x - offset;
            velocity.x *= -1.0;
        }

        // y coordinate
        if (coordinate.y < area.getMin().y + offset)
        {
            coordinate.y = area.getMin().y + offset;
            velocity.y *= -1.0;
        }
        if (coordinate.y > area.getMax().y - offset)
        {
            coordinate.y = area.getMax().y - offset;
            velocity.y *= -1.0;
        }
    }

    /**
     * Procedure performs translation of the atom from one bound to opposite bound within given area
     */
    public void translate(PhysicalArea area, double offset)
    {
        if (!translated)
        {
            // x - coordinate
            if (coordinate.x < area.getMin().x)
            {
                coordinate.x = area.getMax().x + offset;
                translated = true;
            }
            else if (coordinate.x > area.getMax().x)
            {
                coordinate.x = area.getMin().x - offset;
                translated = true;
            }

            // y coordinate
            if (coordinate.y < area.getMin().y)
            {
                coordinate.y = area.getMax().y + offset;
                translated = true;
            }
            else if (coordinate.y > area.getMax().y)
            {
                coordinate.y = area.getMin().y - offset;
                translated = true;
            }
        }
        else if (area.isInside(coordinate))
        {
            translated = false;
        }
    }
}
