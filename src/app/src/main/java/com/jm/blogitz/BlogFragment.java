package com.jm.blogitz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
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
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_SELECT_PHOTO = 2;
    private Blog blog;
    private EditText titleTextView;
    private EditText bodyTextView;
    private MaterialButton selectPhotoButton;
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
        UUID blogId = (UUID) getArguments().getSerializable(ARG_BLOG_ID);
        this.blog = BlogLab.get(getActivity()).getBlog(blogId);
        this.photoFile = BlogLab.get(getActivity()).getPhotoFile(this.blog);
    }

    @Override
    public void onPause() {
        super.onPause();
        BlogLab.get(getActivity()).updateBlog(this.blog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog, container, false);

        this.titleTextView = view.findViewById(R.id.blog_title_edit_text);
        this.titleTextView.setText(this.blog.getTitle());
        this.titleTextView.addTextChangedListener(new TextWatcher() {
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

        this.bodyTextView = view.findViewById(R.id.blog_body_edit_text);
        this.bodyTextView.setText(this.blog.getBody());
        this.bodyTextView.addTextChangedListener(new TextWatcher() {
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

        this.selectPhotoButton = view.findViewById(R.id.blog_select_photo_button);
        this.selectPhotoButton.setOnClickListener(v -> dispatchTakePhotoIntent());

        this.blogPhotoView = view.findViewById(R.id.blog_image_view);
        this.updatePhotoView();

        return view;
    }

    private void dispatchTakePhotoIntent() {
//        PackageManager packageManager = getActivity().getPackageManager();
//        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        boolean canTakePhoto = this.photoFile != null && captureImage.resolveActivity(packageManager) != null;

        final String[] options = { "Take Photo", "Choose from Gallery", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Select Photo");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri photoFileUri = FileProvider.getUriForFile(getActivity(), "com.jm.blogitz.fileprovider", this.photoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager()
                        .queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity: cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName, photoFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(captureImage, REQUEST_TAKE_PHOTO);
            } else if (options[item].equals("Choose from Gallery")) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Photo"), REQUEST_SELECT_PHOTO);
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_TAKE_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(), "com.jm.blogitz.fileprovider", this.photoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            this.updatePhotoView();
        } else if (requestCode == REQUEST_SELECT_PHOTO) {
            if (data == null) {
                return;
            }

            try {
                Uri uri = data.getData();
                File file = FileUtils.from(getActivity(), uri);
                Bitmap bitmap = PictureUtils.getScaledBitmap(file.getPath(), getActivity());
                this.blogPhotoView.setImageBitmap(bitmap);

                FileOutputStream fileOutputStream = new FileOutputStream(this.photoFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePhotoView() {
        if (this.photoFile == null || !this.photoFile.exists()) {
            this.blogPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(this.photoFile.getPath(), getActivity());
            this.blogPhotoView.setImageBitmap(bitmap);
        }
    }
}
