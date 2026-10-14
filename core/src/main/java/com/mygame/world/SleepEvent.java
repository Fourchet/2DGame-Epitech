package com.mygame.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygame.Physics;
import com.mygame.entities.Player;
import com.mygame.render.GameRenderer;
import com.mygame.render.LightManager;

// the bed in the "events" layer: press E on it, oconnor goes to sleep and wakes up when the light has changed
// fade out -> sleeping in bed at night -> fade out -> same but day -> he gets up
// the beds are two tile layers in tiled: "bed" (empty bed) and "bed_sleeping" (him in it), we just swap which one shows
public class SleepEvent {
    private static final float FADE = 0.8f; // seconds to go black, same to come back
    private static final float ASLEEP_TIME = 1.5f; // watching him sleep before the night passes
    private static final float MORNING_TIME = 1.2f; // still in bed in the morning light before he gets up
    private static final String EMPTY_BED_LAYER = "bed";
    private static final String SLEEPING_BED_LAYER = "bed_sleeping";

    // when each step ends, in seconds since E was pressed
    private static final float BLACK_1 = FADE; // fully black, swap to the sleeping bed
    private static final float SHOWN_1 = BLACK_1 + FADE; // back from black, asleep at night
    private static final float HOLD_1 = SHOWN_1 + ASLEEP_TIME;
    private static final float BLACK_2 = HOLD_1 + FADE; // fully black, the night passes
    private static final float SHOWN_2 = BLACK_2 + FADE; // back from black, asleep in the morning
    private static final float END = SHOWN_2 + MORNING_TIME; // he gets up

    private final Array<Body> bodies = new Array<>();
    private float t = -1; // -1 = nothing going on
    private boolean asleep; // sleeping bed shown, player hidden
    private boolean day; // lighting mode, stays across maps
    private boolean switched; // the light already changed this sleep
    private MapLayer emptyBed, sleepingBed;

    public boolean isBusy() {
        return t >= 0;
    }

    public boolean isDay() {
        return day;
    }

    public void update(float delta, Player player, World world, TiledMap map, LightManager lights, GameRenderer renderer) {
        // the sleeping bed stays hidden until he's in it, even if it's left visible in tiled
        MapLayer sleeping = map.getLayers().get(SLEEPING_BED_LAYER);
        if (sleeping != null && !asleep) sleeping.setVisible(false);

        if (t < 0) {
            if (!Gdx.input.isKeyJustPressed(Input.Keys.E)) return;
            float px = player.getPosition().x * Physics.PPM;
            float py = player.getPosition().y * Physics.PPM;
            if (!onBed(map, px, py)) return;
            emptyBed = map.getLayers().get(EMPTY_BED_LAYER);
            sleepingBed = sleeping;
            if (sleepingBed == null) Gdx.app.log("SleepEvent", "no \"" + SLEEPING_BED_LAYER + "\" layer, sleeping without the bed swap");
            asleep = false;
            switched = false;
            t = 0;
        }

        stopPlayer(player, world); // no sliding around while he sleeps
        t += delta;

        if (!asleep && t >= BLACK_1) {
            asleep = true;
            renderer.setPlayerVisible(false); // he's in the bed now
            setPlayerActive(player, world, false); // his body stops blocking light, no shadow on the bed
            showSleeping(true);
        }
        if (!switched && t >= BLACK_2) {
            switched = true;
            day = !day; // sleeping again at day brings the night back
            lights.setDay(day);
        }
        if (t >= END) {
            asleep = false;
            renderer.setPlayerVisible(true); // up, standing where he was
            setPlayerActive(player, world, true);
            showSleeping(false);
            t = -1;
        }
    }

    private void showSleeping(boolean sleeping) {
        if (sleepingBed == null) return;
        sleepingBed.setVisible(sleeping);
        if (emptyBed != null) emptyBed.setVisible(!sleeping);
    }

    // standing in a rectangle named "bed" in the events layer
    private boolean onBed(TiledMap map, float x, float y) {
        MapLayer layer = map.getLayers().get("events");
        if (layer == null) return false;
        for (MapObject object : layer.getObjects()) {
            if (!"bed".equals(object.getName()) || !(object instanceof RectangleMapObject)) continue;
            if (((RectangleMapObject) object).getRectangle().contains(x, y)) return true;
        }
        return false;
    }

    // the player's body is the one with him as userData
    private void stopPlayer(Player player, World world) {
        world.getBodies(bodies);
        for (Body body : bodies) {
            if (body.getUserData() == player) body.setLinearVelocity(0, 0);
        }
    }

    // inactive body = not in the physics world at all, no collisions and no shadows
    private void setPlayerActive(Player player, World world, boolean active) {
        world.getBodies(bodies);
        for (Body body : bodies) {
            if (body.getUserData() == player) body.setActive(active);
        }
    }

    // black amount, 0 = see everything, 1 = black screen
    private float fade() {
        if (t < 0) return 0;
        if (t < BLACK_1) return t / FADE;
        if (t < SHOWN_1) return 1 - (t - BLACK_1) / FADE;
        if (t < HOLD_1) return 0;
        if (t < BLACK_2) return (t - HOLD_1) / FADE;
        if (t < SHOWN_2) return 1 - (t - BLACK_2) / FADE;
        return 0;
    }

    // call after lightManager.render(), black over everything
    public void drawFade(ShapeRenderer shapes, OrthographicCamera camera) {
        float a = fade();
        if (a <= 0) return;
        float w = camera.viewportWidth * camera.zoom + 20; // a bit bigger than the screen so no edge shows
        float h = camera.viewportHeight * camera.zoom + 20;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, Math.min(1, a));
        shapes.rect(camera.position.x - w / 2f, camera.position.y - h / 2f, w, h);
        shapes.end();
    }
}