package com.jm.blogitz;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Activity containing the SearchList.
 */
public class SearchListActivity extends AppCompatActivity {
    /**
     * Create a new instance of the Activity.
     * @param packageContext Current state of the application.
     * @return The new instance.
     */
    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, SearchListActivity.class);
    }

    /**
     * Create the SearchList.
     * @param savedInstanceState Saved state from the Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the view of the activity.
        setContentView(R.layout.list_activity_search);

        // Set the colour of the ActionBar to match the theme.
        ActionBar actionBar = getSupportActionBar();
        int color = ResourcesCompat.getColor(getResources(), R.color.background, getTheme());
        ColorDrawable colorDrawable = new ColorDrawable(color);
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(colorDrawable);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new SearchListFragment();
            // Add the fragment to the list view.
            fragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
}
