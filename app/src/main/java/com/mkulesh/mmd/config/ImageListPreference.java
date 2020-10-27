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

package com.mkulesh.mmd.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.mkulesh.mmd.R;
import com.mkulesh.mmd.utils.SVGUtils;
import com.mkulesh.mmd.widgets.ImageArrayAdapter;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

/**
 * The ImageListPreference class responsible for displaying an image for each item within the list.
 */
public class ImageListPreference extends ListPreference implements Preference.OnPreferenceClickListener, AdapterView.OnItemClickListener
{
    private final ArrayList<Bitmap> images;
    private AlertDialog dialog = null;

    /**
     * Constructor of the ImageListPreference. Initializes the custom images.
     *
     * @param context application context.
     * @param attrs   custom xml attributes.
     */
    public ImageListPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        images = getImages(context, attrs);
        setOnPreferenceClickListener(this);
    }

    @Override
    protected void onClick()
    {
        // disable standard processing in order to process click in the onPreferenceClick method
    }

    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        final FrameLayout frameView = new FrameLayout(getContext());
        LayoutInflater.from(getContext()).inflate(R.layout.image_list_preference, frameView);

        final ListView list = frameView.findViewById(R.id.image_list);
        list.setOnItemClickListener(this);
        list.setAdapter(new ImageArrayAdapter(getContext(), R.layout.image_list_item, getEntries(), images, getValueIndex()));

        dialog = new AlertDialog.Builder(getContext())
                .setTitle(preference.getTitle())
                .setView(frameView)
                .setCancelable(true)
                .setPositiveButton(getPositiveButtonText(), (dialog1, which) -> dialog1.dismiss())
                .create();

        dialog.show();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (position < getEntryValues().length)
        {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor prefEditor = preferences.edit();
            final String newValue = getEntryValues()[position].toString();
            prefEditor.putString(getKey(), newValue);
            prefEditor.commit();
            if (dialog != null)
            {
                dialog.dismiss();
            }
            setValue(newValue);
            if (getOnPreferenceChangeListener() != null)
            {
                getOnPreferenceChangeListener().onPreferenceChange(this, newValue);
            }
        }
    }

    private ArrayList<Bitmap> getImages(Context context, AttributeSet attrs)
    {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageListPreference);
        String[] names = context.getResources().getStringArray(
                typedArray.getResourceId(typedArray.getIndexCount() - 1, -1));

        final ArrayList<Bitmap> objects = new ArrayList<>(names.length);
        for (String imageName : names)
        {
            int imageId = SVGUtils.getResourceIdFromName(imageName, context, true);
            if (imageId != 0)
            {
                Bitmap atomImg = BitmapFactory.decodeResource(context.getResources(), imageId);
                objects.add(atomImg);
            }
            else
            {
                objects.add(null);
            }
        }
        typedArray.recycle();
        return objects;
    }

    private int getValueIndex()
    {
        for (int i = 0; i < getEntryValues().length; i++)
        {
            if (getEntryValues()[i].toString().equals(getValue()))
            {
                return i;
            }
        }
        return 0;
    }
}
