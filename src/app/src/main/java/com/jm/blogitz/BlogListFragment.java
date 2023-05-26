package com.jm.blogitz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textview.MaterialTextView;
import com.jm.blogitz.models.Blog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlogListFragment extends Fragment {
    private RecyclerView blogRecyclerView;
    private BlogAdapter blogAdapter;
    private MenuItem selectAllMenuItem;
    private MenuItem deleteAllMenuItem;
    private ArrayList<BlogHolder> allBlogHolders = new ArrayList<>();
    private ArrayList<BlogHolder> selectedBlogs = new ArrayList<>();
    private boolean multiSelectActive = false;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.top_app_bar, menu);

        this.selectAllMenuItem = menu.findItem(R.id.select_all_menu_item);
        this.deleteAllMenuItem = menu.findItem(R.id.delete_all_menu_item);
        this.deleteAllMenuItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.create_blog_menu_item) {
            Blog blog = new Blog();
            BlogLab.get(getActivity()).addBlog(blog);

            this.dispatchBlogPagerIntent(blog.getId());
            return true;
        } else if (itemId == R.id.select_all_menu_item) {
            this.toggleSelectAll(item);
            return true;
        } else if (itemId == R.id.delete_all_menu_item) {
            this.dispatchDeleteAllAction();
            return true;
        } else if (itemId == R.id.search_menu_item) {
            this.dispatchSearchIntent();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
        this.selectedBlogs = new ArrayList<>();
        this.allBlogHolders = new ArrayList<>();
        this.multiSelectActive = false;
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

        if (this.deleteAllMenuItem != null) {
            this.deleteAllMenuItem.setVisible(false);
        }
        this.selectedBlogs = new ArrayList<>();
        this.allBlogHolders = new ArrayList<>();
        this.multiSelectActive = false;
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

    private void dispatchBlogPagerIntent(UUID blogId) {
        Intent intent = BlogPagerActivity.newIntent(getActivity(), blogId);
        startActivity(intent);
    }

    private void toggleSelectAll(MenuItem item) {
        if (item.getTitle().equals(getString(R.string.select_all_menu_item))) {
            for (BlogHolder holder: this.allBlogHolders) {
                ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
                        .toBuilder()
                        .setAllCorners(CornerFamily.CUT, 0)
                        .build();
                MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
                shapeDrawable.setFillColor(ContextCompat.getColorStateList(requireContext(), R.color.selection));
                shapeDrawable.setStroke(2.0f, ContextCompat.getColor(requireContext(), R.color.white));
                holder.itemView.setBackground(shapeDrawable);
                holder.selected = true;

                if (!this.selectedBlogs.contains(holder)) {
                    this.selectedBlogs.add(holder);
                }
            }

            item.setTitle(R.string.deselect_all_menu_item);
            this.deleteAllMenuItem.setVisible(true);
            this.multiSelectActive = true;
        } else {
            for (BlogHolder holder: this.allBlogHolders) {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                holder.selected = false;
                this.selectedBlogs.add(holder);
            }

            item.setTitle(R.string.select_all_menu_item);
            this.deleteAllMenuItem.setVisible(false);
            this.multiSelectActive = false;
        }
    }

    private void dispatchDeleteAllAction() {
        for (BlogHolder holder : this.selectedBlogs) {
            BlogLab.get(getContext()).deleteBlog(holder.blog);
        }

        this.updateUI();
        this.selectAllMenuItem.setTitle(R.string.select_all_menu_item);
        this.multiSelectActive = false;
        this.selectedBlogs = new ArrayList<>();
        this.allBlogHolders = new ArrayList<>();
    }

    private void dispatchSearchIntent() {
        Intent searchIntent = SearchListActivity.newIntent(getActivity());
        startActivity(searchIntent);
    }

    private class BlogHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final MaterialTextView titleTextView;
        private final MaterialTextView bodyTextView;
        private Blog blog;
        private boolean selected;

        public BlogHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            this.selected = false;
            this.titleTextView = itemView.findViewById(R.id.list_item_blog_title_text_view);
            this.bodyTextView = itemView.findViewById(R.id.list_item_blog_body_text_view);
        }

        public void bindBlog(Blog blog) {
            this.blog = blog;
            this.titleTextView.setText(this.blog.getTitle());
            this.bodyTextView.setText(this.blog.getBody());
            this.itemView.setBackgroundColor(Color.TRANSPARENT);
            this.selected = false;
            allBlogHolders.add(this);
        }

        @Override
        public void onClick(View view) {
            if (!multiSelectActive) {
                Intent intent = BlogPagerActivity.newIntent(getActivity(), this.blog.getId());
                startActivity(intent);
            } else {
                this.toggleSelected();
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (!multiSelectActive) {
                multiSelectActive = true;
                this.toggleSelected();
                return true;
            }

            return false;
        }

        private void toggleSelected() {
            this.selected = !this.selected;
            if (this.selected) {
                selectedBlogs.add(this);
                ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
                        .toBuilder()
                        .setAllCorners(CornerFamily.CUT, 0)
                        .build();
                MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
                shapeDrawable.setFillColor(ContextCompat.getColorStateList(requireContext(), R.color.selection));
                shapeDrawable.setStroke(2.0f, ContextCompat.getColor(requireContext(), R.color.white));
                itemView.setBackground(shapeDrawable);
            } else {
                selectedBlogs.remove(this);
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            if (selectedBlogs.size() == 0) {
                multiSelectActive = false;
                selectAllMenuItem.setTitle(R.string.select_all_menu_item);
                deleteAllMenuItem.setVisible(false);
            } else {
                selectAllMenuItem.setTitle(R.string.deselect_all_menu_item);
                deleteAllMenuItem.setVisible(true);
            }
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
    }
}
