package com.nativegame.animalspop.level;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseLevelConfigRepository {

    public static final String LOG_TAG = "FIREBASE_LEVEL_CONFIG";
    private static final int NO_BASE_LEVEL = -1;
    private static final int MIN_BASE_LEVEL = 1;
    private static final int MAX_BASE_LEVEL = 15;
    private static final int NO_SHOT_LIMIT_OVERRIDE = -1;

    private final FirebaseFirestore mFirestore;
    private static volatile Map<Integer, LevelConfig> sLevelConfigs = Collections.emptyMap();
    private static volatile boolean sCacheReady;

    public FirebaseLevelConfigRepository(FirebaseFirestore firestore) {
        mFirestore = firestore;
    }

    public void preloadAllLevels() {
        preloadAllLevels(null);
    }

    public void preloadAllLevels(Runnable onCacheReady) {
        sCacheReady = false;
        mFirestore.collection("levels")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<Integer, LevelConfig> loadedConfigs = new HashMap<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Long number = document.getLong("number");
                        Object baseLevel = document.get("baseLevel");
                        Long shotLimit = document.getLong("shotLimit");
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
                            int cachedBaseLevel = toBaseLevel(baseLevel, levelNumber);
                            int cachedShotLimit = toShotLimitOverride(shotLimit);
                            LevelConfig config = new LevelConfig(levelNumber, cachedBaseLevel,
                                    cachedShotLimit, targetCount.intValue(), enabled);
                            loadedConfigs.put(levelNumber, config);
                            Log.d(LOG_TAG, "Level " + levelNumber + " config: baseLevel="
                                    + cachedBaseLevel + ", shotLimit=" + cachedShotLimit
                                    + ", targetCount=" + config.getTargetCount() + ", enabled="
                                    + config.isEnabled());
                        } else {
                            Log.w(LOG_TAG, "Firebase config không hợp lệ tại "
                                    + document.getReference().getPath() + "; bỏ qua document");
                        }
                    }

                    sLevelConfigs = Collections.unmodifiableMap(loadedConfigs);
                    sCacheReady = true;
                    if (onCacheReady != null) {
                        onCacheReady.run();
                    }
                    Log.d(LOG_TAG, "Đã cache " + loadedConfigs.size()
                            + " cấu hình level từ Firestore");
                })
                .addOnFailureListener(exception -> {
                    sLevelConfigs = Collections.emptyMap();
                    sCacheReady = false;
                    Log.e(LOG_TAG, "Không thể tải collection levels; sẽ dùng target từ XML",
                            exception);
                });
    }

    public LevelConfig getLevelConfig(int level) {
        return sLevelConfigs.get(level);
    }

    public static boolean isCacheReady() {
        return sCacheReady;
    }

    public static List<LevelConfig> getCachedLevelConfigs() {
        return new ArrayList<>(sLevelConfigs.values());
    }

    private static int toBaseLevel(Object baseLevel, int levelNumber) {
        if (baseLevel instanceof Long) {
            long value = (Long) baseLevel;
            if (value >= MIN_BASE_LEVEL && value <= MAX_BASE_LEVEL) {
                return (int) value;
            }
        }

        if (levelNumber >= MIN_BASE_LEVEL && levelNumber <= MAX_BASE_LEVEL) {
            return levelNumber;
        }
        return NO_BASE_LEVEL;
    }

    private static int toShotLimitOverride(Long shotLimit) {
        if (shotLimit == null || shotLimit <= 0 || shotLimit > Integer.MAX_VALUE) {
            return NO_SHOT_LIMIT_OVERRIDE;
        }
        return shotLimit.intValue();
    }

    public static Boolean getCachedEnabled(int level) {
        LevelConfig config = sLevelConfigs.get(level);
        return config != null ? config.isEnabled() : null;
    }

    public static final class LevelConfig {

        private final int mNumber;
        private final int mBaseLevel;
        private final int mShotLimit;
        private final int mTargetCount;
        private final boolean mEnabled;

        private LevelConfig(int number, int baseLevel, int shotLimit, int targetCount,
                            boolean enabled) {
            mNumber = number;
            mBaseLevel = baseLevel;
            mShotLimit = shotLimit;
            mTargetCount = targetCount;
            mEnabled = enabled;
        }

        public int getNumber() {
            return mNumber;
        }

        public int getBaseLevel() {
            return mBaseLevel;
        }

        public int getShotLimit() {
            return mShotLimit;
        }

        public int getTargetCount() {
            return mTargetCount;
        }

        public boolean isEnabled() {
            return mEnabled;
        }
    }
}
