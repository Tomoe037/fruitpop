package com.nativegame.animalspop.auth;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Initializes the anonymous Firebase session used to identify a player.
 */
public class FirebasePlayerAuth {

    private static final String TAG = "FIREBASE_PLAYER_AUTH";

    private final FirebaseAuth mAuth;

    public FirebasePlayerAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void signInIfNeeded() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Đã có phiên đăng nhập ẩn danh");
            Log.d(TAG, "UID = " + currentUser.getUid());
            return;
        }

        mAuth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Anonymous auth thất bại", task.getException());
                        return;
                    }

                    FirebaseUser signedInUser = mAuth.getCurrentUser();
                    if (signedInUser == null) {
                        Log.e(TAG, "Anonymous auth thành công nhưng không lấy được user");
                        return;
                    }

                    Log.d(TAG, "Anonymous auth thành công");
                    Log.d(TAG, "UID = " + signedInUser.getUid());
                });
    }
}
