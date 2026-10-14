package com.mygame.entities.Enemy.Zombie;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygame.Physics;
import com.mygame.entities.Enemy.Enemy;
import com.mygame.entities.Player.Player;

public class Zombie extends Enemy {
  
  private static final float SPEED = 70f;
  private static final float STOP_DISTANCE = 12f;

  private final Vector2 direction = new Vector2();
  
  private static final float cooldown = 2f;
  private boolean touchingPlayer = false;
  private float attackTimer = 0f;

  public Zombie(World world, float x, float y) {
    super(world, x, y,100f, 12f );
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
    player.takeDamage(this.damage);
  }


  public void setTouchingPlayer(boolean touchingPlayer) {
    this.touchingPlayer = touchingPlayer;
  }

  public void recoverHealth(){

  }
}