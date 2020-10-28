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

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mkulesh.mmd.utils.ViewUtils;

public class FloatingButtonsSet extends LinearLayout implements OnLongClickListener
{

    public FloatingButtonsSet(Context context)
    {
        super(context);
        prepare();
    }

    public FloatingButtonsSet(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        prepare();
    }

    private void prepare()
    {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.RIGHT);
    }

    @Override
    public boolean onLongClick(View b)
    {
        if (b instanceof FloatingActionButton)
        {
            return ViewUtils.showButtonDescription(getContext(), b);
        }
        return false;
    }

    public void activate(int id, OnClickListener handler)
    {
        for (int i = 0; i < getChildCount(); i++)
        {
            final View v = getChildAt(i);
            if (id == v.getId())
            {
                v.setVisibility(View.VISIBLE);
                v.setOnLongClickListener(this);
                v.setOnClickListener(handler);
                if (v instanceof FloatingActionButton)
                {
                    ViewUtils.setMenuIconColor(getContext(), (FloatingActionButton) v);
                }
            }
            else
            {
                v.setVisibility(View.GONE);
                v.setOnLongClickListener(null);
                v.setOnClickListener(null);
            }
        }
    }
}
