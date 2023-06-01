package com.jm.blogitz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.jm.blogitz.models.Blog;
import com.jm.blogitz.utils.FileUtils;
import com.jm.blogitz.utils.PictureUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Container for the Blog in the pager view.
 */
public class BlogFragment extends Fragment {
    /**
     * Extra id.
     */
    private static final String ARG_BLOG_ID = "blog_id";
    /**
     * Blog to display.
     */
    private Blog blog;
    /**
     * Blogs photo.
     */
    private File photoFile;
    /**
     * Image view for the blog photo to display.
     */
    private ImageView blogPhotoView;

    /**
     * Create a new instance of the BlogFragment using the blog id provided.
     * @param blogId Id of the blog to display.
     * @return New BlogFragment.
     */
    public static BlogFragment newInstance(UUID blogId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_BLOG_ID, blogId);
        BlogFragment fragment = new BlogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Create the options menu.
     * @param menu Menu to display.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_top_app_bar, menu);
    }

    /**
     * Handle events in the options menu.
     * @param item Item selected.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        // Back button pressed.
        if (itemId != R.id.publish_menu_item &&
                itemId != R.id.share_via_email_menu_item &&
                itemId != R.id.delete_menu_item) {
            return super.onOptionsItemSelected(item);
        }

        BlogLab blogLab = BlogLab.get(requireContext());
        this.blog.validateBlog(requireActivity());

        File photoFile = blogLab.getPhotoFile(blog);
        Uri photoFileUri = FileProvider.getUriForFile(requireContext(), "com.jm.blogitz.fileprovider", photoFile);
        String title = blog.getTitle();
        String body = blog.getBody();

        if (!FileUtils.exists(photoFileUri, requireContext().getContentResolver())) {
            photoFileUri = null;
        }

        // Launch the appropriate intent based on the menu item clicked.
        if (itemId == R.id.publish_menu_item) {
            dispatchTwitterPublishIntent(title, body, photoFileUri);
            return true;
        } else if (itemId == R.id.share_via_email_menu_item) {
            dispatchSendEmailIntent(title, body, photoFileUri);
            return true;
        } else {
            blogLab.deleteBlog(blog);
            dispatchMoveBackIntent();
            return true;
        }
    }

    /**
     * Create an instance of the fragment.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            throw new NullPointerException();
        }

        setHasOptionsMenu(true);

        UUID blogId = (UUID) getArguments().getSerializable(ARG_BLOG_ID);
        // Get the blog and blog image to display.
        blog = BlogLab.get(getActivity()).getBlog(blogId);
        this.photoFile = BlogLab.get(getActivity()).getPhotoFile(blog);
    }

    /**
     * Create the view for the BlogFragment.
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
        View view = inflater.inflate(R.layout.fragment_blog, container, false);

        // Find the title text view.
        EditText titleTextView = view.findViewById(R.id.blog_title_edit_text);
        // Add the existing blog title into the title field.
        titleTextView.setText(blog.getTitle());
        // Add a text listener for when somebody sets the blog title.
        titleTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                blog.setTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Find the body text view.
        EditText bodyTextView = view.findViewById(R.id.blog_body_edit_text);
        // Add the existing blog body into the title field.
        bodyTextView.setText(blog.getBody());
        // Add a text listener for when somebody sets the blog body.
        bodyTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                blog.setBody(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Add a click listener to the select photo button.
        MaterialButton selectPhotoButton = view.findViewById(R.id.blog_select_photo_button);
        selectPhotoButton.setOnClickListener(v -> dispatchTakePhotoIntent());

        // Update the photo view to display an existing blog photo.
        this.blogPhotoView = view.findViewById(R.id.blog_image_view);
        this.updatePhotoView();

        return view;
    }

    /**
     * Update the database when the activity is paused.
     */
    @Override
    public void onPause() {
        super.onPause();
        this.blog.validateBlog(requireActivity());
        BlogLab.get(requireContext()).updateBlog(this.blog);
    }

    /**
     * Create a take photo intent and capture the response.
     */
    private void openTakePhotoActivityForResult() {
        Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Get the uri for the file that should be associated with the blog.
        Uri photoFileUri = FileProvider.getUriForFile(
                requireActivity(),
                "com.jm.blogitz.fileprovider",
                this.photoFile);
        captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
        List<ResolveInfo> cameraActivities = requireActivity()
                .getPackageManager()
                .queryIntentActivities(captureImageIntent, PackageManager.MATCH_DEFAULT_ONLY);

        // Request permissions to write the photo file.
        for (ResolveInfo activity : cameraActivities) {
            requireActivity()
                    .grantUriPermission(
                            activity.activityInfo.packageName,
                            photoFileUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        // Launch the intent and capture the result.
        takePhotoResultLauncher.launch(captureImageIntent);
    }

    /**
     * Create a choose from gallery intent and capture the response.
     */
    private void openChooseFromGalleryActivityForResult() {
        Intent selectPhotoIntent = new Intent();
        selectPhotoIntent.setType("image/*");
        selectPhotoIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Launch the intent and capture the result.
        chooseFromGalleryResultLauncher.launch(selectPhotoIntent);
    }

    /**
     * Take photo intent launcher.
     */
    ActivityResultLauncher<Intent> takePhotoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                /**
                 * Capture the response from the take photo intent.
                 * @param result Result of the intent.
                 */
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Get the uri of the photo
                        Uri uri = FileProvider.getUriForFile(requireActivity(), "com.jm.blogitz.fileprovider", photoFile);
                        // Remove permissions write permissions from the uri.
                        requireActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        // Update the photo view with the new photo.
                        updatePhotoView();
                    }
                }
            });

    /**
     * Choose from gallery intent launcher.
     */
    ActivityResultLauncher<Intent> chooseFromGalleryResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                /**
                 * Capture the response from the choose photo from gallery intent.
                 * @param result Result of the intent.
                 */
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }

                        try {
                            // Get the uri of the photo.
                            Uri uri = data.getData();
                            // Code from stack overflow post by vishwaxjit76 - https://stackoverflow.com/questions/2975197/convert-file-uri-to-file-in-android#:~:text=38-,Best%20Solution,-Create%20one%20simple
                            File file = FileUtils.from(requireActivity(), uri);
                            Bitmap bitmap = PictureUtils.getScaledBitmap(file.getPath(), requireActivity());
                            // Set the image in the photo view.
                            blogPhotoView.setImageBitmap(bitmap);

                            // Copy the File to the blogs photo file location.
                            FileOutputStream fileOutputStream = new FileOutputStream(photoFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    /**
     * Create a chooser dialog to choose between taking a photo, choose from gallery or cancel.
     */
    private void dispatchTakePhotoIntent() {
        final String[] options = { "Take Photo", "Choose from Gallery", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Select Photo");
        builder.setItems(options, (dialog, item) -> {
            switch (options[item]) {
                case "Take Photo":
                    // Launch the take photo intent action.
                    this.openTakePhotoActivityForResult();
                    break;
                case "Choose from Gallery":
                    // Launch the choose from gallery intent action.
                    this.openChooseFromGalleryActivityForResult();
                    break;
                case "Cancel":
                    // Close the alert dialog.
                    dialog.dismiss();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + options[item]);
            }
        });
        builder.show();
    }

    /**
     * Launch a new intent to publish a message to Twitter.
     * @param title Blog title.
     * @param body Blog body.
     * @param fileUri Blog photo uri.
     */
    private void dispatchTwitterPublishIntent(String title, String body, Uri fileUri) {
        try {
            String content = title + "\n" + body;

            Intent shareToTwitterIntent = new Intent(
                    android.content.Intent.ACTION_SEND);
            // Add the text to the intent data.
            shareToTwitterIntent.putExtra(Intent.EXTRA_TEXT,
                    content);
            shareToTwitterIntent.setType("text/*");
            // Add the image to the intent data.
            if (fileUri != null) {
                shareToTwitterIntent.putExtra(Intent.EXTRA_STREAM,
                        fileUri);
                shareToTwitterIntent.setType("image/*");
            }
            // Launch the Twitter app.
            shareToTwitterIntent.setPackage("com.twitter.android");
            startActivity(shareToTwitterIntent);
        } catch (Exception exception) {
            Toast.makeText(requireActivity(), "Twitter isn't installed!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Launch a new send email Intent.
     * @param subject Blog title.
     * @param body Blog body.
     * @param attachmentUri Blog photo uri.
     */
    private void dispatchSendEmailIntent(String subject, String body, Uri attachmentUri) {
        try {
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setAction(Intent.ACTION_SEND);
            // Set the MIME type of the text and image for emails.
            // Sources from stack overflow by CommonsWare - https://stackoverflow.com/questions/8280166/intent-with-settypemessage-rfc822-for-android-api-level-before-2-3-3#:~:text=9-,First%2C%20%22to%20avoid,-a%20lot%20of
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            if (attachmentUri != null) {
                emailIntent.putExtra(Intent.EXTRA_STREAM, attachmentUri);
            }
            // Grant permissions to read the image.
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(emailIntent, "Send Email Using: "));
        } catch (Exception exception) {
            Toast.makeText(requireActivity(), "Can't send email.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Launch the intent to open the BlogList.
     */
    private void dispatchMoveBackIntent() {
        Intent moveBackIntent = new Intent(requireContext(), BlogListActivity.class);
        startActivity(moveBackIntent);
        requireActivity().finish();
    }

    /**
     * Sourced from the workshop - update the photo view from the stored photo.
     */
    private void updatePhotoView() {
        if (getActivity() != null && this.photoFile != null && this.photoFile.exists()) {
            Bitmap bitmap = PictureUtils.getScaledBitmap(this.photoFile.getPath(), getActivity());
            this.blogPhotoView.setImageBitmap(bitmap);
        }
    }
}
