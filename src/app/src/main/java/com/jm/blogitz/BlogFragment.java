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
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class BlogFragment extends Fragment {
    private static final String ARG_BLOG_ID = "blog_id";
    private Blog blog;
    private File photoFile;
    private ImageView blogPhotoView;

    public static BlogFragment newInstance(UUID blogId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_BLOG_ID, blogId);
        BlogFragment fragment = new BlogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            throw new NullPointerException();
        }
        UUID blogId = (UUID) getArguments().getSerializable(ARG_BLOG_ID);
        this.blog = BlogLab.get(getActivity()).getBlog(blogId);
        this.photoFile = BlogLab.get(getActivity()).getPhotoFile(this.blog);
    }

    @Override
    public void onPause() {
        super.onPause();

        this.blog.validateBlog(getActivity());
        BlogLab.get(getActivity()).updateBlog(this.blog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog, container, false);

        EditText titleTextView = view.findViewById(R.id.blog_title_edit_text);
        titleTextView.setText(this.blog.getTitle());
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

        EditText bodyTextView = view.findViewById(R.id.blog_body_edit_text);
        bodyTextView.setText(this.blog.getBody());
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

        MaterialButton selectPhotoButton = view.findViewById(R.id.blog_select_photo_button);
        selectPhotoButton.setOnClickListener(v -> dispatchTakePhotoIntent());

        this.blogPhotoView = view.findViewById(R.id.blog_image_view);
        this.updatePhotoView();

        return view;
    }

    public void openTakePhotoActivityForResult() {
        Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoFileUri = FileProvider.getUriForFile(requireActivity(), "com.jm.blogitz.fileprovider", this.photoFile);
        captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
        List<ResolveInfo> cameraActivities = requireActivity()
                .getPackageManager()
                .queryIntentActivities(captureImageIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo activity : cameraActivities) {
            requireActivity().grantUriPermission(activity.activityInfo.packageName, photoFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        takePhotoResultLauncher.launch(captureImageIntent);
    }

    public void openChooseFromGalleryActivityForResult() {
        Intent selectPhotoIntent = new Intent();
        selectPhotoIntent.setType("image/*");
        selectPhotoIntent.setAction(Intent.ACTION_GET_CONTENT);

        chooseFromGalleryResultLauncher.launch(selectPhotoIntent);
    }

    ActivityResultLauncher<Intent> takePhotoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = FileProvider.getUriForFile(requireActivity(), "com.jm.blogitz.fileprovider", photoFile);
                        requireActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        updatePhotoView();
                    }
                }
            });

    ActivityResultLauncher<Intent> chooseFromGalleryResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }

                        try {
                            Uri uri = data.getData();
                            File file = FileUtils.from(requireActivity(), uri);
                            Bitmap bitmap = PictureUtils.getScaledBitmap(file.getPath(), requireActivity());
                            blogPhotoView.setImageBitmap(bitmap);

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

    private void dispatchTakePhotoIntent() {
        PackageManager packageManager = requireActivity().getPackageManager();
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureImage.resolveActivity(packageManager) != null) {

        }
        boolean canTakePhoto = this.photoFile != null && captureImage.resolveActivity(packageManager) != null;

        final String[] options = { "Take Photo", "Choose from Gallery", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Select Photo");
        builder.setItems(options, (dialog, item) -> {
            switch (options[item]) {
                case "Take Photo":
                    this.openTakePhotoActivityForResult();
                    break;
                case "Choose from Gallery":
                    this.openChooseFromGalleryActivityForResult();
                    break;
                case "Cancel":
                    dialog.dismiss();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + options[item]);
            }
        });
        builder.show();
    }

    private void updatePhotoView() {
        if (getActivity() != null && this.photoFile != null && this.photoFile.exists()) {
            Bitmap bitmap = PictureUtils.getScaledBitmap(this.photoFile.getPath(), getActivity());
            this.blogPhotoView.setImageBitmap(bitmap);
        }
    }
}
