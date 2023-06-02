package com.jm.blogitz;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

/**
 * The view containing the search list.
  */
public class SearchListFragment extends Fragment {
    /**
     * Pager view.
     */
    private RecyclerView blogRecyclerView;
    /**
     * The recycler view requests the views and binds their data by calling methods in the adapter.
     */
    private BlogAdapter blogAdapter;
    /**
     * Previous text of the search query.
     */
    private String previousText;
    /**
     * Currently filtered blogs.
     */
    private static List<Blog> filteredBlogs = null;
    /**
     * Current search query.
     */
    private static String searchQuery = null;

    /**
     * Handle the MenuItem selected events.
     * @param item The menu item that was selected.
     *
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        requireActivity().onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Create the view of the SearchListFragment.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The new view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get the view of the fragment.
        View view = inflater.inflate(R.layout.fragment_search_blog, container, false);

        this.setHasOptionsMenu(true);

        // Set the recycler view.
        this.blogRecyclerView = view.findViewById(R.id.blog_recycler_view);
        this.blogRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Setup the search box and add an event listener to listen to text inputs.
        EditText searchEditText = view.findViewById(R.id.search_edit_text);
        if (searchQuery != null) {
            // Set the existing search query when returning to the activity.
            searchEditText.setText(searchQuery);
        }
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Check if a backspace is pressed.
                if (previousText != null && charSequence.length() < previousText.length()) {
                    blogAdapter.setBlogs(BlogLab.get(getContext()).getBlogs());
                }

                if (charSequence == null) {
                    charSequence = "";
                }
                // Filter the blogs using the current query.
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

    /**
     * When returning to the fragment update the UI.
     */
    @Override
    public void onResume() {
        super.onResume();
        this.updateUI();
    }

    /**
     * Setup the adapter if it doesn't exist and setup the recycler view.
     */
    private void updateUI() {
        BlogLab blogLab = BlogLab.get(getActivity());
        List<Blog> blogs = blogLab.getBlogs();
        if (this.blogAdapter == null) {
            this.blogAdapter = new BlogAdapter(blogs);
            this.blogRecyclerView.setAdapter(this.blogAdapter);
        } else {
            // Set the blogs either to the existing filter or full list of blogs.
            if (filteredBlogs == null) {
                this.blogAdapter.setBlogs(blogs);
            } else {
                this.blogAdapter.setBlogs(filteredBlogs);
            }
            // Notify the adapter it has new data.
            this.blogAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Holds the view of the blog in a list.
     */
    private class BlogHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        /**
         * Text view of the title.
         */
        private final MaterialTextView titleTextView;
        /**
         * Text view of the body.
         */
        private final MaterialTextView bodyTextView;
        /**
         * The blog to display.
         */
        private Blog blog;

        /**
         * Create an instance of the holder for a given view.
         * @param itemView The view to setup.
         */
        public BlogHolder(View itemView) {
            super(itemView);
            // Add a click listener to the view.
            itemView.setOnClickListener(this);
            this.titleTextView = itemView.findViewById(R.id.list_item_blog_title_text_view);
            this.bodyTextView = itemView.findViewById(R.id.list_item_blog_body_text_view);
        }

        /**
         * Bind the Blog to the view elements.
         * @param blog Blog to bind.
         */
        public void bindBlog(Blog blog) {
            this.blog = blog;
            this.titleTextView.setText(this.blog.getTitle());
            this.bodyTextView.setText(this.blog.getBody());
        }

        /**
         * Click listener for when the holder is clicked.
         * @param view The view that's been clicked.
         */
        @Override
        public void onClick(View view) {
            // Create a new intent for either the filtered blogs or all blogs.
            Intent intent;
            if (filteredBlogs != null) {
                intent = BlogPagerActivity.newIntent(getActivity(), this.blog.getId(), filteredBlogs);
            } else {
                intent = BlogPagerActivity.newIntent(getActivity(), this.blog.getId());
            }
            startActivity(intent);
        }
    }

    /**
     * Used by the RecyclerView to request views and bind data.
     */
    private class BlogAdapter extends RecyclerView.Adapter<BlogHolder> {
        /**
         * Current blogs being handled by the adapter.
         */
        private List<Blog> blogs;

        /**
         * Create an instance of the BlogAdapter.
         * @param blogs Blogs to handle.
         */
        public BlogAdapter(List<Blog> blogs) {
            this.blogs = blogs;
        }

        /**
         * Set the blogs for the adapter to handle.
         * @param blogs Blogs to handle.
         */
        public void setBlogs(List<Blog> blogs) {
            this.blogs = blogs;
        }

        /**
         * Create the view for the holder.
         * @param parent The ViewGroup into which the new View will be added after it is bound to
         *               an adapter position.
         * @param viewType The view type of the new View.
         *
         * @return New BlogHolder.
         */
        @NonNull
        @Override
        public BlogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_blog, parent, false);

            return new BlogHolder(view);
        }

        /**
         * Bind the blog to the view holder.
         * @param holder The ViewHolder which should be updated to represent the contents of the
         *        item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(BlogHolder holder, int position) {
            Blog blog = this.blogs.get(position);
            holder.bindBlog(blog);
        }

        /**
         * Get the current number of items being handled by the adapter.
         * @return Number of blogs.
         */
        @Override
        public int getItemCount() {
            return this.blogs.size();
        }

        /**
         * Filter the blogs to display.
         * @param searchText The current search query.
         */
        private void filterBlogs(String searchText) {
            String loweredSearchText = searchText.toLowerCase();
            // If the search query is empty, reset the adapter to the full list of blogs.
            if (loweredSearchText.equals("")) {
                this.setBlogs(BlogLab.get(getContext()).getBlogs());
                filteredBlogs = null;
                searchQuery = null;
            } else {
                // Set the current search query and filtered blogs
                searchQuery = loweredSearchText;
                filteredBlogs = new ArrayList<>();
                this.blogs = BlogLab.get(getContext()).getBlogs();
                // Foreach blog if the search query matches a blog add to the list of filtered blogs.
                for (Blog blog: this.blogs) {
                    if (blog.getTitle().toLowerCase().contains(loweredSearchText) ||
                        blog.getBody().toLowerCase().contains(loweredSearchText)) {
                        filteredBlogs.add(blog);
                    }
                }
                // Set the blogs on the adapter.
                this.setBlogs(filteredBlogs);
            }

            // Notify the adapter of a data change.
            this.notifyDataSetChanged();
        }
    }
}
