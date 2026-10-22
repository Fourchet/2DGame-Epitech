package com.mygame.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.mygame.entities.Enemy.Zombie.Zombie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EntityTest {

    @BeforeAll
    static void loadNatives() {
        GdxNativesLoader.load();
        Box2D.init();
    }

    @Test
    void zombieDiesAfterEnoughDamageAndHealthNeverGoesNegative() {
        World world = new World(new Vector2(0, 0), true);
        Zombie zombie = new Zombie(world, 0, 0);

        zombie.takeDamage(40f);
        assertFalse(zombie.isDead());
        assertEquals(60f, zombie.getHealth());

        zombie.takeDamage(500f);
        assertTrue(zombie.isDead());
        assertEquals(0f, zombie.getHealth());
        world.dispose();
    }

    @Test
    void corpseCanBeRemovedOnlyAfterDeathDelay() {
        World world = new World(new Vector2(0, 0), true);
        Zombie zombie = new Zombie(world, 0, 0);

        zombie.takeDamage(1000f);
        assertFalse(zombie.canBeDisable());

        zombie.updateDeath(6f);
        assertTrue(zombie.canBeDisable());
        world.dispose();
    }
}