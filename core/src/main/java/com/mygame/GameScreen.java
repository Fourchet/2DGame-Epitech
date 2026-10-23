package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygame.factory.BulletManager;
import com.mygame.factory.EnemiesManager;
import com.mygame.items.Weapons.MilitaryWeapon;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygame.entities.Enemy.Zombie.Zombie;
import com.mygame.entities.Enemy.Zombie.ZombieRenderer;
import com.mygame.entities.Player.Player;
import com.mygame.entities.Player.PlayerRenderer;
import com.badlogic.gdx.utils.Array;
import com.mygame.audio.SoundManager;
import com.mygame.system.CameraController;
import com.mygame.system.WeaponController;
import com.mygame.world.MapManager;
import com.mygame.world.PlayerSpawner;
import com.mygame.render.GameRenderer;
import com.mygame.render.PostProcess;
import com.mygame.render.LightManager;
import com.mygame.world.SleepEvent;
import com.mygame.world.TravelCutscene;



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
    private PostProcess postProcess;
    private LightManager lightManager;
    private final SleepEvent sleepEvent = new SleepEvent(); // bed: press E to sleep till the light changes
    private final TravelCutscene travel = new TravelCutscene(); // doors with a "cutscene" play it before you arrive

    private final ShapeRenderer shapes = new ShapeRenderer();
    private EnemiesManager factory; // ennemy factory
    private BulletManager bullet_factory; // bullet factory

    private final Main game; // to restart a new game after a game over
    private boolean gameOver = false;
    private float gameOverTimer = 0f; // seconds since the player died
    private BitmapFont font;
    private final Matrix4 screenMatrix = new Matrix4(); // screen coordinates for the game over text

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        Box2D.init();
        font = new BitmapFont(); // libGDX default font
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
        postProcess = new PostProcess();
        lightManager = new LightManager(world, mapManager.getMap(), cameraController.getViewport()); // lamps and sun from the map's "lights" layer
        player = playerSpawner.spawnPlayer("start", world, mapManager, weaponController.getCurrentWeapon(), cameraController); // only one player, at the start spawn
        
        // spawns enemies and bullet in creating the managers
        factory = new EnemiesManager();
        bullet_factory = new BulletManager();

        gameRenderer = new GameRenderer(batch, playerRenderer, zombieWalk, bodies, zombieRenderers, bulletSprite, shapes, sounds);

        // spawns enemies
        factory.spawnZombie(world, mapManager.getMapWidth() / 2f + 110, mapManager.getMapHeight() / 2f); //default zombie spawn
        factory.spawnZombie(world, mapManager.getMapWidth() / 2f + 100, mapManager.getMapHeight() / 2f);
        factory.spawnZombie(world, mapManager.getMapWidth() / 2f + 130, mapManager.getMapHeight() / 2f);
        factory.spawnZombie(world, mapManager.getMapWidth() / 2f + 150, mapManager.getMapHeight() / 2f);
        factory.spawnZombie(world, mapManager.getMapWidth() / 2f + 150, mapManager.getMapHeight() / 2f);
        factory.spawnZombie(world, mapManager.getMapWidth() / 2f + 150, mapManager.getMapHeight() / 2f);
        factory.spawnZombie(world, mapManager.getMapWidth() / 2f + 150, mapManager.getMapHeight() / 2f);
        factory.spawnZombie(world, mapManager.getMapWidth() / 2f + 150, mapManager.getMapHeight() / 2f);
        factory.spawnZombie(world, mapManager.getMapWidth() / 2f + 150, mapManager.getMapHeight() / 2f);

        factory.spawnMilitary(world, mapManager.getMapWidth() / 2f - 80, mapManager.getMapHeight() / 2f, new MilitaryWeapon(), bullet_factory); //default military spawn
        factory.spawnMilitary(world, mapManager.getMapWidth() / 2f - 80, mapManager.getMapHeight() / 2f, new MilitaryWeapon(), bullet_factory); //default military spawn
        factory.spawnMilitary(world, mapManager.getMapWidth() / 2f - 80, mapManager.getMapHeight() / 2f, new MilitaryWeapon(), bullet_factory); //default military spawn

        
        playerRenderer.setHoldingRifle(weaponController.getCurrentWeapon() == weaponController.getRifle());
        playerRenderer.setHoldingPistol(weaponController.getCurrentWeapon() == weaponController.getPistol());
        weaponController.setLastAmmo(weaponController.getCurrentWeapon().getActualBullets());
    }


    private void checkMapTransition() {
        float px = player.getPosition().x * Physics.PPM;
        float py = player.getPosition().y * Physics.PPM;

        if (travel.isBusy()) return; // already on the way
        MapObject door = MapEvents.findDoorAt(mapManager.getMap(), px, py);
        if (door == null) return;

        // door with a cutscene: the map changes later, when the screen is black
        if (door.getProperties().containsKey("cutscene")) {
            travel.start(door);
            return;
        }
        changeMap(door.getProperties().get("target", String.class), door.getProperties().get("spawn", String.class));
    }

    private void changeMap(String target, String spawn) {
        mapManager.loadMap(target);
        world = mapManager.getWorld();
        // lights belong to the old world too, new ones from the new map
        lightManager.dispose();
        lightManager = new LightManager(world, mapManager.getMap(), cameraController.getViewport());
        lightManager.setDay(sleepEvent.isDay()); // still morning in the next map
        zombieRenderers.clear(); // old zombies died with the old world
        gameRenderer.clearEffects(); // soldier corpses, blood and shot flashes from the old map
        // old enemies and bullets died with the old world too, start with empty managers
        factory = new EnemiesManager();
        bullet_factory = new BulletManager();
        player = playerSpawner.spawnPlayer(spawn, world, mapManager, weaponController.getCurrentWeapon(), cameraController);
    }

    
    @Override
    public void render(float delta) {
        if (!gameOver && player.isDead()) gameOver = true; // health reached 0 in takeDamage()

        if (travel.update(delta)) changeMap(travel.getTarget(), travel.getSpawn()); // screen is black, swap the map behind it
        sleepEvent.update(delta, player, world, mapManager.getMap(), lightManager, gameRenderer);
        // no moving or shooting while he sleeps, drives or is dead
        if (!gameOver && !sleepEvent.isBusy() && !travel.isBusy()) {
            weaponController.handleWeaponSwap(player, playerRenderer);

            player.update(cameraController.getMouse(), world, bullet_factory, delta);

            weaponController.handleWeaponEffects(playerRenderer, sounds);
        }

        if (!gameOver) factory.update(player, delta); // enemies freeze once the player is dead

        world.step(Math.min(delta, 1f / 30f), 6, 2);
        bullet_factory.removeDestroyedBullets(world);
        factory.removeDeadEnemies(world);
        checkMapTransition();

        cameraController.updateCamera(player, delta);

        lightManager.update(delta, cameraController.getCamera()); // rays and light texture, before the filter
        postProcess.begin(); // game draws into the filter's texture
        gameRenderer.render(delta, world, player, cameraController, mapManager);
        lightManager.render(); // darkness and lights on top of the game
        sleepEvent.drawFade(shapes, cameraController.getCamera()); // black fades on top of everything
        travel.draw(batch, shapes, cameraController.getViewport()); // car cutscene over the whole screen
        postProcess.end(delta); // then onto the screen with the cozy shader
        lightManager.flash(gameRenderer.getNewShots()); // this frame's shots light up the room

        if (gameOver) {
            gameOverTimer += delta;
            drawGameOver();
            // wait 1 s so a key pressed while dying doesn't restart right away
            if (gameOverTimer > 1f && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.setScreen(new GameScreen(game));
                dispose(); // setScreen() only hides the old screen, free its textures, sounds and world
                return;
            }
        }
    }

    // black veil + text over the whole screen, in screen coordinates like TravelCutscene
    private void drawGameOver() {
        cameraController.getViewport().apply();
        float w = cameraController.getViewport().getWorldWidth();
        float h = cameraController.getViewport().getWorldHeight();
        screenMatrix.setToOrtho2D(0, 0, w, h);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(screenMatrix);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, Math.min(0.7f, gameOverTimer)); // fades to black
        shapes.rect(0, 0, w, h);
        shapes.end();

        batch.setProjectionMatrix(screenMatrix);
        batch.begin();
        font.draw(batch, "GAME OVER", 0, h / 2f + 20, w, Align.center, false);
        if (gameOverTimer > 1f) font.draw(batch, "Press ENTER to restart", 0, h / 2f - 10, w, Align.center, false);
        batch.end();
    }


    @Override
    public void resize(int width, int height) {
        cameraController.resize(width, height);
        if (postProcess != null) postProcess.resize(); // filter texture follows the window size
        if (lightManager != null) lightManager.resize(cameraController.getViewport()); // lights follow the black bars
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
        if (postProcess != null) postProcess.dispose();
        if (lightManager != null) lightManager.dispose();
        if (font != null) font.dispose();
        travel.dispose();
    }    
}