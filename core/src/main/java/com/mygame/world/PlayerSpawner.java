package com.mygame.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygame.MapEvents;
import com.mygame.entities.Player;
import com.mygame.items.Weapons.Weapon;
import com.mygame.system.CameraController;

public class PlayerSpawner {

    //  spawns player at a named spawn point, or the middle of the map if it doesn't exist. 
    public Player spawnPlayer(String spawnName, World world, MapManager mapManager, Weapon currentWeapon, CameraController cameraController) {
        Vector2 pos = MapEvents.findSpawn(mapManager.getMap(), spawnName);
        if (pos == null) pos = new Vector2(mapManager.getMapWidth() / 2f, mapManager.getMapHeight() / 2f);

        Player player = new Player(world, pos.x, pos.y, currentWeapon); // keeps his gun and ammo between maps
        cameraController.snapCameraToPlayer(player);

        return player;
    }
}