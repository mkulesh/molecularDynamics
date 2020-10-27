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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mkulesh.mmd.model.Constants.PotentialType;
import com.mkulesh.mmd.model.PhysicalArea;
import com.mkulesh.mmd.model.Potential;
import com.mkulesh.mmd.potentials.BasePotential;
import com.mkulesh.mmd.utils.ViewUtils;
import com.mkulesh.mmd.widgets.ControlDialog;
import com.mkulesh.mmd.widgets.DialogChangeListener;
import com.mkulesh.mmd.widgets.DialogParameters;
import com.mkulesh.mmd.widgets.DialogParameters.Type;
import com.mkulesh.mmd.widgets.PotentialView;

public class MainFragmentPotential extends BaseFragment implements DialogChangeListener
{
    private PotentialType potentialType;
    private DialogParameters potentialChangeDialog = null;

    public MainFragmentPotential()
    {
        // Empty constructor required for fragment subclasses
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewUtils.Debug(this, "onCreateView");

        rootView = inflater.inflate(R.layout.fragment_potential, container, false);
        initializeFragment(POTENTIAL_FRAGMENT_ID);

        // potential change
        potentialChangeDialog = new DialogParameters(DialogParameters.Type.POTENTIAL_CHANGE, true, getResources()
                .getString(R.string.potential_select_dialog));
        potentialChangeDialog.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        potentialChangeDialog.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        potentialChangeDialog.gravity = Gravity.CENTER;

        setPotential(PotentialType.valueOf(preferences.getString(
                SettingsActivity.KEY_POTENTIAL, getResources().getString(R.string.pref_potential_default))));

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_settings).setTitle(activity.getResources().getString(R.string.pref_potential));
        ViewUtils.setMenuIconColor(activity, menu, R.id.action_settings);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ViewUtils.Debug(this, "onResume");
        activity.updateFragmentInfo(this);
    }

    @Override
    public void performAction(int itemId)
    {
        // Handle item selection
        if (itemId == R.id.action_settings)
        {
            ControlDialog d = new ControlDialog(activity, potentialChangeDialog, this);
            d.show();
        }
    }

    @Override
    public int getTitleId()
    {
        return R.string.drawer_potential;
    }

    @Override
    public int getSubTitleId()
    {
        return R.string.drawer_potential_subtitle;
    }

    @Override
    public void onSeekBarChange(Type type, double value)
    {
        // empty
    }

    @Override
    public void onButtonChange(int buttonId)
    {
        // empty
    }

    @Override
    public void onListItemChange(int itemIndex)
    {
        if (itemIndex != potentialType.value() && itemIndex < PotentialType.values().length)
        {
            setPotential(PotentialType.values()[itemIndex]);
        }
    }

    private void setPotential(PotentialType potentialType)
    {
        this.potentialType = potentialType;
        BasePotential potential = Potential.createPotential(potentialType);

        // initialize plots
        PhysicalArea area = new PhysicalArea();
        area.set(0, potential.getThreshold(), -2.0 * Math.abs(potential.getPotentialMin()),
                2.0 * Math.abs(potential.getPotentialMin()));

        // value view
        PotentialView v1 = rootView.findViewById(R.id.activity_potential_value);
        v1.setPotential(potential, BasePotential.ValueType.VALUE, area);
        v1.invalidate();

        // derivative
        PotentialView v2 = rootView.findViewById(R.id.activity_potential_derivative);
        v2.setPotential(potential, BasePotential.ValueType.DERIVATIVE, area);
        v2.invalidate();

        // visual components
        potentialChangeDialog.selectedValue = potentialType.value();
        ((TextView) rootView.findViewById(R.id.activity_potential_name)).setText(potential.getName());

        // store new value
        SharedPreferences.Editor prefEditor = preferences.edit();
        prefEditor.putString(SettingsActivity.KEY_POTENTIAL, potentialType.toString());
        prefEditor.commit();
    }
}
