package com.mygame.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Player {

    public static final float PPM = 32f;

    private static final float SPEED = 120f;
    private static final float SIZE = 16f;

    private final Body body;
    private final Vector2 direction = new Vector2();

    public Player(World world, float x, float y) {
        // decrit le corps physique sans le créer
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(
                x / PPM,
                y / PPM);

        // créer le body
        body = world.createBody(bodyDef);

        // créer la forme physique du joueur
        PolygonShape shape = new PolygonShape();

        shape.setAsBox(
                SIZE / 2f / PPM,
                SIZE / 2f / PPM);

        // associe la forme au body
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

    public void update() {
        direction.setZero();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            direction.y += 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            direction.y -= 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            direction.x -= 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            direction.x += 1;
        }

        if (!direction.isZero()) {
            direction.nor();

            body.setLinearVelocity(
                    direction.x * SPEED / PPM,
                    direction.y * SPEED / PPM);
        } else {
            body.setLinearVelocity(0, 0);
        }
    }

    public void draw(ShapeRenderer shapes) {

        float x = body.getPosition().x * PPM;
        float y = body.getPosition().y * PPM;

        shapes.setColor(Color.LIGHT_GRAY);

        shapes.rect(
                x - SIZE / 2f,
                y - SIZE / 2f,
                SIZE,
                SIZE);
    }

    public Body getBody() {
        return body;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }
}