package com.nativegame.animalspop.level;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.nativegame.nattyengine.level.Level;
import com.nativegame.nattyengine.level.LevelManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Oscar Liang on 2022/09/18
 */

public class MyLevelManager extends LevelManager {

    private static final String FILE_NAME = "data.xml";
    private static final String FILE_TAG = "level";

    private String mLevelTagName;

    private final Context mContext;
    private final FirebaseLevelConfigRepository mLevelConfigRepository;
    private MyLevel mLevel;

    public MyLevelManager(Context context, FirebaseLevelConfigRepository levelConfigRepository) {
        mContext = context;
        mLevelConfigRepository = levelConfigRepository;
    }

    @Override
    public Level getLevel(int level) {
        mLevelTagName = "level" + level;
        mLevel = new MyLevel(level);
        // Open file
        try {
            InputStream file = mContext.getAssets().open(FILE_NAME);
            parse(file);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        applyFirebaseTargetOverride(level);
        applyFirebaseShotOverride(level);

        return mLevel;
    }

    private void applyFirebaseTargetOverride(int level) {
        int xmlTarget = mLevel.mTarget;
        FirebaseLevelConfigRepository.LevelConfig config =
                mLevelConfigRepository.getLevelConfig(level);

        if (config == null) {
            Log.d(FirebaseLevelConfigRepository.LOG_TAG,
                    "Level " + level + " không có Firebase config, sử dụng XML target="
                            + xmlTarget);
            return;
        }

        if (!config.isEnabled()) {
            Log.w(FirebaseLevelConfigRepository.LOG_TAG,
                    "Level " + level + " đang bị disabled, sử dụng XML target=" + xmlTarget);
            return;
        }

        int firebaseTarget = config.getTargetCount();
        if (firebaseTarget > 0) {
            mLevel.mTarget = firebaseTarget;
            Log.d(FirebaseLevelConfigRepository.LOG_TAG,
                    "Level " + level + " target override: XML=" + xmlTarget
                            + ", Firebase=" + firebaseTarget);
        } else {
            Log.w(FirebaseLevelConfigRepository.LOG_TAG,
                    "Level " + level + " có Firebase target không hợp lệ=" + firebaseTarget
                            + ", sử dụng XML target=" + xmlTarget);
        }
    }

    private void applyFirebaseShotOverride(int level) {
        FirebaseLevelConfigRepository.LevelConfig config =
                mLevelConfigRepository.getLevelConfig(level);

        if (config == null || !config.isEnabled()) {
            return;
        }

        int shotLimit = config.getShotLimit();
        if (shotLimit < 5 || shotLimit > 100
                || mLevel.mPlayer == null || mLevel.mPlayer.isEmpty()) {
            return;
        }

        String xmlPlayer = mLevel.mPlayer;
        int xmlPlayerLength = xmlPlayer.length();
        String normalizedPlayer;

        if (shotLimit < xmlPlayerLength) {
            normalizedPlayer = xmlPlayer.substring(0, shotLimit);
        } else if (shotLimit == xmlPlayerLength) {
            normalizedPlayer = xmlPlayer;
        } else {
            StringBuilder playerBuilder = new StringBuilder(shotLimit);
            for (int i = 0; i < shotLimit; i++) {
                playerBuilder.append(xmlPlayer.charAt(i % xmlPlayerLength));
            }
            normalizedPlayer = playerBuilder.toString();
        }

        mLevel.mPlayer = normalizedPlayer;
        mLevel.mMove = normalizedPlayer.length();

        Log.d(FirebaseLevelConfigRepository.LOG_TAG,
                "Level " + level + " shot override: XML playerLength=" + xmlPlayerLength
                        + ", Firebase shotLimit=" + shotLimit
                        + ", normalizedPlayerLength=" + normalizedPlayer.length());
    }

    private void parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            read(parser);
        } finally {
            in.close();
        }
    }

    private void read(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, FILE_TAG);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the level tag
            if (name.equals(mLevelTagName)) {
                readLevel(parser);
            } else {
                skip(parser);
            }
        }
    }

    private void readLevel(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, mLevelTagName);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the level info tag
            setLevelInfo(name, parser.nextText());   // We pass in tag name and text
        }
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    protected void setLevelInfo(String name, String text) {
        switch (name) {
            case ("level_type"):
                mLevel.setLevelType(text);
                break;
            case ("level_tutorial"):
                mLevel.setLevelTutorial(text);
                break;
            case ("target"):
                mLevel.mTarget = Integer.parseInt(text);
                break;
            case ("player"):
                mLevel.mPlayer = text;
                mLevel.mMove = text.length();
                break;
            case ("bubble"):
                mLevel.mBubble = text;
                break;
        }
    }

}
