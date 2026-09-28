package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** The player: position, movement, drawing. */
public class Player {
    private static final float SPEED = 120f; // pixels per second
    private static final float SIZE = 16f;

    private float x, y;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float delta) {
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx += 1;

        // Diagonal movement shouldn't be faster than straight movement
        if (dx != 0 && dy != 0) {
            dx *= 0.7071f;
            dy *= 0.7071f;
        }

        x += dx * SPEED * delta;
        y += dy * SPEED * delta;
    }

    public void draw(ShapeRenderer shapes) {
        shapes.setColor(Color.LIGHT_GRAY);
        shapes.rect(x - SIZE / 2f, y - SIZE / 2f, SIZE, SIZE);
    }

    public float getX() { return x; }
    public float getY() { return y; }
}