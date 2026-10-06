package com.mygame.factory;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygame.entities.Bullet;
import com.mygame.items.Weapon;
import com.mygame.system.BulletOwner;

public class BulletFactory {
  private final Array<Bullet> bulletsArray = new Array<Bullet>();

  public void generateBullet(World world,float x,float y, Vector2 direction,float speed,float damage, BulletOwner owner, Weapon weapon){
    Bullet bullet = new Bullet(world, x, y, direction, speed, damage, owner);
    bulletsArray.add(bullet);
    weapon.reduceBullet();
    System.out.println("Bullet created. Total bullets: " + bulletsArray.size);
  }



  public void draw(ShapeRenderer shapes) {
      for (Bullet bullet : bulletsArray) {
          bullet.draw(shapes);
      }
  }

  public Array<Bullet> getBullets() {
      return bulletsArray;
  }

}
