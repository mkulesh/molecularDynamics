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

package com.mkulesh.mmd.widgets;

public interface DialogChangeListener
{

    /**
     * Procedure will be called if a SeekBar is changed
     */
    void onSeekBarChange(DialogParameters.Type type, double value);

    /**
     * Procedure will be called if a button is pressed
     */
    void onButtonChange(int buttonId);

    /**
     * Procedure will be called if a new list item is selected
     */
    void onListItemChange(int itemIndex);

}
