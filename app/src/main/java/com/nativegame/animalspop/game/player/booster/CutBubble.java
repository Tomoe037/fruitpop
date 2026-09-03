package com.nativegame.animalspop.game.player.booster;

import com.nativegame.animalspop.R;
import com.nativegame.animalspop.game.bubble.Bubble;
import com.nativegame.animalspop.game.bubble.BubbleSystem;
import com.nativegame.animalspop.game.player.dot.DotSystem;
import com.nativegame.nattyengine.Game;

/**
 * A booster shot that pops exactly one regular colored bubble.
 */
public class CutBubble extends BoosterBubble {

    public CutBubble(BubbleSystem bubbleSystem, Game game) {
        super(bubbleSystem, game, R.drawable.cut_bubble);
        mDotSystem.setDotBitmap(R.drawable.dot_white);
    }

    @Override
    protected DotSystem getDotSystem() {
        return new DotSystem(this, mGame);
    }

    @Override
    protected void onBubbleHit(Bubble bubble) {
        if (mConsume) {
            return;
        }

        // Special board bubbles are left untouched, but the shot still ends
        // and is consumed through the existing BoosterBubble lifecycle.
        mBubbleSystem.popRegularBubble(bubble);
        reset();
    }
}
