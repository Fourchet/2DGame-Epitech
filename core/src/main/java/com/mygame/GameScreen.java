package com.mygame;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygame.entities.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygame.entities.PlayerRenderer;
import com.mygame.factory.BulletManager;
import com.mygame.factory.EnemiesManager;
import com.mygame.items.MilitaryWeapon;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygame.entities.ZombieRenderer;
import com.mygame.entities.Zombie;
import com.badlogic.gdx.utils.Array;
import com.mygame.audio.SoundManager;
import com.mygame.system.CameraController;
import com.mygame.system.WeaponController;
import com.mygame.world.MapManager;
import com.mygame.world.PlayerSpawner;
import com.mygame.render.GameRenderer;



public class GameScreen extends ScreenAdapter {
    private static final float WORLD_WIDTH = 640;
    private static final float WORLD_HEIGHT = 360;
    private SpriteBatch batch;
    private PlayerRenderer playerRenderer;
    private Texture zombieWalkTexture;
    private Animation<TextureRegion> zombieWalk; // shared by every zombie
    private final Array<Body> bodies = new Array<>(); // reused every frame
    private final ObjectMap<Zombie, ZombieRenderer> zombieRenderers = new ObjectMap<>(); // one renderer per zombie

    private SoundManager sounds;
    private Texture bulletTexture;
    private TextureRegion bulletSprite;

    private World world;
    private Player player;
    private CameraController cameraController;
    private MapManager mapManager;
    private WeaponController weaponController;
    private PlayerSpawner playerSpawner;
    private GameRenderer gameRenderer;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private EnemiesManager factory; // ennemy factory
    private BulletManager bullet_factory; // bullet factory

    @Override
    public void show() {
        Box2D.init();
        cameraController = new CameraController(WORLD_WIDTH, WORLD_HEIGHT);
        mapManager = new MapManager();
        weaponController = new WeaponController();
        playerSpawner = new PlayerSpawner();
        batch = new SpriteBatch();
        playerRenderer = new PlayerRenderer();
        zombieWalkTexture = new Texture("sprites/zombie_walk.png");
        zombieWalk = ZombieRenderer.loadWalk(zombieWalkTexture);
        sounds = new SoundManager();
        bulletTexture = new Texture("sprites/bullet.png");
        bulletSprite = new TextureRegion(bulletTexture);
        mapManager.loadMap("map.tmx");
        world = mapManager.getWorld();
        player = playerSpawner.spawnPlayer("start", world, mapManager, weaponController.getCurrentWeapon(), cameraController); // only one player, at the start spawn
        
        // spawns enemies and bullet in creating the managers
        factory = new EnemiesManager();
        bullet_factory = new BulletManager();

        gameRenderer = new GameRenderer(batch, playerRenderer, zombieWalk, bodies, zombieRenderers, bulletSprite, shapes, sounds);

        // spawns enemies
        factory.spawnZombie(world, mapManager.getMapWidth() / 2f + 100, mapManager.getMapHeight() / 2f);
        factory.spawnZombie(world, mapManager.getMapWidth() / 2f - 100, mapManager.getMapHeight() / 2f);
        factory.spawnMilitary(world, mapManager.getMapWidth() / 2f - 80, mapManager.getMapHeight() / 2f, new MilitaryWeapon(), bullet_factory);
        playerRenderer.setHoldingRifle(weaponController.getCurrentWeapon() == weaponController.getRifle());
        playerRenderer.setHoldingPistol(weaponController.getCurrentWeapon() == weaponController.getPistol());
        weaponController.setLastAmmo(weaponController.getCurrentWeapon().getActualBullets());
    }


    private void checkMapTransition() {
        float px = player.getPosition().x * Physics.PPM;
        float py = player.getPosition().y * Physics.PPM;

        MapObject door = MapEvents.findDoorAt(mapManager.getMap(), px, py);
        if (door == null) return;

        String target = door.getProperties().get("target", String.class);
        String spawn = door.getProperties().get("spawn", String.class);
        mapManager.loadMap(target);
        world = mapManager.getWorld();
        zombieRenderers.clear(); // old zombies died with the old world
        player = playerSpawner.spawnPlayer(spawn, world, mapManager, weaponController.getCurrentWeapon(), cameraController);
    }

    
    @Override
    public void render(float delta) {
        weaponController.handleWeaponSwap(player, playerRenderer);

        player.update(cameraController.getMouse(), world, bullet_factory, delta);

        weaponController.handleWeaponEffects(playerRenderer, sounds);

        factory.update(player, delta);

        world.step(Math.min(delta, 1f / 30f), 6, 2);
        bullet_factory.removeDestroyedBullets(world);
        checkMapTransition();

        cameraController.updateCamera(player, delta);

        gameRenderer.render(delta, world, player, cameraController, mapManager);

    }


    @Override
    public void resize(int width, int height) {
        cameraController.resize(width, height);
    }


    @Override
    public void dispose() {
        shapes.dispose();
        if (mapManager != null) mapManager.dispose();
        if (zombieWalkTexture != null) zombieWalkTexture.dispose();
        if (bulletTexture != null) bulletTexture.dispose();
        if (sounds != null) sounds.dispose();
        if (batch != null) batch.dispose();
        if (playerRenderer != null) playerRenderer.dispose();
    }    
}