package com.mygame.items;

public abstract class Weapon {
  protected String name;
  protected float damage;
  protected float bulletSpeed;
  protected int maxBullets;
  protected float reloadTime;
  private float reloadTimer = 0f;
  protected int actualBullets;
  protected boolean isReloading = false;
  
  public Weapon(String name, int damage, float bulletSpeed, int maxBullets, float reloadTime){
    this.damage = damage;
    this.name = name;
    this.bulletSpeed = bulletSpeed;
    this.maxBullets = maxBullets;
    this.reloadTime = reloadTime;
    this.actualBullets = maxBullets;
  }

  public void update(float delta){
    if (!isReloading){
      return;
    }

    reloadTimer -= delta;
      if (reloadTimer <= 0f) {
          actualBullets = maxBullets;
          isReloading = false;
      }
    }

  public void reduceBullet(){
    if (actualBullets > 0){
      actualBullets -= 1;
    }

    
  }

  public boolean canShoot() {
    return actualBullets > 0 && !isReloading;
  }

  public void Reload(){
    if (isReloading){
      return;
    }

    if (actualBullets == maxBullets){
      return;
    }

    isReloading = true;
    reloadTimer = reloadTime;

  }
  
  // getters
  public float getDamage() {
    return damage;
  }

  public String getName() {
    return name;
  }

  public float getBulletSpeed() {
    return bulletSpeed;
  }

  public int getMaxBullets() {
    return maxBullets;
  }

  public float getReloadTime() {
    return reloadTime;
  }
  
  public int getActualBullets() {
    return actualBullets;
  }

  public float getReloadTimer() {
    return reloadTimer;
  }

  public boolean getIsReloading(){
    return isReloading;
  }

}
