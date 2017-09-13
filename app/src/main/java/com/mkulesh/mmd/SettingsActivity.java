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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.mkulesh.mmd.model.Constants.PotentialType;
import com.mkulesh.mmd.model.Potential;
import com.mkulesh.mmd.model.Vector2D;
import com.mkulesh.mmd.potentials.BasePotential;
import com.mkulesh.mmd.utils.CompatUtils;

import java.text.DecimalFormat;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On handset devices, settings are presented
 * as a single list. On tablets, settings are split by category, with category headers shown to the left of the list of
 * settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android Design: Settings</a> for design
 * guidelines and the <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings API Guide</a> for
 * more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity
{

    // Initial layout
    public static final String KEY_REBUILD = "pref_rebuild";
    public static final String KEY_FILL_METHODS = "pref_fill_methods";
    public static final String KEY_GRID_HOR_DIMENSION = "pref_grid_hor_dimension";
    public static final String KEY_GRID_VER_DIMENSION = "pref_grid_ver_dimension";

    // Physical area
    public static final String KEY_AREA_MIN_X = "pref_area_min_x";
    public static final String KEY_AREA_MAX_X = "pref_area_max_x";
    public static final String KEY_AREA_MIN_Y = "pref_area_min_y";
    public static final String KEY_AREA_MAX_Y = "pref_area_max_y";
    public static final String KEY_AREA_ZOOM = "pref_area_zoom";
    public static final String KEY_AREA_SCROLL_X = "pref_area_scroll_x";
    public static final String KEY_AREA_SCROLL_Y = "pref_area_scroll_y";

    // Initial and boundary condition
    public static final String KEY_BOUND_GRAVITY = "pref_bound_gravity";
    public static final String KEY_BOUND_THERMAL_CHANGE = "pref_bound_thermal_change";
    public static final String KEY_INIT_TEMPERATURE = "pref_init_temperature";
    public static final String KEY_ENERGY_NORM = "pref_energy_norm";

    // Atom parameters
    public static final String KEY_POTENTIAL = "pref_potential";
    public static final String KEY_ATOM_RADIUS = "pref_atom_radius";
    public static final String KEY_ATOM_MASS = "pref_atom_mass";
    public static final String KEY_ATOM_SKIN = "pref_atom_skin";

    // Calculation parameters
    public static final String KEY_CALC_TIME_STEP = "pref_calc_time_step";

    // Design
    public static final String KEY_SYSTEM_WALLPAPER = "pref_system_wallpaper";

    private double potentialOptDistance = 0.0;
    private DecimalFormat decimalFormat = null;
    boolean areaUpdateEnabled = false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        // Prepare action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(CompatUtils.getDrawable(this, R.drawable.action_bar_background));
            actionBar.setElevation(3);
            actionBar.setTitle(R.string.action_settings);
        }

        // get extra parameters
        PotentialType potentialType = PotentialType.valueOf(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_POTENTIAL, getResources().getString(R.string.pref_potential_default)));
        BasePotential potential = Potential.createPotential(potentialType);
        potentialOptDistance = potential.getOptDistance();
        decimalFormat = CompatUtils.getDecimalFormat("0.00");

        // set custom listener
        bindPreferenceSummaryToValue(KEY_GRID_HOR_DIMENSION);
        bindPreferenceSummaryToValue(KEY_GRID_VER_DIMENSION);

        bindPreferenceSummaryToValue(KEY_AREA_MIN_X);
        bindPreferenceSummaryToValue(KEY_AREA_MAX_X);
        bindPreferenceSummaryToValue(KEY_AREA_MIN_Y);
        bindPreferenceSummaryToValue(KEY_AREA_MAX_Y);
        bindPreferenceSummaryToValue(KEY_AREA_ZOOM);

        bindPreferenceSummaryToValue(KEY_BOUND_GRAVITY);
        bindPreferenceSummaryToValue(KEY_BOUND_THERMAL_CHANGE);
        bindPreferenceSummaryToValue(KEY_INIT_TEMPERATURE);
        bindPreferenceSummaryToValue(KEY_ENERGY_NORM);

        bindPreferenceSummaryToValue(KEY_POTENTIAL);
        bindPreferenceSummaryToValue(KEY_ATOM_RADIUS);
        bindPreferenceSummaryToValue(KEY_ATOM_MASS);
        bindPreferenceSummaryToValue(KEY_ATOM_SKIN);

        bindPreferenceSummaryToValue(KEY_CALC_TIME_STEP);
        // the method list must be initialized after number field
        // in order to update their visibility
        bindPreferenceSummaryToValue(KEY_FILL_METHODS);
        areaUpdateEnabled = true;
    }

    /**
     * Custom change listener
     */
    private Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener()
    {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value)
        {
            String stringValue = value.toString();

            if (preference instanceof ListPreference)
            {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            }
            else
            {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                if (!checkDoubleValue(preference, stringValue))
                {
                    return false;
                }
                preference.setSummary(stringValue);
            }
            setDependentValue(preference, stringValue);
            return true;
        }
    };

    /**
     * Procedure checks that the given string contains an allowed double value
     */
    private boolean checkDoubleValue(Preference preference, String stringValue)
    {
        if (stringValue == null || stringValue.length() == 0)
        {
            return false;
        }
        double dValue = 0.0;
        try
        {
            dValue = Double.parseDouble(stringValue);
        }
        catch (Exception ex)
        {
            return false;
        }
        if (preference.getKey().equals(KEY_INIT_TEMPERATURE))
        {
            if (dValue < 0.0)
            {
                return false;
            }
        }
        if (preference.getKey().equals(KEY_AREA_ZOOM))
        {
            double min = Double.parseDouble(getResources().getString(R.string.pref_area_zoom_min));
            double max = Double.parseDouble(getResources().getString(R.string.pref_area_zoom_max));
            if (dValue < min || dValue > max)
            {
                return false;
            }
        }
        if (preference.getKey().equals(KEY_BOUND_GRAVITY))
        {
            double min = Double.parseDouble(getResources().getString(R.string.pref_bound_gravity_min));
            double max = Double.parseDouble(getResources().getString(R.string.pref_bound_gravity_max));
            if (dValue < min || dValue > max)
            {
                return false;
            }
        }
        if (preference.getKey().equals(KEY_BOUND_THERMAL_CHANGE))
        {
            double min = Double.parseDouble(getResources().getString(R.string.pref_bound_thermal_change_min));
            double max = Double.parseDouble(getResources().getString(R.string.pref_bound_thermal_change_max));
            if (dValue < min || dValue > max)
            {
                return false;
            }
        }
        if (preference.getKey().equals(KEY_CALC_TIME_STEP))
        {
            double min = Double.parseDouble(getResources().getString(R.string.pref_calc_time_step_min));
            double max = Double.parseDouble(getResources().getString(R.string.pref_calc_time_step_max));
            if (dValue < min || dValue > max)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Procedure sets default value into physical area parameters depending on grid dimension
     */
    private void setDependentValue(Preference preference, String stringValue)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        if (!sharedPref.getBoolean(KEY_REBUILD, false) || !areaUpdateEnabled)
        {
            return;
        }
        if (preference.getKey().equals(KEY_GRID_HOR_DIMENSION) && preference.isEnabled())
        {
            int n = Integer.parseInt(stringValue) + 1;
            Vector2D dimv = new Vector2D(n, n).prod(potentialOptDistance);
            Vector2D min = dimv.prod(-1.0 / 2.0);
            Vector2D max = dimv.prod(1.0 / 2.0);
            updateAreaPref(sharedPref, KEY_AREA_MIN_X, decimalFormat.format(min.x));
            updateAreaPref(sharedPref, KEY_AREA_MAX_X, decimalFormat.format(max.x));
        }
        if (preference.getKey().equals(KEY_GRID_VER_DIMENSION) && preference.isEnabled())
        {
            int n = Integer.parseInt(stringValue) + 1;
            Vector2D dimv = new Vector2D(n, n).prod(potentialOptDistance);
            Vector2D min = dimv.prod(-1.0 / 2.0);
            Vector2D max = dimv.prod(1.0 / 2.0);
            updateAreaPref(sharedPref, KEY_AREA_MIN_Y, decimalFormat.format(min.y));
            updateAreaPref(sharedPref, KEY_AREA_MAX_Y, decimalFormat.format(max.y));
        }
    }

    @SuppressWarnings("deprecation")
    private void updateAreaPref(SharedPreferences sharedPref, String key, String value)
    {
        Preference pref = findPreference(key);
        if (pref != null)
        {
            SharedPreferences.Editor prefEditor = sharedPref.edit();
            prefEditor.putString(key, value);
            prefEditor.commit();
            ((EditTextPreference) pref).setText(value);
            ((EditTextPreference) pref).setSummary(value);
        }
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is changed, its summary
     * (line of text below the preference title) is updated to reflect the value. The summary is also immediately
     * updated upon calling this method. The exact display format is dependent on the type of preference.
     *
     * @see #changeListener
     */
    @SuppressWarnings("deprecation")
    private void bindPreferenceSummaryToValue(String prefStr)
    {
        Preference pref = findPreference(prefStr);
        if (pref != null)
        {
            // Set the listener to watch for value changes.
            pref.setOnPreferenceChangeListener(changeListener);
            // Trigger the listener immediately with the preference's
            // current value.
            changeListener.onPreferenceChange(pref, PreferenceManager.getDefaultSharedPreferences(pref.getContext())
                    .getString(pref.getKey(), ""));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Procedure returns index of current ListPreference property
     */
    public static int getListPreferenceIndex(Context context, String key, int defValueId, int valuesArrayId)
    {
        int retValue = -1;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String value = pref.getString(key, context.getResources().getString(defValueId));
        TypedArray values = context.getResources().obtainTypedArray(valuesArrayId);
        for (int idx = 0; idx < values.length(); idx++)
        {
            if (values.getString(idx) != null && value != null && value.equals(values.getString(idx)))
            {
                retValue = idx;
                break;
            }
        }
        values.recycle();
        return retValue;
    }
}
