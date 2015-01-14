package com.lucasiturbide.lisectionarrayadapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lucasiturbide.lisectionarrayadapter.fragments.SectionedExpandableListViewFragment;
import com.lucasiturbide.lisectionarrayadapter.fragments.SectionedListViewFragment;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private final static Class<?>[] menu_items = new Class<?>[]{SectionedExpandableListViewFragment.class, SectionedListViewFragment.class};
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private MenuDrawerToggle mDrawerToggle;
    private Fragment mFragment;
    private String selectedItemTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.menu_drawer);
        mDrawerList.setOnItemClickListener(this);
        mDrawerToggle = new MenuDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.app_name,  /* "open drawer" description for accessibility */
                R.string.app_name /* "close drawer" description for accessibility */
        );
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        ArrayAdapter<Class<?>> listAdapter = new ArrayAdapter<Class<?>>(this, android.R.layout.simple_list_item_1, android.R.id.text1, menu_items);
        mDrawerList.setAdapter(listAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switchFragment(menu_items[position].getName(), null);
    }

    public void popFragmentStackToRoot(){
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        getSupportFragmentManager().popBackStackImmediate("ROOT", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void switchFragment(String tag, Bundle args) {
        popFragmentStackToRoot();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment preInitializedFragment = getSupportFragmentManager().findFragmentByTag(selectedItemTag);
        if (preInitializedFragment != null) {
            ft.detach(preInitializedFragment);
        } else if (mFragment != null) {
            ft.detach(mFragment);
        }
        preInitializedFragment = getSupportFragmentManager().findFragmentByTag(tag);
        selectedItemTag = tag;
        if (preInitializedFragment == null) {
            mFragment = Fragment.instantiate(this, (String) tag);
            if (args != null) {
                mFragment.setArguments(args);
            }
            ft.add(R.id.fragment_content, mFragment, tag);
        } else {
            mFragment = preInitializedFragment;
            Bundle existingArgs = preInitializedFragment.getArguments();
            if (existingArgs != null){
                existingArgs.clear();
                if (args != null) {
                    existingArgs.putAll(args);
                }
            }else{
                if (args != null) {
                    preInitializedFragment.setArguments(args);
                }
            }
            ft.attach(preInitializedFragment);
        }
        ft.commit();
    }

    private class MenuDrawerToggle extends ActionBarDrawerToggle {

        private boolean actionbarShowing;


        public MenuDrawerToggle(Activity activity, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
            actionbarShowing = true;
            setDrawerIndicatorEnabled(false);
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            super.onDrawerSlide(drawerView, slideOffset);
            if (slideOffset > 0.25f && actionbarShowing){
                getSupportActionBar().hide();
                actionbarShowing = false;
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }else if (slideOffset <= 0.25f && !actionbarShowing){
                actionbarShowing = true;
                getSupportActionBar().show();
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        }

        @Override
        public void onDrawerClosed(View view) {
            supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
    }
}
