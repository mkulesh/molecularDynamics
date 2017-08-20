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

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.MotionEvent;

public class VerticalSeekBar extends AppCompatSeekBar
{

    private OnSeekBarChangeListener myListener;

    public VerticalSeekBar(Context context)
    {
        super(context);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener mListener)
    {
        this.myListener = mListener;
    }

    protected void onDraw(Canvas c)
    {
        c.rotate(-90);
        c.translate(-getHeight(), 0);
        super.onDraw(c);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!isEnabled())
        {
            return false;
        }

        switch (event.getAction())
        {
        case MotionEvent.ACTION_DOWN:
            if (myListener != null)
            {
                myListener.onStartTrackingTouch(this);
            }
            break;
        case MotionEvent.ACTION_MOVE:
            int p = getMax() - (int) (getMax() * event.getY() / getHeight());
            if (p < 0)
                p = 0;
            if (p > getMax())
                p = getMax();
            if (p != getProgress())
            {
                setProgress(p);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                if (myListener != null)
                {
                    myListener.onProgressChanged(this, p, true);
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            if (myListener != null)
            {
                myListener.onStopTrackingTouch(this);
            }
            break;

        case MotionEvent.ACTION_CANCEL:
            break;
        }
        return true;
    }
}
