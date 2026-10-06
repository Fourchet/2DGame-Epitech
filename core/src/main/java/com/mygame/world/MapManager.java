package com.mygame.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygame.Physics;
import com.mygame.collisions.GameContactListener;

public class MapManager {
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private String currentMap;
    private float mapWidth;
    private float mapHeight;
    private World world;

    //physics stuff
    public void loadMap(String fileName) {
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
        world.setContactListener(new GameContactListener());
        Physics.createWalls(world, map);
    }

    public TiledMap getMap() {
        return map;
    }

    public OrthogonalTiledMapRenderer getMapRenderer() {
        return mapRenderer;
    }

    public World getWorld() {
        return world;
    }

    public float getMapWidth() {
        return mapWidth;
    }

    public float getMapHeight() {
        return mapHeight;
    }

    public String getCurrentMap() {
        return currentMap;
    }

    public void dispose() {
        if (mapRenderer != null) mapRenderer.dispose();
        if (map != null) map.dispose();
        if (world != null) world.dispose();
    }
}