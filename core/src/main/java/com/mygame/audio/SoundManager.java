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
    private final Sound reloadPistol;
    private final Sound reloadRifle;
    private final Sound dryFire;
    private final Sound headPop;
    private final Array<Sound> groans = new Array<>(); // picked at random so it doesn't loop the same one

    public SoundManager() {
        pistolShot = load("sounds/pistol.wav");
        rifleShot = load("sounds/rifle.wav");
        reloadPistol = load("sounds/reload_pistol.wav");
        reloadRifle = load("sounds/reload_ak.wav");
        dryFire = load("sounds/dry_fire.wav");
        headPop = load("sounds/zombie_head_pop.wav");
        for (int i = 1; i <= 3; i++) {
            Sound groan = load("sounds/zombie_groan" + i + ".wav");
            if (groan != null) groans.add(groan);
        }
    }

    // null if the file isn't there yet or can't be read (wrong format), the game keeps running
    private Sound load(String path) {
        if (!Gdx.files.internal(path).exists()) return null;
        try {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
            loaded.add(sound);
            return sound;
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "can't load " + path + ": " + e.getMessage());
            return null;
        }
    }

    private void play(Sound sound, float volume) {
        if (sound == null) return;
        sound.play(volume, MathUtils.random(0.92f, 1.08f), 0f); // small pitch change so it's not robotic
    }

    public void playShot(boolean rifle) {
        play(rifle ? rifleShot : pistolShot, rifle ? 0.7f : 0.6f);
    }

    // each gun has its own reload
    public void playReload(boolean rifle) {
        play(rifle ? reloadRifle : reloadPistol, 0.5f);
    }

    // clicking with an empty mag
    public void playDryFire() {
        play(dryFire, 0.5f);
    }

    // dx dy = zombie minus player, louder when close, left or right depending on where it is
    public void playGroan(float dx, float dy) {
        if (groans.size == 0) return;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > HEARING_DISTANCE) return;
        float volume = 0.6f * (1f - distance / HEARING_DISTANCE);
        float pan = MathUtils.clamp(dx / HEARING_DISTANCE, -1f, 1f); // only works on mono files
        groans.random().play(volume, MathUtils.random(0.85f, 1.05f), pan);
    }

    // rare death, same as the groans: louder when close, from his side
    public void playHeadPop(float dx, float dy) {
        if (headPop == null) return;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > HEARING_DISTANCE) return;
        float volume = 0.8f * (1f - distance / HEARING_DISTANCE);
        float pan = MathUtils.clamp(dx / HEARING_DISTANCE, -1f, 1f); // only works on mono files
        headPop.play(volume, MathUtils.random(0.95f, 1.05f), pan);
    }

    public void dispose() {
        for (Sound sound : loaded) sound.dispose();
    }
}