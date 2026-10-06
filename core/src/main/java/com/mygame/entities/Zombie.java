package com.mygame.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygame.Physics;

public class Zombie extends Enemy {
  
  private static final float SPEED = 70f;
  private static final float STOP_DISTANCE = 12f;

  private final Vector2 direction = new Vector2();
  
  private static final float cooldown = 2f;
  private boolean touchingPlayer = false;
  private float attackTimer = 0f;

  public Zombie(World world, float x, float y) {
    super(world, x, y,100, 12 );
  }

  @Override
  public void update(Player player, float delta) {

    Vector2 zombiePosition = getPosition();
    Vector2 playerPosition = player.getPosition();

    direction.set(
        playerPosition.x - zombiePosition.x,
        playerPosition.y - zombiePosition.y);

    float distance = direction.len() * Physics.PPM;

    if (distance > STOP_DISTANCE) {

      direction.nor();

      body.setLinearVelocity(
          direction.x * SPEED / Physics.PPM,
          direction.y * SPEED / Physics.PPM);

    } else {

      body.setLinearVelocity(0, 0);
    }
      if (touchingPlayer) {

          attackTimer += delta;

          if (attackTimer >= cooldown) {
              attack(player);
              attackTimer = 0f;

          }

      } else {

          attackTimer = 0f;
      }
    }

  @Override 
  public void attack(Player player){
    System.out.println("Zombie attack "+ player);
    player.TakeDamage(this.damage);
  }


  public void setTouchingPlayer(boolean touchingPlayer) {
    this.touchingPlayer = touchingPlayer;
  }

  public void TakeDamage(int damageSustained){
    this.health -= damageSustained;

    if (this.health < 0){
      this.health = 0;
      System.out.println("Zombie's dead");
    }
  }

  public void recoverHealth(){

  }
}