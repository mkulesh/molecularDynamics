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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.mkulesh.mmd.utils.ViewUtils;

public abstract class MmdActivity extends ActionBarActivity implements OnNavigationListener
{

    protected int navigationItemIndex;
    protected ActionBar actionBar = null;
    protected Display display = null;

    protected abstract int getContentLayoutId();

    @SuppressLint("Recycle")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        ViewUtils.Debug(this, "App started, current rotation = " + display.getRotation());

        setContentView(getContentLayoutId());

        // action bar (v7 compatibility library)
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        TypedArray titles = getResources().obtainTypedArray(R.array.activity_titles);
        TypedArray subtitles = getResources().obtainTypedArray(R.array.activity_subtitles);
        NavigationListAdapter navigationListApdater = new NavigationListAdapter(this, null, titles, subtitles);
        actionBar.setListNavigationCallbacks(navigationListApdater, this);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getSupportActionBar().setSelectedNavigationItem(navigationItemIndex);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem p_item)
    {
        switch (p_item.getItemId())
        {
        case R.id.action_exit:
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(p_item);
        }
    }

    /**
     * Get navigation list index for current activity.
     */
    protected int getCurrentNavigationItem(Activity p_activity, TypedArray p_titles)
    {
        String title = p_activity.getTitle().toString();
        int position = 0;

        for (int i = 0, n = p_titles.length(); i < n; i++)
        {
            if (p_titles.getString(i).equals(title))
            {
                position = i;
                break;
            }
        }
        return position;
    }

    @Override
    public boolean onNavigationItemSelected(int p_itemPosition, long p_itemId)
    {
        if (p_itemPosition == navigationItemIndex)
        {
            return true;
        }

        Intent intent = null;
        if (p_itemPosition == 0)
        {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        else if (p_itemPosition == 1)
        {
            intent = new Intent(this, PotentialActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        else if (p_itemPosition == 2)
        {
            intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse(getResources().getString(R.string.wiki_about_method)));
        }

        if (intent != null)
        {
            startActivity(intent);
        }
        return true;
    }

    /**
     * Custom navigation list adapter.
     */
    private final class NavigationListAdapter extends BaseAdapter implements SpinnerAdapter
    {
        private LayoutInflater m_layoutInflater;
        private TypedArray m_logos;
        private TypedArray m_titles;
        private TypedArray m_subtitles;

        public NavigationListAdapter(Context p_context, TypedArray p_logos, TypedArray p_titles, TypedArray p_subtitles)
        {
            m_layoutInflater = LayoutInflater.from(p_context);
            m_logos = p_logos;
            m_titles = p_titles;
            m_subtitles = p_subtitles;
        }

        @Override
        public int getCount()
        {
            return m_titles.length();
        }

        @Override
        public Object getItem(int p_position)
        {
            return p_position;
        }

        @Override
        public long getItemId(int p_position)
        {
            return m_titles.getResourceId(p_position, 0);
        }

        @Override
        public View getView(int p_position, View p_convertView, ViewGroup p_parent)
        {
            View view = p_convertView;
            if (view == null)
            {
                view = m_layoutInflater.inflate(R.layout.navigation_list_item, p_parent, false);
            }

            // Title...
            TextView tv_title = (TextView) view.findViewById(R.id.title);
            tv_title.setText(m_titles.getString(p_position));

            // Subtitle...
            TextView tv_subtitle = ((TextView) view.findViewById(R.id.subtitle));
            tv_subtitle.setText(m_subtitles.getString(p_position));
            tv_subtitle.setVisibility("".equals(tv_subtitle.getText()) ? View.GONE : View.VISIBLE);

            return view;
        }

        @Override
        public View getDropDownView(int p_position, View p_convertView, ViewGroup p_parent)
        {
            View view = p_convertView;
            if (view == null)
            {
                view = m_layoutInflater.inflate(R.layout.navigation_list_dropdown_item, p_parent, false);
            }

            // Icon...
            ImageView iv_logo = (ImageView) view.findViewById(R.id.logo);
            if (m_logos != null)
            {
                iv_logo.setImageDrawable(m_logos.getDrawable(p_position));
            }

            // Title...
            TextView tv_title = (TextView) view.findViewById(R.id.title);
            tv_title.setText(m_titles.getString(p_position));

            // Subtitle...
            TextView tv_subtitle = ((TextView) view.findViewById(R.id.subtitle));
            tv_subtitle.setText(m_subtitles.getString(p_position));
            tv_subtitle.setVisibility("".equals(tv_subtitle.getText()) ? View.GONE : View.VISIBLE);

            return view;
        }
    }

}
