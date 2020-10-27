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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.graphics.drawable.DrawableCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.mkulesh.mmd.R;

public class ViewUtils
{
    @SuppressWarnings({"unused", "EmptyMethod"})
    public static void Debug(Object o, String text)
    {
        //Log.d("mmdLogs", o.getClass().getSimpleName() + ": " + text + ".");
    }

    /**
     * Procedure converts a given view into bitmap
     */
    public static Bitmap drawViewToBitmap(View view, Rect infoRect)
    {
        // measure the original view size
        int measureWidth = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(measureWidth, measuredHeight);

        // re-measure in order to fill given area
        measureWidth = View.MeasureSpec.makeMeasureSpec(infoRect.width(), View.MeasureSpec.EXACTLY);
        measuredHeight = View.MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(), View.MeasureSpec.EXACTLY);
        view.measure(measureWidth, measuredHeight);

        // re-seze layout
        int bitmapWidth = view.getMeasuredWidth();
        int bitmapHeight = view.getMeasuredHeight();
        view.layout(0, 0, bitmapWidth, bitmapHeight);

        // create and draw image
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            // does not have background drawable, then draw white background
            // on
            // the canvas
            canvas.drawColor(android.graphics.Color.TRANSPARENT);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Procedure hows toast that contains description of the given button
     */
    @SuppressLint("RtlHardcoded")
    public static boolean showButtonDescription(Context context, View button)
    {
        CharSequence contentDesc = button.getContentDescription();
        if (contentDesc != null && contentDesc.length() > 0)
        {
            int[] pos = new int[2];
            button.getLocationOnScreen(pos);

            Toast t = Toast.makeText(context, contentDesc, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP | Gravity.LEFT, 0, 0);
            t.getView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            final int x = pos[0] + button.getMeasuredWidth() / 2 - (t.getView().getMeasuredWidth() / 2);
            final int y = pos[1] - button.getMeasuredHeight() / 2 - t.getView().getMeasuredHeight()
                    - context.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
            t.setGravity(Gravity.TOP | Gravity.LEFT, x, y);
            t.show();
            return true;
        }
        return false;
    }

    public static void setMenuIconColor(Context context, Menu menu, int actionId)
    {
        Drawable drawable = menu.findItem(actionId).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ViewUtils.getThemeColor(context, R.attr.colorInfoText));
        menu.findItem(actionId).setIcon(drawable);
    }

    public static void setMenuIconColor(Context context, FloatingActionButton menu)
    {
        Drawable drawable = menu.getDrawable();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ViewUtils.getThemeColor(context, R.attr.colorInfoText));
        menu.setImageDrawable(drawable);
    }

    public static int getThemeColor (final Context context, int resId)
    {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (resId, value, true);
        return value.data;
    }
}
