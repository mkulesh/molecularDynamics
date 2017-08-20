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

import java.util.ArrayList;

import com.mkulesh.mmd.R;

import android.app.Activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

/**
 * The ImageArrayAdapter is the array adapter used for displaying an additional image to a list preference item.
 */
public class ImageArrayAdapter extends ArrayAdapter<CharSequence>
{
    private int textViewResourceId = 0;
    private ArrayList<Bitmap> images = null;
    private int index = 0;
    private int textColor = android.graphics.Color.TRANSPARENT;

    /**
     * ImageArrayAdapter constructor.
     * 
     * @param context
     *            the context.
     * @param textViewResourceId
     *            resource id of the text view.
     * @param objects
     *            to be displayed.
     * @param imageResourceIds
     *            resource id of the images to be displayed.
     * @param index
     *            index of the previous selected item.
     */
    public ImageArrayAdapter(Context context, int textViewResourceId, CharSequence[] objects, ArrayList<Bitmap> images,
            int index)
    {
        super(context, textViewResourceId, objects);
        this.textViewResourceId = textViewResourceId;
        this.images = images;
        this.index = index;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {

        LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        View row = inflater.inflate(textViewResourceId, parent, false);

        CheckedTextView checkedTextView = (CheckedTextView) row.findViewById(R.id.image_list_item_checkbox);
        if (images != null && images.get(position) != null)
        {
            checkedTextView.setCompoundDrawablesWithIntrinsicBounds(
                    new BitmapDrawable(((Activity) getContext()).getResources(), images.get(position)), null, null,
                    null);
        }
        checkedTextView.setText(getItem(position));
        if (textColor != android.graphics.Color.TRANSPARENT)
        {
            checkedTextView.setTextColor(textColor);
        }
        if (position == index)
        {
            checkedTextView.setChecked(true);
        }

        return row;
    }

    public void setTextColor(int color)
    {
        textColor = color;
    }

}
