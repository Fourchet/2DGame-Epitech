package com.mygame.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

// draws a soldier: legs follow where he walks, torso and rifle always aim at the player
public class MilitaryRenderer {
    private static final int LEGS_FRAME_SIZE = 50; // military_legs frames are 50x50
    private static final int TORSO_FRAME_WIDTH = 120; // military_torso frames are 120x70
    private static final float FRAME_DURATION = 0.08f; // short quick steps, tactical
    private static final float SCALE = 0.7f; // same size as oconnor
    private static final float FLINCH = 2f; // px knocked back when hit
    private static final float FLINCH_TIME = 0.08f; // how long the knock back lasts

    private final Animation<TextureRegion> legs;
    private final TextureRegion torso;
    private final BloodEffects blood;
    private float torsoAngle; // degrees, 0 = facing right
    private float legsAngle;
    private float walkTime;
    private boolean moving;

    private float lastX, lastY;
    private boolean hasLastPosition;

    private float lastHealth = -1f; // to notice when he gets hit
    private float flinchTimer;
    private boolean dead;

    public MilitaryRenderer(Animation<TextureRegion> legs, TextureRegion torso, BloodEffects blood) {
        this.legs = legs;
        this.torso = torso;
        this.blood = blood;
        walkTime = MathUtils.random(0f, 1f); // so soldiers don't all step in sync
    }

    // load the legs strip once and share it between all soldiers
    public static Animation<TextureRegion> loadLegs(Texture texture) {
        TextureRegion[] frames = TextureRegion.split(texture, LEGS_FRAME_SIZE, LEGS_FRAME_SIZE)[0];
        Animation<TextureRegion> animation = new Animation<>(FRAME_DURATION, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        return animation;
    }

    // torso is a single pose for now, first frame if it's ever a strip
    public static TextureRegion loadTorso(Texture texture) {
        int w = Math.min(TORSO_FRAME_WIDTH, texture.getWidth());
        return new TextureRegion(texture, 0, 0, w, texture.getHeight());
    }

    // call once per frame with his and the player's positions, in world pixels, and his health
    public void update(float delta, float x, float y, float targetX, float targetY, float health) {
        if (flinchTimer > 0) flinchTimer -= delta;
        if (dead) return;

        if (hasLastPosition) {
            float dx = x - lastX;
            float dy = y - lastY;
            moving = dx * dx + dy * dy > 0.01f;
            if (moving) {
                legsAngle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
                walkTime += delta;
            }
        }
        lastX = x;
        lastY = y;
        hasLastPosition = true;

        torsoAngle = MathUtils.atan2(targetY - y, targetX - x) * MathUtils.radiansToDegrees; // rifle on the player

        // health went down = he got shot, blood sprays out the back, away from the player
        if (lastHealth >= 0 && health < lastHealth) {
            blood.spawn(x, y, torsoAngle + 180);
            flinchTimer = FLINCH_TIME;
        }
        lastHealth = health;

        if (health <= 0) dead = true; // no death anim yet, he just disappears
    }

    public boolean isDead() {
        return dead;
    }

    public void draw(SpriteBatch batch) {
        if (!hasLastPosition || dead) return;

        // knocked back a bit for a moment when hit
        float push = flinchTimer > 0 ? FLINCH : 0;
        float x = lastX - MathUtils.cosDeg(torsoAngle) * push;
        float y = lastY - MathUtils.sinDeg(torsoAngle) * push;

        if (moving) drawCentered(batch, legs.getKeyFrame(walkTime), x, y, legsAngle); // legs hidden when standing
        drawCentered(batch, torso, x, y, torsoAngle);
    }

    private void drawCentered(SpriteBatch batch, TextureRegion region, float x, float y, float angle) {
        float w = region.getRegionWidth();
        float h = region.getRegionHeight();
        // rotates around the center of the image
        batch.draw(region, Math.round(x) - w / 2f, Math.round(y) - h / 2f,
                w / 2f, h / 2f, w, h, SCALE, SCALE, angle);
    }
}