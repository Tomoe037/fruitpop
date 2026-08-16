package com.nativegame.animalspop.player;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.nativegame.animalspop.timer.LivesTimer;

import java.util.HashMap;
import java.util.Map;

/** Applies one-time lives commands addressed to the current Firebase player. */
public class PlayerLivesAdminControlRepository {

    private static final String TAG = "FIREBASE_LIVES_ADMIN";
    private static final int MIN_LIVES = 0;
    private static final int MAX_LIVES = 5;

    private final LivesTimer mLivesTimer;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mFirestore;
    private final FirebaseAuth.AuthStateListener mAuthStateListener;

    private ListenerRegistration mPlayerListener;
    private String mListeningUid;
    private boolean mStarted;

    public PlayerLivesAdminControlRepository(LivesTimer livesTimer) {
        mLivesTimer = livesTimer;
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mAuthStateListener = this::onAuthStateChanged;
    }

    public void start() {
        if (mStarted) {
            return;
        }
        mStarted = true;
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    public void shutdown() {
        if (!mStarted) {
            return;
        }
        mStarted = false;
        mAuth.removeAuthStateListener(mAuthStateListener);
        removePlayerListener();
    }

    private void onAuthStateChanged(FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            removePlayerListener();
            return;
        }

        String uid = user.getUid();
        if (uid.equals(mListeningUid) && mPlayerListener != null) {
            return;
        }

        removePlayerListener();
        mListeningUid = uid;
        DocumentReference playerDocument = mFirestore.collection("players").document(uid);
        mPlayerListener = playerDocument.addSnapshotListener((snapshot, exception) -> {
            if (exception != null) {
                Log.e(TAG, "Failed to listen for lives admin command", exception);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                handleSnapshot(playerDocument, snapshot);
            }
        });
    }

    private void handleSnapshot(DocumentReference playerDocument, DocumentSnapshot snapshot) {
        Object adminControlValue = snapshot.get("adminControl");
        if (!(adminControlValue instanceof Map)) {
            return;
        }

        Map<?, ?> adminControl = (Map<?, ?>) adminControlValue;
        Object commandIdValue = adminControl.get("commandId");
        Integer requestedLives = parseLives(adminControl.get("setLives"));
        if (!(commandIdValue instanceof String)
                || ((String) commandIdValue).trim().isEmpty()
                || requestedLives == null) {
            Log.w(TAG, "Ignored invalid lives admin command");
            return;
        }

        String commandId = (String) commandIdValue;
        String lastAppliedCommandId = mLivesTimer.getLastAppliedLivesCommandId();
        if (commandId.equals(lastAppliedCommandId)) {
            Log.d(TAG, "Ignored already applied command: commandId=" + commandId);
            ensureAcknowledged(playerDocument, snapshot, commandId, requestedLives);
            ensureLivesMirrored(playerDocument, snapshot);
            return;
        }

        Log.d(TAG, "Received lives command: commandId=" + commandId
                + ", setLives=" + requestedLives);
        try {
            mLivesTimer.setLivesFromAdmin(requestedLives, commandId);
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Ignored invalid lives admin command: commandId=" + commandId,
                    exception);
            return;
        }

        int appliedLives = mLivesTimer.getLives();
        Log.d(TAG, "Applied lives command: commandId=" + commandId
                + ", lives=" + appliedLives);
        writeAppliedResult(playerDocument, commandId, appliedLives);
    }

    private Integer parseLives(Object value) {
        if (!(value instanceof Byte)
                && !(value instanceof Short)
                && !(value instanceof Integer)
                && !(value instanceof Long)) {
            return null;
        }

        long lives = ((Number) value).longValue();
        if (lives < MIN_LIVES || lives > MAX_LIVES) {
            return null;
        }
        return (int) lives;
    }

    private void ensureAcknowledged(DocumentReference playerDocument, DocumentSnapshot snapshot,
                                    String commandId, int appliedLives) {
        Object ackValue = snapshot.get("adminControlAck");
        if (ackValue instanceof Map) {
            Object acknowledgedCommandId = ((Map<?, ?>) ackValue).get("commandId");
            if (commandId.equals(acknowledgedCommandId)) {
                return;
            }
        }
        writeAck(playerDocument, commandId, appliedLives, false);
    }

    private void writeAppliedResult(DocumentReference playerDocument, String commandId,
                                    int appliedLives) {
        writeAck(playerDocument, commandId, appliedLives, true);
    }

    private void ensureLivesMirrored(DocumentReference playerDocument, DocumentSnapshot snapshot) {
        Integer remoteLives = parseLives(snapshot.get("lives"));
        int localLives = mLivesTimer.getLives();
        if (remoteLives != null && remoteLives == localLives) {
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("lives", localLives);
        playerDocument.set(update, SetOptions.merge())
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Failed to mirror applied local lives", exception));
    }

    private void writeAck(DocumentReference playerDocument, String commandId, int appliedLives,
                          boolean includeLives) {
        Map<String, Object> ack = new HashMap<>();
        ack.put("commandId", commandId);
        ack.put("appliedLives", appliedLives);
        ack.put("appliedAt", FieldValue.serverTimestamp());

        Map<String, Object> update = new HashMap<>();
        update.put("adminControlAck", ack);
        if (includeLives) {
            update.put("lives", appliedLives);
        }

        playerDocument.set(update, SetOptions.merge())
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Failed to acknowledge lives command: commandId=" + commandId,
                                exception));
    }

    private void removePlayerListener() {
        if (mPlayerListener != null) {
            mPlayerListener.remove();
            mPlayerListener = null;
        }
        mListeningUid = null;
    }
}
