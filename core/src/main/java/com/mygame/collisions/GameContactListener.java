package com.mygame.collisions;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.mygame.entities.Player;
import com.mygame.entities.Zombie;

public class GameContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {

        Object objectA = contact.getFixtureA().getBody().getUserData();
        Object objectB = contact.getFixtureB().getBody().getUserData();

        if (objectA instanceof Zombie && objectB instanceof Player) {
            Zombie zombie = (Zombie) objectA;
            zombie.setTouchingPlayer(true);
        }

        if (objectB instanceof Zombie && objectA instanceof Player) {
            Zombie zombie = (Zombie) objectB;
            zombie.setTouchingPlayer(true);
        }
    }

    @Override
    public void endContact(Contact contact) {

        Object objectA = contact.getFixtureA().getBody().getUserData();
        Object objectB = contact.getFixtureB().getBody().getUserData();

        if (objectA instanceof Zombie && objectB instanceof Player) {
            Zombie zombie = (Zombie) objectA;
            zombie.setTouchingPlayer(false);
        }

        if (objectB instanceof Zombie && objectA instanceof Player) {
            Zombie zombie = (Zombie) objectB;
            zombie.setTouchingPlayer(false);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}