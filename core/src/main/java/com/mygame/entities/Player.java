package com.mygame.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygame.Physics;

public class Player {

    private static final float SPEED = 120f;
    private static final float SIZE = 16f;

    private int health = 250;
    private final Body body;
    private final Vector2 direction = new Vector2();

    public Player(World world, float x, float y) {
        // decrit le corps physique sans le créer
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(
                x / Physics.PPM,
                y / Physics.PPM);

        // créer le body
        body = world.createBody(bodyDef);

        // créer la forme physique du joueur
        PolygonShape shape = new PolygonShape();

        shape.setAsBox(
                SIZE / 2f / Physics.PPM,
                SIZE / 2f / Physics.PPM);

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
                    direction.x * SPEED / Physics.PPM,
                    direction.y * SPEED / Physics.PPM);
        } else {
            body.setLinearVelocity(0, 0);
        }
    }


    public void TakeDamage(int damageSustained){
        this.health -= damageSustained;
        System.out.println(this + "is taking damage, he has now " + health +"hp");
        if (this.health < 0){
            this.health = 0;
            System.out.println("Player's dead");
        }
    }

    // getters 
    public Body getBody() {
        return body;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public int getHealth() {
      return health;
    }
}