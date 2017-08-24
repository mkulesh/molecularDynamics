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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mkulesh.mmd.utils.CompatUtils;
import com.mkulesh.mmd.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    protected ActionBar actionBar = null;
    private DrawerLayout mDrawerLayout = null;
    private ListView mDrawerList = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private DrawerListAdapter drawerListAdapter = null;
    private Display display = null;
    private Experiment experiment = null;

    /**
     * Dummy array used to avoid lint warning about unused resources
     */
    public final static int [] usedResources = {
            R.drawable.anonymous_amibe, R.drawable.atom_blue_red, R.drawable.ball_volley_ball,
            R.drawable.atom_gelb, R.drawable.autumn_leaf_01, R.drawable.autumn_leaf_02,
            R.drawable.ball_blue_ball, R.drawable.ball_soccer, R.drawable.ball_tennis,
            R.drawable.ball_waves_sketch, R.drawable.bird_carton_style, R.raw.convert,
            R.raw.convert_file, R.drawable.drop_water_blue, R.drawable.drop_water_green,
            R.raw.formula_pd_born_mayer, R.raw.formula_pd_lennard_jones, R.raw.formula_pd_morse,
            R.raw.formula_pv_born_mayer, R.raw.formula_pv_lennard_jones, R.raw.formula_pv_morse,
            R.drawable.ic_boundary1, R.raw.ic_boundary1, R.drawable.ic_boundary2,
            R.raw.ic_boundary2, R.drawable.ic_boundary3, R.raw.ic_boundary3,
            R.drawable.ic_grid_diagonal, R.drawable.ic_grid_square, R.drawable.molecule_6atoms,
            R.raw.ic_launcher, R.drawable.molecule_water, R.raw.multitouch_drag,
            R.raw.multitouch_pinch, R.raw.multitouch_simpletap, R.drawable.planet_earth_01,
            R.drawable.planet_earth_02, R.drawable.planet_red_planet, R.drawable.planet_saturn,
            R.drawable.snow_flake, R.drawable.ufo_cartoon_style, R.drawable.whirlpool,
    };

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PackageManager pm = getPackageManager();
        ViewUtils.Debug(
                this,
                "App started, android version " + Build.VERSION.SDK_INT + ", installation source: "
                        + pm.getInstallerPackageName(getPackageName()));

        // action bar (v7 compatibility library)
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setShowHideAnimationEnabled(true);
        actionBar.setBackgroundDrawable(CompatUtils.getDrawable(this, R.drawable.action_bar_background));
        actionBar.setElevation(3);

        // Action bar drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.main_left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        drawerListAdapter = new DrawerListAdapter(this);
        mDrawerList.setAdapter(drawerListAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open)
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

        display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        if (experiment == null)
        {
            experiment = new Experiment(this);
        }

        if (savedInstanceState == null)
        {
            selectItem(BaseFragment.EXPERIMENT_FRAGMENT_ID);
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
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        BaseFragment baseFragment = getVisibleFragment();

        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(menuItem))
        {
            return true;
        }

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

    public void setTitle(CharSequence name)
    {
        actionBar.setTitle(name);
    }

    public void setSubTitle(CharSequence name)
    {
        actionBar.setSubtitle(name);
    }

    @SuppressLint("RestrictedApi")
    public BaseFragment getVisibleFragment()
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

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            selectItem(position);
        }
    }

    public void selectItem(int position)
    {
        Fragment fragment = null;
        if (position == BaseFragment.EXPERIMENT_FRAGMENT_ID)
        {
            fragment = new MainFragmentExperiment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
        }
        else if (position == BaseFragment.POTENTIAL_FRAGMENT_ID)
        {
            fragment = new MainFragmentPotential();
            Bundle args = new Bundle();
            fragment.setArguments(args);
        }
        else if (position == BaseFragment.ABOUT_METHOD_FRAGMENT_ID)
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
        }

        if (fragment != null)
        {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_content_frame, fragment);
            transaction.commit();
        }

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /**
     * Custom drawer list adapter.
     */
    private final class DrawerListAdapter extends BaseAdapter
    {
        private LayoutInflater layoutInflater;
        private ArrayList<Bitmap> logos = null;
        private CharSequence[] titles = null, subtitles = null;

        public DrawerListAdapter(Context context)
        {
            layoutInflater = LayoutInflater.from(context);
            titles = context.getResources().getStringArray(R.array.activity_titles);
            subtitles = context.getResources().getStringArray(R.array.activity_subtitles);
            String[] imageNames = context.getResources().getStringArray(R.array.activity_logos);
            logos = new ArrayList<Bitmap>(imageNames.length);
            for (int i = 0; i < imageNames.length; i++)
            {
                final String imageName = "drawable/" + imageNames[i];
                final int imageId = context.getResources().getIdentifier(imageName, null, context.getPackageName());
                if (imageId != 0)
                {
                    Bitmap image = BitmapFactory.decodeResource(context.getResources(), imageId);
                    logos.add(image);
                }
                else
                {
                    logos.add(null);
                }
            }
        }

        @Override
        public int getCount()
        {
            return titles.length;
        }

        @Override
        public Object getItem(int position)
        {
            return position;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View inView, ViewGroup parent)
        {
            View view = inView;
            if (view == null)
            {
                view = layoutInflater.inflate(R.layout.activity_drawer_list_item, parent, false);
            }

            // Icon...
            ImageView logo = (ImageView) view.findViewById(R.id.main_drawer_logo);
            if (logos != null)
            {
                logo.setImageDrawable(new BitmapDrawable(getResources(), logos.get(position)));
            }

            // Title...
            TextView title = (TextView) view.findViewById(R.id.main_drawer_title);
            title.setText(titles[position]);

            // Subtitle...
            TextView subtitle = ((TextView) view.findViewById(R.id.main_drawer_subtitle));
            subtitle.setText(subtitles[position]);
            subtitle.setVisibility("".equals(subtitle.getText()) ? View.GONE : View.VISIBLE);

            return view;
        }
    }
}
