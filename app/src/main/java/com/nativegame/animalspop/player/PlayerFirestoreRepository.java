package com.nativegame.animalspop.player;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.nativegame.animalspop.database.DatabaseHelper;
import com.nativegame.animalspop.item.Item;
import com.nativegame.animalspop.timer.LivesTimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Synchronizes the current SQLite player snapshot to Cloud Firestore.
 */
public class PlayerFirestoreRepository {

    private static final String TAG = "FIREBASE_PLAYER_SYNC";
    private static final int TOTAL_LEVEL = 15;

    private final DatabaseHelper mDatabaseHelper;
    private final LivesTimer mLivesTimer;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mFirestore;
    private final ExecutorService mDatabaseExecutor;

    public PlayerFirestoreRepository(DatabaseHelper databaseHelper, LivesTimer livesTimer) {
        mDatabaseHelper = databaseHelper;
        mLivesTimer = livesTimer;
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mDatabaseExecutor = Executors.newSingleThreadExecutor();
    }

    public void syncPlayer() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Không sync player: chưa có authenticated user");
            return;
        }

        String uid = currentUser.getUid();
        int lives = mLivesTimer.getLives();
        try {
            mDatabaseExecutor.execute(() -> syncPlayerSnapshot(uid, lives));
        } catch (RuntimeException exception) {
            Log.e(TAG, "Không thể lên lịch player sync cho UID = " + uid, exception);
        }
    }

    private void syncPlayerSnapshot(String uid, int lives) {
        try {
            ArrayList<Integer> levelStars = mDatabaseHelper.getAllLevelStar();
            int currentLevel = Math.min(levelStars.size() + 1, TOTAL_LEVEL);
            int totalStars = 0;
            for (int star : levelStars) {
                totalStars += star;
            }

            int coins = mDatabaseHelper.getItemNum(Item.COIN);

            Map<String, Object> boosters = new HashMap<>();
            boosters.put("colorBall", mDatabaseHelper.getItemNum(Item.COLOR_BALL));
            boosters.put("fireball", mDatabaseHelper.getItemNum(Item.FIREBALL));
            boosters.put("bomb", mDatabaseHelper.getItemNum(Item.BOMB));
            boosters.put("cutBall", mDatabaseHelper.getItemNum(Item.CUT_BALL));
            boosters.put("lightningBall", mDatabaseHelper.getItemNum(Item.LIGHTNING_BALL));

            Map<String, Object> player = new HashMap<>();
            player.put("playerId", uid);
            player.put("currentLevel", currentLevel);
            player.put("totalStars", totalStars);
            player.put("coins", coins);
            player.put("lives", lives);
            player.put("boosters", boosters);
            player.put("updatedAt", FieldValue.serverTimestamp());

            int syncedCurrentLevel = currentLevel;
            int syncedTotalStars = totalStars;
            mFirestore.collection("players")
                    .document(uid)
                    .set(player, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "Player sync thành công");
                        Log.d(TAG, "UID = " + uid);
                        Log.d(TAG, "currentLevel = " + syncedCurrentLevel);
                        Log.d(TAG, "totalStars = " + syncedTotalStars);
                        Log.d(TAG, "coins = " + coins);
                        Log.d(TAG, "lives = " + lives);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Player sync thất bại khi ghi Firestore cho UID = " + uid,
                                    exception));
        } catch (RuntimeException exception) {
            Log.e(TAG, "Player sync thất bại khi đọc snapshot SQLite cho UID = " + uid,
                    exception);
        }
    }

    public void shutdown() {
        mDatabaseExecutor.shutdown();
    }
}
