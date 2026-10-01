package com.mygame.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

// torso follows mouse like in intravenous or hotline miami, legs follow movement
public class PlayerRenderer {
    private static final float FRAME_DURATION = 0.1f; // seconds per leg frame, lower = faster steps

    private final Texture torsoTexture;
    private final TextureRegion torso;
    private float torsoAngle; // degrees, 0 = facing right

    private final Texture legsTexture;
    private final Animation<TextureRegion> walk;
    private float legsAngle;
    private float walkTime;
    private boolean moving;

    private float lastX, lastY;
    private boolean hasLastPosition;

    public PlayerRenderer() {
        torsoTexture = new Texture("sprites/oconnor_default.png");
        torso = new TextureRegion(torsoTexture);

        // Legs strip: frames side by side, each frame is a square as tall as the image
        legsTexture = new Texture("sprites/oconnor_legs.png");
        int frameSize = legsTexture.getHeight();
        TextureRegion[] frames = TextureRegion.split(legsTexture, frameSize, frameSize)[0];
        walk = new Animation<>(FRAME_DURATION, frames);
        walk.setPlayMode(Animation.PlayMode.LOOP);
    }

    
    public void update(float delta, float x, float y) {
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

    /** Turns the torso toward a point in the world (e.g. the mouse). */
    public void aimAt(float x, float y, float targetX, float targetY) {
        torsoAngle = MathUtils.atan2(targetY - y, targetX - x) * MathUtils.radiansToDegrees;
    }

    // renders legs and torso
    public void draw(SpriteBatch batch, float x, float y) {
        // When standing still, the legs face the same way as the torso, may want to fix that but really hard ifl
        float angle = moving ? legsAngle : torsoAngle;
        drawCentered(batch, walk.getKeyFrame(walkTime), x, y, angle);
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
        legsTexture.dispose();
    }
}