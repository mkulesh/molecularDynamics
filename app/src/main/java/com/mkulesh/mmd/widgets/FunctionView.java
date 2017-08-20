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

import java.text.DecimalFormat;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.PictureDrawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.caverock.androidsvg.SVG;
import com.mkulesh.mmd.R;
import com.mkulesh.mmd.model.PhysicalArea;
import com.mkulesh.mmd.model.Vector2D;
import com.mkulesh.mmd.potentials.BasePotential;
import com.mkulesh.mmd.utils.CompatUtils;

public class FunctionView extends AppCompatTextView
{

    // custom attributes
    private int axisArrowSize, axisLabelSize = 8, labelNumber = 10;
    private String domainName, valueName;
    float formulaWidth = 0.5f;

    // variables used for plotting
    private Context context = null;
    private BasePotential potential = null;
    private BasePotential.ValueType valueType = BasePotential.ValueType.VALUE;
    private PhysicalArea area = new PhysicalArea();
    private Paint axisPaint = new Paint(), plotPaint = new Paint();
    private Rect rect = new Rect(), textBounds = new Rect();
    private Point p1 = new Point(), p2 = new Point();
    private Vector2D vec = new Vector2D();
    private DecimalFormat xDecimalFormat = null, yDecimalFormat = null;
    private SVG svg = null;
    private float svgRatio = 0.0f;
    public static final int pointNumber = 1000;

    public FunctionView(Context context)
    {
        super(context);
        prepare(context, null);
    }

    public FunctionView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        prepare(context, attrs);
    }

    public FunctionView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        prepare(context, attrs);
    }

    private void prepare(Context context, AttributeSet attrs)
    {
        this.context = context;
        int axisColor = android.graphics.Color.BLACK;
        int axisWidth = 2;
        int plotLineColor = android.graphics.Color.RED;
        int plotLineWidth = 5;
        axisArrowSize = 21;
        axisLabelSize = 8;
        if (attrs != null)
        {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PotentialView, 0, 0);
            axisColor = a.getColor(R.styleable.PotentialView_axisColor, axisColor);
            axisWidth = a.getDimensionPixelSize(R.styleable.PotentialView_axisWidth, axisWidth);
            plotLineColor = a.getColor(R.styleable.PotentialView_plotLineColor, plotLineColor);
            plotLineWidth = a.getDimensionPixelSize(R.styleable.PotentialView_plotLineWidth, axisWidth);
            axisArrowSize = a.getDimensionPixelSize(R.styleable.PotentialView_axisArrowSize, axisArrowSize);
            axisLabelSize = a.getDimensionPixelSize(R.styleable.PotentialView_axisLabelSize, axisLabelSize);
            domainName = a.getString(R.styleable.PotentialView_domainName);
            valueName = a.getString(R.styleable.PotentialView_valueName);
            labelNumber = a.getInteger(R.styleable.PotentialView_labelNumber, labelNumber);
            formulaWidth = a.getFloat(R.styleable.PotentialView_formulaWidth, formulaWidth);
            a.recycle();
        }

        axisPaint.setColor(axisColor);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(axisWidth);
        axisPaint.setAntiAlias(true);

        xDecimalFormat = CompatUtils.getDecimalFormat("0.0");
        yDecimalFormat = CompatUtils.getDecimalFormat("0.00");

        plotPaint.setColor(plotLineColor);
        plotPaint.setStyle(Paint.Style.STROKE);
        plotPaint.setStrokeWidth(plotLineWidth);
        plotPaint.setAntiAlias(true);
    }

    public void setPotential(BasePotential potential, BasePotential.ValueType valueType, PhysicalArea area)
    {
        this.potential = potential;
        this.valueType = valueType;
        this.area.assign(area);
        int resId = potential.getFormulaResourceId(context, valueType);
        try
        {
            svg = SVG.getFromResource(context.getResources(), resId);
            svgRatio = svg.getDocumentWidth() / svg.getDocumentHeight();
            svg.setDocumentWidth("100%");
            svg.setDocumentHeight("100%");
        }
        catch (Exception e)
        {
            svg = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (potential == null)
        {
            return;
        }

        rect.set(0 + getPaddingLeft(), 0 + getPaddingTop(), this.getRight() - this.getLeft() - getPaddingRight(),
                this.getBottom() - this.getTop() - getPaddingBottom());

        // Horizontal axe
        {
            vec.set(area.getMin().x, 0.0);
            area.toScreenPoint(vec, rect, p1);
            vec.set(area.getMax().x, 0.0);
            area.toScreenPoint(vec, rect, p2);
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, axisPaint);
            drawArrowHead(canvas, p2, p1, axisArrowSize, axisPaint);
            // labels
            double dx = area.getDim().x / (double) labelNumber;
            for (int i = 1; i < labelNumber; i++)
            {
                double x = (double) i * dx + area.getMin().x;
                vec.set(x, 0.0);
                area.toScreenPoint(vec, rect, p1);
                canvas.drawLine(p1.x, p1.y - axisLabelSize, p1.x, p1.y + axisLabelSize, axisPaint);
                if (Math.abs(x) >= dx / 2)
                {
                    String label = xDecimalFormat.format(x);
                    getPaint().getTextBounds(label, 0, label.length(), textBounds);
                    textBounds.offset(p1.x - textBounds.width() / 2 - 1, p1.y + axisLabelSize + textBounds.width() / 2
                            + 1);
                    canvas.drawText(label, textBounds.left, textBounds.bottom, getPaint());
                }
            }
            // domain name
            if (domainName != null && domainName.length() > 0)
            {
                getPaint().getTextBounds(domainName, 0, domainName.length(), textBounds);
                textBounds.offset(rect.right - textBounds.width() - 3, p1.y + axisArrowSize + textBounds.height());
                canvas.drawText(domainName, textBounds.left, textBounds.bottom, getPaint());
            }
        }

        // Vertical axe
        {
            vec.set(0.0, area.getMin().y);
            area.toScreenPoint(vec, rect, p1);
            vec.set(0.0, area.getMax().y);
            area.toScreenPoint(vec, rect, p2);
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, axisPaint);
            drawArrowHead(canvas, p2, p1, axisArrowSize, axisPaint);
            // labels
            double dy = area.getDim().y / (double) labelNumber;
            for (int i = 1; i < labelNumber; i++)
            {
                double y = (double) i * dy + area.getMin().y;
                vec.set(0.0, y);
                area.toScreenPoint(vec, rect, p1);
                canvas.drawLine(p1.x - axisLabelSize, p1.y, p1.x + axisLabelSize, p1.y, axisPaint);
                if (Math.abs(y) >= dy / 2)
                {
                    String label = yDecimalFormat.format(y);
                    getPaint().getTextBounds(label, 0, label.length(), textBounds);
                    textBounds.offset(p1.x + axisLabelSize + 3, p1.y + textBounds.height() / 2 - 1);
                    canvas.drawText(label, textBounds.left, textBounds.bottom, getPaint());
                }
            }
            // value name
            if (valueName != null && valueName.length() > 0)
            {
                getPaint().getTextBounds(valueName, 0, valueName.length(), textBounds);
                textBounds.offset(p1.x + axisArrowSize, rect.top + textBounds.height() / 2);
                canvas.drawText(valueName, textBounds.left, textBounds.bottom, getPaint());
            }

        }

        // function
        {
            double prevX = 0.0, prevY = 0.0;
            double dx = area.getDim().x / ((double) pointNumber);
            for (int i = 1; i < pointNumber; i++)
            {
                double x = (double) i * dx + area.getMin().x;
                double y = (valueType == BasePotential.ValueType.VALUE) ? potential.getValue(x) : potential
                        .getDerivative(x);
                if (i > 1)
                {
                    vec.set(x, y);
                    area.toScreenPoint(vec, rect, p1);
                    vec.set(prevX, prevY);
                    area.toScreenPoint(vec, rect, p2);
                    boolean isInside = rect.contains(p1.x, p1.y) && rect.contains(p2.x, p2.y)
                            && (p1.x < rect.width() - axisArrowSize && p2.x < rect.width() - axisArrowSize)
                            && (p1.y > axisArrowSize && p2.y > axisArrowSize);
                    if (isInside)
                    {
                        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, plotPaint);
                    }
                }
                prevX = x;
                prevY = y;
            }
        }

        // formula
        if (svg != null)
        {
            int width = (int) ((float) rect.width() * formulaWidth);
            int height = (int) ((float) width / svgRatio);
            canvas.drawBitmap(loadFormula(width, height), rect.right - width, rect.top, axisPaint);
        }

    }

    private Bitmap loadFormula(int width, int height)
    {
        PictureDrawable pictureDrawable = new PictureDrawable(svg.renderToPicture(width, height));
        Bitmap img = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(), pictureDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c1 = new Canvas(img);
        c1.drawPicture(pictureDrawable.getPicture());
        return img;
    }

    private void drawArrowHead(Canvas c, Point tip, Point tail, int size, Paint paint)
    {
        double dy = tip.y - tail.y;
        double dx = tip.x - tail.x;
        double theta = Math.atan2(dy, dx);
        for (int s = size; s > 2; s--)
        {
            double phi = Math.toRadians(s);
            double rho = theta + phi;
            for (int j = 0; j < 2; j++)
            {
                double x = tip.x - s * Math.cos(rho);
                double y = tip.y - s * Math.sin(rho);
                c.drawLine(tip.x, tip.y, (float) x, (float) y, paint);
                rho = theta - phi;
            }
        }
    }

}
