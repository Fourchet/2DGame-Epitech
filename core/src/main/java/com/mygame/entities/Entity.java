package com.mygame.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygame.Physics;

public abstract class Entity {

    private float health;
    private final Body body;

    public Entity(World world, float x, float y, float size, float health) {
        this.health = health;

        // décrit le corps physique
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(
                x / Physics.PPM,
                y / Physics.PPM
        );

        // crée le body
        body = world.createBody(bodyDef);

        // forme physique
        PolygonShape shape = new PolygonShape();

        shape.setAsBox(
                size / 2f / Physics.PPM,
                size / 2f / Physics.PPM
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;

        body.createFixture(fixtureDef);

        shape.dispose();

        body.setFixedRotation(true);
        body.setUserData(this);
    }

    public Body getBody() {
        return body;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }
}