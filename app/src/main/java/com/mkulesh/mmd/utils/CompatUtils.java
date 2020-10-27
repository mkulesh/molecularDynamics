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

package com.mkulesh.mmd.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

/**
 * Class collecting version compatibility helper methods
 */
@SuppressLint("NewApi")
public class CompatUtils
{
    private static boolean isMarshMallowOrLater()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @SuppressWarnings("deprecation")
    public static void setDrawerListener(DrawerLayout mDrawerLayout, ActionBarDrawerToggle mDrawerToggle)
    {

        if (isMarshMallowOrLater())
        {
            mDrawerLayout.removeDrawerListener(mDrawerToggle);
            mDrawerLayout.addDrawerListener(mDrawerToggle);
        }
        else
        {
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }
    }

    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(Context context, int icon)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            return context.getResources().getDrawable(icon, context.getTheme());
        }
        else
        {
            return context.getResources().getDrawable(icon);
        }
    }

    /**
     * Procedure creates new dot-separated DecimalFormat
     */
    @SuppressLint("ObsoleteSdkInt")
    public static DecimalFormat getDecimalFormat(String format)
    {
        DecimalFormat df = new DecimalFormat(format);
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
        {
            dfs.setExponentSeparator("e");
        }
        df.setDecimalFormatSymbols(dfs);
        return df;
    }

    public static void setColorFilter(@NonNull Drawable drawable, @ColorInt int color, PorterDuff.Mode mode)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
        }
        else
        {
            drawable.setColorFilter(color, mode);
        }
    }
}
