package com.example.minghan.expense;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener, HistoryFragment.OnFragmentInteractionListener, CategoryFragment.OnFragmentInteractionListener, ChartFragment.OnFragmentInteractionListener{

    private TextView mTextMessage;
    private int mSelectedItem;
    private static final String SELECTED_ITEM = "arg_selected_item";
    private BottomNavigationView bottomNavigationView;

//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
//                    return true;
//                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
//                    return true;
//                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
//                    return true;
//            }
//            return false;
//        }
//
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MenuItem menuItem;
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });

        if(savedInstanceState != null){
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);
            menuItem = bottomNavigationView.getMenu().findItem(mSelectedItem);
        } else{
            menuItem = bottomNavigationView.getMenu().getItem(0);
        }
        selectFragment(menuItem);
        bottomNavigationView.getMenu().getItem(0).setChecked(true);
    }

    private void selectFragment(MenuItem item) {
        Fragment frag = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                frag = new MainFragment();
                break;
            case R.id.navigation_dashboard:
                frag = new CategoryFragment();
                break;
//            case R.id.navigation_notifications:
//                frag = new HistoryFragment();
//                break;
            case R.id.navigation_chart:
                frag = new ChartFragment();
                break;
        }

        // update selected item
        mSelectedItem = item.getItemId();

        for (int i = 0; i< bottomNavigationView.getMenu().size(); i++) {
            MenuItem menuItem = bottomNavigationView.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == mSelectedItem);
        }

        if (frag != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, frag)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM, mSelectedItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        MenuItem homeItem = bottomNavigationView.getMenu().getItem(0);
        if (mSelectedItem != homeItem.getItemId()) {
            selectFragment(homeItem);
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) { }
}
