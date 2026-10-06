package com.mygame.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.mygame.Physics;
import com.mygame.system.BulletOwner;

public class Bullet {

    private static final float SIZE = 4f;
    private final BulletOwner owner;
    private final Body body;
    private final float damage;
    
    public Bullet(
            World world,
            float x,
            float y,
            Vector2 direction,
            float speed,
            float damage, 
            BulletOwner owner) {

        this.damage = damage;
        this.owner = owner;

        // description du body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(
                x / Physics.PPM,
                y / Physics.PPM);

        // creation du body dans Box2D
        body = world.createBody(bodyDef);

        body.setBullet(true);

        CircleShape shape = new CircleShape();
        shape.setRadius(SIZE / 2f / Physics.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);

        shape.dispose();

        body.setUserData(this);

        Vector2 bulletDirection = new Vector2(direction).nor();

        body.setLinearVelocity(
                bulletDirection.x * speed / Physics.PPM,
                bulletDirection.y * speed / Physics.PPM);
    }
    
    public void draw(ShapeRenderer shapes) {

        float x = body.getPosition().x * Physics.PPM;
        float y = body.getPosition().y * Physics.PPM;

        shapes.setColor(Color.YELLOW);

        shapes.circle(
                x,
                y,
                SIZE / 2f);
    }


    // getters 

    public Body getBody() {
        return body;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public float getDamage() {
        return damage;
    }

    public BulletOwner getOwner() {
      return owner;
    }
}