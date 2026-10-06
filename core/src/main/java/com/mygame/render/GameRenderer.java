package com.mygame.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygame.Physics;
import com.mygame.audio.SoundManager;
import com.mygame.entities.Bullet;
import com.mygame.entities.Enemy;
import com.mygame.entities.Player;
import com.mygame.entities.PlayerRenderer;
import com.mygame.entities.Zombie;
import com.mygame.entities.ZombieRenderer;
import com.mygame.system.CameraController;
import com.mygame.world.MapManager;

public class GameRenderer {
    private SpriteBatch batch;
    private PlayerRenderer playerRenderer;
    private Animation<TextureRegion> zombieWalk;
    private final Array<Body> bodies;
    private final ObjectMap<Zombie, ZombieRenderer> zombieRenderers;
    private TextureRegion bulletSprite;
    private ShapeRenderer shapes;
    private SoundManager sounds;

    public GameRenderer(SpriteBatch batch, PlayerRenderer playerRenderer, Animation<TextureRegion> zombieWalk, Array<Body> bodies, ObjectMap<Zombie, ZombieRenderer> zombieRenderers, TextureRegion bulletSprite, ShapeRenderer shapes, SoundManager sounds) {
        this.batch = batch;
        this.playerRenderer = playerRenderer;
        this.zombieWalk = zombieWalk;
        this.bodies = bodies;
        this.zombieRenderers = zombieRenderers;
        this.bulletSprite = bulletSprite;
        this.shapes = shapes;
        this.sounds = sounds;
    }

    public void render(float delta, World world, Player player, CameraController cameraController, MapManager mapManager) {
        ScreenUtils.clear(0.05f, 0.05f, 0.07f, 1f);
        cameraController.updateMouse();

        OrthographicCamera camera = cameraController.getCamera();
        FitViewport viewport = cameraController.getViewport();
        Vector2 mouse = cameraController.getMouse();

        viewport.apply();

        float margin = 128; // larger render distance
        mapManager.getMapRenderer().setView(
            camera.combined,
            camera.position.x - viewport.getWorldWidth() / 2f - margin,
            camera.position.y - viewport.getWorldHeight() / 2f - margin,
            viewport.getWorldWidth() + margin * 2,
            viewport.getWorldHeight() + margin * 2);
        mapManager.getMapRenderer().render();

        float px = player.getPosition().x * Physics.PPM; // replaces the placeholder greysquare with the custom character i made, !!!!!! DO LEGS NEXT !!!!!!
        float py = player.getPosition().y * Physics.PPM;
        playerRenderer.update(delta, px, py);
        playerRenderer.aimAt(px, py, mouse.x, mouse.y);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // draws every zombie in the world, each with its own renderer
        world.getBodies(bodies);
        for (Body body : bodies) {
            if (!(body.getUserData() instanceof Zombie)) continue;
            Zombie z = (Zombie) body.getUserData();
            ZombieRenderer r = zombieRenderers.get(z);
            if (r == null) {
                r = new ZombieRenderer(zombieWalk);
                zombieRenderers.put(z, r);
            }
            float zx = z.getPosition().x * Physics.PPM;
            float zy = z.getPosition().y * Physics.PPM;
            // if the enemy is dead, we don't update the player pos
            if (!z.isDead()) {
              r.update(delta, zx, zy, px, py);
            }
            if (MathUtils.random() < delta * 0.15f) sounds.playGroan(zx - px, zy - py); // a groan every ~7 s per zombie
            r.draw(batch, zx, zy);
        }
        playerRenderer.draw(batch, px, py);

        // bullets with the sprite, turned toward where they fly
        for (Body body : bodies) {
            if (!(body.getUserData() instanceof Bullet)) continue;
            float bx = body.getPosition().x * Physics.PPM;
            float by = body.getPosition().y * Physics.PPM;
            float angle = body.getLinearVelocity().angleDeg();
            float w = bulletSprite.getRegionWidth();
            float h = bulletSprite.getRegionHeight();
            batch.draw(bulletSprite, bx - w / 2f, by - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, angle);
        }
        batch.end();
        

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // enemies without a sprite yet (military) as white squares
        for (Body body : bodies) {
            Object data = body.getUserData();
            if (data instanceof Enemy && !(data instanceof Zombie)) ((Enemy) data).draw(shapes);
        }
        shapes.end();

    }
}