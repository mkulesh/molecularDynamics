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

package com.mkulesh.mmd.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.PictureDrawable;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

public class SVGUtils
{

    /**
     * Procedure returns resource id for given name
     */
    public static int getResourceIdFromName(String name, Context context, boolean asDrawable)
    {
        String imageName;
        try
        {
            imageName = name.substring(name.indexOf('/') + 1, name.lastIndexOf('.'));
        }
        catch (Exception e)
        {
            return 0;
        }
        if (asDrawable)
        {
            imageName = imageName.replaceFirst("raw", "drawable");
        }
        return context.getResources().getIdentifier(imageName, null, context.getPackageName());
    }

    /**
     * Procedure converts a SVG resource into bitmap
     */
    public static Bitmap getFromResource(Resources resources, int resourceId, int width, int height, Bitmap.Config cfg)
    {
        Bitmap img;
        try
        {
            SVG svg = SVG.getFromResource(resources, resourceId);
            // svg.setDocumentViewBox(0, 0, 48, 48);
            svg.setDocumentWidth("100%");
            svg.setDocumentHeight("100%");
            PictureDrawable pictureDrawable = new PictureDrawable(svg.renderToPicture(width, height));
            img = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(), pictureDrawable.getIntrinsicHeight(), cfg);
            Canvas c1 = new Canvas(img);
            c1.drawPicture(pictureDrawable.getPicture());
        }
        catch (SVGParseException e)
        {
            img = null;
        }
        return img;
    }

}
