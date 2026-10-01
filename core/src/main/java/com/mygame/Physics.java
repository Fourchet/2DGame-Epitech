package com.mygame;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

// Box2D helpers. Box2D works in meters, the game works in pixels: PPM converts between them. 
public final class Physics {
    public static final float PPM = 16f; // pixels per meter

    private Physics() {}

    // Creates a static wall body for every rectangle in the map's "collisions" object layer. 
    public static void createWalls(World world, TiledMap map) {
        MapLayer layer = map.getLayers().get("collisions");
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;
            Rectangle r = ((RectangleMapObject) object).getRectangle();

            BodyDef def = new BodyDef();
            def.type = BodyDef.BodyType.StaticBody;
            def.position.set((r.x + r.width / 2f) / PPM, (r.y + r.height / 2f) / PPM);
            Body body = world.createBody(def);

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(r.width / 2f / PPM, r.height / 2f / PPM);
            body.createFixture(shape, 0f);
            shape.dispose();
        }
    }
}
