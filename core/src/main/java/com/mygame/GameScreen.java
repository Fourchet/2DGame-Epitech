package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** The main gameplay screen. */
public class GameScreen extends ScreenAdapter {
    // Size of the visible area in pixels. The window scales it to fit.
    private static final float WORLD_WIDTH = 640;
    private static final float WORLD_HEIGHT = 360;

    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
    private final ShapeRenderer shapes = new ShapeRenderer();

    private final World world = new World(new Vector2(0, 0), true); // no gravity: top-down
    private final Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Player player;

    public GameScreen() {
        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        Physics.createWalls(world, map);

        // Spawn position in pixels. (0, 0) is the BOTTOM-left corner of the map.
        player = new Player(world, 100, 100);
    }

    @Override
    public void render(float delta) {
        handleDevKeys();

        player.update(delta);
        world.step(1 / 60f, 6, 2);

        // Camera follows the player
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        ScreenUtils.clear(0, 0, 0, 1f);
        viewport.apply();

        mapRenderer.setView(camera);
        mapRenderer.render();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        player.draw(shapes);
        shapes.end();

        // Green outlines of walls and player. Remove once everything lines up.
        debugRenderer.render(world, camera.combined.cpy().scl(Physics.PPM));
    }

    private void handleDevKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) Gdx.graphics.setWindowedMode(1280, 720);
            else Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        debugRenderer.dispose();
        world.dispose();
        mapRenderer.dispose();
        map.dispose();
    }
}