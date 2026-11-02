package com.mottainai.operacional.utils;

import android.net.Uri;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mottainai.operacional.R;

/** Cloudinary avatar with a device-local URL reference keyed by Firebase UID. */
public final class ProfilePhoto {
    private final Fragment fragment;
    private final ActivityResultLauncher<String[]> picker;
    private ImageView image;
    private String pendingUid;
    private boolean uploading;

    public ProfilePhoto(Fragment fragment) {
        this.fragment = fragment;
        picker = fragment.registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            String uid = uid();
            if (uri == null || uid == null || !uid.equals(pendingUid) || !fragment.isAdded()) return;
            if (uploading) return;
            uploading = true;
            android.content.Context context = fragment.requireContext().getApplicationContext();
            Toast.makeText(context, R.string.profile_photo_uploading, Toast.LENGTH_SHORT).show();
            // PhotoStorageRepository validates MIME and limits images to 8 MB. Read off the UI thread.
            new Thread(() -> new com.mottainai.operacional.storage.PhotoStorageRepository()
                    .uploadPhoto(context, uri, "profiles", uid,
                            new com.mottainai.operacional.storage.PhotoStorageRepository.PhotoCallback() {
                        @Override public void onSuccess(String url) {
                            complete(() -> {
                                if (!uid.equals(uid())) return;
                                context.getSharedPreferences("profile_photos", 0).edit()
                                        .putString(uid, url).apply();
                                refresh();
                                Toast.makeText(context, R.string.profile_photo_saved, Toast.LENGTH_LONG).show();
                            });
                        }
                        @Override public void onError(String message) {
                            complete(() -> {
                                if (uid.equals(uid())) Toast.makeText(context,
                                        message==null?context.getString(R.string.profile_photo_error):message,
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    }), "profile-photo-upload").start();
        });
    }

    public void bind(TextView initials) {
        ViewGroup parent = (ViewGroup) initials.getParent();
        int index = parent.indexOfChild(initials);
        ViewGroup.LayoutParams params = initials.getLayoutParams();
        parent.removeView(initials);
        FrameLayout frame = new FrameLayout(initials.getContext());
        parent.addView(frame, index, params);
        frame.addView(initials, new FrameLayout.LayoutParams(-1, -1));
        image = new ImageView(initials.getContext());
        frame.addView(image, new FrameLayout.LayoutParams(-1, -1));
        frame.setContentDescription(fragment.getString(R.string.profile_photo_change));
        frame.setFocusable(true);
        frame.setOnClickListener(v -> {
            pendingUid = uid();
            if (pendingUid != null && !uploading) picker.launch(new String[]{"image/*"});
        });
        refresh();
    }

    public void refresh() {
        if (image == null || !fragment.isAdded()) return;
        Glide.with(fragment).clear(image);
        image.setImageDrawable(null);
        String uid = uid();
        String uri = uid == null ? null : fragment.requireContext()
                .getSharedPreferences("profile_photos", 0).getString(uid, null);
        if (uri != null) Glide.with(fragment).load(Uri.parse(uri)).circleCrop().into(image);
    }

    public void clearView() { image = null; }

    private void complete(Runnable action) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            uploading = false;
            action.run();
        });
    }

    private static String uid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? null : user.getUid();
    }
}
