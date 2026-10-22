package com.mygame.items.Weapons;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class WeaponTest {

    @Test
    void cannotShootWhenMagazineEmpty() {
        Weapon pistol = new Pistol();
        for (int i = 0; i < pistol.getMaxBullets(); i++) pistol.reduceBullet();

        assertEquals(0, pistol.getActualBullets());
        assertFalse(pistol.canShoot());
    }

    @Test
    void reloadRefill() {
        Weapon pistol = new Pistol();
        pistol.reduceBullet();
        pistol.Reload();

        pistol.update(pistol.getReloadTime() / 2);
        assertTrue(pistol.getIsReloading());

        pistol.update(pistol.getReloadTime());
        assertFalse(pistol.getIsReloading());
        assertEquals(pistol.getMaxBullets(), pistol.getActualBullets());
    }
}