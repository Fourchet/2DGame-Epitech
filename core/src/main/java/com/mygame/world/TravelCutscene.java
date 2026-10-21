package com.mygame.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.viewport.Viewport;

// a door with a "cutscene" property plays an animation full screen before you arrive (oconnor driving there)
// fade to black -> the new map loads behind it -> animation -> fade into the new map
public class TravelCutscene {
    private static final float FADE = 0.5f; // seconds, game to black and black to the new map
    private static final float ANIM_FADE = 0.3f; // seconds, the animation fading in and out of black

    private float t = -1; // -1 = nothing going on
    private float duration; // how long the animation shows
    private boolean loaded; // new map already swapped in
    private String target, spawn;
    private Texture texture;
    private Animation<TextureRegion> animation;
    private final Matrix4 screen = new Matrix4();

    public boolean isBusy() {
        return t >= 0;
    }

    public String getTarget() {
        return target;
    }

    public String getSpawn() {
        return spawn;
    }

    // door properties: cutscene (image path), frames (how many in the strip), frameTime (seconds each), duration (seconds, loops if longer than one play)
    public void start(MapObject door) {
        MapProperties p = door.getProperties();
        target = p.get("target", String.class);
        spawn = p.get("spawn", String.class);
        String path = p.get("cutscene", String.class);
        int frames = Math.max(1, (int) num(p, "frames", 1));
        float frameTime = num(p, "frameTime", 0.1f);

        if (texture != null) texture.dispose();
        texture = null;
        animation = null;
        if (path != null && Gdx.files.internal(path).exists()) {
            texture = new Texture(path);
            TextureRegion[] regions = TextureRegion.split(texture, texture.getWidth() / frames, texture.getHeight())[0];
            animation = new Animation<>(frameTime, regions);
            animation.setPlayMode(Animation.PlayMode.LOOP); // car keeps driving if duration is longer than the strip
        } else {
            Gdx.app.log("TravelCutscene", "no " + path + ", just a black screen");
        }
        duration = num(p, "duration", frames * frameTime); // default = plays once
        Gdx.app.log("TravelCutscene", path + ": " + frames + " frames, " + frameTime + " s each, " + duration + " s total");

        loaded = false;
        t = 0;
    }

    // true once, the frame the screen is fully black: that's when GameScreen swaps the map
    public boolean update(float delta) {
        if (t < 0) return false;
        t += delta;
        float animEnd = FADE + duration;
        // space or enter skips the drive
        if (t > FADE && t < animEnd - ANIM_FADE
                && (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER))) {
            t = animEnd - ANIM_FADE;
        }
        if (t >= animEnd + FADE) {
            t = -1; // done, back to playing
            return false;
        }
        if (!loaded && t >= FADE) {
            loaded = true;
            return true;
        }
        return false;
    }

    // black amount over everything, 0 = see through, 1 = black
    private float black() {
        float animStart = FADE, animEnd = FADE + duration;
        if (t < animStart) return t / FADE; // old map going black
        if (t < animStart + ANIM_FADE) return 1 - (t - animStart) / ANIM_FADE; // animation appearing
        if (t < animEnd - ANIM_FADE) return 0;
        if (t < animEnd) return (t - (animEnd - ANIM_FADE)) / ANIM_FADE; // animation going black
        return 1 - (t - animEnd) / FADE; // new map appearing
    }

    // call last, after the lights and the sleep fade, draws over the whole screen
    public void draw(SpriteBatch batch, ShapeRenderer shapes, Viewport viewport) {
        if (t < 0) return;
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        screen.setToOrtho2D(0, 0, w, h); // screen coordinates, not the map's
        float animStart = FADE, animEnd = FADE + duration;
        boolean showingAnim = t >= animStart && t < animEnd;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(screen);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (showingAnim) {
            shapes.setColor(0, 0, 0, 1);
            shapes.rect(0, 0, w, h); // black behind the animation
        }
        shapes.end();

        if (showingAnim && animation != null) {
            TextureRegion frame = animation.getKeyFrame(t - animStart);
            // as big as fits, whole pixels when it's smaller than the screen so pixel art stays crisp
            float scale = Math.min(w / frame.getRegionWidth(), h / frame.getRegionHeight());
            if (scale >= 1) scale = (float) Math.floor(scale);
            float fw = frame.getRegionWidth() * scale;
            float fh = frame.getRegionHeight() * scale;
            batch.setProjectionMatrix(screen);
            batch.begin();
            batch.draw(frame, (w - fw) / 2f, (h - fh) / 2f, fw, fh);
            batch.end();
        }

        float a = Math.max(0, Math.min(1, black()));
        if (a > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0, 0, 0, a);
            shapes.rect(0, 0, w, h);
            shapes.end();
        }
    }

    // number property, int or float in tiled both work
    private static float num(MapProperties p, String key, float fallback) {
        Object value = p.get(key);
        if (value instanceof Number) return ((Number) value).floatValue();
        try {
            return Float.parseFloat(String.valueOf(value).trim()); // typed as a string in tiled, still works
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}