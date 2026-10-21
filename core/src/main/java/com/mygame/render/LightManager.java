package com.mygame.render;

import box2dLight.ConeLight;
import box2dLight.DirectionalLight;
import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygame.Physics;

// darkness + lights for one map, everything comes from the tiled "lights" layer
// walls block the light so they cast real shadows
public class LightManager {
    private static final Color DEFAULT_AMBIENT = new Color(0.1f, 0.08f, 0.12f, 0.35f); // map with no "ambient" property: dim evening
    private static final Color DEFAULT_DAY = new Color(1f, 0.85f, 0.6f, 0.95f); // map with no "dayAmbient" property: warm morning
    private static final float DAY_TINT = 0.06f; // how much of the day colour gets added on top, more = washed out
    private static final Color DEFAULT_LAMP = new Color(1f, 0.75f, 0.45f, 0.85f); // warm bulb
    private static final Color DEFAULT_SUN = new Color(1f, 0.9f, 0.7f, 0.6f); // late afternoon sun
    private static final float DEFAULT_RADIUS = 120f; // px
    private static final float CONE_EXTRA_ANGLE = 12f; // degrees added on each side, the edges are dim so it looked too tight
    private static final float CONE_EXTRA_LENGTH = 1.4f; // light fades to nothing at its end, so it goes a bit past your shape
    private static final float DARKNESS = 1f; // whole map without lights, lower = darker (1 = exactly the map's ambient)
    private static final float LAMP_STRENGTH = 0.6f; // every lamp, cone and tv, lower = dimmer and less washed out (gun flashes not included)
    private static final Color FLASH_COLOR = new Color(1f, 0.82f, 0.5f, 1f); // gunpowder orange, full strength
    private static final float FLASH_RADIUS_MIN = 240f; // px, each flash picks a range in between
    private static final float FLASH_RADIUS_MAX = 320f; // about 5-8 m at our characters scale, like source engine muzzle flashes
    private static final float FLASH_TIME_MIN = 0.05f; // seconds, each flash picks a length in between
    private static final float FLASH_TIME_MAX = 0.1f;
    private static final int FLASH_POOL = 8; // max flashes at once, enough for bursts
    private static final float FLASH_CONE_MIN = 45f; // half angle, each flash picks a wideness in between
    private static final float FLASH_CONE_MAX = 75f; // 60 = 120 degrees in front of the barrel, nothing behind
    private static final float MIN_SHAPE_RADIUS = 32f; // smaller ellipses just mark where the lamp is, they get the default radius
    private static final int LAMP_RAYS = 128; // more = smoother shadows, slower
    private static final int SUN_RAYS = 512;
    private static final Color[] TV_COLORS = { // what's on screen changes, the room changes colour with it
            new Color(0.55f, 0.7f, 1f, 0.8f), new Color(0.8f, 0.85f, 1f, 0.9f),
            new Color(0.4f, 0.55f, 0.95f, 0.6f), new Color(0.9f, 0.8f, 0.7f, 0.75f)};

    private final RayHandler rayHandler;
    private final Matrix4 lightMatrix = new Matrix4(); // camera matrix but in meters
    private final Array<Light> flickering = new Array<>();
    private final Array<Float> flickerRadius = new Array<>(); // their normal size
    private final Array<Light> tvs = new Array<>();
    private final Array<Float> tvTimers = new Array<>(); // time until the next "scene cut"
    private final Array<Color> tvColors = new Array<>(); // the colour you gave it in tiled
    private float time;
    private Color nightAmbient, dayAmbient;
    private final Array<Light> nightOnly = new Array<>(); // lamps switched off in the day
    private final Array<ConeLight> flashes = new Array<>(); // reused muzzle flash lights
    private final float[] flashTimers = new float[FLASH_POOL];
    private final float[] flashDurations = new float[FLASH_POOL]; // how long this one lasts
    private final float[] flashRadius = new float[FLASH_POOL]; // how far this one reaches, in meters

    public LightManager(World world, TiledMap map, Viewport viewport) {
        RayHandler.useDiffuseLight(false); // back to the darker look from before
        rayHandler = new RayHandler(world);
        rayHandler.setBlurNum(2); // soft edges
        // ambient alpha = how bright the map is without lights, DARKNESS on top to tune every map at once
        Color a = readColor(map.getProperties(), "ambient", DEFAULT_AMBIENT);
        nightAmbient = new Color(a.r, a.g, a.b, a.a * DARKNESS);
        // the ambient colour gets added over the whole screen, so only a touch of it or the day goes white
        // alpha = how bright the day is, 1 = the art at full brightness
        Color d = readColor(map.getProperties(), "dayAmbient", DEFAULT_DAY);
        dayAmbient = new Color(d.r * DAY_TINT, d.g * DAY_TINT, d.b * DAY_TINT, d.a);
        rayHandler.setAmbientLight(nightAmbient);
        resize(viewport);

        // muzzle flash lights made once and reused, off until someone shoots
        for (int i = 0; i < FLASH_POOL; i++) {
            ConeLight flash = new ConeLight(rayHandler, 64, FLASH_COLOR, FLASH_RADIUS_MAX / Physics.PPM, 0, 0, 0, FLASH_CONE_MAX);
            flash.setActive(false);
            flashes.add(flash);
        }

        MapLayer layer = map.getLayers().get("lights");
        if (layer == null) return; // no lights layer = just the ambient

        for (MapObject object : layer.getObjects()) {
            MapProperties p = object.getProperties();
            float w = num(p, "width", 0f);
            float h = num(p, "height", 0f);
            float x = num(p, "x", 0f) + w / 2f; // center of the object, in px
            float y = num(p, "y", 0f) + h / 2f;

            // object named "sun": lights the whole map from one direction
            if ("sun".equals(object.getName())) {
                float angle = num(p, "angle", -60f); // where the light goes, -90 = straight down the screen
                new DirectionalLight(rayHandler, SUN_RAYS, readColor(p, "color", DEFAULT_SUN), angle);
                continue;
            }

            Color tiledColor = readColor(p, "color", DEFAULT_LAMP);
            Color color = new Color(tiledColor.r, tiledColor.g, tiledColor.b, tiledColor.a * LAMP_STRENGTH);
            Light light;
            float radius;

            // polygon or polyline: a cone of light (tv, car headlights...), the first point you clicked is where it comes from
            float[] v = null;
            if (object instanceof PolygonMapObject) v = ((PolygonMapObject) object).getPolygon().getTransformedVertices();
            else if (object instanceof PolylineMapObject) v = ((PolylineMapObject) object).getPolyline().getTransformedVertices();

            if (v != null && v.length >= 6) {
                // points close to the first one are the source edge (like both corners of a tv screen),
                // the light comes from their middle, the others are where it shines to
                float reach = 0;
                for (int i = 2; i < v.length; i += 2) reach = Math.max(reach, dist(v[0], v[1], v[i], v[i + 1]));
                float ax = 0, ay = 0, cx = 0, cy = 0;
                int sources = 0, targets = 0;
                for (int i = 0; i < v.length; i += 2) {
                    if (i == 0 || dist(v[0], v[1], v[i], v[i + 1]) < reach * 0.3f) {
                        ax += v[i]; ay += v[i + 1]; sources++;
                    } else {
                        cx += v[i]; cy += v[i + 1]; targets++;
                    }
                }
                ax /= sources; ay /= sources;
                cx /= targets; cy /= targets;
                float direction = MathUtils.atan2(cy - ay, cx - ax) * MathUtils.radiansToDegrees; // aims at the middle of the targets
                // wide enough to reach every target, long enough to reach the furthest
                float halfAngle = 0, far = 0;
                for (int i = 2; i < v.length; i += 2) {
                    if (dist(v[0], v[1], v[i], v[i + 1]) < reach * 0.3f) continue; // part of the source
                    float angle = MathUtils.atan2(v[i + 1] - ay, v[i] - ax) * MathUtils.radiansToDegrees;
                    float diff = Math.abs(((angle - direction) % 360 + 540) % 360 - 180);
                    halfAngle = Math.max(halfAngle, diff);
                    far = Math.max(far, dist(ax, ay, v[i], v[i + 1]));
                }
                halfAngle = Math.min(halfAngle + CONE_EXTRA_ANGLE, 180f);
                radius = num(p, "radius", far * CONE_EXTRA_LENGTH);
                light = new ConeLight(rayHandler, LAMP_RAYS, color, radius / Physics.PPM,
                        ax / Physics.PPM, ay / Physics.PPM, direction, halfAngle);
                Gdx.app.log("LightManager", "cone '" + object.getName() + "' from " + (int) ax + "," + (int) ay
                        + " aiming " + (int) direction + " deg, width " + (int) (halfAngle * 2) + " deg, length " + (int) radius + " color " + color);
            } else {
                // anything else is a round lamp, radius from the property, or from the ellipse if it's drawn big
                radius = num(p, "radius", w / 2f >= MIN_SHAPE_RADIUS ? w / 2f : DEFAULT_RADIUS);
                light = new PointLight(rayHandler, LAMP_RAYS, color,
                        radius / Physics.PPM, x / Physics.PPM, y / Physics.PPM);
                Gdx.app.log("LightManager", "lamp '" + object.getName() + "' at " + (int) x + "," + (int) y
                        + " radius " + (int) radius + " color " + color); // to check every lamp got loaded right
            }

            if (bool(p, "flicker")) {
                flickering.add(light);
                flickerRadius.add(radius / Physics.PPM);
            }
            if (bool(p, "tv")) {
                tvs.add(light);
                tvTimers.add(0f);
                tvColors.add(new Color(tiledColor));
            }
            if (!bool(p, "tv") && !bool(p, "dayOn")) nightOnly.add(light); // lamps go off in the day, tv and dayOn ones stay
        }
    }

    // day = bright ambient and lamps off, night = back to the map's ambient and lamps on
    public void setDay(boolean day) {
        rayHandler.setAmbientLight(day ? dayAmbient : nightAmbient);
        for (Light light : nightOnly) light.setActive(!day);
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

    private static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    // bool property, a real bool or the text "true" both work
    private static boolean bool(MapProperties p, String key) {
        Object value = p.get(key);
        return value instanceof Boolean ? (Boolean) value : "true".equalsIgnoreCase(String.valueOf(value).trim());
    }

    // tiled color property, works whether libgdx gives a Color or the "#AARRGGBB" string
    private static Color readColor(MapProperties p, String key, Color fallback) {
        Object value = p.get(key);
        if (value instanceof Color) return (Color) value;
        if (!(value instanceof String)) return fallback;
        String hex = ((String) value).replace("#", "");
        if (hex.length() == 8) hex = hex.substring(2) + hex.substring(0, 2); // tiled puts alpha first, libgdx last
        return Color.valueOf(hex);
    }

    // call after gameRenderer.render() with its muzzle flashes, they light up next frame
    public void flash(Array<Vector3> shots) {
        for (Vector3 shot : shots) {
            // the one with the least time left gets reused
            int pick = 0;
            for (int i = 1; i < FLASH_POOL; i++) if (flashTimers[i] < flashTimers[pick]) pick = i;
            flashDurations[pick] = MathUtils.random(FLASH_TIME_MIN, FLASH_TIME_MAX);
            flashTimers[pick] = flashDurations[pick];
            ConeLight flash = flashes.get(pick);
            flash.setPosition(shot.x / Physics.PPM, shot.y / Physics.PPM);
            flash.setDirection(shot.z); // z = where the bullet goes
            // every shot a bit different
            flashRadius[pick] = MathUtils.random(FLASH_RADIUS_MIN, FLASH_RADIUS_MAX) / Physics.PPM;
            flash.setConeDegree(MathUtils.random(FLASH_CONE_MIN, FLASH_CONE_MAX));
            flash.setDistance(flashRadius[pick]);
            flash.setActive(true);
        }
    }

    // call before postProcess.begin(), computes the rays and the light texture
    public void update(float delta, OrthographicCamera camera) {
        time += delta;
        // flashes shrink a little then cut off
        for (int i = 0; i < FLASH_POOL; i++) {
            if (flashTimers[i] <= 0) continue;
            flashTimers[i] -= delta;
            ConeLight flash = flashes.get(i);
            if (flashTimers[i] <= 0) flash.setActive(false);
            else flash.setDistance(flashRadius[i] * (0.75f + 0.25f * flashTimers[i] / flashDurations[i])); // stays big till it cuts
        }
        // dying bulbs, a bit of wobble plus a random dip now and then
        for (int i = 0; i < flickering.size; i++) {
            float wobble = 1f + 0.04f * MathUtils.sin(time * 13f + i * 7f);
            if (MathUtils.random() < 0.02f) wobble *= 0.6f;
            flickering.get(i).setDistance(flickerRadius.get(i) * wobble);
        }
        // tv: jumps to another colour and brightness every now and then
        for (int i = 0; i < tvs.size; i++) {
            float timer = tvTimers.get(i) - delta;
            if (timer <= 0) {
                // mostly your colour getting brighter and darker, sometimes a different scene
                Color c = MathUtils.random() < 0.3f ? TV_COLORS[MathUtils.random(TV_COLORS.length - 1)] : tvColors.get(i);
                tvs.get(i).setColor(c.r, c.g, c.b, c.a * LAMP_STRENGTH * MathUtils.random(0.55f, 1f));
                timer = MathUtils.random(0.15f, 1.5f);
            }
            tvTimers.set(i, timer);
        }
        lightMatrix.set(camera.combined).scale(Physics.PPM, Physics.PPM, 1f);
        rayHandler.setCombinedMatrix(lightMatrix);
        rayHandler.update();
        rayHandler.prepareRender(); // done outside the filter texture so they don't fight over it
    }

    // call right after gameRenderer.render(), darkness and lights over everything
    public void render() {
        Gdx.gl.glEnable(GL20.GL_BLEND); // batch.end() turns blending off, without it the lights paint over the whole game
        rayHandler.renderOnly();
    }

    // same black bars as the game view, call on window resize
    public void resize(Viewport viewport) {
        rayHandler.useCustomViewport(viewport.getScreenX(), viewport.getScreenY(),
                viewport.getScreenWidth(), viewport.getScreenHeight());
    }

    public void dispose() {
        rayHandler.dispose(); // also removes all its lights
    }
}