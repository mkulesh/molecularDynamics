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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.mkulesh.mmd.R;

/**
 * This is a custom vertical seek bar that works with Marshmallow operating system
 * See http://stackoverflow.com/questions/33112277/android-6-0-marshmallow-stops-showing-vertical-seekbar-thumb
 * <p>
 * Code source: https://github.com/chaviw/VerticalSeekBar
 */
public class VerticalSeekBar extends AppCompatSeekBar
{
    private Drawable customThumb;

    public VerticalSeekBar(Context context)
    {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr)
    {
        TypedArray customAttr = context.obtainStyledAttributes(attrs,
                R.styleable.VerticalSeekBar,
                defStyleAttr,
                0);

        Drawable customThumb = customAttr.getDrawable(R.styleable.VerticalSeekBar_customThumb);
        setCustomThumb(customThumb);

        customAttr.recycle();
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
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!isEnabled())
        {
            return false;
        }

        switch (event.getAction())
        {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
        case MotionEvent.ACTION_UP:
            setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
            onSizeChanged(getWidth(), getHeight(), 0, 0);
            break;

        case MotionEvent.ACTION_CANCEL:
            break;
        }
        return true;
    }

    protected void onDraw(Canvas c)
    {
        c.rotate(-90);
        c.translate(-getHeight(), 0);
        drawThumb(c); //redrawing thumb

        super.onDraw(c);
    }

    private void drawThumb(Canvas canvas)
    {
        Drawable customThumb = getCustomThumb();

        if (customThumb != null)
        {
            int available = getHeight() - getPaddingTop() - getPaddingBottom();
            final int thumbWidth = customThumb.getIntrinsicWidth();
            available -= thumbWidth;
            // The extra space for the thumb to move on the track
            available += getThumbOffset() * 2;

            int thumbPos = (int) (getScale() * available + 0.5f);

            final int top, bottom;
            if (getThumbOffset() == Integer.MIN_VALUE)
            {
                final Rect oldBounds = customThumb.getBounds();
                top = oldBounds.top;
                bottom = oldBounds.bottom;
            }
            else
            {
                top = 0;
                bottom = customThumb.getIntrinsicHeight();
            }
            final int left = thumbPos;
            final int right = left + thumbWidth;

            Rect thumbBounds = customThumb.getBounds();
            customThumb.setBounds(left, top, right, bottom);

            canvas.save();
            canvas.rotate(90, thumbBounds.exactCenterX(), thumbBounds.exactCenterY());
            customThumb.draw(canvas);
            canvas.restore();
        }
    }

    private float getScale()
    {
        final int max = getMax();
        return max > 0 ? getProgress() / (float) max : 0;
    }

    public Drawable getCustomThumb()
    {
        return customThumb;
    }

    public void setCustomThumb(Drawable customThumb)
    {
        this.customThumb = customThumb;
        invalidate();
    }
}
