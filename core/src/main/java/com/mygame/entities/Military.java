package com.mygame.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygame.Physics;
import com.mygame.factory.BulletFactory;
import com.mygame.items.Weapon;
import com.mygame.system.BulletOwner;

public class Military extends Enemy {
  
  private static final float speed = 70f;
  private static final float STOP_DISTANCE = 12f;
  private static final float melee_range = 18f;
  private static final float shoot_range = 180f;
  private static final float cooldown = 2f;

  private final Vector2 direction = new Vector2();
  private final BulletFactory bulletFactory;
  private final Vector2 shootDirection = new Vector2();

  private World world;
  private Weapon weapon;
  private float attackTimer = 0f;

  public Military(World world, float x, float y, Weapon weapon, BulletFactory bulletFactory) {
    super(world, x, y,150, 12 );
    this.weapon  = weapon;
    this.world = world;
    this.bulletFactory = bulletFactory;
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

    if (distance > STOP_DISTANCE) {

      direction.nor();

      body.setLinearVelocity(
          direction.x * speed / Physics.PPM,
          direction.y * speed / Physics.PPM);

    } else {

      body.setLinearVelocity(0, 0);
    }

    if (weapon != null) {
      if (distance > shoot_range) {
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

  public void TakeDamage(int damageSustained){
    this.health -= damageSustained;

    if (this.health < 0){
      this.health = 0;
      System.out.println("Military's dead");
    }
  }

  private void meleeAttack(Player player){
    player.TakeDamage(getDamage());
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
    float bulletSpawnOffset = 12f;

    shootDirection.set(
            playerPosition.x - shooterPosition.x,
            playerPosition.y - shooterPosition.y);

    shootDirection.nor();
    
    float shooterX = shooterPosition.x * Physics.PPM;
    float shooterY = shooterPosition.y * Physics.PPM;

    float spawnX = shooterX + shootDirection.x * bulletSpawnOffset;
    float spawnY = shooterY + shootDirection.y * bulletSpawnOffset;

    bulletFactory.generateBullet(world,spawnX,spawnY,shootDirection,weapon.getBulletSpeed(), weapon.getDamage(), BulletOwner.ENEMY, this.weapon);
  }

  public void recoverHealth(){}
}