package com.conversify.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;

import com.conversify.BuildConfig;
import com.conversify.R;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

public class ImagePicker {
    private File imageFile;

    /**
     * Activity object that will be used while calling startActivityForResult(). Activity then will
     * receive the callbacks to its own onActivityResult() and is responsible of calling the
     * onActivityResult() of the ImagePicker for handling result and being notified.
     */
    private Activity context;

    /**
     * Fragment object that will be used while calling startActivityForResult(). Fragment then will
     * receive the callbacks to its own onActivityResult() and is responsible of calling the
     * onActivityResult() of the ImagePicker for handling result and being notified.
     */
    private Fragment fragment;

    private AlertDialog pickerDialog;

    private ImagePickerListener imagePickerListener;

    public ImagePicker(@NonNull Activity activity) {
        this.context = activity;
        setupPickerDialog();
    }

    public ImagePicker(@NonNull Fragment fragment) {
        this.fragment = fragment;
        this.context = fragment.getActivity();
        setupPickerDialog();
    }

    public void setImagePickerListener(@NonNull ImagePickerListener imagePickerListener) {
        this.imagePickerListener = imagePickerListener;
    }

    /**
     * Removes all listeners and references
     */
    public void clear() {
        dismissImagePicker();
        this.imagePickerListener = null;
        this.context = null;
        this.fragment = null;
        this.imageFile = null;
        this.pickerDialog = null;
    }

    private void setupPickerDialog() {
        String[] pickerItems = {
                context.getString(R.string.image_picker_dialog_camera),
                context.getString(R.string.image_picker_dialog_gallery),
                context.getString(android.R.string.cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.image_picker_dialog_select_your_choice);
        builder.setItems(pickerItems, (dialog, which) -> {
            switch (which) {
                case 0:
                    openCamera();
                    break;

                case 1:
                    openGallery();
                    break;
            }
            dialog.dismiss();
        });
        pickerDialog = builder.create();
    }

    public void showImagePicker() {
        if (pickerDialog != null)
            pickerDialog.show();
    }

    public void dismissImagePicker() {
        if (pickerDialog != null && pickerDialog.isShowing())
            pickerDialog.dismiss();
    }

    /**
     * Returns the gallery/camera imageFile.
     * <p>
     * File object might be null if method is called before calling the openCamera() or openGallery()
     */
    @Nullable
    public File getImageFile() {
        return imageFile;
    }

    /**
     * Set the image file. Used in case the existing file needs to be updated with compressed/resized image file
     */
    public void setImageFile(@NonNull File imageFile) {
        this.imageFile = imageFile;
    }

    /**
     * Handles the result of events that the Activity or Fragment receives on its own
     * onActivityResult(). This method must be called inside the onActivityResult()
     * of the container Activity or Fragment.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == AppConstants.REQ_CODE_GALLERY_IMAGE) && (resultCode == Activity.RESULT_OK)) {
            if (data.getData() != null) {
                String imagePath = getImagePathFromGallery(context, data.getData());
                if (imagePath != null) {
                    imageFile = new File(imagePath);
                    imagePickerListener.onImageSelectedFromPicker(imageFile);
                }
            }
        } else if ((requestCode == AppConstants.REQ_CODE_CAMERA_IMAGE) && (resultCode == Activity.RESULT_OK)) {
            if (imageFile != null) {
                imagePickerListener.onImageSelectedFromPicker(imageFile);
                revokeUriPermission();
            }
        }
    }

    /**
     * Save the image to device external cache
     */
    private void openCamera() {
        checkListener();

        File imageDirectory = context.getExternalCacheDir();

        if (imageDirectory != null)
            startCameraIntent(imageDirectory.getAbsolutePath());
    }

    /**
     * Save the image to a custom directory
     */
    private void openCamera(@NonNull final String imageDirectory) {
        checkListener();

        startCameraIntent(imageDirectory);
    }

    private void startCameraIntent(@NonNull final String imageDirectory) {
        try {
            imageFile = createImageFile(imageDirectory);

            if (fragment == null)
                context.startActivityForResult(getCameraIntent(), AppConstants.REQ_CODE_CAMERA_IMAGE);
            else
                fragment.startActivityForResult(getCameraIntent(), AppConstants.REQ_CODE_CAMERA_IMAGE);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /**
     * Returns the camera intent using FileProvider to avoid the FileUriExposedException in Android N and above
     */
    private Intent getCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Put the uri of the image file as intent extra
        Uri imageUri = FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider",
                imageFile);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        // Get a list of all the camera apps
        List<ResolveInfo> resolvedIntentActivities =
                context.getPackageManager()
                        .queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);

        // Grant uri read/write permissions to the camera apps
        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;

            context.grantUriPermission(packageName, imageUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        return cameraIntent;
    }

    private void openGallery() {
        checkListener();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            if (fragment == null)
                context.startActivityForResult(intent, AppConstants.REQ_CODE_GALLERY_IMAGE);
            else
                fragment.startActivityForResult(intent, AppConstants.REQ_CODE_GALLERY_IMAGE);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    private String getImagePathFromGallery(@NonNull final Context context, @NonNull final Uri imageUri) {
        String imagePath = null;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(imageUri, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imagePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return imagePath;
    }

    @Nullable
    private File createImageFile(@NonNull final String directory) throws IOException {
        File imageFile = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File storageDir = new File(directory);
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    return null;
                }
            }
            String imageFileName = "IMG_ANDROID_" + UUID.randomUUID().toString();

            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        }
        return imageFile;
    }

    /**
     * Revoke access permission for the content URI to the specified package otherwise the permission won't be
     * revoked until the device restarts.
     */
    private void revokeUriPermission() {
        context.revokeUriPermission(FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider", imageFile),
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    private void checkListener() {
        if (imagePickerListener == null) {
            throw new RuntimeException("ImagePickerListener must be set before calling openCamera() or openGallery()");
        }
    }

    public interface ImagePickerListener {
        void onImageSelectedFromPicker(@NonNull File imageFile);
    }
}