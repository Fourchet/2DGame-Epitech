package com.mygame.factory;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.mygame.entities.Enemy.Enemy;
import com.mygame.entities.Enemy.Military.Military;
import com.mygame.entities.Enemy.Zombie.Zombie;
import com.mygame.entities.Player.Player;
import com.mygame.items.Weapons.MilitaryWeapon;
import com.mygame.items.Weapons.Pistol;
import com.mygame.items.Weapons.Weapon;
import com.mygame.system.BulletOwner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FactoryTest {

    @BeforeAll
    static void loadNatives() {
        GdxNativesLoader.load();
        Box2D.init();
    }

    @Test
    void bulletManagerUsesAmmoAndCleansUpDestroyedBullets() {
        World world = new World(new Vector2(0, 0), true);
        BulletManager bullets = new BulletManager();
        Weapon pistol = new Pistol();
        int ammoBefore = pistol.getActualBullets();

        bullets.generateBullet(world, 0, 0, new Vector2(1, 0), pistol.getBulletSpeed(),
                pistol.getDamage(), BulletOwner.PLAYER, pistol);

        assertEquals(1, bullets.getBullets().size);
        assertEquals(ammoBefore - 1, pistol.getActualBullets());

        bullets.getBullets().first().destroy();
        bullets.removeDestroyedBullets(world);

        assertEquals(0, bullets.getBullets().size);
        assertEquals(0, world.getBodyCount());
        world.dispose();
    }

    @Test
    void enemiesManagerHandlesZombiesAndMilitaryThroughTheEnemyType() {
        World world = new World(new Vector2(0, 0), true);
        BulletManager bullets = new BulletManager();
        EnemiesManager enemies = new EnemiesManager();
        Player player = new Player(world, 1000, 1000, new Pistol()); 

        Zombie zombie = enemies.spawnZombie(world, 0, 0);
        enemies.spawnMilitary(world, 50, 0, new MilitaryWeapon(), bullets);

        assertEquals(2, enemies.getEnemies().size);
        for (Enemy e : enemies.getEnemies()) {
            assertFalse(e.isDead());
        }

        zombie.takeDamage(1000f);
        enemies.update(player, 6f);             
        enemies.removeDeadEnemies(world);

        assertEquals(1, enemies.getEnemies().size);
        assertTrue(enemies.getEnemies().first() instanceof Military);
        world.dispose();
    }
}