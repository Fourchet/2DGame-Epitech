package com.mygame.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygame.Physics;
import com.mygame.audio.SoundManager;
import com.mygame.entities.Enemy.Enemy;
import com.mygame.entities.Enemy.Military.Military;
import com.mygame.entities.Enemy.Military.MilitaryRenderer;
import com.mygame.entities.Enemy.Zombie.Zombie;
import com.mygame.entities.Enemy.Zombie.ZombieRenderer;
import com.mygame.entities.Player.Player;
import com.mygame.entities.Player.PlayerRenderer;
import com.mygame.entities.others.BloodEffects;
import com.mygame.entities.others.Bullet;
import com.mygame.system.CameraController;
import com.mygame.world.MapManager;
import com.badlogic.gdx.graphics.Texture;
import com.mygame.system.BulletOwner;
import com.badlogic.gdx.utils.ObjectSet;

public class GameRenderer {
    private SpriteBatch batch;
    private PlayerRenderer playerRenderer;
    private Animation<TextureRegion> zombieWalk;
    private final Array<Body> bodies;
    private final ObjectMap<Zombie, ZombieRenderer> zombieRenderers;
    private TextureRegion bulletSprite;
    private ShapeRenderer shapes;
    private SoundManager sounds;
    private final Texture zombieDeathTexture = new Texture("sprites/zombie_death.png");
    private final Animation<TextureRegion> zombieDeath = ZombieRenderer.loadDeath(zombieDeathTexture, ZombieRenderer.DEATH_FRAMES);
    private final Texture zombieDeath2Texture = new Texture("sprites/zombie_death2.png");
    private final Animation<TextureRegion> zombieDeath2 = ZombieRenderer.loadDeath(zombieDeath2Texture, ZombieRenderer.DEATH2_FRAMES);
    private final BloodEffects blood = new BloodEffects();
    private final Texture militaryLegsTexture = new Texture("sprites/military_legs.png");
    private final Texture militaryTorsoTexture = new Texture("sprites/military_torso.png");
    private final Animation<TextureRegion> militaryLegs = MilitaryRenderer.loadLegs(militaryLegsTexture);
    private final TextureRegion militaryTorso = MilitaryRenderer.loadTorso(militaryTorsoTexture);
    private final Texture militaryIdleTexture = new Texture("sprites/military_torso_idle.png");
    private final Animation<TextureRegion> militaryIdle = MilitaryRenderer.loadTorsoIdle(militaryIdleTexture);
    private final Texture militaryDeathTexture = new Texture("sprites/military_death.png");
    private final Animation<TextureRegion> militaryDeath = MilitaryRenderer.loadDeath(militaryDeathTexture);
    private final ObjectMap<Military, MilitaryRenderer> militaryRenderers = new ObjectMap<>(); // one renderer per soldier
    private final Array<Vector3> newShots = new Array<>(); // x, y, angle of this frame's shots, the LightManager flashes there
    private static final float PLAYER_MUZZLE_OFFSET = 36f; // px from where bullets spawn to the end of the barrel, measured on oconnor_torso_rifle_fire
    private static final float PLAYER_MUZZLE_SIDE = -3f; // px, the rifle sits a bit to his right
    private static final float ENEMY_MUZZLE_OFFSET = 27.5f; // soldiers, 0 if bullets already spawn at the barrel
    private boolean playerVisible = true;
    private ObjectSet<Bullet> seenBullets = new ObjectSet<>(); // bullets already on screen last frame
    private ObjectSet<Bullet> currentBullets = new ObjectSet<>();

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
        // update every zombie still in the world
        world.getBodies(bodies);
        for (Body body : bodies) {
            if (!(body.getUserData() instanceof Zombie)) continue;
            Zombie z = (Zombie) body.getUserData();
            ZombieRenderer r = zombieRenderers.get(z);
            if (r == null) {
                r = new ZombieRenderer(zombieWalk, zombieDeath, zombieDeath2, blood);
                zombieRenderers.put(z, r);
            }
            float zx = z.getPosition().x * Physics.PPM;
            float zy = z.getPosition().y * Physics.PPM;
            // always updated, it's how the renderer sees the hits and the death (corpse stays where he fell)
            r.update(delta, zx, zy, px, py, z.getHealth());
            if (!r.isDead() && MathUtils.random() < delta * 0.15f) sounds.playGroan(zx - px, zy - py); // a groan every ~7 s per zombie, corpses stay quiet
        }
        // corpses first so the living walk over them, they stay even once the body is gone
        for (ZombieRenderer r : zombieRenderers.values()) {
            if (!r.isDead()) continue;
            if (r.headPopNow()) sounds.playHeadPop(r.getX() - px, r.getY() - py); // 1 s after the rare death
            r.draw(batch);
        }
        for (ZombieRenderer r : zombieRenderers.values()) if (!r.isDead()) r.draw(batch);
        // soldiers, each with its own renderer
        for (Body body : bodies) {
            if (!(body.getUserData() instanceof Military)) continue;
            Military m = (Military) body.getUserData();
            MilitaryRenderer r = militaryRenderers.get(m);
            if (r == null) {
                r = new MilitaryRenderer(militaryLegs, militaryTorso, militaryIdle, militaryDeath, blood);
                militaryRenderers.put(m, r);
            }
            r.update(delta, m.getPosition().x * Physics.PPM, m.getPosition().y * Physics.PPM, px, py, m.getHealth());
            if (!r.isDead()) r.draw(batch);
        }
        // soldier corpses, they stay even once the body is gone
        for (MilitaryRenderer r : militaryRenderers.values()) {
            if (!r.isDead()) continue;
            if (r.deathSoundNow()) sounds.playHelmetHeadshot(r.getX() - px, r.getY() - py); // the frame he dies
            r.draw(batch);
        }
        // blood on top
        blood.update(delta);
        blood.draw(batch);
        if (playerVisible) playerRenderer.draw(batch, px, py); // hidden while he's in the bed sprite

        // bullets with the sprite, turned toward where they fly
        currentBullets.clear();
        newShots.clear();
        for (Body body : bodies) {
            if (!(body.getUserData() instanceof Bullet)) continue;
            Bullet bullet = (Bullet) body.getUserData();
            currentBullets.add(bullet);
            // new bullet = someone just fired, remember where the barrel was for the light
            if (!seenBullets.contains(bullet)) {
                Vector2 vel = body.getLinearVelocity();
                float a = vel.angleDeg();
                float step = Math.min(delta, 1f / 30f); // it already flew one physics step, go back to where it spawned
                boolean enemy = bullet.getOwner() == BulletOwner.ENEMY;
                float forward = enemy ? ENEMY_MUZZLE_OFFSET : PLAYER_MUZZLE_OFFSET;
                float side = enemy ? 0f : PLAYER_MUZZLE_SIDE;
                // forward along the shot, side = sideways from it
                float mx = (body.getPosition().x - vel.x * step) * Physics.PPM + MathUtils.cosDeg(a) * forward - MathUtils.sinDeg(a) * side;
                float my = (body.getPosition().y - vel.y * step) * Physics.PPM + MathUtils.sinDeg(a) * forward + MathUtils.cosDeg(a) * side;
                newShots.add(new Vector3(mx, my, a));
            }
            // new enemy bullet = a soldier just fired, mp5 sound from where it left
            if (!seenBullets.contains(bullet) && bullet.getOwner() == BulletOwner.ENEMY) {
                sounds.playEnemyShot(body.getPosition().x * Physics.PPM - px, body.getPosition().y * Physics.PPM - py);
            }
            float bx = body.getPosition().x * Physics.PPM;
            float by = body.getPosition().y * Physics.PPM;
            float angle = body.getLinearVelocity().angleDeg();
            float w = bulletSprite.getRegionWidth();
            float h = bulletSprite.getRegionHeight();
            batch.draw(bulletSprite, bx - w / 2f, by - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, angle);
        }
        batch.end();
        // swap so next frame knows which bullets are new
        ObjectSet<Bullet> swap = seenBullets;
        seenBullets = currentBullets;
        currentBullets = swap;
        

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // enemies without a sprite yet (military) as white squares
        for (Body body : bodies) {
            Object data = body.getUserData();
            if (data instanceof Enemy && !(data instanceof Zombie) && !(data instanceof Military)) ((Enemy) data).draw(shapes);
        }
        shapes.end();

    }

    // sprays from the old map, call on map change
    public void clearEffects() {
        blood.clear();
        militaryRenderers.clear(); // soldiers from the old map
        seenBullets.clear();
        newShots.clear();
    }

    // false = player not drawn, for cutscenes like sleeping
    public void setPlayerVisible(boolean playerVisible) {
        this.playerVisible = playerVisible;
    }

    // where shots happened this frame, for the LightManager
    public Array<Vector3> getNewShots() {
        return newShots;
    }

    public void dispose() {
        zombieDeathTexture.dispose();
        zombieDeath2Texture.dispose();
        militaryLegsTexture.dispose();
        militaryTorsoTexture.dispose();
        militaryIdleTexture.dispose();
        militaryDeathTexture.dispose();
        blood.dispose();
    }
}