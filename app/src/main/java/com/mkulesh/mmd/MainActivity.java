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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.mkulesh.mmd.utils.CompatUtils;
import com.mkulesh.mmd.utils.ViewUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private Toolbar mToolbar = null;
    private DrawerLayout mDrawerLayout = null;
    private NavigationView navigationView = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private Display display = null;
    private Experiment experiment = null;

    /**
     * Dummy array used to avoid lint warning about unused resources
     */
    @SuppressWarnings("unused")
    public final static int[] usedResources = {
            R.raw.drawer_experiment, R.drawable.drawer_experiment,
            R.raw.drawer_potential, R.drawable.drawer_potential,
            R.raw.drawer_documentation, R.drawable.drawer_documentation,
            R.raw.formula_pd_born_mayer, R.raw.formula_pd_lennard_jones, R.raw.formula_pd_morse,
            R.raw.formula_pv_born_mayer, R.raw.formula_pv_lennard_jones, R.raw.formula_pv_morse,
            R.raw.ic_action_settings, R.raw.ic_action_help, R.raw.ic_seek_bar, R.raw.ic_launcher,
            R.raw.convert_file, R.raw.convert, R.raw.convert_single,
            R.raw.ic_boundary1, R.raw.ic_boundary2, R.raw.ic_boundary3,
            R.raw.multitouch_drag, R.raw.multitouch_pinch, R.raw.multitouch_simpletap,
            R.raw.flb_action_play, R.raw.flb_action_stop,
            R.drawable.anonymous_amibe, R.drawable.atom_blue_red, R.drawable.ball_volley_ball,
            R.drawable.atom_gelb, R.drawable.autumn_leaf_01, R.drawable.autumn_leaf_02,
            R.drawable.ball_blue_ball, R.drawable.ball_soccer, R.drawable.ball_tennis,
            R.drawable.ball_waves_sketch, R.drawable.bird_carton_style, R.drawable.drop_water_blue,
            R.drawable.drop_water_green, R.drawable.ic_boundary1, R.drawable.ic_boundary2,
            R.drawable.ic_boundary3, R.drawable.ic_grid_diagonal, R.drawable.ic_grid_square,
            R.drawable.molecule_6atoms, R.drawable.molecule_water, R.drawable.planet_earth_01,
            R.drawable.planet_earth_02, R.drawable.planet_red_planet, R.drawable.planet_saturn,
            R.drawable.snow_flake, R.drawable.ufo_cartoon_style, R.drawable.whirlpool
    };

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        setContentView(R.layout.activity_main);

        PackageManager pm = getPackageManager();
        ViewUtils.Debug(
                this,
                "App started, android version " + Build.VERSION.SDK_INT + ", installation source: "
                        + pm.getInstallerPackageName(getPackageName()));

        // Action bar (v7 compatibility library): use Toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Action bar drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        if (navigationView != null)
        {
            navigationView.setNavigationItemSelectedListener(menuItem -> {
                selectNavigationItem(menuItem);
                return true;
            });
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close)
        {
            public void onDrawerClosed(View view)
            {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView)
            {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        CompatUtils.setDrawerListener(mDrawerLayout, mDrawerToggle);

        if (experiment == null)
        {
            experiment = new Experiment(this);
        }

        if (savedInstanceState == null)
        {
            selectNavigationItem(navigationView.getMenu().getItem(0));
        }
    }

    public Display getDisplay()
    {
        return display;
    }

    public Experiment getExperiment()
    {
        return experiment;
    }

    public void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        return true;
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem)
    {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(menuItem))
        {
            return true;
        }

        BaseFragment baseFragment = getVisibleFragment();
        if (baseFragment == null)
        {
            return true;
        }
        switch (menuItem.getItemId())
        {
        case R.id.action_settings:
        case R.id.action_help:
            baseFragment.performAction(menuItem.getItemId());
            return true;
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(menuItem);
        }
    }

    /*********************************************************
     * Navigation drawer
     *********************************************************/

    @SuppressLint("RestrictedApi")
    private BaseFragment getVisibleFragment()
    {
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments)
        {
            if (fragment != null && fragment.isVisible() && (fragment instanceof BaseFragment))
            {
                return (BaseFragment) fragment;
            }
        }
        return null;
    }

    private void selectNavigationItem(MenuItem menuItem)
    {
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();

        BaseFragment fragment = null;
        switch (menuItem.getItemId())
        {
        case R.id.nav_experiment:
        {
            fragment = new MainFragmentExperiment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            break;
        }
        case R.id.nav_potential:
        {
            fragment = new MainFragmentPotential();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            break;
        }
        case R.id.nav_documentation:
        {
            try
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(getResources().getString(R.string.wiki_about_method)));
                startActivity(intent);
            }
            catch (Exception e)
            {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
            break;
        }
        }

        if (fragment != null)
        {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_content_frame, fragment);
            transaction.commit();
        }
    }

    public void updateFragmentInfo(BaseFragment fragment)
    {
        if (fragment != null)
        {
            mToolbar.setTitle(fragment.getTitleId());
            mToolbar.setSubtitle(fragment.getSubTitleId());
            ViewUtils.Debug(this, "Toolbar title: " + mToolbar.getTitle() + "/" + mToolbar.getSubtitle());
            final int fragmentNumber = fragment.getFragmentNumber();
            if (fragmentNumber != BaseFragment.INVALID_FRAGMENT_ID &&
                    fragmentNumber < navigationView.getMenu().size())
            {
                final MenuItem navMenu = navigationView.getMenu().getItem(fragmentNumber);
                if (navMenu != null && !navMenu.isChecked())
                {
                    navMenu.setChecked(true);
                }
            }
        }
    }

    public void readExperiment()
    {
        if (experiment != null)
        {
            experiment.readParameters(this);
            experiment.updateBackgroundMode(true);
        }
    }
}
