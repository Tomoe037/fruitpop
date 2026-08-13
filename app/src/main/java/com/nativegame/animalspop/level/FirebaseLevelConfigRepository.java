package com.nativegame.animalspop.level;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseLevelConfigRepository {

    public static final String LOG_TAG = "FIREBASE_LEVEL_CONFIG";

    private static final int LEVEL_ONE = 1;

    private final FirebaseFirestore mFirestore;
    private volatile Integer mLevelOneTargetCount;

    public FirebaseLevelConfigRepository(FirebaseFirestore firestore) {
        mFirestore = firestore;
    }

    public void preloadLevelOne() {
        mFirestore.collection("levels")
                .document("level_1")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long number = documentSnapshot.getLong("number");
                    Long targetCount = documentSnapshot.getLong("targetCount");

                    if (documentSnapshot.exists()
                            && number != null
                            && number == LEVEL_ONE
                            && targetCount != null
                            && targetCount > 0
                            && targetCount <= Integer.MAX_VALUE) {
                        mLevelOneTargetCount = targetCount.intValue();
                        Log.d(LOG_TAG, "Đã cache targetCount cho level 1");
                    } else {
                        mLevelOneTargetCount = null;
                        Log.w(LOG_TAG, "Dữ liệu levels/level_1 không hợp lệ; sẽ dùng target từ XML");
                    }
                })
                .addOnFailureListener(exception -> {
                    mLevelOneTargetCount = null;
                    Log.e(LOG_TAG, "Không thể tải levels/level_1; sẽ dùng target từ XML", exception);
                });
    }

    public Integer getTargetCount(int level) {
        return level == LEVEL_ONE ? mLevelOneTargetCount : null;
    }
}
