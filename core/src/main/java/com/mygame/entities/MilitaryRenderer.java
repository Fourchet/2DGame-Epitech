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
    private static final float IDLE_FRAME_DURATION = 0.8f; // slow breathing when he stands still
    public static final int DEATH_FRAMES = 7; // how many frames you drew in military_death.png, the width is worked out from it
    private static final float DEATH_FRAME_DURATION = 0.1f; // speed of the fall
    private static final float SCALE = 0.7f; // same size as oconnor
    private static final float FLINCH = 2f; // px knocked back when hit
    private static final float FLINCH_TIME = 0.08f; // how long the knock back lasts

    private final Animation<TextureRegion> legs;
    private final TextureRegion torso;
    private final Animation<TextureRegion> torsoIdle; // breathing when standing, null = still torso
    private float idleTime;
    private boolean alert = true; // spotted the player = combat pose, for now always true until the ai exists
    private final BloodEffects blood;
    private final Animation<TextureRegion> death; // null = he just disappears
    private float deathTime;
    private float torsoAngle; // degrees, 0 = facing right
    private float legsAngle;
    private float walkTime;
    private boolean moving;

    private float lastX, lastY;
    private boolean hasLastPosition;

    private float lastHealth = -1f; // to notice when he gets hit
    private float flinchTimer;
    private boolean dead;
    private boolean deathSoundPending; // just died, helmet headshot not played yet

    public MilitaryRenderer(Animation<TextureRegion> legs, TextureRegion torso,
                            Animation<TextureRegion> torsoIdle, Animation<TextureRegion> death, BloodEffects blood) {
        this.death = death;
        this.legs = legs;
        this.torso = torso;
        this.torsoIdle = torsoIdle;
        this.blood = blood;
        walkTime = MathUtils.random(0f, 1f); // so soldiers don't all step in sync
        idleTime = MathUtils.random(0f, 2f); // and don't all breathe in sync
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

    // plays once and stays on the last frame (the corpse)
    public static Animation<TextureRegion> loadDeath(Texture texture) {
        int w = texture.getWidth() / DEATH_FRAMES; // cuts exactly between frames
        Animation<TextureRegion> animation = new Animation<>(DEATH_FRAME_DURATION, TextureRegion.split(texture, w, texture.getHeight())[0]);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        return animation;
    }

    // breathing strip, same frame size as the torso, plays back and forth
    public static Animation<TextureRegion> loadTorsoIdle(Texture texture) {
        int w = Math.min(TORSO_FRAME_WIDTH, texture.getWidth());
        Animation<TextureRegion> animation = new Animation<>(IDLE_FRAME_DURATION, TextureRegion.split(texture, w, texture.getHeight())[0]);
        animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG); // in then out, no snapping back
        return animation;
    }

    // call once per frame with his and the player's positions, in world pixels, and his health
    public void update(float delta, float x, float y, float targetX, float targetY, float health) {
        if (flinchTimer > 0) flinchTimer -= delta;
        if (dead) {
            deathTime += delta; // corpse doesn't move or turn anymore
            return;
        }
        idleTime += delta;

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

        if (health <= 0) {
            dead = true; // death anim starts, he stays where he fell
            deathSoundPending = true; // helmet headshot plays right away
            flinchTimer = 0; // no knock back on the corpse, the anim does the fall
        }
    }

    // true once, the frame he dies, that's when the helmet headshot sound should play
    public boolean deathSoundNow() {
        if (!deathSoundPending) return false;
        deathSoundPending = false;
        return true;
    }

    // where he is (or where the corpse lies), in world pixels
    public float getX() {
        return lastX;
    }

    public float getY() {
        return lastY;
    }

    // false = hasn't spotted you yet, relaxed idle pose (walking or standing)
    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public boolean isDead() {
        return dead;
    }

    public void draw(SpriteBatch batch) {
        if (!hasLastPosition) return;
        if (dead) {
            if (death != null) drawCentered(batch, death.getKeyFrame(deathTime), lastX, lastY, torsoAngle); // corpse, no legs
            return;
        }

        // knocked back a bit for a moment when hit
        float push = flinchTimer > 0 ? FLINCH : 0;
        float x = lastX - MathUtils.cosDeg(torsoAngle) * push;
        float y = lastY - MathUtils.sinDeg(torsoAngle) * push;

        if (moving) drawCentered(batch, legs.getKeyFrame(walkTime), x, y, legsAngle); // legs hidden when standing
        // relaxed pose until he spots you, combat pose after
        TextureRegion body = (!alert && torsoIdle != null) ? torsoIdle.getKeyFrame(idleTime) : torso;
        drawCentered(batch, body, x, y, torsoAngle);
    }

    private void drawCentered(SpriteBatch batch, TextureRegion region, float x, float y, float angle) {
        float w = region.getRegionWidth();
        float h = region.getRegionHeight();
        // rotates around the center of the image
        batch.draw(region, Math.round(x) - w / 2f, Math.round(y) - h / 2f,
                w / 2f, h / 2f, w, h, SCALE, SCALE, angle);
    }
}