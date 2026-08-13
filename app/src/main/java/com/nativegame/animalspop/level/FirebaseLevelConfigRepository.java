package com.nativegame.animalspop.level;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FirebaseLevelConfigRepository {

    public static final String LOG_TAG = "FIREBASE_LEVEL_CONFIG";

    private final FirebaseFirestore mFirestore;
    private static volatile Map<Integer, LevelConfig> sLevelConfigs = Collections.emptyMap();

    public FirebaseLevelConfigRepository(FirebaseFirestore firestore) {
        mFirestore = firestore;
    }

    public void preloadAllLevels() {
        mFirestore.collection("levels")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<Integer, LevelConfig> loadedConfigs = new HashMap<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Long number = document.getLong("number");
                        Long targetCount = document.getLong("targetCount");
                        Boolean enabled = document.getBoolean("enabled");

                        if (number != null
                                && number > 0
                                && number <= Integer.MAX_VALUE
                                && targetCount != null
                                && targetCount >= Integer.MIN_VALUE
                                && targetCount <= Integer.MAX_VALUE
                                && enabled != null) {
                            int levelNumber = number.intValue();
                            loadedConfigs.put(levelNumber,
                                    new LevelConfig(levelNumber, targetCount.intValue(), enabled));
                        } else {
                            Log.w(LOG_TAG, "Firebase config không hợp lệ tại "
                                    + document.getReference().getPath() + "; bỏ qua document");
                        }
                    }

                    sLevelConfigs = Collections.unmodifiableMap(loadedConfigs);
                    Log.d(LOG_TAG, "Đã cache " + loadedConfigs.size()
                            + " cấu hình level từ Firestore");
                })
                .addOnFailureListener(exception -> {
                    sLevelConfigs = Collections.emptyMap();
                    Log.e(LOG_TAG, "Không thể tải collection levels; sẽ dùng target từ XML",
                            exception);
                });
    }

    public LevelConfig getLevelConfig(int level) {
        return sLevelConfigs.get(level);
    }

    public static Boolean getCachedEnabled(int level) {
        LevelConfig config = sLevelConfigs.get(level);
        return config != null ? config.isEnabled() : null;
    }

    public static final class LevelConfig {

        private final int mNumber;
        private final int mTargetCount;
        private final boolean mEnabled;

        private LevelConfig(int number, int targetCount, boolean enabled) {
            mNumber = number;
            mTargetCount = targetCount;
            mEnabled = enabled;
        }

        public int getNumber() {
            return mNumber;
        }

        public int getTargetCount() {
            return mTargetCount;
        }

        public boolean isEnabled() {
            return mEnabled;
        }
    }
}
