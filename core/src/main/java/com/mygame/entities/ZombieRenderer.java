package com.mygame.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

// draws a zombie with the shambling walk, always staring at the player
public class ZombieRenderer {
    private static final int FRAME_WIDTH = 60; // zombie_walk frames are 60x50
    private static final float FRAME_DURATION = 0.14f; // slow steps sell the shamble
    private static final float SCALE = 0.7f; // same size as oconnor

    private final Animation<TextureRegion> walk;
    private float angle; // degrees, 0 = facing right
    private float walkTime;
    private boolean moving;

    private float lastX, lastY;
    private boolean hasLastPosition;

    public ZombieRenderer(Animation<TextureRegion> walk) {
        this.walk = walk;
        walkTime = MathUtils.random(0f, 2f); // so zombies don't all step in sync
    }

    // load the strip once and share it between all zombies
    public static Animation<TextureRegion> loadWalk(Texture texture) {
        TextureRegion[] frames = TextureRegion.split(texture, FRAME_WIDTH, texture.getHeight())[0];
        Animation<TextureRegion> animation = new Animation<>(FRAME_DURATION, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        return animation;
    }

    // call once per frame with the zombie's and the player's positions, in world pixels
    public void update(float delta, float x, float y, float targetX, float targetY) {
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
    }

    public void draw(SpriteBatch batch, float x, float y) {
        // standing still (attacking, blocked): freeze on the first frame
        TextureRegion frame = moving ? walk.getKeyFrame(walkTime) : walk.getKeyFrames()[0];
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        // rotates around the center of the image
        batch.draw(frame, Math.round(x) - w / 2f, Math.round(y) - h / 2f,
                w / 2f, h / 2f, w, h, SCALE, SCALE, angle);
    }
}
