package com.mygame.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

// torso follows mouse like in intravenous or hotline miami, legs follow movement
public class PlayerRenderer {
    private static final float FRAME_DURATION = 0.1f; // seconds per leg frame, lower = faster steps
    private static final float IDLE_FRAME_DURATION = 0.8f; // seconds per breathing frame, higher = slower breathing
    private static final float SCALE = 0.7f; // character size, 1 = original size
    private static final int PISTOL_FRAME_WIDTH = 100; // pistol frames are 100x50
    private static final int RIFLE_FRAME_WIDTH = 150; // rifle holding and firing frames are 150x70, not squares
    private static final int RIFLE_RELOAD_FRAME_WIDTH = 120; // reload strip still 120x70, change it if you redraw it wider
    private static final float FIRE_FRAME_DURATION = 0.05f; // firing anim is quick, has to end before the next shot

    private final Texture torsoTexture;
    private final Animation<TextureRegion> torsoIdle;
    private float torsoAngle; // degrees, 0 = facing right
    private float idleTime;

    private final Texture torsoWalkTexture;
    private final Animation<TextureRegion> torsoWalk;
    private final Animation<TextureRegion> torsoWalkBackward;

    private final Texture torsoPistolTexture;
    private final Animation<TextureRegion> torsoPistol;
    private boolean holdingPistol;

    private final Texture torsoRifleTexture;
    private final Animation<TextureRegion> torsoRifle;
    private boolean holdingRifle;

    // firing anims, null until the strip exists
    private Texture torsoPistolFireTexture;
    private Animation<TextureRegion> torsoPistolFire;
    private Texture torsoRifleFireTexture;
    private Animation<TextureRegion> torsoRifleFire;
    private float fireTime;
    private boolean firing;

    // reload anims, null until the strip exists
    private Texture torsoPistolReloadTexture;
    private TextureRegion[] torsoPistolReload;
    private Texture torsoRifleReloadTexture;
    private TextureRegion[] torsoRifleReload;
    private float reloadProgress = -1f; // 0 to 1 while reloading, -1 when not

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

        // holding the pistol, arms locked forward so it just breathes
        torsoPistolTexture = new Texture("sprites/oconnor_torso_gun.png");
        TextureRegion[] pistolFrames = splitStrip(torsoPistolTexture, PISTOL_FRAME_WIDTH);
        torsoPistol = new Animation<>(IDLE_FRAME_DURATION, pistolFrames);
        torsoPistol.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        // holding the rifle, bigger canvas so it gets its own frame width
        torsoRifleTexture = new Texture("sprites/oconnor_torso_rifle.png");
        TextureRegion[] rifleFrames = splitStrip(torsoRifleTexture, RIFLE_FRAME_WIDTH);
        torsoRifle = new Animation<>(IDLE_FRAME_DURATION, rifleFrames);
        torsoRifle.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        // firing strips, played once per shot, skipped if not drawn yet
        if (Gdx.files.internal("sprites/oconnor_torso_gun_fire.png").exists()) {
            torsoPistolFireTexture = new Texture("sprites/oconnor_torso_gun_fire.png");
            torsoPistolFire = new Animation<>(FIRE_FRAME_DURATION,
                    splitStrip(torsoPistolFireTexture, PISTOL_FRAME_WIDTH));
        }
        if (Gdx.files.internal("sprites/oconnor_torso_rifle_fire.png").exists()) {
            torsoRifleFireTexture = new Texture("sprites/oconnor_torso_rifle_fire.png");
            torsoRifleFire = new Animation<>(FIRE_FRAME_DURATION,
                    splitStrip(torsoRifleFireTexture, RIFLE_FRAME_WIDTH));
        }

        // reload strips, stretched over the whole reload, skipped if not drawn yet
        if (Gdx.files.internal("sprites/oconnor_torso_gun_reload.png").exists()) {
            torsoPistolReloadTexture = new Texture("sprites/oconnor_torso_gun_reload.png");
            torsoPistolReload = splitStrip(torsoPistolReloadTexture, PISTOL_FRAME_WIDTH);
        }
        if (Gdx.files.internal("sprites/oconnor_torso_rifle_reload.png").exists()) {
            torsoRifleReloadTexture = new Texture("sprites/oconnor_torso_rifle_reload.png");
            torsoRifleReload = splitStrip(torsoRifleReloadTexture, RIFLE_RELOAD_FRAME_WIDTH);
        }

        // Legs strip: frames side by side, each frame is a square as tall as the image
        legsTexture = new Texture("sprites/oconnor_legs.png");
        int frameSize = legsTexture.getHeight();
        TextureRegion[] frames = TextureRegion.split(legsTexture, frameSize, frameSize)[0];
        walk = new Animation<>(FRAME_DURATION, frames);
        walk.setPlayMode(Animation.PlayMode.LOOP);
        walkBackward = new Animation<>(FRAME_DURATION, frames);
        walkBackward.setPlayMode(Animation.PlayMode.LOOP_REVERSED);
    }

    // cuts a strip into frames, if the image is narrower than one frame it becomes a single frame instead of crashing
    private static TextureRegion[] splitStrip(Texture texture, int frameWidth) {
        int w = Math.min(frameWidth, texture.getWidth());
        return TextureRegion.split(texture, w, texture.getHeight())[0];
    }

    public void update(float delta, float x, float y) {
        idleTime += delta; // breathing never stops
        if (firing) fireTime += delta;
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

    // plays the firing anim from the start, call it on every shot
    public void fire() {
        firing = true;
        fireTime = 0;
    }

    // 0 to 1 while reloading, -1 when not, the anim follows the real reload time
    public void setReloadProgress(float reloadProgress) {
        this.reloadProgress = reloadProgress;
    }

    // true = draws him holding the pistol
    public void setHoldingPistol(boolean holdingPistol) {
        this.holdingPistol = holdingPistol;
    }

    // true = draws him holding the rifle
    public void setHoldingRifle(boolean holdingRifle) {
        this.holdingRifle = holdingRifle;
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

        if (holdingRifle) torso = torsoRifle.getKeyFrame(idleTime); // arms locked on the rifle, no swing
        else if (holdingPistol) torso = torsoPistol.getKeyFrame(idleTime); // same with the pistol

        // firing anim of the weapon in hand, back to holding once it's done
        Animation<TextureRegion> fireAnim = holdingRifle ? torsoRifleFire : holdingPistol ? torsoPistolFire : null;
        if (firing && fireAnim != null) {
            if (fireAnim.isAnimationFinished(fireTime)) firing = false;
            else torso = fireAnim.getKeyFrame(fireTime);
        }

        // reload anim of the weapon in hand, frame picked from how far the reload is
        TextureRegion[] reloadFrames = holdingRifle ? torsoRifleReload : holdingPistol ? torsoPistolReload : null;
        if (reloadProgress >= 0 && reloadFrames != null) {
            int i = Math.min(reloadFrames.length - 1, (int) (reloadProgress * reloadFrames.length));
            torso = reloadFrames[i];
        }

        if (moving) drawCentered(batch, legs, x, y, angle); // legs hidden when standing, torso covers them
        drawCentered(batch, torso, x, y, torsoAngle);
    }

    private void drawCentered(SpriteBatch batch, TextureRegion region, float x, float y, float angle) {
        float w = region.getRegionWidth();
        float h = region.getRegionHeight();
        // Rotates around the center of the image
        batch.draw(region, Math.round(x) - w / 2f, Math.round(y) - h / 2f,
                w / 2f, h / 2f, w, h, SCALE, SCALE, angle);
    }

    public void dispose() {
        torsoTexture.dispose();
        torsoWalkTexture.dispose();
        torsoPistolTexture.dispose();
        torsoRifleTexture.dispose();
        if (torsoPistolFireTexture != null) torsoPistolFireTexture.dispose();
        if (torsoRifleFireTexture != null) torsoRifleFireTexture.dispose();
        if (torsoPistolReloadTexture != null) torsoPistolReloadTexture.dispose();
        if (torsoRifleReloadTexture != null) torsoRifleReloadTexture.dispose();
        legsTexture.dispose();
    }
}