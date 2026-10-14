package com.mygame.entities.others;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

// short blood sprays where zombies get shot
public class BloodEffects {
    private static final float FRAME_DURATION = 0.05f; // whole spray lasts ~0.25 s
    private static final float SCALE = 0.7f; // same size as the characters

    private final Texture texture; // null if blood_hit.png isn't there yet
    private final Animation<TextureRegion> spray;
    private final Array<float[]> active = new Array<>(); // each one = x, y, angle, time

    public BloodEffects() {
        if (Gdx.files.internal("sprites/blood_hit.png").exists()) {
            texture = new Texture("sprites/blood_hit.png");
            int size = texture.getHeight(); // square frames
            spray = new Animation<>(FRAME_DURATION, TextureRegion.split(texture, size, size)[0]);
        } else {
            texture = null;
            spray = null;
        }
    }

    // angle = where the blood flies, a bit random so hits don't all look the same
    public void spawn(float x, float y, float angle) {
        if (spray == null) return;
        active.add(new float[] {x, y, angle + MathUtils.random(-15f, 15f), 0f});
    }

    public void update(float delta) {
        if (spray == null) return;
        for (int i = active.size - 1; i >= 0; i--) {
            float[] b = active.get(i);
            b[3] += delta;
            if (spray.isAnimationFinished(b[3])) active.removeIndex(i); // done, gone
        }
    }

    public void draw(SpriteBatch batch) {
        if (spray == null) return;
        for (float[] b : active) {
            TextureRegion frame = spray.getKeyFrame(b[3]);
            float w = frame.getRegionWidth();
            float h = frame.getRegionHeight();
            // impact point = center of the frame
            batch.draw(frame, b[0] - w / 2f, b[1] - h / 2f, w / 2f, h / 2f, w, h, SCALE, SCALE, b[2]);
        }
    }

    // sprays belong to the old map, clear them on transition
    public void clear() {
        active.clear();
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}