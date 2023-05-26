package com.jm.blogitz;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.jm.blogitz.models.Blog;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class BlogPagerActivity extends AppCompatActivity {
    private static final String EXTRA_BLOG_ID = "com.jm.blogitz.blog_id";
    private List<Blog> blogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_pager);

        assert getSupportActionBar() != null;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        int color = ResourcesCompat.getColor(getResources(), R.color.background_dark, getTheme());
        ColorDrawable colorDrawable = new ColorDrawable(color);
        actionBar.setBackgroundDrawable(colorDrawable);

        UUID blogId = (UUID) getIntent().getSerializableExtra(EXTRA_BLOG_ID);
        this.blogs = BlogLab.get(this).getBlogs();

        ViewPager2 viewPager = findViewById(R.id.activity_blog_pager_view_pager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        int blogIndex = this.findBlogIndex(blogId);
        viewPager.setCurrentItem(blogIndex);
    }

    public static Intent newIntent(Context packageContext, UUID blogId) {
        Intent intent = new Intent(packageContext, BlogPagerActivity.class);
        intent.putExtra(EXTRA_BLOG_ID, blogId);
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_top_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        UUID blogId = (UUID) getIntent().getSerializableExtra(EXTRA_BLOG_ID);
        BlogLab blogLab = BlogLab.get(this);

        Blog blog = blogLab.getBlog(blogId);
        blog.validateBlog(this);

        File photoFile = blogLab.getPhotoFile(blog);
        Uri photoFileUri = FileProvider.getUriForFile(this, "com.jm.blogitz.fileprovider", photoFile);
        String title = blog.getTitle();
        String body = blog.getBody();

        int itemId = item.getItemId();
        if (itemId == R.id.publish_menu_item) {
            dispatchTwitterPublishIntent(title, body, photoFileUri);
            return true;
        } else if (itemId == R.id.share_via_email_menu_item) {
            dispatchSendEmailIntent(title, body, photoFileUri);
            return true;
        } else if (itemId == R.id.delete_menu_item) {
            blogLab.deleteBlog(blog);
            dispatchMoveBackIntent();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.dispatchMoveBackIntent();
        return true;
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Blog blog = blogs.get(position);
            return BlogFragment.newInstance(blog.getId());
        }

        @Override
        public int getItemCount() {
            return blogs.size();
        }
    }

    private void dispatchTwitterPublishIntent(String title, String body, Uri fileUri) {
        try {
            String content = title + "\n" + body;

            Intent shareToTwitterIntent = new Intent(
                    android.content.Intent.ACTION_SEND);
            shareToTwitterIntent.putExtra(Intent.EXTRA_TEXT,
                    content);
            shareToTwitterIntent.setType("text/*");
            shareToTwitterIntent.putExtra(Intent.EXTRA_STREAM,
                    fileUri);
            shareToTwitterIntent.setType("image/*");
            shareToTwitterIntent.setPackage("com.twitter.android");
            startActivity(shareToTwitterIntent);
        } catch (Exception exception) {
            Toast.makeText(this, "Twitter isn't installed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void dispatchSendEmailIntent(String subject, String body, Uri attachmentUri) {
        try {
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setAction(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            emailIntent.putExtra(Intent.EXTRA_STREAM, attachmentUri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(emailIntent, "Send Email Using: "));
        } catch (Exception exception) {
            Toast.makeText(this, "Can't send email.", Toast.LENGTH_SHORT).show();
        }
    }

    private void dispatchMoveBackIntent() {
        Intent moveBackIntent = new Intent(this, BlogListActivity.class);
        startActivity(moveBackIntent);
        finish();
    }

    private int findBlogIndex(UUID blogId) {
        int index = 0;
        for (Blog blog: this.blogs) {
            if (blog.getId().equals(blogId)) {
                break;
            }
            index++;
        }
        return index;
    }
}
