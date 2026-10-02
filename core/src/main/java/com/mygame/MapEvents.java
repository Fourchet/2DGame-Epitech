package com.mygame;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

// voir les objets "events" sur les maps tiled
public final class MapEvents {
    private static final String LAYER = "events";

    private MapEvents() {}

    // spawn at center of spawnName (rectangle dans tiled)
    public static Vector2 findSpawn(TiledMap map, String spawnName) {
        MapLayer layer = map.getLayers().get(LAYER);
        if (layer == null || spawnName == null) return null;

        MapObject object = layer.getObjects().get(spawnName);
        if (!(object instanceof RectangleMapObject)) return null;

        Rectangle r = ((RectangleMapObject) object).getRectangle();
        return new Vector2(r.x + r.width / 2f, r.y + r.height / 2f);
    }

    // door transition
    public static MapObject findDoorAt(TiledMap map, float x, float y) {
        MapLayer layer = map.getLayers().get(LAYER);
        if (layer == null) return null;

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;
            if (!object.getProperties().containsKey("target")) continue;
            if (((RectangleMapObject) object).getRectangle().contains(x, y)) return object;
        }
        return null;
    }
}