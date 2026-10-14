package com.mygame.entities.Enemy.Military;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygame.Physics;
import com.mygame.entities.Enemy.Enemy;
import com.mygame.entities.Player.Player;
import com.mygame.factory.BulletManager;
import com.mygame.items.Weapons.Weapon;
import com.mygame.system.BulletOwner;

public class Military extends Enemy {
  
  private static final float speed = 70f;
  private final float stopDistance = MathUtils.random(10f, 20f); // each soldier stops at his own distance
  private static final float melee_range = 18f;
  private final float shootRange = MathUtils.random(200f, 300f); // each soldier opens fire from his own distance
  private static final float cooldown = MathUtils.random(0.2f, 0.5f);

  private final Vector2 direction = new Vector2();
  private final BulletManager bulletManager;
  private final Vector2 shootDirection = new Vector2();

  private World world;
  private Weapon weapon;
  private float attackTimer = 0f;

  public Military(World world, float x, float y, Weapon weapon, BulletManager bulletManager) {
    super(world, x, y,50f, 12f ); // adjust health here
    this.weapon  = weapon;
    this.world = world;
    this.bulletManager = bulletManager;
  }

  @Override
  public void update(Player player, float delta) {
    if (weapon != null){
        weapon.update(delta);
    }

    Vector2 militaryPosition = getPosition();
    Vector2 playerPosition = player.getPosition();

    direction.set(
        playerPosition.x - militaryPosition.x,
        playerPosition.y - militaryPosition.y);

    float distance = direction.len() * Physics.PPM;

    if (distance > stopDistance) {

      direction.nor();

      body.setLinearVelocity(
          direction.x * speed / Physics.PPM,
          direction.y * speed / Physics.PPM);

    } else {

      body.setLinearVelocity(0, 0);
    }

    if (weapon != null) {
      if (distance > shootRange) {
        direction.nor();
          body.setLinearVelocity(
            direction.x * speed / Physics.PPM,
            direction.y * speed / Physics.PPM);
        } else {
          body.setLinearVelocity(0, 0);
          attackTimer -= delta;
          if (attackTimer <= 0f) {
            attack(player);
            attackTimer = cooldown;
          }
        }
      return;
    }


    if (distance > melee_range) {
      direction.nor();
      body.setLinearVelocity(
        direction.x * speed / Physics.PPM,
        direction.y * speed / Physics.PPM);

    } else {
      body.setLinearVelocity(0, 0);
      attackTimer -= delta;

      if (attackTimer <= 0f) {
        attack(player);
        attackTimer = cooldown;
      }
    }
  }


  @Override 
  public void attack(Player player){
      if (weapon == null){
        meleeAttack(player);
      } else {
        shoot(player);
      }
      
  }

  private void meleeAttack(Player player){
    player.takeDamage(getDamage());
  }

  private void shoot(Player player){
    if (!weapon.canShoot()){
      if (weapon.getActualBullets() == 0){
        weapon.Reload();
      }
      return;
    }

    Vector2 shooterPosition = getPosition();
    Vector2 playerPosition = player.getPosition();

    shootDirection.set(
            playerPosition.x - shooterPosition.x,
            playerPosition.y - shooterPosition.y);

    shootDirection.nor();
    
    float shooterX = shooterPosition.x * Physics.PPM;
    float shooterY = shooterPosition.y * Physics.PPM;


    float forward = 32f, side = -3.5f; //counter the fact that the gun isnt perfectly centered
    float spawnX = shooterX + shootDirection.x * forward - shootDirection.y * side;
    float spawnY = shooterY + shootDirection.y * forward + shootDirection.x * side;

    bulletManager.generateBullet(world,spawnX,spawnY,shootDirection,weapon.getBulletSpeed(), weapon.getDamage(), BulletOwner.ENEMY, this.weapon);
  }

  public void recoverHealth(){}
}