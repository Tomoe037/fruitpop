package com.nativegame.animalspop.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nativegame.animalspop.MainActivity;
import com.nativegame.animalspop.item.Item;
import com.nativegame.animalspop.level.FirebaseLevelConfigRepository;
import com.nativegame.animalspop.player.PlayerFirestoreRepository;
import com.nativegame.animalspop.ui.TransitionEffect;
import com.nativegame.animalspop.R;
import com.nativegame.animalspop.ui.UIEffect;
import com.nativegame.animalspop.database.DatabaseHelper;
import com.nativegame.animalspop.ui.dialog.AdLivesDialog;
import com.nativegame.animalspop.ui.dialog.LevelDialog;
import com.nativegame.animalspop.ui.dialog.SettingDialog;
import com.nativegame.animalspop.ui.dialog.ShopDialog;
import com.nativegame.animalspop.ui.dialog.WheelDialog;
import com.nativegame.animalspop.sound.MySoundEvent;
import com.nativegame.animalspop.timer.LivesTimer;
import com.nativegame.animalspop.timer.WheelTimer;
import com.nativegame.nattyengine.ui.GameFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Oscar Liang on 2022/09/18
 */

public class MapFragment extends GameFragment implements View.OnClickListener,
        TransitionEffect.OnTransitionListener {

    private static final int FALLBACK_TOTAL_LEVEL = 15;
    private static final int LEVELS_PER_SEGMENT = 5;
    // The legacy map placed its next/previous connector nodes at 0.15/0.85,
    // leaving matching 15% edge areas on map_03 and map_01 for the cycle join.
    private static final float MAP_SEGMENT_OVERLAP_RATIO = 0.15f;
    private static final String FIREBASE_LEVEL_ACCESS_TAG = "FIREBASE_LEVEL_ACCESS";
    private static final float[][] SLOT_HORIZONTAL_BIAS = new float[][]{
            {0.33f, 0.37f, 0.58f, 0.70f, 0.56f},
            {0.64f, 0.81f, 0.80f, 0.60f, 0.47f},
            {0.58f, 0.48f, 0.33f, 0.35f, 0.54f}
    };
    private static final float[][] SLOT_VERTICAL_BIAS = new float[][]{
            {0.67f, 0.45f, 0.30f, 0.12f, 0.00f},
            {0.88f, 0.72f, 0.50f, 0.33f, 0.13f},
            {1.00f, 0.86f, 0.68f, 0.48f, 0.33f}
    };

    private DatabaseHelper mDatabaseHelper;
    private PlayerFirestoreRepository mPlayerFirestoreRepository;
    private LivesTimer mLivesTimer;
    private WheelTimer mWheelTimer;
    private TransitionEffect mTransitionEffect;

    private ArrayList<Integer> mLevelStar;
    private final ArrayList<Integer> mLevelNumbers = new ArrayList<>();
    private final Map<Integer, FirebaseLevelConfigRepository.LevelConfig> mLevelConfigs =
            new HashMap<>();
    private final Map<Integer, View> mLevelNodes = new HashMap<>();
    private final Map<Integer, ImageView> mLevelStars = new HashMap<>();
    private int mCurrentLevel;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabaseHelper = ((MainActivity) getGameActivity()).getDatabaseHelper();
        mPlayerFirestoreRepository = new PlayerFirestoreRepository(mDatabaseHelper);
        mLivesTimer = ((MainActivity) getGameActivity()).getLivesTimer();
        mWheelTimer = new WheelTimer(getGameActivity());
        mTransitionEffect = new TransitionEffect(getGameActivity());
        mTransitionEffect.setListener(this);
        // Load level data from db and init
        mLevelStar = mDatabaseHelper.getAllLevelStar();
        mCurrentLevel = mLevelStar.size() + 1;
        init();
    }

    @Override
    protected void onLayoutCreated() {
        View currentLevelNode = mLevelNodes.get(mCurrentLevel);
        if (currentLevelNode == null) {
            return;
        }
        // Scroll to current level position
        ScrollView scrollView = getView().findViewById(R.id.layout_map);
        Rect levelBounds = new Rect();
        currentLevelNode.getDrawingRect(levelBounds);
        scrollView.offsetDescendantRectToMyCoords(currentLevelNode, levelBounds);
        scrollView.scrollTo(0, Math.max(0,
                levelBounds.centerY() - scrollView.getHeight() / 2));
    }

    private void init() {
        // Init button
        ImageButton btnSetting = (ImageButton) getView().findViewById(R.id.btn_setting);
        ImageButton btnShop = (ImageButton) getView().findViewById(R.id.btn_shop);
        ImageButton btnCoin = (ImageButton) getView().findViewById(R.id.btn_coin);
        ImageButton btnWheel = (ImageButton) getView().findViewById(R.id.btn_wheel);
        btnSetting.setOnClickListener(this);
        btnShop.setOnClickListener(this);
        btnCoin.setOnClickListener(this);
        btnWheel.setOnClickListener(this);
        UIEffect.createButtonEffect(btnSetting);
        UIEffect.createButtonEffect(btnShop);
        UIEffect.createButtonEffect(btnCoin);
        UIEffect.createButtonEffect(btnWheel);
        // Init lives text
        TextView txtLives = (TextView) getView().findViewById(R.id.txt_lives);
        txtLives.setOnClickListener(this);
        UIEffect.createButtonEffect(txtLives);
        // Init layout
        initLevelMap();
        initLevelButton();
        initLevelStar();
        initAd();
        loadCoin();

        // We only show one dialog at a time
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mWheelTimer.isWheelReady()) {
                    // Show the wheel dialog
                    showWheelDialogAndShowLevel();
                } else {
                    // Show the current level dialog
                    showLevelDialog(mCurrentLevel);
                }
            }
        }, 1200);
    }

    private void initLevelMap() {
        mLevelNumbers.clear();
        mLevelConfigs.clear();
        mLevelNodes.clear();
        mLevelStars.clear();

        List<FirebaseLevelConfigRepository.LevelConfig> cachedConfigs =
                FirebaseLevelConfigRepository.getCachedLevelConfigs();
        if (FirebaseLevelConfigRepository.isCacheReady() && !cachedConfigs.isEmpty()) {
            Collections.sort(cachedConfigs,
                    (first, second) -> Integer.compare(first.getNumber(), second.getNumber()));
            for (FirebaseLevelConfigRepository.LevelConfig config : cachedConfigs) {
                if (config.getNumber() > 0) {
                    mLevelConfigs.put(config.getNumber(), config);
                }
            }
            mLevelNumbers.addAll(mLevelConfigs.keySet());
            Collections.sort(mLevelNumbers);
        } else {
            for (int level = 1; level <= FALLBACK_TOTAL_LEVEL; level++) {
                mLevelNumbers.add(level);
            }
        }

        LinearLayout segmentContainer = getView().findViewById(R.id.layout_map_segments);
        segmentContainer.removeAllViews();

        ArrayList<Integer> pageIndexes = new ArrayList<>();
        for (int levelNumber : mLevelNumbers) {
            int pageIndex = (levelNumber - 1) / LEVELS_PER_SEGMENT;
            if (!pageIndexes.contains(pageIndex)) {
                pageIndexes.add(pageIndex);
            }
        }
        Collections.sort(pageIndexes, Collections.reverseOrder());

        LayoutInflater inflater = LayoutInflater.from(getGameActivity());
        for (int pagePosition = 0; pagePosition < pageIndexes.size(); pagePosition++) {
            int pageIndex = pageIndexes.get(pagePosition);
            View segment = inflater.inflate(R.layout.view_dynamic_map_segment,
                    segmentContainer, false);
            ImageView background = segment.findViewById(R.id.image_map_background);
            background.setImageResource(getMapDrawable(pageIndex));
            ConstraintLayout overlay = segment.findViewById(R.id.layout_level_overlay);

            for (int levelNumber : mLevelNumbers) {
                int levelPageIndex = (levelNumber - 1) / LEVELS_PER_SEGMENT;
                if (levelPageIndex == pageIndex) {
                    addLevelNode(inflater, overlay, levelNumber, pageIndex);
                }
            }
            segmentContainer.addView(segment);

            boolean hasAdjacentLowerPage = pagePosition + 1 < pageIndexes.size()
                    && pageIndexes.get(pagePosition + 1) == pageIndex - 1;
            if (hasAdjacentLowerPage && pageIndex > 0 && pageIndex % 3 == 0) {
                applyCycleSegmentOverlap(segment);
            }
        }
    }

    private void applyCycleSegmentOverlap(View upperMap01Segment) {
        upperMap01Segment.post(new Runnable() {
            @Override
            public void run() {
                int overlap = Math.round(
                        upperMap01Segment.getHeight() * MAP_SEGMENT_OVERLAP_RATIO);
                ViewGroup.LayoutParams layoutParams = upperMap01Segment.getLayoutParams();
                if (overlap <= 0 || !(layoutParams instanceof LinearLayout.LayoutParams)) {
                    return;
                }

                LinearLayout.LayoutParams segmentParams =
                        (LinearLayout.LayoutParams) layoutParams;
                segmentParams.bottomMargin = -overlap;
                upperMap01Segment.setLayoutParams(segmentParams);
            }
        });
    }

    private int getMapDrawable(int pageIndex) {
        switch (pageIndex % 3) {
            case 1:
                return R.drawable.map_02;
            case 2:
                return R.drawable.map_03;
            default:
                return R.drawable.map_01;
        }
    }

    private void addLevelNode(LayoutInflater inflater, ConstraintLayout overlay,
                              int levelNumber, int pageIndex) {
        View node = inflater.inflate(R.layout.view_dynamic_level_node, overlay, false);
        node.setId(View.generateViewId());

        TextView levelText = node.findViewById(R.id.txt_dynamic_level);
        ImageView levelStar = node.findViewById(R.id.image_dynamic_level_star);
        levelText.setText(String.valueOf(levelNumber));

        int mapTemplateIndex = pageIndex % 3;
        int slotIndex = (levelNumber - 1) % LEVELS_PER_SEGMENT;
        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) node.getLayoutParams();
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.horizontalBias = SLOT_HORIZONTAL_BIAS[mapTemplateIndex][slotIndex];
        params.verticalBias = SLOT_VERTICAL_BIAS[mapTemplateIndex][slotIndex];
        overlay.addView(node, params);

        mLevelNodes.put(levelNumber, node);
        mLevelStars.put(levelNumber, levelStar);
    }

    private void initLevelButton() {
        // Init button listener and star
        for (int levelNumber : mLevelNumbers) {
            View levelNode = mLevelNodes.get(levelNumber);
            TextView txtLevel = levelNode.findViewById(R.id.txt_dynamic_level);

            // Init button listener
            boolean progressUnlocked = levelNumber <= mCurrentLevel;
            FirebaseLevelConfigRepository.LevelConfig config =
                    mLevelConfigs.get(levelNumber);
            boolean firebaseEnabled = config == null || config.isEnabled();

            if (progressUnlocked && firebaseEnabled) {
                int level = levelNumber;
                txtLevel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLevelDialog(level);
                        getGameActivity().getSoundManager().playSound(MySoundEvent.BUTTON_CLICK);
                    }
                });
                txtLevel.setBackgroundResource(R.drawable.btn_level);
                txtLevel.setTextColor(getResources().getColor(R.color.brown));
                UIEffect.createButtonEffect(txtLevel);

                if (config == null) {
                    Log.d(FIREBASE_LEVEL_ACCESS_TAG,
                            "Level " + levelNumber
                                    + " unlocked by progress; Firebase cache unavailable");
                } else {
                    Log.d(FIREBASE_LEVEL_ACCESS_TAG,
                            "Level " + levelNumber + " unlocked by progress and Firebase");
                }
            } else {
                txtLevel.setOnClickListener(null);
                txtLevel.setBackgroundResource(R.drawable.btn_level_lock);
                txtLevel.setTextColor(getResources().getColor(R.color.white));

                if (!progressUnlocked) {
                    Log.d(FIREBASE_LEVEL_ACCESS_TAG,
                            "Level " + levelNumber + " locked by progress");
                } else {
                    Log.d(FIREBASE_LEVEL_ACCESS_TAG,
                            "Level " + levelNumber + " locked by Firebase enabled=false");
                }
            }
        }
    }

    private void initLevelStar() {
        // Init button listener and star
        for (int levelNumber : mLevelNumbers) {
            ImageView imageStar = mLevelStars.get(levelNumber);

            // Update level star
            if (levelNumber < mCurrentLevel && levelNumber - 1 < mLevelStar.size()) {
                int star = mLevelStar.get(levelNumber - 1);
                switch (star) {
                    case 1:
                        imageStar.setImageResource(R.drawable.star_set_01);
                        break;
                    case 2:
                        imageStar.setImageResource(R.drawable.star_set_02);
                        break;
                    case 3:
                        imageStar.setImageResource(R.drawable.star_set_03);
                        break;
                }
                imageStar.setVisibility(View.VISIBLE);
            } else {
                imageStar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void initAd() {
        AdView adView = getView().findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void loadCoin() {
        TextView textCoin = (TextView) getView().findViewById(R.id.txt_coin);
        int coin = mDatabaseHelper.getItemNum(Item.COIN);
        textCoin.setText(String.valueOf(coin));
    }

    private void showLevelDialog(int level) {
        if (!mLevelNodes.containsKey(level)) {
            return;
        }
        LevelDialog levelDialog = new LevelDialog(getGameActivity(), level) {
            @Override
            public void navigateToGame() {
                // Check player lives
                if (mLivesTimer.isEnoughLives()) {
                    super.navigateToGame();
                } else {
                    showLiveNotEnoughDialog();
                }
            }

            @Override
            public void startGame() {
                getGameActivity().navigateToFragment(MyGameFragment.newInstance(level));
                // Stop the bgm
                getGameActivity().getSoundManager().unloadMusic();
            }
        };
        showDialog(levelDialog);
    }

    private void showLiveNotEnoughDialog() {
        AdLivesDialog adLivesDialog = new AdLivesDialog(getGameActivity());
        showDialog(adLivesDialog);
    }

    private void showWheelDialog() {
        WheelDialog wheelDialog = new WheelDialog(getGameActivity(), mWheelTimer) {
            @Override
            public void updateCoin() {
                loadCoin();
            }
        };
        showDialog(wheelDialog);
    }

    private void showWheelDialogAndShowLevel() {
        WheelDialog wheelDialog = new WheelDialog(getGameActivity(), mWheelTimer) {
            @Override
            public void showLevel() {
                showLevelDialog(mCurrentLevel);
            }

            @Override
            public void updateCoin() {
                loadCoin();
            }
        };
        showDialog(wheelDialog);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_setting) {
            getGameActivity().getSoundManager().playSound(MySoundEvent.BUTTON_CLICK);
            SettingDialog settingDialog = new SettingDialog(getGameActivity());
            showDialog(settingDialog);
        } else if (id == R.id.btn_shop || id == R.id.btn_coin) {
            getGameActivity().getSoundManager().playSound(MySoundEvent.BUTTON_CLICK);
            ShopDialog shopDialog = new ShopDialog(getGameActivity()) {
                @Override
                public void updateMapCoin() {
                    loadCoin();
                }
            };
            showDialog(shopDialog);
        } else if (id == R.id.btn_wheel) {
            getGameActivity().getSoundManager().playSound(MySoundEvent.BUTTON_CLICK);
            showWheelDialog();
        } else if (id == R.id.txt_lives) {
            getGameActivity().getSoundManager().playSound(MySoundEvent.BUTTON_CLICK);
            if (!mLivesTimer.isLivesFull()) {
                showLiveNotEnoughDialog();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mLivesTimer.start();
        mPlayerFirestoreRepository.syncPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLivesTimer.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPlayerFirestoreRepository.shutdown();
    }

    @Override
    public boolean onBackPressed() {
        mTransitionEffect.show();
        return true;
    }

    @Override
    public void onTransition() {
        getGameActivity().navigateToFragment(new MenuFragment());
    }

}
