package com.mygame.entities.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.mygame.Physics;
import com.mygame.entities.Entity;
import com.mygame.factory.BulletManager;
import com.mygame.items.Weapons.Weapon;
import com.mygame.system.BulletOwner;

public class Player extends Entity {

    private static final float SPEED = 120f;
    private static final float SIZE = 16f;

    private final Vector2 direction = new Vector2();
    private Weapon weapon;

    Body body = this.getBody();
    float health = this.getHealth();
    
    public Player(World world, float x, float y, Weapon weapon) {
        super(world, x, y, SIZE, 250f);

        this.weapon = weapon;
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