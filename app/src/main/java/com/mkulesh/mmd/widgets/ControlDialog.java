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

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.mkulesh.mmd.R;
import com.mkulesh.mmd.utils.CompatUtils;

import java.text.DecimalFormat;

public class ControlDialog extends Dialog implements OnSeekBarChangeListener, OnClickListener, OnItemClickListener
{

    private AppCompatActivity context = null;
    private SeekBar progressBar = null;
    private TextView progressText = null;
    private DialogParameters par = null;
    private DialogChangeListener myListener;
    private DecimalFormat decimalFormat = null;

    public ControlDialog(AppCompatActivity context, DialogParameters par, DialogChangeListener listener)
    {
        super(context);
        this.context = context;
        this.par = par;
        this.myListener = listener;

        decimalFormat = CompatUtils.getDecimalFormat(par.valueFormat);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window w = getWindow();
        w.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        w.getAttributes().gravity = par.gravity;
        if ((w.getAttributes().gravity & Gravity.TOP) == Gravity.TOP)
        {
            w.getAttributes().y = par.top;
        }
        setCanceledOnTouchOutside(true);
        if (par.type == DialogParameters.Type.SURFACE_SCHEMA)
        {
            createSurfaceSchemaInterface();
        }
        else if (par.type == DialogParameters.Type.CONDITIONS_CHANGE)
        {
            createButtonInterface();
        }
        else if (par.type == DialogParameters.Type.POTENTIAL_CHANGE)
        {
            createListInterface();
        }
        else
        {
            createSeekBarInterface();
        }
    }

    private void createSurfaceSchemaInterface()
    {
        setContentView(R.layout.surface_touch_schema);
        getWindow().setLayout(par.width, par.height);
        ((Button) findViewById(R.id.button_cancel)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });
    }

    private void createButtonInterface()
    {
        setContentView(R.layout.control_dialog_buttons);
        ((TextView) findViewById(R.id.control_dialog_title)).setText(par.title);
        findViewById(R.id.button_norm_fullenergy).setOnClickListener(this);
        findViewById(R.id.button_norm_temperature).setOnClickListener(this);
        findViewById(R.id.button_norm_none).setOnClickListener(this);
    }

    private void createSeekBarInterface()
    {
        setContentView(R.layout.control_dialog_seekbar);
        ((TextView) findViewById(R.id.control_dialog_title)).setText(par.title);
        LinearLayout layout = (LinearLayout) findViewById(R.id.control_dialog_seekbar_sbarea);
        {
            progressBar = par.isVertical ? new VerticalSeekBar(context) : new SeekBar(context);
            ViewGroup.LayoutParams lp1 = new ViewGroup.LayoutParams(par.width, par.height);
            progressBar.setLayoutParams(lp1);
            progressBar.setMax(100);
            int progress = (int) (100 * (par.selectedValue - par.min) / (par.max - par.min));
            progressBar.setProgress(progress);
            progressBar.setOnSeekBarChangeListener(this);
            progressBar.setThumb(CompatUtils.getDrawable(context, R.drawable.ic_seek_bar));
            progressBar.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(context, R.color.mmd_icons), android.graphics.PorterDuff.Mode.SRC_IN);
            layout.addView(progressBar);
        }
        progressText = (TextView) findViewById(R.id.control_dialog_seekbar_value);
        progressText.setText(decimalFormat.format(par.selectedValue));
    }

    private void createListInterface()
    {
        setContentView(R.layout.control_dialog_list);
        ((TextView) findViewById(R.id.control_dialog_title)).setText(par.title);
        resetListView((int) par.selectedValue);
        ((ListView) findViewById(R.id.control_dialog_listview)).setOnItemClickListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser)
    {
        double selectedValue = DialogParameters.progressToDouble(par.min, par.max, progress);
        progressText.setText(decimalFormat.format(selectedValue));
        if (myListener != null)
        {
            myListener.onSeekBarChange(par.type, selectedValue);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar bar)
    {
        // empty
    }

    @Override
    public void onStopTrackingTouch(SeekBar bar)
    {
        // empty
    }

    @Override
    public void onClick(View v)
    {
        if (myListener != null)
        {
            myListener.onButtonChange(v.getId());
        }
        dismiss();
    }

    private void resetListView(int position)
    {
        String[] potentialNames = context.getResources().getStringArray(R.array.pref_potential_names);
        ImageArrayAdapter adapter = new ImageArrayAdapter(context, R.layout.image_list_item, potentialNames, null,
                position);
        adapter.setTextColor(ContextCompat.getColor(context, R.color.mmd_icons));
        ((ListView) findViewById(R.id.control_dialog_listview)).setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (position != (int) par.selectedValue)
        {
            resetListView(position);
            if (myListener != null)
            {
                myListener.onListItemChange(position);
            }
        }
        dismiss();
    }

}
