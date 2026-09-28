package com.mygame;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygame.entities.Player;

public class GameScreen extends ScreenAdapter {

        private static final float WORLD_WIDTH = 640;
        private static final float WORLD_HEIGHT = 360;

        private final OrthographicCamera camera = new OrthographicCamera();

        private final FitViewport viewport = new FitViewport(
                        WORLD_WIDTH,
                        WORLD_HEIGHT,
                        camera);

        private final ShapeRenderer shapes = new ShapeRenderer();

        private World world;
        private Player player;

        @Override
        public void show() {
                Box2D.init();

                world = new World(
                                new Vector2(0, 0),
                                true);

                player = new Player(
                                world,
                                WORLD_WIDTH / 2f,
                                WORLD_HEIGHT / 2f);
        }

        @Override
        public void render(float delta) {

                player.update();

                world.step(
                                Math.min(delta, 1f / 30f),
                                6,
                                2);

                ScreenUtils.clear(
                                0.05f,
                                0.05f,
                                0.07f,
                                1f);

                viewport.apply();

                shapes.setProjectionMatrix(
                                camera.combined);

                shapes.begin(
                                ShapeRenderer.ShapeType.Filled);

                player.draw(shapes);

                shapes.end();
        }

        @Override
        public void resize(int width, int height) {
                viewport.update(
                                width,
                                height,
                                true);
        }

        @Override
        public void dispose() {
                shapes.dispose();

                if (world != null) {
                        world.dispose();
                }
        }
}