package com.nativegame.animalspop.game.player.booster;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nativegame.animalspop.MainActivity;
import com.nativegame.animalspop.R;
import com.nativegame.animalspop.item.Item;
import com.nativegame.animalspop.ui.UIEffect;
import com.nativegame.animalspop.database.DatabaseHelper;
import com.nativegame.animalspop.game.bubble.BubbleSystem;
import com.nativegame.nattyengine.Game;

/**
 * Created by Oscar Liang on 2022/09/18
 */

public class BoosterManager {

    private final Activity mActivity;
    private final DatabaseHelper mDatabaseHelper;

    // Booster button
    private final ImageButton mBtnColorBubble;
    private final ImageButton mBtnFireBubble;
    private final ImageButton mBtnBombBubble;
    private final ImageButton mBtnCutBubble;

    // Booster text
    private final TextView mTxtColorBubble;
    private final TextView mTxtFireBubble;
    private final TextView mTxtBombBubble;
    private final TextView mTxtCutBubble;

    // Booster
    private final ColorBubble mColorBubble;
    private final FireBubble mFireBubble;
    private final BombBubble mBombBubble;
    private final CutBubble mCutBubble;

    // Booster number
    private int mColorBubbleNum;
    private int mFireBubbleNum;
    private int mBombBubbleNum;
    private int mCutBubbleNum;

    private Booster mBooster;   // A pointer to current booster using

    private enum Booster {
        COLOR_BUBBLE,
        FIRE_BUBBLE,
        BOMB_BUBBLE,
        CUT_BUBBLE
    }

    public BoosterManager(BubbleSystem bubbleSystem, Game game) {
        mActivity = game.getGameActivity();
        mDatabaseHelper = ((MainActivity) mActivity).getDatabaseHelper();

        // Init booster button
        mBtnColorBubble = (ImageButton) mActivity.findViewById(R.id.btn_color_bubble);
        mBtnFireBubble = (ImageButton) mActivity.findViewById(R.id.btn_fire_bubble);
        mBtnBombBubble = (ImageButton) mActivity.findViewById(R.id.btn_bomb_bubble);
        mBtnCutBubble = (ImageButton) mActivity.findViewById(R.id.btn_cut_bubble);

        // Init booster num text
        mTxtColorBubble = (TextView) mActivity.findViewById(R.id.txt_color_bubble);
        mTxtFireBubble = (TextView) mActivity.findViewById(R.id.txt_fire_bubble);
        mTxtBombBubble = (TextView) mActivity.findViewById(R.id.txt_bomb_bubble);
        mTxtCutBubble = (TextView) mActivity.findViewById(R.id.txt_cut_bubble);

        // Init booster bubble
        mColorBubble = new ColorBubble(bubbleSystem, game);
        mFireBubble = new FireBubble(bubbleSystem, game);
        mBombBubble = new BombBubble(bubbleSystem, game);
        mCutBubble = new CutBubble(bubbleSystem, game);

        init();
    }

    private void init() {
        // Init button listener
        mBtnColorBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mColorBubbleNum == 0 || !mColorBubble.getEnable()) {
                    return;
                }
                if (mBooster == null) {
                    mColorBubble.addToGame();
                    mBooster = Booster.COLOR_BUBBLE;
                    lockButton();
                } else if (mBooster == Booster.COLOR_BUBBLE) {
                    mColorBubble.removeFromGame();
                    unlockButton();
                    mBooster = null;
                }
            }
        });
        mBtnFireBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFireBubbleNum == 0 || !mFireBubble.getEnable()) {
                    return;
                }
                if (mBooster == null) {
                    // Init the fire bubble
                    mFireBubble.addToGame();
                    mBooster = Booster.FIRE_BUBBLE;
                    lockButton();
                } else if (mBooster == Booster.FIRE_BUBBLE) {
                    // Remove the fire bubble
                    mFireBubble.removeFromGame();
                    unlockButton();
                    mBooster = null;
                }
            }
        });
        mBtnBombBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBombBubbleNum == 0 || !mBombBubble.getEnable()) {
                    return;
                }
                if (mBooster == null) {
                    mBombBubble.addToGame();
                    mBooster = Booster.BOMB_BUBBLE;
                    lockButton();
                } else if (mBooster == Booster.BOMB_BUBBLE) {
                    mBombBubble.removeFromGame();
                    unlockButton();
                    mBooster = null;
                }
            }
        });
        mBtnCutBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCutBubbleNum == 0 || !mCutBubble.getEnable()) {
                    return;
                }
                if (mBooster == null) {
                    mCutBubble.addToGame();
                    mBooster = Booster.CUT_BUBBLE;
                    lockButton();
                } else if (mBooster == Booster.CUT_BUBBLE) {
                    mCutBubble.removeFromGame();
                    unlockButton();
                    mBooster = null;
                }
            }
        });

        // Init button effect
        UIEffect.createButtonEffect(mBtnColorBubble);
        UIEffect.createButtonEffect(mBtnFireBubble);
        UIEffect.createButtonEffect(mBtnBombBubble);
        UIEffect.createButtonEffect(mBtnCutBubble);

        // Update state
        UIEffect.clearColorFilter(mBtnColorBubble);
        UIEffect.clearColorFilter(mBtnFireBubble);
        UIEffect.clearColorFilter(mBtnBombBubble);
        UIEffect.clearColorFilter(mBtnCutBubble);
        UIEffect.clearColorFilter(mTxtColorBubble);
        UIEffect.clearColorFilter(mTxtFireBubble);
        UIEffect.clearColorFilter(mTxtBombBubble);
        UIEffect.clearColorFilter(mTxtCutBubble);

        initBoosterText();
    }

    public void initBoosterText() {
        // Init booster num from db
        mColorBubbleNum = mDatabaseHelper.getItemNum(Item.COLOR_BALL);
        mFireBubbleNum = mDatabaseHelper.getItemNum(Item.FIREBALL);
        mBombBubbleNum = mDatabaseHelper.getItemNum(Item.BOMB);
        mCutBubbleNum = mDatabaseHelper.getItemNum(Item.CUT_BALL);

        // Init booster num text
        mTxtColorBubble.setText(String.valueOf(mColorBubbleNum));
        mTxtFireBubble.setText(String.valueOf(mFireBubbleNum));
        mTxtBombBubble.setText(String.valueOf(mBombBubbleNum));
        mTxtCutBubble.setText(String.valueOf(mCutBubbleNum));
    }

    public void setEnable(boolean enable) {
        // We set the booster enable here, since booster may not available in engine
        mFireBubble.setEnable(enable);
        mBombBubble.setEnable(enable);
        mColorBubble.setEnable(enable);
        mCutBubble.setEnable(enable);
    }

    public void consumeBooster() {
        updateBoosterNum();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateBoosterText();
            }
        });
    }

    private void updateBoosterNum() {
        switch (mBooster) {
            case COLOR_BUBBLE:
                mColorBubbleNum--;
                mDatabaseHelper.updateItemNum(Item.COLOR_BALL, mColorBubbleNum);
                break;
            case FIRE_BUBBLE:
                mFireBubbleNum--;
                mDatabaseHelper.updateItemNum(Item.FIREBALL, mFireBubbleNum);
                break;
            case BOMB_BUBBLE:
                mBombBubbleNum--;
                mDatabaseHelper.updateItemNum(Item.BOMB, mBombBubbleNum);
                break;
            case CUT_BUBBLE:
                mCutBubbleNum--;
                mDatabaseHelper.updateItemNum(Item.CUT_BALL, mCutBubbleNum);
                break;
        }
    }

    private void updateBoosterText() {
        switch (mBooster) {
            case COLOR_BUBBLE:
                mTxtColorBubble.setText(String.valueOf(mColorBubbleNum));
                unlockButton();
                break;
            case FIRE_BUBBLE:
                mTxtFireBubble.setText(String.valueOf(mFireBubbleNum));
                unlockButton();
                break;
            case BOMB_BUBBLE:
                mTxtBombBubble.setText(String.valueOf(mBombBubbleNum));
                unlockButton();
                break;
            case CUT_BUBBLE:
                mTxtCutBubble.setText(String.valueOf(mCutBubbleNum));
                unlockButton();
                break;
        }
        mBooster = null;
    }

    private void lockButton() {
        switch (mBooster) {
            case FIRE_BUBBLE:
                UIEffect.createColorFilter(mBtnBombBubble);
                UIEffect.createColorFilter(mBtnColorBubble);
                UIEffect.createColorFilter(mBtnCutBubble);
                UIEffect.createColorFilter(mTxtBombBubble);
                UIEffect.createColorFilter(mTxtColorBubble);
                UIEffect.createColorFilter(mTxtCutBubble);
                mBtnBombBubble.setEnabled(false);
                mBtnColorBubble.setEnabled(false);
                mBtnCutBubble.setEnabled(false);
                break;
            case BOMB_BUBBLE:
                UIEffect.createColorFilter(mBtnFireBubble);
                UIEffect.createColorFilter(mBtnColorBubble);
                UIEffect.createColorFilter(mBtnCutBubble);
                UIEffect.createColorFilter(mTxtFireBubble);
                UIEffect.createColorFilter(mTxtColorBubble);
                UIEffect.createColorFilter(mTxtCutBubble);
                mBtnFireBubble.setEnabled(false);
                mBtnColorBubble.setEnabled(false);
                mBtnCutBubble.setEnabled(false);
                break;
            case COLOR_BUBBLE:
                UIEffect.createColorFilter(mBtnBombBubble);
                UIEffect.createColorFilter(mBtnFireBubble);
                UIEffect.createColorFilter(mBtnCutBubble);
                UIEffect.createColorFilter(mTxtBombBubble);
                UIEffect.createColorFilter(mTxtFireBubble);
                UIEffect.createColorFilter(mTxtCutBubble);
                mBtnBombBubble.setEnabled(false);
                mBtnFireBubble.setEnabled(false);
                mBtnCutBubble.setEnabled(false);
                break;
            case CUT_BUBBLE:
                UIEffect.createColorFilter(mBtnColorBubble);
                UIEffect.createColorFilter(mBtnFireBubble);
                UIEffect.createColorFilter(mBtnBombBubble);
                UIEffect.createColorFilter(mTxtColorBubble);
                UIEffect.createColorFilter(mTxtFireBubble);
                UIEffect.createColorFilter(mTxtBombBubble);
                mBtnColorBubble.setEnabled(false);
                mBtnFireBubble.setEnabled(false);
                mBtnBombBubble.setEnabled(false);
                break;
        }
    }

    private void unlockButton() {
        switch (mBooster) {
            case FIRE_BUBBLE:
                UIEffect.clearColorFilter(mBtnBombBubble);
                UIEffect.clearColorFilter(mBtnColorBubble);
                UIEffect.clearColorFilter(mBtnCutBubble);
                UIEffect.clearColorFilter(mTxtBombBubble);
                UIEffect.clearColorFilter(mTxtColorBubble);
                UIEffect.clearColorFilter(mTxtCutBubble);
                mBtnBombBubble.setEnabled(true);
                mBtnColorBubble.setEnabled(true);
                mBtnCutBubble.setEnabled(true);
                break;
            case BOMB_BUBBLE:
                UIEffect.clearColorFilter(mBtnFireBubble);
                UIEffect.clearColorFilter(mBtnColorBubble);
                UIEffect.clearColorFilter(mBtnCutBubble);
                UIEffect.clearColorFilter(mTxtFireBubble);
                UIEffect.clearColorFilter(mTxtColorBubble);
                UIEffect.clearColorFilter(mTxtCutBubble);
                mBtnFireBubble.setEnabled(true);
                mBtnColorBubble.setEnabled(true);
                mBtnCutBubble.setEnabled(true);
                break;
            case COLOR_BUBBLE:
                UIEffect.clearColorFilter(mBtnBombBubble);
                UIEffect.clearColorFilter(mBtnFireBubble);
                UIEffect.clearColorFilter(mBtnCutBubble);
                UIEffect.clearColorFilter(mTxtBombBubble);
                UIEffect.clearColorFilter(mTxtFireBubble);
                UIEffect.clearColorFilter(mTxtCutBubble);
                mBtnBombBubble.setEnabled(true);
                mBtnFireBubble.setEnabled(true);
                mBtnCutBubble.setEnabled(true);
                break;
            case CUT_BUBBLE:
                UIEffect.clearColorFilter(mBtnColorBubble);
                UIEffect.clearColorFilter(mBtnFireBubble);
                UIEffect.clearColorFilter(mBtnBombBubble);
                UIEffect.clearColorFilter(mTxtColorBubble);
                UIEffect.clearColorFilter(mTxtFireBubble);
                UIEffect.clearColorFilter(mTxtBombBubble);
                mBtnColorBubble.setEnabled(true);
                mBtnFireBubble.setEnabled(true);
                mBtnBombBubble.setEnabled(true);
                break;
        }
    }

}
