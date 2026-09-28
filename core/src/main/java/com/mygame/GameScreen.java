package com.mygame;
 
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
 
/** The main gameplay screen. */
public class GameScreen extends ScreenAdapter {
    // Size of the game world in pixels. The window scales it to fit.
    private static final float WORLD_WIDTH = 640;
    private static final float WORLD_HEIGHT = 360;
 
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Player player = new Player(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f);
 
    @Override
    public void render(float delta) {
        player.update(delta);
 
        ScreenUtils.clear(0.05f, 0.05f, 0.07f, 1f);
        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
 
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        player.draw(shapes);
        shapes.end();
    }
 
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
 
    @Override
    public void dispose() {
        shapes.dispose();
    }
}
