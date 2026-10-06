package com.mygame.factory;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygame.entities.Enemy;
import com.mygame.entities.Military;
import com.mygame.entities.Player;
import com.mygame.entities.Zombie;
import com.mygame.items.Weapon;

public class EnemiesManager {

    private final Array<Enemy> enemies = new Array<>();
    // spawn enemies 
    public Zombie spawnZombie(World world, float x, float y) {

        Zombie zombie = new Zombie(world, x, y);

        enemies.add(zombie);

        return zombie;
    }

    public Military spawnMilitary(World world, float x, float y, Weapon weapon, BulletManager bulletFactory){
        Military military = new Military(world, x, y, weapon, bulletFactory);
        enemies.add(military);
        return military;
    }

    public void update(Player player, float delta) {

        for (Enemy enemy : enemies) {
            enemy.update(player, delta);
        }
    }

    // draw enemies
    public void draw(ShapeRenderer shapes) {

        for (Enemy enemy : enemies) {
            enemy.draw(shapes);
        }
    }

    public Array<Enemy> getEnemies() {
        return enemies;
    }
}