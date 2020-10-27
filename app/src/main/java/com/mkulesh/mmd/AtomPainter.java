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

package com.mkulesh.mmd;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mkulesh.mmd.model.Atom;
import com.mkulesh.mmd.model.AtomSet;
import com.mkulesh.mmd.model.Vector2D;
import com.mkulesh.mmd.utils.SVGUtils;
import com.mkulesh.mmd.utils.ThreadContol;
import com.mkulesh.mmd.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Class that implements painting thread
 */
public class AtomPainter implements Runnable
{

    /**
     * Enumeration defining the background for atom panel
     */
    enum BackgroudMode
    {
        NONE,
        WALLPAPER,
        MMD
    }

    // drawing parameters
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean drawVector = false;
    private final Atom.VectorType vectorType = Atom.VectorType.COORDINATE;
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean rotateAtom = true;
    private BackgroudMode backgroudMode = BackgroudMode.NONE;

    // visual components
    private AtomBackground background = null;
    private final AtomPanel atomPanel = new AtomPanel();
    private InfoPanel infoPanel = null;
    private ZoomPanel zoomPanel = null;
    private final Context context;
    private final SurfaceHolder holder;
    private final WallpaperOffsets wallpaperOffsets = new WallpaperOffsets();

    // synchronization with calculation thread
    private final BlockingQueue<AtomSet> waitingQueue;
    private final BlockingQueue<AtomSet> immediateQueue;

    // thread implementation
    private Thread thread = null;
    private final ThreadContol threadControl = new ThreadContol();

    // timing statistic and fps control
    private static final float maxFps = 25.0f;
    private static final int maxLastDurations = 10;
    private final ArrayList<Long> lastDurations = new ArrayList<>();

    private int zoomPanelOffset = 0;

    /**
     * Default constructor
     */
    AtomPainter(Context context, SurfaceHolder holder)
    {
        super();
        this.holder = holder;
        this.context = context;
        waitingQueue = new ArrayBlockingQueue<>(1, true);
        immediateQueue = new ArrayBlockingQueue<>(100, true);
    }

    /**
     * Procedure prepares info panel
     */
    void enableInfoPanel()
    {
        infoPanel = new InfoPanel(context);
        zoomPanel = new ZoomPanel();
        zoomPanelOffset = context.getResources().getDimensionPixelOffset(R.dimen.activity_flb_margin);
    }

    /**
     * Procedure sets the background mode
     */
    void setBackgroundMode()
    {
        boolean useSystemWallpaper = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                SettingsActivity.KEY_SYSTEM_WALLPAPER,
                context.getResources().getBoolean(R.bool.pref_system_wallpaper_default));
        BackgroudMode mode = (useSystemWallpaper) ? AtomPainter.BackgroudMode.WALLPAPER : AtomPainter.BackgroudMode.MMD;
        if (backgroudMode != mode)
        {
            ViewUtils.Debug(this, "setting new background mode " + mode.toString());
            backgroudMode = mode;
            background = null;
        }
    }

    /**
     * Process change of wallpaper offset if app is running as MMDWallpaperEngine
     */
    void wallpaperOffsetsChanged(float xOffset, float yOffset)
    {
        synchronized (wallpaperOffsets)
        {
            wallpaperOffsets.set(xOffset, yOffset);
        }
    }

    /**
     * Procedure starts painting thread
     */
    synchronized void resume()
    {
        if (thread == null)
        {
            thread = new Thread(this, this.getClass().getSimpleName());
            threadControl.setInterrupted(false);
            thread.start();
        }
    }

    /**
     * Procedure interrupts painting thread
     */
    synchronized public void stop()
    {
        if (thread != null)
        {
            threadControl.setInterrupted(true);
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public void run()
    {
        ViewUtils.Debug(this, "(pid: " + Thread.currentThread().getId() + "): started");

        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                synchronized (threadControl)
                {
                    if (threadControl.isInterrupted())
                    {
                        ViewUtils.Debug(this, "(pid: " + Thread.currentThread().getId() + "): interrupted");
                        break;
                    }
                }

                AtomSet a1 = waitingQueue.peek();
                if (a1 != null)
                {
                    draw(a1);
                    waitingQueue.poll();
                }

                AtomSet a2 = immediateQueue.peek();
                if (a2 != null)
                {
                    draw(a2);
                    immediateQueue.poll();
                }
            }
            catch (Exception ex)
            {
                break;
            }
        }

        waitingQueue.clear();
        immediateQueue.clear();
        ViewUtils.Debug(this, "(pid: " + Thread.currentThread().getId() + "): stopped");
    }

    /**
     * Procedure paints atoms field
     */
    private void draw(AtomSet set)
    {
        long startTime = Calendar.getInstance().getTimeInMillis();
        float fps = 0f;

        // checks if the lockCanvas() method will be success,and if not,
        // will check this statement again
        while (!holder.getSurface().isValid()) ;

        // Start editing pixels in this surface
        Canvas c = holder.lockCanvas();

        // paint a background color
        if (backgroudMode != BackgroudMode.NONE)
        {
            if (background == null)
            {
                background = new AtomBackground(context);
            }
            background.draw(c);
        }
        else
        {
            c.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
        }

        synchronized (threadControl)
        {
            if (threadControl.isInterrupted() || set == null)
            {
                holder.unlockCanvasAndPost(c);
                return;
            }
        }

        if (set.getAtoms().isEmpty())
        {
            // if atom set is empty, draw the info panel only
            if (infoPanel != null)
            {
                infoPanel.setRect(0, 0, c.getWidth(), 0);
                infoPanel.draw(c, set, fps);
            }
        }
        else
        {
            // paint atoms and all panels
            atomPanel.setRect(0, 0, c.getWidth(), c.getHeight());
            atomPanel.draw(c, set);

            if (zoomPanel != null)
            {
                float hm = context.getResources().getDimension(R.dimen.activity_horizontal_margin) + zoomPanelOffset;
                float vm = context.getResources().getDimension(R.dimen.activity_vertical_margin) + zoomPanelOffset;
                int width = c.getWidth() / 10;
                int height = c.getHeight() / 10;
                zoomPanel.setRect((int) hm, c.getHeight() - (int) vm - height, (int) hm + width, c.getHeight()
                        - (int) vm);
                zoomPanel.draw(c, set);
            }

            // calculate fps and delay thread is necessary
            {
                long maxStepDuration = (long) (1000f / maxFps);
                long t1 = Calendar.getInstance().getTimeInMillis() - startTime;
                if (t1 < maxStepDuration)
                {
                    try
                    {
                        Thread.sleep(maxStepDuration - t1);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    t1 = Calendar.getInstance().getTimeInMillis() - startTime;
                }
                long duration = Math.max(set.getCalculationTime(), t1);
                long averagedDuration = getAveragedDuration(duration);
                fps = (averagedDuration != 0) ? 1000f / averagedDuration : 0;
            }

            if (infoPanel != null)
            {
                infoPanel.setRect(0, 0, c.getWidth(), 0);
                infoPanel.draw(c, set, fps);
            }
        }

        // End of painting to canvas
        holder.unlockCanvasAndPost(c);
    }

    /**
     * Procedure stores new duration and calculates averaged duration used for fps calculations
     */
    private long getAveragedDuration(long duration)
    {
        long retValue = 0;
        while (lastDurations.size() >= maxLastDurations)
        {
            lastDurations.remove(0);
        }
        lastDurations.add(duration);
        for (Long d : lastDurations)
        {
            retValue += d;
        }
        return retValue / lastDurations.size();
    }

    /**
     * Procedure waits and puts given atom into drawing queue
     */
    void waitAndPut(AtomSet atoms)
    {
        if (atoms == null || thread == null)
        {
            return;
        }
        try
        {
            waitingQueue.put(atoms);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Procedure puts given atom into drawing queue without waiting
     */
    void put(AtomSet atoms)
    {
        if (atoms == null || thread == null)
        {
            return;
        }
        try
        {
            immediateQueue.put(atoms);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Helper basis class for a custom panel
     */
    private class CustomPanel
    {
        final Rect rect = new Rect();

        void setRect(int left, int top, int right, int bottom)
        {
            rect.set(left, top, right, bottom);
        }
    }

    /**
     * Helper class used that holds current wallpaper offset
     */
    private class WallpaperOffsets
    {
        float xOffset = 0;
        float yOffset = 0;

        void set(float xOffset, float yOffset)
        {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
    }

    /**
     * Helper class used to draw background in the live wallpaper mode
     */
    private class AtomBackground extends CustomPanel
    {
        private int canvasWidth = -1, canvasHeight = -1;
        private final Paint paint = new Paint();
        private Bitmap bgBitmap = null;
        private WallpaperManager wallpaper = null;
        private final Rect srcRect = new Rect();
        private final Rect destRect = new Rect();

        AtomBackground(Context context)
        {
            if (backgroudMode == BackgroudMode.WALLPAPER)
            {
                wallpaper = WallpaperManager.getInstance(context);
            }
        }

        private void loadBackground(int width, int height)
        {
            ViewUtils.Debug(this, "loading new background: [" + width + ", " + height + "]");
            Bitmap orig = null;

            // try to get system wallpaper
            if (wallpaper != null)
            {
                try
                {
                    Drawable bgDrawable = wallpaper.getDrawable();
                    if (bgDrawable != null) {
                        orig = Bitmap.createBitmap(bgDrawable.getIntrinsicWidth(), bgDrawable.getIntrinsicHeight(),
                                Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(orig);
                        bgDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        bgDrawable.draw(canvas);
                    }
                }
                catch (Exception ex)
                {
                    ViewUtils.Debug(this, "Cannot open wallpaper image: " + ex.getLocalizedMessage());
                }
            }

            // set MMD background if the system wallpaper is not available
            if (orig == null)
            {
                orig = BitmapFactory.decodeResource(context.getResources(), R.drawable.aluminium_texture);
            }

            if (orig.getWidth() >= width && orig.getHeight() >= height)
            {
                bgBitmap = orig;
            }
            else
            {
                bgBitmap = Bitmap.createScaledBitmap(orig, Math.max(orig.getWidth(), width),
                        Math.max(orig.getHeight(), height), false);
            }
        }

        void draw(Canvas c)
        {
            if (bgBitmap != null)
            {
                if (c.getWidth() != canvasWidth || c.getHeight() != canvasHeight)
                {
                    bgBitmap = null;
                }
            }
            canvasHeight = c.getHeight();
            canvasWidth = c.getWidth();

            if (bgBitmap == null)
            {
                loadBackground(c.getWidth(), c.getHeight());
            }

            synchronized (threadControl)
            {
                if (threadControl.isInterrupted())
                {
                    return;
                }
            }

            final int width = c.getWidth();
            final int height = c.getHeight();
            if (bgBitmap.getWidth() > width)
            {
                float xOffset, yOffset;
                synchronized (wallpaperOffsets)
                {
                    xOffset = wallpaperOffsets.xOffset;
                    yOffset = wallpaperOffsets.yOffset;
                }
                final int x = (int) (xOffset * (bgBitmap.getWidth() - width));
                final int y = (int) (yOffset * (bgBitmap.getHeight() - height));
                srcRect.set(x, y, x + width, y + height);
                destRect.set(0, 0, width, height);
                c.drawBitmap(bgBitmap, srcRect, destRect, paint);
            }
            else
            {
                c.drawBitmap(bgBitmap, 0, 0, paint);
            }
        }
    }

    /**
     * Helper class used to draw atom panel
     */
    private class AtomPanel extends CustomPanel
    {

        private double lastRadius = 0.0;
        private final Vector2D lastDim = new Vector2D();
        private String lastImage = "";
        private Bitmap image = null;
        private final Matrix matrix = new Matrix();
        private final Point p1 = new Point();
        private final Point p2 = new Point();
        private final Paint paint = new Paint();

        AtomPanel()
        {
            // empty
        }

        private void loadImage(AtomSet set, Rect atomsRect)
        {
            ViewUtils.Debug(this, "loading new atom image");

            lastRadius = set.atomRadius;
            lastDim.assign(set.getVewPort().getDim());
            lastImage = set.atomImage;
            int atomSize = Math.min(set.getVewPort().toScreenXLength(2.0 * lastRadius, atomsRect), set.getVewPort()
                    .toScreenYLength(2.0 * lastRadius, atomsRect));
            int imageId = SVGUtils.getResourceIdFromName(lastImage, context, false);
            image = SVGUtils.getFromResource(context.getResources(), imageId, atomSize, atomSize, Config.ARGB_8888);
            if (image == null)
            {
                Bitmap orig = BitmapFactory.decodeResource(context.getResources(), R.drawable.atom_blue_gray);
                image = Bitmap.createScaledBitmap(orig, atomSize, atomSize, false);
            }
        }

        void draw(Canvas c, AtomSet set)
        {
            if (image == null || lastRadius != set.atomRadius || !lastDim.isEqual(set.getVewPort().getDim())
                    || lastImage != set.atomImage)
            {
                loadImage(set, rect);
            }
            for (Atom a : set.getAtoms())
            {

                set.getVewPort().toScreenPoint(a.coordinate, rect, p1);
                Vector2D atomVector = a.getAttribute(vectorType);

                if (drawVector)
                {
                    Vector2D vector = atomVector.normalize(2.0 * lastRadius).sum(a.coordinate);
                    set.getVewPort().toScreenPoint(vector, rect, p2);
                    c.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
                    drawArrowHead(c, p2, p1, 15, paint);
                }

                matrix.reset();
                matrix.setTranslate(p1.x - image.getWidth() / 2, p1.y - image.getHeight() / 2);

                if (rotateAtom)
                {
                    double degrees = Math.toDegrees(Math.atan2(atomVector.y, atomVector.x));
                    matrix.postRotate((float) degrees, p1.x, p1.y);
                }

                c.drawBitmap(image, matrix, paint);
            }
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

    /**
     * Helper class used to draw info panel
     */
    private class InfoPanel extends CustomPanel
    {

        private final Paint paint = new Paint();
        private final LinearLayout layout;

        InfoPanel(Context context)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            final ViewGroup nullRootGroup = null;
            layout = (LinearLayout) inflater.inflate(R.layout.info_panel, nullRootGroup, false);
        }

        void draw(Canvas c, AtomSet set, float fps)
        {
            if (layout.getChildCount() == 4)
            {
                ((TextView) layout.getChildAt(0)).setText("N=" + set.getAtoms().size());
                ((TextView) layout.getChildAt(1)).setText("E=" + String.format("%4.3e", set.eKinetic + set.ePotenz));
                ((TextView) layout.getChildAt(2)).setText("T=" + String.format("%4.3e", set.temperature));
                ((TextView) layout.getChildAt(3)).setText("" + String.format("%.1f fps", fps));
            }
            c.drawBitmap(ViewUtils.drawViewToBitmap(layout, rect), rect.left, rect.top, paint);
        }

    }

    /**
     * Helper class used to draw zoom panel
     */
    private class ZoomPanel extends CustomPanel
    {

        private final Paint paint = new Paint();
        private final Point pMin = new Point();
        private final Point pMax = new Point();
        private final Rect viewPortRect = new Rect();

        ZoomPanel()
        {
            // empty
        }

        void draw(Canvas c, AtomSet set)
        {
            if (!set.getVewPort().isZoomed())
            {
                return;
            }
            set.getArea().toScreenPoint(set.getVewPort().getMin(), rect, pMin);
            set.getArea().toScreenPoint(set.getVewPort().getMax(), rect, pMax);
            viewPortRect.set(pMin.x, pMax.y, pMax.x, pMin.y);
            // fill panel
            paint.setColor(ViewUtils.getThemeColor(context, R.attr.colorPrimaryDark));
            paint.setStyle(Paint.Style.FILL);
            c.drawRect(rect, paint);
            // stroke panel
            paint.setColor(ViewUtils.getThemeColor(context, R.attr.colorInfoText));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            c.drawRect(rect, paint);
            // fill viewport
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setAlpha(150);
            paint.setStrokeWidth(1);
            c.drawRect(viewPortRect, paint);
            // stroke viewport
            paint.setAlpha(0);
            paint.setStyle(Paint.Style.STROKE);
            c.drawRect(viewPortRect, paint);
        }
    }

}
