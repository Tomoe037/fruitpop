package com.nativegame.animalspop.game.player.booster;

import com.nativegame.animalspop.R;
import com.nativegame.animalspop.game.bubble.Bubble;
import com.nativegame.animalspop.game.bubble.BubbleSystem;
import com.nativegame.animalspop.game.player.dot.DotSystem;
import com.nativegame.nattyengine.Game;

/**
 * A booster shot that pops every regular colored bubble matching the hit bubble.
 */
public class LightningBubble extends BoosterBubble {

    public LightningBubble(BubbleSystem bubbleSystem, Game game, int drawableId) {
        super(bubbleSystem, game, drawableId);
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

        mBubbleSystem.popRegularBubblesByColor(bubble);
        reset();
    }
}
