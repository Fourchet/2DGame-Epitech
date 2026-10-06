package com.mygame;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygame.collisions.GameContactListener;
import com.mygame.entities.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygame.entities.PlayerRenderer;
import com.mygame.factory.BulletManager;
import com.mygame.factory.EnemiesManager;
import com.mygame.items.MilitaryWeapon;
import com.mygame.items.Pistol;
import com.badlogic.gdx.maps.MapObject;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_WIDTH = 640;
    private static final float WORLD_HEIGHT = 360;
    private SpriteBatch batch;
    private PlayerRenderer playerRenderer;

    private static final float LOOK_AHEAD = 0.4f;       // how far the camera leans toward the mouse
    private static final float SHIFT_LOOK_AHEAD = 1.0f; // while holding Shift
    private static final float CAMERA_SMOOTHING = 6f;   

    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
    private final ShapeRenderer shapes = new ShapeRenderer();

    private final Vector2 mouse = new Vector2();
    private float camX, camY; // smooth camera position 

    private World world;
    private Player player;
    private EnemiesManager factory; // ennemy factory
    private BulletManager bullet_factory; // bullet factory

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private String currentMap;
    private float mapWidth;
    private float mapHeight;

    @Override
    public void show() {
        Box2D.init();
        batch = new SpriteBatch();
        playerRenderer = new PlayerRenderer();
        loadMap("map.tmx");
        player = new Player(world, mapWidth / 2f, mapHeight / 2f, new Pistol());
        
        // spawns enemies and bullet in creating the managers
        factory = new EnemiesManager();
        bullet_factory = new BulletManager();

        // spawns enemies
        factory.spawnZombie(world, mapWidth / 2f + 100, mapHeight / 2f);
        factory.spawnZombie(world, mapWidth / 2f - 100, mapHeight / 2f);
        factory.spawnMilitary(world, mapWidth / 2f - 80, mapHeight / 2f, new MilitaryWeapon(), bullet_factory);


        snapCameraToPlayer();
        spawnPlayer("start");
    }

    //physics stuff
    private void loadMap(String fileName) {
        if (map != null) map.dispose();
        if (world != null) world.dispose();

        map = new TmxMapLoader().load(fileName);
        currentMap = fileName;

        int tileW = map.getProperties().get("tilewidth", Integer.class);
        int tileH = map.getProperties().get("tileheight", Integer.class);
        mapWidth = map.getProperties().get("width", Integer.class) * tileW;
        mapHeight = map.getProperties().get("height", Integer.class) * tileH;

        if (mapRenderer == null) {
            mapRenderer = new OrthogonalTiledMapRenderer(map);
        } else {
            mapRenderer.setMap(map);
        }

        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new GameContactListener());
        Physics.createWalls(world, map);
    }

    private void checkMapTransition() {
        float px = player.getPosition().x * Physics.PPM;
        float py = player.getPosition().y * Physics.PPM;

        MapObject door = MapEvents.findDoorAt(map, px, py);
        if (door == null) return;

        String target = door.getProperties().get("target", String.class);
        String spawn = door.getProperties().get("spawn", String.class);
        loadMap(target);
        spawnPlayer(spawn);
    }

    //  spawns player at a named spawn point, or the middle of the map if it doesn't exist. 
    private void spawnPlayer(String spawnName) {
        Vector2 pos = MapEvents.findSpawn(map, spawnName);
        if (pos == null) pos = new Vector2(mapWidth / 2f, mapHeight / 2f);

        player = new Player(world, pos.x, pos.y, new Pistol());
        snapCameraToPlayer();
    }

    @Override
    public void render(float delta) {

        player.update(mouse, world, bullet_factory, delta);
        factory.update(player, delta);

        world.step(Math.min(delta, 1f / 30f), 6, 2);
        checkMapTransition();

        updateCamera(delta);

        ScreenUtils.clear(0.05f, 0.05f, 0.07f, 1f);
        mouse.set(Gdx.input.getX(), Gdx.input.getY());

        viewport.unproject(mouse);
        viewport.apply();

        float margin = 128; // larger render distance
        mapRenderer.setView(
            camera.combined,
            camera.position.x - viewport.getWorldWidth() / 2f - margin,
            camera.position.y - viewport.getWorldHeight() / 2f - margin,
            viewport.getWorldWidth() + margin * 2,
            viewport.getWorldHeight() + margin * 2);
        mapRenderer.render();

        float px = player.getPosition().x * Physics.PPM; // replaces the placeholder greysquare with the custom character i made, !!!!!! DO LEGS NEXT !!!!!!
        float py = player.getPosition().y * Physics.PPM;
        playerRenderer.update(delta, px, py);
        playerRenderer.aimAt(px, py, mouse.x, mouse.y);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        playerRenderer.draw(batch, px, py);
        batch.end();
        
        shapes.setProjectionMatrix(camera.combined);    
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        factory.draw(shapes);  
        bullet_factory.draw(shapes); 
        shapes.end();
    }

    /** Camera follows the player but leans toward the mouse. TUNE IT DOWN A BIT LATER*/
    private void updateCamera(float delta) {
        float px = player.getPosition().x * Physics.PPM;
        float py = player.getPosition().y * Physics.PPM;

        // Mouse position relative to the center of the screen, in world pixels
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);
        float offsetX = mouse.x - camera.position.x;
        float offsetY = mouse.y - camera.position.y;

        float lean = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? SHIFT_LOOK_AHEAD : LOOK_AHEAD;
        float targetX = px + offsetX * lean;
        float targetY = py + offsetY * lean;

        // Slide toward the target instead of snapping
        float t = Math.min(1f, CAMERA_SMOOTHING * delta);
        camX += (targetX - camX) * t;
        camY += (targetY - camY) * t;

        // Whole pixels only, so no seams between tiles
        camera.position.set(Math.round(camX), Math.round(camY), 0);
        camera.update();
    }

    /** Puts the camera directly on the player (used at spawn and on map change). */
    private void snapCameraToPlayer() {
        camX = player.getPosition().x * Physics.PPM;
        camY = player.getPosition().y * Physics.PPM;
        camera.position.set(Math.round(camX), Math.round(camY), 0);
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (map != null) map.dispose();
        if (world != null) world.dispose();

        if (batch != null) batch.dispose();
        if (playerRenderer != null) playerRenderer.dispose();
    }    
}