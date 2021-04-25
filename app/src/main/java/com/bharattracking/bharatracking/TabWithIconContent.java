package com.bharattracking.bharatracking;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.bharattracking.bharatracking.fragments.AlertFragment;
import com.bharattracking.bharatracking.fragments.CommandFragment;
import com.bharattracking.bharatracking.fragments.RecordFragment;

/**
 * Created by swadhin on 14/2/18.
 */

public class TabWithIconContent {

    //Current Activity
    private Activity _activity;
    //This is our tablayout
    private TabLayout tabLayout;

    //This is our viewPager
    private ViewPager viewPager;

    private String[] tabLayoutTitles;

    ViewPagerAdapter adapter;

    //Fragments

    private RecordFragment recordFragment;
    private AlertFragment alertFragment;
    public CommandFragment commandFragment;

    public RecordFragment getRecordFragment() {
        return recordFragment;
    }

    public AlertFragment getAlertFragment() {
        return alertFragment;
    }

    public CommandFragment getCommandFragment() {
        return commandFragment;
    }

    public void setIconToTab(int tabIndex, Drawable drawable) {
        if (tabIndex < 3 && tabLayout.getTabAt(tabIndex) != null){
            tabLayout.getTabAt(tabIndex).setIcon(drawable);
        }
    }

    public TabWithIconContent(Activity activity){
        this._activity = activity;
    }
    public void setViewWithCurrentTab(int position){
        viewPager.setCurrentItem(position);
    }
    public void setUpTabWIthIconContent(FragmentManager fragmentManager) {
        tabLayoutTitles = _activity.getResources().getStringArray(R.array.tabLayoutTitles);
        //Initializing viewPager
        viewPager = _activity.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(tabLayoutTitles.length);
        setupViewPager(viewPager,fragmentManager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //Initializing the tablayout
        tabLayout =  _activity.findViewById(R.id.tablayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(),true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    public void updateTabIconCount(int pos, int count){
        TextView tv_count = tabLayout.getTabAt(pos).getCustomView().findViewById(R.id.tv_count);
        tv_count.setText(String.valueOf(count));
    }

    public int getTabIconCount(int pos){
        TextView tv_count = tabLayout.getTabAt(pos).getCustomView().findViewById(R.id.tv_count);
        return Integer.valueOf(tv_count.getText().toString());
    }

    private View prepareTabView(int pos) {
        View view = _activity.getLayoutInflater().inflate(R.layout.custom_tab_layout,null);
        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_count = view.findViewById(R.id.tv_count);
        tv_title.setText(tabLayoutTitles[pos]);
        if (pos == 0){
            tv_count.setText("0");
            tv_count.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private void setupTabIcons()
    {
        for(int i=0;i< tabLayoutTitles.length;i++)
        {
            tabLayout.getTabAt(i).setCustomView(prepareTabView(i));
        }
    }

    private void setupViewPager(ViewPager viewPager, FragmentManager fragmentManager) {
        adapter = new ViewPagerAdapter(fragmentManager);
        recordFragment=new RecordFragment();
        alertFragment=new AlertFragment();
        commandFragment = new CommandFragment();
        adapter.addFragment(recordFragment,tabLayoutTitles[0]);
        adapter.addFragment(alertFragment,tabLayoutTitles[1]);
        adapter.addFragment(commandFragment,tabLayoutTitles[2]);
//        tabLayout.getTabAt(0).show
        viewPager.setAdapter(adapter);
    }

}

