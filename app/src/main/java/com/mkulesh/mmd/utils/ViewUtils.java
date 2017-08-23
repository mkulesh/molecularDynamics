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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mkulesh.mmd.R;

public class ViewUtils
{
    public static void Debug(Object o, String text)
    {
        //Log.d("mmdLogs", o.getClass().getSimpleName() + ": " + text + ".");
    }

    /**
     * Procedure returns the index of given view within given layout
     */
    public static int getViewIndex(LinearLayout layout, View v)
    {
        int textIndex = -1;
        for (int i = 0; i < layout.getChildCount(); i++)
        {
            if (layout.getChildAt(i) == v)
            {
                textIndex = i;
                break;
            }
        }
        return textIndex;
    }

    /**
     * Procedure returns an array of formatted values
     */
    public static String[] catValues(double[] values, int strMaxLength, int decMaxLength, int expMaxLength)
    {
        String[] strValues = new String[values.length];
        Set<String> trial = new HashSet<String>();

        // First run: try to find suitable simple decimal format
        // Second run: we shall use exponential format to ensure given maximum length
        for (int run = 0; run < 2; run++)
        {
            final int maxLength = (run == 0) ? decMaxLength : expMaxLength;
            for (int pos = 0; pos <= maxLength; pos++)
            {
                String format = (pos < 1) ? "0" : "0.";
                for (int k = 0; k < pos; k++)
                {
                    format += "0";
                }
                if (run == 1)
                {
                    format += "E0";
                }
                final DecimalFormat df = CompatUtils.getDecimalFormat(format);
                trial.clear();
                boolean hasDuplicate = false;
                int trialLength = 0;
                for (int i = 0; i < values.length; i++)
                {
                    String fValue = (values[i] != 0.0) ? df.format(values[i]) : "0";
                    if (fValue != null && (format.equals(fValue) || ("-" + format).equals(fValue)))
                    {
                        fValue = "0";
                    }
                    strValues[i] = fValue;
                    if (!trial.add(fValue))
                    {
                        hasDuplicate = true;
                    }
                    trialLength = Math.max(trialLength, fValue.length());
                }
                if (!hasDuplicate && trialLength <= strMaxLength)
                {
                    return strValues;
                }
            }
        }
        return strValues;
    }

    /**
     * Procedure checks duplicate elements
     */
    public static boolean checkDuplicate(String[] input)
    {
        Set<String> tempSet = new HashSet<String>();
        for (String str : input)
        {
            if (!tempSet.add(str))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Procedure rounds the given value to the given number of significant digits see
     * http://stackoverflow.com/questions/202302
     * 
     * Note: The maximum double value in Java is on the order of 10^308, while the minimum value is on the order of
     * 10^-324. Therefore, you can run into trouble when applying the function roundToSignificantFigures to something
     * that's within a few powers of ten of Double.MIN_VALUE.
     * 
     * Consequently, the variable magnitude may become Infinity, and it's all garbage from then on out. Fortunately,
     * this is not an insurmountable problem: it is only the factor magnitude that's overflowing. What really matters is
     * the product num * magnitude, and that does not overflow. One way of resolving this is by breaking up the
     * multiplication by the factor magintude into two steps.
     */
    public static double roundToNumberOfSignificantDigits(double num, int n)
    {
        final double maxPowerOfTen = Math.floor(Math.log10(Double.MAX_VALUE));

        if (num == 0)
        {
            return 0;
        }

        try
        {
            return new BigDecimal(num).round(new MathContext(n, RoundingMode.HALF_EVEN)).doubleValue();
        }
        catch (ArithmeticException ex)
        {
            // nothing to do
        }

        final double d = Math.ceil(Math.log10(num < 0 ? -num : num));
        final int power = n - (int) d;

        double firstMagnitudeFactor = 1.0;
        double secondMagnitudeFactor = 1.0;
        if (power > maxPowerOfTen)
        {
            firstMagnitudeFactor = Math.pow(10.0, maxPowerOfTen);
            secondMagnitudeFactor = Math.pow(10.0, (double) power - maxPowerOfTen);
        }
        else
        {
            firstMagnitudeFactor = Math.pow(10.0, (double) power);
        }

        double toBeRounded = num * firstMagnitudeFactor;
        toBeRounded *= secondMagnitudeFactor;

        final long shifted = Math.round(toBeRounded);
        double rounded = ((double) shifted) / firstMagnitudeFactor;
        rounded /= secondMagnitudeFactor;
        return rounded;
    }

    /**
     * Procedure converts DP to pixels
     */
    public static int dpToPx(DisplayMetrics displayMetrics, int dp)
    {
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    /**
     * Procedure converts pixels tp DP
     */
    public static int pxToDp(DisplayMetrics displayMetrics, int px)
    {
        return (int) ((px / displayMetrics.density) + 0.5);
    }

    /**
     * Procedure collects all components from the given layout recursively
     */
    public static void collectElemets(LinearLayout layout, ArrayList<View> out)
    {
        for (int k = 0; k < layout.getChildCount(); k++)
        {
            View v = layout.getChildAt(k);
            out.add(v);
            if (v instanceof LinearLayout)
            {
                collectElemets((LinearLayout) v, out);
            }
        }
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
}
