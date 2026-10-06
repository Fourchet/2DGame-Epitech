package com.mygame.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

// all the game's sounds in one place, missing files are skipped so they can be added one by one
public class SoundManager {
    private static final float HEARING_DISTANCE = 400f; // px, zombies further than this are silent

    private final Array<Sound> loaded = new Array<>(); // everything to dispose
    private final Sound pistolShot;
    private final Sound rifleShot;
    private final Sound reload;
    private final Sound dryFire;
    private final Array<Sound> groans = new Array<>(); // picked at random so it doesn't loop the same one

    public SoundManager() {
        pistolShot = load("sounds/pistol.wav");
        rifleShot = load("sounds/rifle.wav");
        reload = load("sounds/reload.wav");
        dryFire = load("sounds/dry_fire.wav");
        for (int i = 1; i <= 3; i++) {
            Sound groan = load("sounds/zombie_groan" + i + ".wav");
            if (groan != null) groans.add(groan);
        }
    }

    // null if the file isn't there yet
    private Sound load(String path) {
        if (!Gdx.files.internal(path).exists()) return null;
        Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
        loaded.add(sound);
        return sound;
    }

    private void play(Sound sound, float volume) {
        if (sound == null) return;
        sound.play(volume, MathUtils.random(0.92f, 1.08f), 0f); 
    }

    public void playShot(boolean rifle) {
        play(rifle ? rifleShot : pistolShot, rifle ? 0.7f : 0.6f);
    }

    public void playReload() {
        play(reload, 0.5f);
    }

    // clicking with an empty mag
    public void playDryFire() {
        play(dryFire, 0.5f);
    }

    
    public void playGroan(float dx, float dy) {
        if (groans.size == 0) return;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > HEARING_DISTANCE) return;
        float volume = 0.6f * (1f - distance / HEARING_DISTANCE);
        float pan = MathUtils.clamp(dx / HEARING_DISTANCE, -1f, 1f); 
        groans.random().play(volume, MathUtils.random(0.85f, 1.05f), pan);
    }

    public void dispose() {
        for (Sound sound : loaded) sound.dispose();
    }
}