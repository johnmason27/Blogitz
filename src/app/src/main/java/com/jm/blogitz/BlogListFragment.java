package com.jm.blogitz;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
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

/**
 * Fragment of the BlogList.
 */
public class BlogListFragment extends Fragment {
    /**
     * Pager view of the blog list.
     */
    private RecyclerView blogRecyclerView;
    /**
     * The recycler view requests the views and binds their data by calling methods in the adapter.
     */
    private BlogAdapter blogAdapter;
    /**
     * Select All MenuItem.
     */
    private MenuItem selectAllMenuItem;
    /**
     * Delete All MenuItem.
     */
    private MenuItem deleteAllMenuItem;
    /**
     * All BlogHolders.
     */
    private ArrayList<BlogHolder> allBlogHolders = new ArrayList<>();
    /**
     * All the Selected BlogHolders.
     */
    private ArrayList<BlogHolder> selectedBlogs = new ArrayList<>();
    /**
     * Is the multiselect mode active.
     */
    private boolean multiSelectActive = false;

    /**
     * Create the Options Menu and set the values.
     * @param menu The options menu in which you place your items.
     *
     * @param inflater Menu inflater.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.top_app_bar, menu);

        this.selectAllMenuItem = menu.findItem(R.id.select_all_menu_item);
        this.deleteAllMenuItem = menu.findItem(R.id.delete_all_menu_item);
        this.deleteAllMenuItem.setVisible(false);
    }

    /**
     * Handle the MenuItem selected events.
     * @param item The menu item that was selected.
     *
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        // Check which menu item was clicked and launch the appropriate action.
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

    /**
     * Called when creating the Fragment.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
        // Reset the data.
        this.selectedBlogs = new ArrayList<>();
        this.allBlogHolders = new ArrayList<>();
        this.multiSelectActive = false;
    }


    /**
     * Create the Fragment view.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The Fragment view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog_list, container, false);

        this.blogRecyclerView = view.findViewById(R.id.blog_recycler_view);
        this.blogRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        this.updateUI();
        return view;
    }

    /**
     * Called when returning to the Fragment.
     */
    @Override
    public void onResume() {
        super.onResume();
        this.updateUI();

        if (this.deleteAllMenuItem != null) {
            this.deleteAllMenuItem.setVisible(false);
        }

        // If we have some blogs selected display the delete button.
        if (this.selectedBlogs.size() > 0) {
            this.multiSelectActive = true;
            if (this.deleteAllMenuItem != null) {
                this.deleteAllMenuItem.setVisible(true);
            }
        }
    }

    /**
     * Update the UI to match the state.
     */
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

    /**
     * Launch a new BlogPager.
     * @param blogId Blog to start on.
     */
    private void dispatchBlogPagerIntent(UUID blogId) {
        Intent intent = BlogPagerActivity.newIntent(getActivity(), blogId);
        startActivity(intent);
    }

    /**
     * Toggle the select all button.
     * @param item Menu item clicked.
     */
    private void toggleSelectAll(MenuItem item) {
        // If we are selecting all.
        if (item.getTitle().equals(getString(R.string.select_all_menu_item))) {
            // Go through each blog holder and toggle the selection color.
            for (BlogHolder holder: this.allBlogHolders) {
                toggleSelectionColor(holder.itemView);
                holder.selected = true;

                // Add the holder to the selected blog list.
                if (!this.selectedBlogs.contains(holder)) {
                    this.selectedBlogs.add(holder);
                }
            }

            // Set the title of the Select all menu item to Deselect all.
            item.setTitle(R.string.deselect_all_menu_item);
            // Display the Delete all button.
            this.deleteAllMenuItem.setVisible(true);
            this.deleteAllMenuItem.setTitle(R.string.delete_all_menu_item);
            this.multiSelectActive = true;
        } else {
            // Deselect all the blogs.
            for (BlogHolder holder: this.allBlogHolders) {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                holder.selected = false;
                this.selectedBlogs.remove(holder);
            }

            // Reset the menu items.
            item.setTitle(R.string.select_all_menu_item);
            this.deleteAllMenuItem.setVisible(false);
            this.multiSelectActive = false;
        }
    }

    /**
     * Launch the delete all action.
     */
    private void dispatchDeleteAllAction() {
        BlogLab blogLab = BlogLab.get(requireContext());
        List<Blog> blogs = blogLab.getBlogs();
        if (this.deleteAllMenuItem.getTitle().equals("Delete All")) {
            for (Blog blog : blogs) {
                BlogLab.get(getContext()).deleteBlog(blog);
            }
        } else {
            // Delete Selected
            for (BlogHolder holder : this.selectedBlogs) {
                BlogLab.get(getContext()).deleteBlog(holder.blog);
            }
        }

        // Reset the UI now nothing is selected.
        this.updateUI();
        this.selectAllMenuItem.setTitle(R.string.select_all_menu_item);
        this.deleteAllMenuItem.setVisible(false);
        this.multiSelectActive = false;
        this.selectedBlogs = new ArrayList<>();
        this.allBlogHolders = new ArrayList<>();
    }

    /**
     * Launch the search intent.
     */
    private void dispatchSearchIntent() {
        Intent searchIntent = SearchListActivity.newIntent(getActivity());
        startActivity(searchIntent);
    }

    /**
     * Holds the view of the blog in a list.
     */
    private class BlogHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
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
         * Whether the blog is selected in the view.
         */
        private boolean selected;

        /**
         * Create an instance of the holder for a given view.
         * @param itemView The view to setup.
         */
        public BlogHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            this.selected = false;
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
            this.itemView.setBackgroundColor(Color.TRANSPARENT);
            this.selected = false;
            allBlogHolders.add(this);
        }

        /**
         * Click listener for when the holder is clicked.
         * @param view The view that's been clicked.
         */
        @Override
        public void onClick(View view) {
            // If multiselect is active toggle the blog as selected.
            if (!multiSelectActive) {
                Intent intent = BlogPagerActivity.newIntent(getActivity(), this.blog.getId());
                startActivity(intent);
            } else {
                this.toggleSelected();
            }
        }

        /**
         * Click listener for when the holder is long clicked.
         * @param view The view that's been long clicked.
         */
        @Override
        public boolean onLongClick(View view) {
            // Activate multiselect.
            if (!multiSelectActive) {
                multiSelectActive = true;
                this.toggleSelected();
                return true;
            }

            return false;
        }

        /**
         * Toggle the selected state of the blog.
         */
        private void toggleSelected() {
            // Update the selection.
            this.selected = !this.selected;
            // If selected add to the selected blog list. Else remove from the list.
            if (this.selected) {
                selectedBlogs.add(this);
                toggleSelectionColor(itemView);
            } else {
                selectedBlogs.remove(this);
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            // If there is no selected blogs. Turn off multiselect, set the Select all menu item text and disable the delete all menu item.
            if (selectedBlogs.size() == 0) {
                multiSelectActive = false;
                selectAllMenuItem.setTitle(R.string.select_all_menu_item);
                deleteAllMenuItem.setVisible(false);
            } else {
                // If all blogs are selected update the select all and delete all menu item texts.
                if (selectedBlogs.size() == allBlogHolders.size()) {
                    selectAllMenuItem.setTitle(R.string.deselect_all_menu_item);
                    deleteAllMenuItem.setTitle(R.string.delete_all_menu_item);
                } else {
                    // If there are some selected blogs but not all set the text of the select all and delete all menu items.
                    selectAllMenuItem.setTitle(R.string.deselect_selected_menu_item);
                    deleteAllMenuItem.setTitle(R.string.delete_selected_menu_item);
                }

                // If we have a selected blog set the delete all menu item to visible.
                deleteAllMenuItem.setVisible(true);
            }
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
    }

    /**
     * Toggle the Selection Colour on the Blog item.
     * @param itemView Blog item.
     */
    private void toggleSelectionColor(View itemView) {
        ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.CUT, 0)
                .build();
        MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
        Resources.Theme applicationTheme = requireActivity().getTheme();
        ColorStateList fillColor = ResourcesCompat.getColorStateList(getResources(), R.color.selection, applicationTheme);
        shapeDrawable.setFillColor(fillColor);
        itemView.setBackground(shapeDrawable);
    }
}
