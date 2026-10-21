package com.mygame.entities.Enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.mygame.Physics;
import com.mygame.entities.Entity;
import com.mygame.entities.Player.Player;

public abstract class Enemy extends Entity {

    private static final float SIZE = 16f;
    private static final float DEATH_DELAY = 5f;
    private boolean dead = false;
    private float deathTimer = DEATH_DELAY;
    protected float damage;

    Body body = this.getBody();
    float health = this.getHealth();

    public Enemy(World world, float x, float y, float health, float damage) {
      super(world, x, y, SIZE, health);
      this.damage = damage;
    }

  public void draw(ShapeRenderer shapes) {
    float x = body.getPosition().x * Physics.PPM;
    float y = body.getPosition().y * Physics.PPM;

    shapes.setColor(Color.WHITE);

    shapes.rect(x - SIZE / 2f,y - SIZE / 2f, SIZE,SIZE);
  }


  // getters 

  public float getDamage() {
    return damage;
  }

  public boolean isDead() {
    return dead;
  }

  // setters 

  public void setDamage(int damage) {
    this.damage = damage;
  }


  public void takeDamage(float damageSustained){
    this.health -= damageSustained;

    if (this.health <= 0 && !dead){
      this.health = 0;
      dead = true;
      body.setLinearVelocity(0, 0);
      body.setAngularVelocity(0);
      body.setFixedRotation(true);
    }
    System.out.println(getClass().getName() + " has " + health);
  }
  
  public void updateDeath(float delta) {
    if (!dead) {
        return;
    }
    deathTimer -= delta;
  }

  public void disableCollision() {
    for (Fixture fixture : body.getFixtureList()) {
        Filter filter = fixture.getFilterData();
        filter.maskBits = 0;
        fixture.setFilterData(filter);
    }
  }

  public boolean canBeDisable() {
      return dead && deathTimer <= 0f;
  }
  
  // abstract methods
  public abstract void update(Player player, float delta);
  public abstract void recoverHealth();
  public abstract void attack(Player player);
}