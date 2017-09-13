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
package com.mkulesh.mmd.widgets;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.ListAdapter;

import com.mkulesh.mmd.R;
import com.mkulesh.mmd.utils.SVGUtils;

import java.util.ArrayList;

/**
 * The ImageListPreference class responsible for displaying an image for each item within the list.
 */
public class ImageListPreference extends ListPreference
{
    private ArrayList<Bitmap> images = null;

    /**
     * Constructor of the ImageListPreference. Initializes the custom images.
     *
     * @param context application context.
     * @param attrs   custom xml attributes.
     */
    public ImageListPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageListPreference);
        String[] imageNames = context.getResources().getStringArray(
                typedArray.getResourceId(typedArray.getIndexCount() - 1, -1));

        images = new ArrayList<Bitmap>(imageNames.length);
        for (int i = 0; i < imageNames.length; i++)
        {
            int imageId = SVGUtils.getResourceIdFromName(imageNames[i], context, true);
            if (imageId != 0)
            {
                Bitmap atomImg = BitmapFactory.decodeResource(context.getResources(), imageId);
                images.add(atomImg);
            }
            else
            {
                images.add(null);
            }
        }

        typedArray.recycle();
    }

    /**
     * {@inheritDoc}
     */
    protected void onPrepareDialogBuilder(Builder builder)
    {
        int index = findIndexOfValue(getSharedPreferences().getString(getKey(), "1"));
        ListAdapter listAdapter = new ImageArrayAdapter(getContext(), R.layout.image_list_item, getEntries(), images,
                index);
        builder.setAdapter(listAdapter, this);
        super.onPrepareDialogBuilder(builder);
    }
}
