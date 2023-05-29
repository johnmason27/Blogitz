package com.jm.blogitz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class SearchListActivity extends AppCompatActivity {
    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, SearchListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity_search);

        ActionBar actionBar = getSupportActionBar();
        int color = ResourcesCompat.getColor(getResources(), R.color.background, getTheme());
        ColorDrawable colorDrawable = new ColorDrawable(color);
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(colorDrawable);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new SearchListFragment();
            fragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
}
