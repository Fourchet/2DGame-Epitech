package com.mygame.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

// torso follows mouse like in intravenous or hotline miami, legs follow movement
public class PlayerRenderer {
    private static final float FRAME_DURATION = 0.1f; // seconds per leg frame, lower = faster steps
    private static final float IDLE_FRAME_DURATION = 0.8f; // seconds per breathing frame, higher = slower breathing

    private final Texture torsoTexture;
    private final Animation<TextureRegion> torsoIdle;
    private float torsoAngle; // degrees, 0 = facing right
    private float idleTime;

    private final Texture torsoWalkTexture;
    private final Animation<TextureRegion> torsoWalk;
    private final Animation<TextureRegion> torsoWalkBackward;

    private final Texture legsTexture;
    private final Animation<TextureRegion> walk;
    private final Animation<TextureRegion> walkBackward;
    private float legsAngle;
    private float walkTime;
    private boolean moving;

    private float lastX, lastY;
    private boolean hasLastPosition;

    public PlayerRenderer() {
        // breathing strip, same deal as the legs
        torsoTexture = new Texture("sprites/oconnor_torso_idle.png");
        int torsoFrameSize = torsoTexture.getHeight();
        TextureRegion[] torsoFrames = TextureRegion.split(torsoTexture, torsoFrameSize, torsoFrameSize)[0];
        torsoIdle = new Animation<>(IDLE_FRAME_DURATION, torsoFrames);
        torsoIdle.setPlayMode(Animation.PlayMode.LOOP_PINGPONG); // in then out, no snapping back

        // walking strip, same frame count as the legs so they stay synced
        torsoWalkTexture = new Texture("sprites/oconnor_torso_walk.png");
        int walkFrameSize = torsoWalkTexture.getHeight();
        TextureRegion[] torsoWalkFrames = TextureRegion.split(torsoWalkTexture, walkFrameSize, walkFrameSize)[0];
        torsoWalk = new Animation<>(FRAME_DURATION, torsoWalkFrames);
        torsoWalk.setPlayMode(Animation.PlayMode.LOOP);
        torsoWalkBackward = new Animation<>(FRAME_DURATION, torsoWalkFrames);
        torsoWalkBackward.setPlayMode(Animation.PlayMode.LOOP_REVERSED);

        // Legs strip: frames side by side, each frame is a square as tall as the image
        legsTexture = new Texture("sprites/oconnor_legs.png");
        int frameSize = legsTexture.getHeight();
        TextureRegion[] frames = TextureRegion.split(legsTexture, frameSize, frameSize)[0];
        walk = new Animation<>(FRAME_DURATION, frames);
        walk.setPlayMode(Animation.PlayMode.LOOP);
        walkBackward = new Animation<>(FRAME_DURATION, frames);
        walkBackward.setPlayMode(Animation.PlayMode.LOOP_REVERSED);
    }

    public void update(float delta, float x, float y) {
        idleTime += delta; // breathing never stops
        if (hasLastPosition) {
            float dx = x - lastX;
            float dy = y - lastY;
            moving = dx * dx + dy * dy > 0.01f;
            if (moving) {
                legsAngle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
                walkTime += delta;
            } else {
                walkTime = 0; // back to the first frame when standing still (default oconnor)
            }
        }
        lastX = x;
        lastY = y;
        hasLastPosition = true;
    }

    // Turns the torso toward a point in the world
    public void aimAt(float x, float y, float targetX, float targetY) {
        torsoAngle = MathUtils.atan2(targetY - y, targetX - x) * MathUtils.radiansToDegrees;
    }

    // renders legs and torso
    //Draws legs then torso, centered on (x, y), in world pixels.
    public void draw(SpriteBatch batch, float x, float y) {
        TextureRegion legs = walk.getKeyFrame(walkTime);
        TextureRegion torso = torsoIdle.getKeyFrame(idleTime);
        float angle = torsoAngle; // standing still: legs face the same way as the torso

        if (moving) {
            // Angle between where he walks and where he looks, from -180 to 180
            float diff = ((legsAngle - torsoAngle) % 360 + 540) % 360 - 180;

            if (Math.abs(diff) > 90) {
                // Walking backward: legs turn around to match the torso, steps play in reverse
                legs = walkBackward.getKeyFrame(walkTime);
                torso = torsoWalkBackward.getKeyFrame(walkTime); // arms swing in reverse too
                angle = legsAngle + 180;
            } else {
                torso = torsoWalk.getKeyFrame(walkTime); // arms swing with the steps
                angle = legsAngle;
            }
        }

        if (moving) drawCentered(batch, legs, x, y, angle); // legs hidden when standing, torso covers them
            drawCentered(batch, torso, x, y, torsoAngle);
    }


    private void drawCentered(SpriteBatch batch, TextureRegion region, float x, float y, float angle) {
        float w = region.getRegionWidth();
        float h = region.getRegionHeight();
        // Rotates around the center of the image
        batch.draw(region, Math.round(x) - w / 2f, Math.round(y) - h / 2f,
                w / 2f, h / 2f, w, h, 1f, 1f, angle);
    }

    public void dispose() {
        torsoTexture.dispose();
        torsoWalkTexture.dispose();
        legsTexture.dispose();
    }
}