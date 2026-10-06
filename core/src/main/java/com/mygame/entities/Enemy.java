package com.mygame.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygame.Physics;

public abstract class Enemy {

  private static final float SIZE = 16f;

  protected float health;
  protected float damage;
  protected final Body body;

  private static final float DEATH_DELAY = 5f;
  private boolean dead = false;
  private float deathTimer = DEATH_DELAY;

  public Enemy(World world, float x, float y, float health, float damage) {
    this.health = health;
    this.damage = damage;

    // decrit le corps physique sans le créer
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;

    bodyDef.position.set(
        x / Physics.PPM,
        y / Physics.PPM);

    // créer le body
    body = world.createBody(bodyDef);

    // créer la forme physique de l'ennemi
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

  public void draw(ShapeRenderer shapes) {
    float x = body.getPosition().x * Physics.PPM;
    float y = body.getPosition().y * Physics.PPM;

    shapes.setColor(Color.WHITE);

    shapes.rect(
            x - SIZE / 2f,
            y - SIZE / 2f,
            SIZE,
            SIZE);
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

  public float getHealth() {
    return health;
  }

  public boolean isDead() {
    return dead;
  }

  // setters 

  public void setDamage(int damage) {
    this.damage = damage;
  }

  public void setHealth(int health) {
    this.health = health;
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