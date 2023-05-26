package com.jm.blogitz;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.jm.blogitz.models.Blog;

import java.util.ArrayList;
import java.util.List;

public class SearchListFragment extends Fragment {
    private RecyclerView blogRecyclerView;
    private BlogAdapter blogAdapter;
    private String previousText;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_blog, container, false);

        this.blogRecyclerView = view.findViewById(R.id.blog_recycler_view);
        this.blogRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        EditText searchEditText = view.findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (previousText != null && charSequence.length() < previousText.length()) {
                    blogAdapter.setBlogs(BlogLab.get(getContext()).getBlogs());
                }

                blogAdapter.filterBlogs(charSequence.toString());
                previousText = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

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

    private class BlogHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MaterialTextView titleTextView;
        private final MaterialTextView bodyTextView;
        private Blog blog;

        public BlogHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
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
    }

    private class BlogAdapter extends RecyclerView.Adapter<BlogHolder> {
        private List<Blog> blogs;

        public BlogAdapter(List<Blog> blogs) {
            this.blogs = blogs;
        }

        public void setBlogs(List<Blog> blogs) {
            this.blogs = blogs;
        }

        @NonNull
        @Override
        public BlogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_blog, parent, false);

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

        private void filterBlogs(String searchText) {
            if (searchText.equals("")) {
                this.setBlogs(BlogLab.get(getContext()).getBlogs());
            } else {
                ArrayList<Blog> filteredBlogs = new ArrayList<>();
                for (Blog blog: this.blogs) {
                    if (blog.getTitle().contains(searchText) || blog.getBody().contains(searchText)) {
                        filteredBlogs.add(blog);
                    }
                }
                this.setBlogs(filteredBlogs);
            }

            this.notifyDataSetChanged();
        }
    }
}
