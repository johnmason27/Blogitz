package com.jm.blogitz;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.jm.blogitz.models.Blog;

import java.util.List;
import java.util.UUID;

/**
 * Viewpager to swipe through the blog list.
 */
public class BlogPagerActivity extends AppCompatActivity {
    /**
     * Current blog to launch with.
     */
    private static final String EXTRA_BLOG_ID = "com.jm.blogitz.blog_id";
    /**
     * Current list of blogs to page.
     */
    private List<Blog> blogs;
    /**
     * Filtered blogs from the search list.
     */
    private static List<Blog> filteredBlogs;

    /**
     * Called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        filteredBlogs = null;
    }

    /**
     * Setup the data and views.
     * @param savedInstanceState Instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // View of the pager.
        setContentView(R.layout.activity_blog_pager);

        // Add the action bar and set the colour to match the theme.
        assert getSupportActionBar() != null;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        int color = ResourcesCompat.getColor(getResources(), R.color.background, getTheme());
        ColorDrawable colorDrawable = new ColorDrawable(color);
        actionBar.setBackgroundDrawable(colorDrawable);

        // Setup the blogs to view if they don't exist in the filter.
        UUID blogId = (UUID) getIntent().getSerializableExtra(EXTRA_BLOG_ID);
        if (filteredBlogs == null) {
            blogs = BlogLab.get(this).getBlogs();
        } else {
            blogs = filteredBlogs;
        }

        // Add the adapter to the view pager.
        ViewPager2 viewPager = findViewById(R.id.activity_blog_pager_view_pager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Set the current blog to the one opened.
        int blogIndex = this.findBlogIndex(blogId);
        viewPager.setCurrentItem(blogIndex);
    }

    /**
     * Create a new instance of the Activity for a given blog.
     * @param packageContext The application state.
     * @param blogId Blog to display.
     * @return The new Activity.
     */
    public static Intent newIntent(Context packageContext, UUID blogId) {
        Intent intent = new Intent(packageContext, BlogPagerActivity.class);
        intent.putExtra(EXTRA_BLOG_ID, blogId);
        return intent;
    }

    /**
     * Create a new instance of the Activity for a given blog.
     * @param packageContext The application state.
     * @param blogId Blog to display.
     * @param blogs Blogs to page through.
     * @return The new Activity.
     */
    public static Intent newIntent(Context packageContext, UUID blogId, List<Blog> blogs) {
        filteredBlogs = blogs;
        return newIntent(packageContext, blogId);
    }

    /**
     * When the back button pressed.
     * @return Boolean.
     */
    @Override
    public boolean onSupportNavigateUp() {
        this.dispatchMoveBackIntent();
        return true;
    }

    /**
     * Fragment adapter for the pager.
     */
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        /**
         * Create the fragment to display.
         * @param position Current page.
         * @return New BlogFragment.
         */
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Blog blog = blogs.get(position);
            return BlogFragment.newInstance(blog.getId());
        }

        /**
         * Get the number of blogs to page.
         * @return Number of blogs.
         */
        @Override
        public int getItemCount() {
            return blogs.size();
        }
    }

    /**
     * Launch the intent to open the BlogList.
     */
    private void dispatchMoveBackIntent() {
        Intent moveBackIntent = new Intent(this, BlogListActivity.class);
        startActivity(moveBackIntent);
        finish();
    }

    /**
     * Find the index of the current blog to display.
     * @param blogId Blog id.
     * @return The index.
     */
    private int findBlogIndex(UUID blogId) {
        int index = 0;
        for (Blog blog: blogs) {
            if (blog.getId().equals(blogId)) {
                break;
            }
            index++;
        }
        return index;
    }
}
