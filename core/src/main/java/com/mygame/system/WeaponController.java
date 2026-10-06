package com.mygame.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mygame.audio.SoundManager;
import com.mygame.entities.Player;
import com.mygame.entities.PlayerRenderer;
import com.mygame.items.Pistol;
import com.mygame.items.Rifle;
import com.mygame.items.Weapon;

public class WeaponController {
    private final Weapon pistol = new Pistol();
    private final Weapon rifle = new Rifle();
    private Weapon currentWeapon = rifle; // testing, rifle on spawn
    private int lastAmmo; // to notice when he fires
    private boolean wasReloading; // to notice when a reload starts

    // 1 = pistol, 2 = rifle, Q = switch between them
    public void handleWeaponSwap(Player player, PlayerRenderer playerRenderer) {
        Weapon next = currentWeapon;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) next = pistol;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) next = rifle;
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) next = (currentWeapon == pistol) ? rifle : pistol;
        if (next == currentWeapon) return;

        currentWeapon = next;
        player.setWeapon(currentWeapon);
        playerRenderer.setHoldingRifle(currentWeapon == rifle);
        playerRenderer.setHoldingPistol(currentWeapon == pistol);
        lastAmmo = currentWeapon.getActualBullets(); // other gun, other mag
        wasReloading = currentWeapon.getIsReloading();
    }

    public void handleWeaponEffects(PlayerRenderer playerRenderer, SoundManager sounds) {
        // ammo went down = he just fired, plays the firing anim and the shot
        int ammo = currentWeapon.getActualBullets();
        if (ammo < lastAmmo) {
            playerRenderer.fire();
            sounds.playShot(currentWeapon == rifle);
        } else if (ammo == 0 && !currentWeapon.getIsReloading()
                && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            sounds.playDryFire(); // click, empty mag
        }
        lastAmmo = ammo;

        // reload sound when a reload starts
        boolean reloading = currentWeapon.getIsReloading();
        if (reloading && !wasReloading) sounds.playReload();
        wasReloading = reloading;
    }

    public Weapon getPistol() {
        return pistol;
    }

    public Weapon getRifle() {
        return rifle;
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public int getLastAmmo() {
        return lastAmmo;
    }

    public void setLastAmmo(int lastAmmo) {
        this.lastAmmo = lastAmmo;
    }

    public boolean getWasReloading() {
        return wasReloading;
    }

    public void setWasReloading(boolean wasReloading) {
        this.wasReloading = wasReloading;
    }
}