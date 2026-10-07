package com.mygame.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

// draws a zombie with the shambling walk, always staring at the player
// blood when it gets shot, death anim at 0 hp, then the corpse stays on the floor
public class ZombieRenderer {
    private static final int FRAME_WIDTH = 60; // zombie_walk frames are 60x50
    public static final int DEATH_FRAMES = 5; // how many frames you drew in zombie_death.png, the width is worked out from it
    public static final int DEATH2_FRAMES = 9; // same for zombie_death2.png (100x50 frames)
    private static final float RARE_DEATH_CHANCE = 0.1f; // zombie_death plays 10% of the time, zombie_death2 the rest
    private static final float FRAME_DURATION = 0.14f; // slow steps sell the shamble
    private static final float DEATH_FRAME_DURATION = 0.1f; // speed of the fall
    private static final float SCALE = 0.7f; // same size as oconnor
    private static final float FLINCH = 2f; // px knocked back when hit
    private static final float FLINCH_TIME = 0.08f; // how long the knock back lasts
    private static final float HEAD_POP_DELAY = 0.5f; // seconds after death before the head pop sound, rare death only

    private final Animation<TextureRegion> walk;
    private final Animation<TextureRegion> rareDeath; // zombie_death, the rare one
    private final Animation<TextureRegion> commonDeath; // zombie_death2, the usual one
    private Animation<TextureRegion> death; // the one picked when he dies, null = he just freezes
    private final BloodEffects blood;
    private float angle; // degrees, 0 = facing right
    private float walkTime;
    private boolean moving;

    private float lastX, lastY;
    private boolean hasLastPosition;

    private float lastHealth = -1f; // to notice when he gets hit
    private float flinchTimer;
    private boolean dead;
    private float deathTime;
    private boolean headPopPending; // rare death picked, sound not played yet

    public ZombieRenderer(Animation<TextureRegion> walk, Animation<TextureRegion> rareDeath,
                          Animation<TextureRegion> commonDeath, BloodEffects blood) {
        this.walk = walk;
        this.rareDeath = rareDeath;
        this.commonDeath = commonDeath;
        this.blood = blood;
        walkTime = MathUtils.random(0f, 2f); // so zombies don't all step in sync
    }

    // load the strip once and share it between all zombies
    public static Animation<TextureRegion> loadWalk(Texture texture) {
        TextureRegion[] frames = TextureRegion.split(texture, FRAME_WIDTH, texture.getHeight())[0];
        Animation<TextureRegion> animation = new Animation<>(FRAME_DURATION, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        return animation;
    }

    // same for the death, plays once and stays on the last frame (the corpse)
    public static Animation<TextureRegion> loadDeath(Texture texture, int frameCount) {
        int w = texture.getWidth() / frameCount; // cuts exactly between frames whatever their size
        TextureRegion[] frames = TextureRegion.split(texture, w, texture.getHeight())[0];
        Animation<TextureRegion> animation = new Animation<>(DEATH_FRAME_DURATION, frames);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        return animation;
    }

    // call once per frame with the zombie's and the player's positions, in world pixels, and its health
    public void update(float delta, float x, float y, float targetX, float targetY, float health) {
        if (flinchTimer > 0) flinchTimer -= delta;
        if (dead) {
            deathTime += delta; // corpse doesn't move or turn anymore
            return;
        }

        if (hasLastPosition) {
            float dx = x - lastX;
            float dy = y - lastY;
            moving = dx * dx + dy * dy > 0.01f;
            if (moving) walkTime += delta;
        }
        lastX = x;
        lastY = y;
        hasLastPosition = true;

        angle = MathUtils.atan2(targetY - y, targetX - x) * MathUtils.radiansToDegrees; // always facing the player

        // health went down = he got shot, blood sprays out the back, away from the player
        if (lastHealth >= 0 && health < lastHealth) {
            blood.spawn(x, y, angle + 180);
            flinchTimer = FLINCH_TIME;
        }
        lastHealth = health;

        if (health <= 0) {
            dead = true; // death anim starts, he stays where he fell
            death = MathUtils.random() < RARE_DEATH_CHANCE ? rareDeath : commonDeath; // which death this time
            headPopPending = death == rareDeath; // the rare one comes with the head pop
            flinchTimer = 0; // no knock back on the corpse, the anim does the fall
        }
    }

    public boolean isDead() {
        return dead;
    }

    // true once, 1 s after a rare death, that's when the head pop sound should play
    public boolean headPopNow() {
        if (!headPopPending || deathTime < HEAD_POP_DELAY) return false;
        headPopPending = false;
        return true;
    }

    // where he is (or where the corpse lies), in world pixels
    public float getX() {
        return lastX;
    }

    public float getY() {
        return lastY;
    }

    // draws at the last known position, so the corpse stays even once the body is gone
    public void draw(SpriteBatch batch) {
        if (!hasLastPosition) return;

        TextureRegion frame;
        if (dead && death != null) frame = death.getKeyFrame(deathTime);
        else if (moving && !dead) frame = walk.getKeyFrame(walkTime);
        else frame = walk.getKeyFrames()[0]; // standing still (attacking, blocked): freeze on the first frame

        // knocked back a bit for a moment when hit
        float push = flinchTimer > 0 ? FLINCH : 0;
        float x = lastX - MathUtils.cosDeg(angle) * push;
        float y = lastY - MathUtils.sinDeg(angle) * push;

        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        // rotates around the center of the image
        batch.draw(frame, Math.round(x) - w / 2f, Math.round(y) - h / 2f,
                w / 2f, h / 2f, w, h, SCALE, SCALE, angle);
    }
}