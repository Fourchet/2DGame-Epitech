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
import com.mygame.factory.BulletManager;
import com.mygame.items.Weapon;
import com.mygame.system.BulletOwner;

public class Player {

    private static final float SPEED = 120f;
    private static final float SIZE = 16f;

    private float health = 250f;
    private final Body body;
    private final Vector2 direction = new Vector2();
    private Weapon weapon;

    public Player(World world, float x, float y, Weapon weapon) {
        this.weapon = weapon;
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

    public void update(Vector2 mousePosition, World world,BulletManager bulletManager, float delta) {
        if (weapon != null){
            weapon.update(delta);
        }

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

        // shoot
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            this.shoot(mousePosition, this.weapon, bulletManager, world);
            System.out.println("Left is pressed");
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


    public void shoot(Vector2 target,Weapon weapon,BulletManager bulletManager, World world) {
        if (weapon == null ){
            return;
        }

        if (!weapon.canShoot()){
            if (weapon.getActualBullets() == 0){
                weapon.Reload();
            }
            return;
        }

        float playerX = body.getPosition().x * Physics.PPM;
        float playerY = body.getPosition().y * Physics.PPM;
        Vector2 shootDirection = new Vector2(target.x - playerX, target.y - playerY).nor();
        float bulletSpawnOffset = 12f;
        float spawnX = playerX + shootDirection.x * bulletSpawnOffset;
        float spawnY = playerY + shootDirection.y * bulletSpawnOffset;

        bulletManager.generateBullet(world, spawnX, spawnY, shootDirection, weapon.getBulletSpeed(), weapon.getDamage(), BulletOwner.PLAYER, this.weapon);
    }  

    public void takeDamage(float damageSustained){
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

    public float getHealth() {
      return health;
    }

    // setters 

    public void setWeapon(Weapon weapon) {
      this.weapon = weapon;
    }

    
}