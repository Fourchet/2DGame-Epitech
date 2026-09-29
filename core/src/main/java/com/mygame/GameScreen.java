package com.mygame;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygame.entities.Player;

public class GameScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 640;
    private static final float WORLD_HEIGHT = 360;

    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
    private final ShapeRenderer shapes = new ShapeRenderer();

    private World world;
    private Player player;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private String currentMap;
    private float mapWidth;
    private float mapHeight;

    @Override
    public void show() {
        Box2D.init();
        loadMap("map.tmx");
        player = new Player(world, mapWidth / 2f, mapHeight / 2f);
    }

    /** Replaces the current map and physics world with a new one. */
    private void loadMap(String fileName) {
        if (map != null) map.dispose();
        if (world != null) world.dispose();

        map = new TmxMapLoader().load(fileName);
        currentMap = fileName;

        int tileW = map.getProperties().get("tilewidth", Integer.class);
        int tileH = map.getProperties().get("tileheight", Integer.class);
        mapWidth = map.getProperties().get("width", Integer.class) * tileW;
        mapHeight = map.getProperties().get("height", Integer.class) * tileH;

        if (mapRenderer == null) {
            mapRenderer = new OrthogonalTiledMapRenderer(map);
        } else {
            mapRenderer.setMap(map);
        }

        world = new World(new Vector2(0, 0), true);
        Physics.createWalls(world, map);
    }

    private void checkMapTransition() {
        float py = player.getPosition().y * Physics.PPM;

        if (currentMap.equals("map.tmx") && py >= mapHeight - 8) {
            loadMap("house.tmx");
            player = new Player(world, mapWidth / 2f, 24); // bottom-center of the house
        }
    }

    @Override
    public void render(float delta) {
        player.update();
        world.step(Math.min(delta, 1f / 30f), 6, 2);
        checkMapTransition();

        // camera follows the player
        camera.position.set(
            player.getPosition().x * Physics.PPM,
            player.getPosition().y * Physics.PPM,
            0);
        camera.update();

        ScreenUtils.clear(0.05f, 0.05f, 0.07f, 1f);
        viewport.apply();

        mapRenderer.setView(camera);
        mapRenderer.render();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        player.draw(shapes);
        shapes.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (map != null) map.dispose();
        if (world != null) world.dispose();
    }
}