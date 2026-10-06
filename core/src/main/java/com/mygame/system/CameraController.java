package com.mygame.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygame.Physics;
import com.mygame.entities.Player;

public class CameraController {
    private static final float LOOK_AHEAD = 0.4f;       // how far the camera leans toward the mouse
    //private static final float SHIFT_LOOK_AHEAD = 1.0f; // while holding Shift
    private static final float CAMERA_SMOOTHING = 6f;

    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport;

    private final Vector2 mouse = new Vector2();
    private float camX, camY; // smooth camera position 

    public CameraController(float worldWidth, float worldHeight) {
        viewport = new FitViewport(worldWidth, worldHeight, camera);
    }

    /** Camera follows the player but leans toward the mouse. TUNE IT DOWN A BIT LATER*/
    public void updateCamera(Player player, float delta) {
        float px = player.getPosition().x * Physics.PPM;
        float py = player.getPosition().y * Physics.PPM;

        // Mouse position relative to the center of the screen, in world pixels
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);
        float offsetX = mouse.x - camera.position.x;
        float offsetY = mouse.y - camera.position.y;

        float targetX = px + offsetX * LOOK_AHEAD;
        float targetY = py + offsetY * LOOK_AHEAD;

        // Slide toward the target instead of snapping
        float t = Math.min(1f, CAMERA_SMOOTHING * delta);
        camX += (targetX - camX) * t;
        camY += (targetY - camY) * t;

        // Whole pixels only, so no seams between tiles
        camera.position.set(Math.round(camX), Math.round(camY), 0);
        camera.update();
    }

    /** Puts the camera directly on the player (used at spawn and on map change). */
    public void snapCameraToPlayer(Player player) {
        camX = player.getPosition().x * Physics.PPM;
        camY = player.getPosition().y * Physics.PPM;
        camera.position.set(Math.round(camX), Math.round(camY), 0);
        camera.update();
    }

    public void updateMouse() {
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public FitViewport getViewport() {
        return viewport;
    }

    public Vector2 getMouse() {
        return mouse;
    }
}