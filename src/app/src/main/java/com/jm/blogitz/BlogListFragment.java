package com.jm.blogitz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.jm.blogitz.models.Blog;

import java.util.ArrayList;
import java.util.List;

public class BlogListFragment extends Fragment {
    private RecyclerView blogRecyclerView;
    private BlogAdapter blogAdapter;
    private final ArrayList<Blog> selected = new ArrayList<>();
    private final ArrayList<View> views = new ArrayList<>();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.top_app_bar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_blog_menu_item:
                Blog blog = new Blog();
                BlogLab.get(getActivity()).addBlog(blog);
                Intent intent = BlogPagerActivity.newIntent(getActivity(), blog.getId());
                startActivity(intent);
                return true;
            case R.id.select_all_menu_item:
                for (Blog b : this.blogAdapter.blogs) {
                    selected.add(b);
                }

                for (View v: this.views) {
                    v.setBackgroundColor(getResources().getColor(R.color.selection, getContext().getTheme()));
                }
            case R.id.search_menu_item:
                Intent searchIntent = SearchListActivity.newIntent(getActivity());
                startActivity(searchIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog_list, container, false);

        this.blogRecyclerView = view.findViewById(R.id.blog_recycler_view);
        this.blogRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        this.updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateUI();
    }

    private void updateUI() {
        BlogLab blogLab = BlogLab.get(getActivity());
        List<Blog> blogs = blogLab.getBlogs();
        if (this.blogAdapter == null) {
            this.blogAdapter = new BlogAdapter(blogs);
            this.blogRecyclerView.setAdapter(this.blogAdapter);
        } else {
            this.blogAdapter.setBlogs(blogs);
            this.blogAdapter.notifyDataSetChanged();
        }
    }

    private class BlogHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private MaterialTextView titleTextView;
        private MaterialTextView bodyTextView;
        private Blog blog;

        public BlogHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            this.titleTextView = itemView.findViewById(R.id.list_item_blog_title_text_view);
            this.bodyTextView = itemView.findViewById(R.id.list_item_blog_body_text_view);
        }

        public void bindBlog(Blog blog) {
            this.blog = blog;
            this.titleTextView.setText(this.blog.getTitle());
            this.bodyTextView.setText(this.blog.getBody());
        }

        @Override
        public void onClick(View view) {
            Intent intent = BlogPagerActivity.newIntent(getActivity(), this.blog.getId());
            startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition();
            Blog blog = blogAdapter.blogs.get(position);
            if (selected.contains(position)) {
                view.setBackgroundColor(getResources().getColor(R.color.background_dark, getContext().getTheme()));
                selected.remove(position);
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.selection, getContext().getTheme()));
                selected.add(blog);
            }
            return false;
        }
    }

    private class BlogAdapter extends RecyclerView.Adapter<BlogHolder> {
        private List<Blog> blogs;

        public BlogAdapter(List<Blog> blogs) {
            this.blogs = blogs;
        }

        public void setBlogs(List<Blog> blogs) {
            this.blogs = blogs;
        }

        @Override
        public BlogHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_blog, parent, false);
            views.add(view);

            return new BlogHolder(view);
        }

        @Override
        public void onBindViewHolder(BlogHolder holder, int position) {
            Blog blog = this.blogs.get(position);
            holder.bindBlog(blog);
        }

        @Override
        public int getItemCount() {
            return this.blogs.size();
        }
    }
}
